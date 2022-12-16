package com.epam.epmcacm.storageservice.s3;


import com.epam.epmcacm.storageservice.exceptions.S3Exception;
import com.epam.epmcacm.storageservice.model.Storage;
import com.epam.epmcacm.storageservice.model.StorageType;
import com.epam.epmcacm.storageservice.service.StorageRepository;
import com.epam.epmcacm.storageservice.util.ResourceUtility;
import com.mpatric.mp3agic.InvalidDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class S3ClientService {

    private static final Logger logger = LoggerFactory.getLogger(S3ClientService.class);
    public static final String PERMANENT_BUCKET = "permanent";
    public static final String STAGING_BUCKET = "staging";

    private S3Client s3Client;

    @Value("${amazonProperties.accessKey}")
    private String accessKey;
    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    @Value("${amazonProperties.url}")
    private String url;

    @PostConstruct
    private void initClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(this.accessKey, this.secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(this.url))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

    }

    // Get the byte[] from this Amazon S3 object.
    public ResponseBytes<GetObjectResponse> getResourceFromStorage(String objectKey,String bucket) throws ResourceNotFoundException {
        try {
            return this.s3Client.getObjectAsBytes(buildGetObjectRequest(objectKey,bucket));
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            logger.error(e.awsErrorDetails().errorMessage());
            throw new ResourceNotFoundException("Resource does not exist with given id");
        }
    }

    public void createOrUpdateResourceInStorage(byte[] data, String objectKey, String bucket) throws S3Exception {
        try {
            putObject(data,objectKey,bucket);
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            logger.error(e.getMessage());
            throw new S3Exception("Error putting data into s3 local cloud storage simulator!");
        }
    }

    public void deleteResourcesFromStorage(List<Long> ids, String bucket) throws S3Exception {
        try {
            ids.stream().map(String::valueOf).forEach(id -> deleteObject(id, bucket));
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            logger.error(e.getMessage());
            throw new S3Exception("Error deleting data from s3 local cloud storage simulator!");
        }
    }

    private GetObjectRequest buildGetObjectRequest(String objectKey, String bucket) {
        return GetObjectRequest
                .builder()
                .key(objectKey)
                .bucket(bucket)
                .build();
    }

    void putObject(byte[] data, String objectKey, String bucket) {
        this.s3Client.putObject(buildPutObjectRequest(objectKey, bucket),RequestBody.fromBytes(data));
    }

    private PutObjectRequest buildPutObjectRequest(String objectKey, String bucket) {
        return PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();
    }

    void deleteObject(String objectKey, String bucket) {
        this.s3Client.deleteObject(buildDeleteObjectRequest(objectKey,bucket));
    }

    private DeleteObjectRequest buildDeleteObjectRequest(String objectKey, String bucket) {
        return DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();
    }

    public Bucket getResourceBucketAndCreateIfNotExists(String bucketName) {
        logger.info("Creating S3 bucket if it doesn't exist already...");
        Optional<Bucket> bucket = findBucketByName(bucketName);
        if(bucket.isPresent()) {
            logger.info("Bucket already exists!");
            return bucket.get();
        }
        return createBucket(bucketName).orElseThrow(() -> software.amazon.awssdk.services.s3.model.S3Exception.builder().message("Bucket not created!").build());
    }

    private Optional<Bucket> findBucketByName(String name) {
        ListBucketsResponse bucketResponse = s3Client.listBuckets();
        if(!bucketResponse.hasBuckets()) return Optional.empty();
        return bucketResponse.buckets().stream().filter(bucket -> bucket.name().equals(name)).findFirst();
    }

    public Optional<Bucket> createBucket(String name) {
        S3Waiter s3Waiter = s3Client.waiter();
        CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(name)
                .build();

        s3Client.createBucket(bucketRequest);
        HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
                .bucket(name)
                .build();

        WaiterResponse<HeadBucketResponse> waiterResponse = s3Waiter.waitUntilBucketExists(bucketRequestWait);
        var response = waiterResponse.matched().response();
        if(response.isEmpty()) throw software.amazon.awssdk.services.s3.model.S3Exception.builder().message("New bucket is not created!").build();
        logger.info("S3 bucket created successfully!");
        return findBucketByName(name);
    }
}

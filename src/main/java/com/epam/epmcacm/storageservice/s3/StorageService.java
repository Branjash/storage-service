package com.epam.epmcacm.storageservice.s3;

import com.epam.epmcacm.storageservice.exceptions.S3Exception;
import com.epam.epmcacm.storageservice.model.Storage;
import com.epam.epmcacm.storageservice.model.StorageType;
import com.epam.epmcacm.storageservice.service.StorageRepository;
import com.epam.epmcacm.storageservice.util.ResourceUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);


    @Autowired
    StorageRepository storageRepository;

    @Autowired
    S3ClientService s3ClientService;

    public List<Storage> getAllStorages() {
        return storageRepository.findAll();
    }

    public Storage putObjectToStagingBucket(MultipartFile file) throws IOException {
        Storage storage = new Storage(file.getOriginalFilename(), StorageType.STAGING);
        storage = storageRepository.save(storage);
        s3ClientService.putObject(file.getBytes(), String.valueOf(storage.getId()), S3ClientService.STAGING_BUCKET);
        logger.info("Successfully save storage data: {}", storage);
        return storage;
    }

    @Transactional
    public Storage putObjectFromStagingToPermanentBucket(Long storageId) throws S3Exception {
        String s3Key = String.valueOf(storageId);
        Storage currentStorage = storageRepository.findById(storageId).orElseThrow(() -> new S3Exception("There is no stored data with this id!"));
        if(currentStorage.getStorageType() == StorageType.PERMANENT) new S3Exception("This storage item is already permanent!");
        ResponseBytes<GetObjectResponse> currentStagedData = s3ClientService.getResourceFromStorage(s3Key,  S3ClientService.STAGING_BUCKET);
        s3ClientService.putObject(currentStagedData.asByteArray(), s3Key,  S3ClientService.PERMANENT_BUCKET);
        s3ClientService.deleteObject(s3Key,  S3ClientService.STAGING_BUCKET);
        currentStorage.setStorageType(StorageType.PERMANENT);
        logger.info("Successfully updated storage data: {}", currentStorage);
        return storageRepository.save(currentStorage);
    }

    public ResponseEntity<ByteArrayResource> getStorageById(Long id) throws S3Exception {
        Storage storage = storageRepository.findById(id)
                .orElseThrow(() -> new S3Exception("Storage does not exist with given id!"));
        ResponseBytes<GetObjectResponse> s3ResourceBytes = s3ClientService.getResourceFromStorage(String.valueOf(storage.getId()),  S3ClientService.PERMANENT_BUCKET);
        logger.info("Successfully found storage data: {}", storage);
        return ResourceUtility.createResponseForGetResource(s3ResourceBytes,storage.getPath());
    }

    public void deleteStorages(List<Long> ids) throws S3Exception {
        s3ClientService.deleteResourcesFromStorage(ids, S3ClientService.PERMANENT_BUCKET);

    }
}

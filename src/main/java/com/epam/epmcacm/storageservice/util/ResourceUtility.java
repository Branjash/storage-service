package com.epam.epmcacm.storageservice.util;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.HashMap;
import java.util.Map;

public class ResourceUtility {

    public static final String MOCK_FILE_NAME = "Sample.mp3";

    private ResourceUtility() {
        throw new IllegalStateException("Utility class!");
    }

    public static ResponseEntity<ByteArrayResource> createResponseForGetResource(ResponseBytes<GetObjectResponse> responseBytes, String fileName){
        GetObjectResponse responseObject = responseBytes.response();
        ByteArrayResource byteResource = new ByteArrayResource(responseBytes.asByteArray());
        return ResponseEntity.ok()
                .headers(createResponseHeadersForResourceDownload(fileName))
                .contentLength(responseObject.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(byteResource);
    }

    public static HttpHeaders createResponseHeadersForResourceDownload(String fileName) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        responseHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
        responseHeaders.add("Pragma", "no-cache");
        responseHeaders.add("Expires", "0");
        return responseHeaders;
    }

    public static ResponseEntity<?> singlePropertyOkResponse(@NonNull String key, @NonNull Object value) {
        Map<String,Object> result = new HashMap<>();
        result.put(key, value);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }



}

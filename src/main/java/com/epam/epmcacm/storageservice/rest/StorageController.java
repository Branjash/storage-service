package com.epam.epmcacm.storageservice.rest;

import com.epam.epmcacm.storageservice.exceptions.S3Exception;
import com.epam.epmcacm.storageservice.model.Storage;
import com.epam.epmcacm.storageservice.s3.StorageService;
import com.epam.epmcacm.storageservice.util.FileUtil;
import com.epam.epmcacm.storageservice.util.ResourceUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping(path = "/storages")
public class StorageController {

    @Autowired
    StorageService storageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveFileToStorage(@RequestPart("file") MultipartFile file) {
        try {
            FileUtil.validateMultipartRequest(file);
            Storage storage = storageService.putObjectToStagingBucket(file);
            return ResourceUtility.singlePropertyOkResponse("id", storage.getId());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected  file reading error!");
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getStorageById(@PathVariable("id") Long storageId) {
        try {
            return storageService.getStorageById(storageId);
        } catch (S3Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("{id}")
    ResponseEntity<?> updateResourceInStorageToPermanent(@PathVariable("id") Long storageId) {
        try {
            storageService.putObjectFromStagingToPermanentBucket(storageId);
            return ResourceUtility.singlePropertyOkResponse("id", storageId);
        } catch (S3Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected s3 storage save error!");
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteStorageResourcesByIds(@RequestParam("id") List<Long> ids) {
        try {
            storageService.deleteStorages(ids);
            return ResourceUtility.singlePropertyOkResponse("ids",ids);
        } catch (S3Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected s3 storage delete error!");

        }
    }

}

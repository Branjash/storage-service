package com.epam.epmcacm.storageservice;

import com.epam.epmcacm.storageservice.s3.S3ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
class StartAppListener implements ApplicationListener<ApplicationReadyEvent> {


  @Autowired
  S3ClientService s3ClientService;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    s3ClientService.getResourceBucketAndCreateIfNotExists(S3ClientService.PERMANENT_BUCKET);
    s3ClientService.getResourceBucketAndCreateIfNotExists(S3ClientService.STAGING_BUCKET);
  }

}
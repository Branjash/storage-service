package com.epam.epmcacm.storageservice.service;

import com.epam.epmcacm.storageservice.model.Storage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageRepository extends CrudRepository<Storage, Long> {

    List<Storage> findAll();

}

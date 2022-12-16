package com.epam.epmcacm.storageservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity(name = "storage_tb")
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private long id;

    @JsonProperty
    private String path;

    @JsonProperty
    @Enumerated(EnumType.STRING)
    StorageType storageType;

    public Storage() {}

    public Storage(String path, StorageType storageType) {
        this.path = path;
        this.storageType = storageType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    @Override
    public String toString() {
        return "Storage{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", storageType=" + storageType +
                '}';
    }
}

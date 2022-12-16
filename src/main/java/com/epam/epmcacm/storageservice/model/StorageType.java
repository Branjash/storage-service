package com.epam.epmcacm.storageservice.model;

public enum StorageType {

    PERMANENT("Permanent"),
    STAGING("Staging");

    private String name;

    private StorageType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

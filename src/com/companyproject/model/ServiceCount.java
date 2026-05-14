package com.companyproject.model;

public class ServiceCount {

    private final String serviceType;
    private final int count;

    public ServiceCount(String serviceType, int count) {
        this.serviceType = serviceType;
        this.count = count;
    }

    public String getServiceType() {
        return serviceType;
    }

    public int getCount() {
        return count;
    }
}

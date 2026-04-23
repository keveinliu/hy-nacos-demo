package com.example.demo.common.api;

public class ServiceRequest implements java.io.Serializable {
    private String name;

    public ServiceRequest() {}

    public ServiceRequest(String name) { this.name = name; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

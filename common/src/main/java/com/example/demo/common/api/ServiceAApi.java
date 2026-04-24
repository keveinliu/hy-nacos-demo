package com.example.demo.common.api;

import com.example.demo.common.proto.ServiceRequest;
import com.example.demo.common.proto.ServiceResponse;

public interface ServiceAApi {
    ServiceResponse greeting(ServiceRequest request);
}

package com.example.demo.common.api;

import com.example.demo.common.proto.ServiceRequest;
import com.example.demo.common.proto.ServiceResponse;

public interface ServiceCApi {
    ServiceResponse process(ServiceRequest request);
}

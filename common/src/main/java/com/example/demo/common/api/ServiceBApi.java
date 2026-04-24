package com.example.demo.common.api;

import com.example.demo.common.proto.ServiceRequest;
import com.example.demo.common.proto.ServiceResponse;

public interface ServiceBApi {
    ServiceResponse process(ServiceRequest request);
}

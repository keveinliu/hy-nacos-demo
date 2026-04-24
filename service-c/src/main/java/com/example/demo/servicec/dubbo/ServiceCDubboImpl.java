package com.example.demo.servicec.dubbo;

import com.example.demo.common.api.ServiceCApi;
import com.example.demo.common.proto.ServiceRequest;
import com.example.demo.common.proto.ServiceResponse;
import com.example.demo.common.routing.RoutingConstants;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DubboService
public class ServiceCDubboImpl implements ServiceCApi {

    private static final Logger log = LoggerFactory.getLogger(ServiceCDubboImpl.class);

    @Override
    public ServiceResponse process(ServiceRequest request) {
        String unit = RpcContext.getContext().getAttachment(RoutingConstants.UNIT_KEY);
        String idc = RpcContext.getContext().getAttachment(RoutingConstants.IDC_KEY);
        log.info("ServiceC.process: name={}, unit={}, idc={}", request.getName(), unit, idc);

        return ServiceResponse.newBuilder()
                .setMessage("Hello from Service C, name=" + request.getName())
                .setFromService("service-c")
                .setTrace("C")
                .build();
    }
}

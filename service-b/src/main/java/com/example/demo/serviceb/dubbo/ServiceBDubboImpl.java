package com.example.demo.serviceb.dubbo;

import com.example.demo.common.api.ServiceBApi;
import com.example.demo.common.api.ServiceCApi;
import com.example.demo.common.api.ServiceRequest;
import com.example.demo.common.api.ServiceResponse;
import com.example.demo.common.routing.RoutingConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DubboService
public class ServiceBDubboImpl implements ServiceBApi {

    private static final Logger log = LoggerFactory.getLogger(ServiceBDubboImpl.class);

    @DubboReference(check = false)
    private ServiceCApi serviceCApi;

    @Override
    public ServiceResponse process(ServiceRequest request) {
        String unit = RpcContext.getContext().getAttachment(RoutingConstants.UNIT_KEY);
        String idc = RpcContext.getContext().getAttachment(RoutingConstants.IDC_KEY);
        log.info("ServiceB.process: name={}, unit={}, idc={}", request.getName(), unit, idc);

        ServiceResponse cResponse = serviceCApi.process(request);

        return new ServiceResponse(
                "[B received] -> " + cResponse.getMessage(),
                "service-b",
                "B -> " + cResponse.getTrace()
        );
    }
}

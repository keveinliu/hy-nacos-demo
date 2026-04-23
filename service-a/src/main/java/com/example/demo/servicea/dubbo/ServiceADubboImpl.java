package com.example.demo.servicea.dubbo;

import com.example.demo.common.api.ServiceAApi;
import com.example.demo.common.api.ServiceBApi;
import com.example.demo.common.api.ServiceRequest;
import com.example.demo.common.api.ServiceResponse;
import com.example.demo.common.routing.RoutingConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DubboService
public class ServiceADubboImpl implements ServiceAApi {

    private static final Logger log = LoggerFactory.getLogger(ServiceADubboImpl.class);

    @DubboReference(check = false)
    private ServiceBApi serviceBApi;

    @Override
    public ServiceResponse greeting(ServiceRequest request) {
        String unit = RpcContext.getContext().getAttachment(RoutingConstants.UNIT_KEY);
        String idc = RpcContext.getContext().getAttachment(RoutingConstants.IDC_KEY);
        log.info("ServiceA.greeting(Dubbo): name={}, unit={}, idc={}", request.getName(), unit, idc);

        ServiceResponse bResponse = serviceBApi.process(request);

        return new ServiceResponse(
                bResponse.getMessage(),
                "service-a -> " + bResponse.getFromService(),
                "A -> " + bResponse.getTrace()
        );
    }
}

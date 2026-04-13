package com.example.demo.servicec.grpc;

import com.example.demo.common.routing.RoutingContext;
import com.example.demo.grpc.ServiceCGrpc;
import com.example.demo.grpc.ServiceRequest;
import com.example.demo.grpc.ServiceResponse;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class ServiceCGrpcImpl extends ServiceCGrpc.ServiceCImplBase {

    private static final Logger log = LoggerFactory.getLogger(ServiceCGrpcImpl.class);

    @Override
    public void process(ServiceRequest request, StreamObserver<ServiceResponse> responseObserver) {
        String unit = RoutingContext.UNIT_CTX_KEY.get(Context.current());
        String idc  = RoutingContext.IDC_CTX_KEY.get(Context.current());
        String user = RoutingContext.USER_CTX_KEY.get(Context.current());

        log.info("ServiceC.process: name={}, unit={}, idc={}, user={}", request.getName(), unit, idc, user);

        ServiceResponse response = ServiceResponse.newBuilder()
                .setMessage("Hello from Service C, name=" + request.getName())
                .setFromService("service-c")
                .setTrace("C")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

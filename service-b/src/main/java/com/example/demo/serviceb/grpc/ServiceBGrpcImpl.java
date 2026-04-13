package com.example.demo.serviceb.grpc;

import com.example.demo.common.routing.RoutingContext;
import com.example.demo.grpc.ServiceBGrpc;
import com.example.demo.grpc.ServiceCGrpc;
import com.example.demo.grpc.ServiceRequest;
import com.example.demo.grpc.ServiceResponse;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class ServiceBGrpcImpl extends ServiceBGrpc.ServiceBImplBase {

    private static final Logger log = LoggerFactory.getLogger(ServiceBGrpcImpl.class);

    @GrpcClient("service-c")
    private ServiceCGrpc.ServiceCBlockingStub serviceCStub;

    @Override
    public void process(ServiceRequest request, StreamObserver<ServiceResponse> responseObserver) {
        String unit = RoutingContext.UNIT_CTX_KEY.get(Context.current());
        String idc  = RoutingContext.IDC_CTX_KEY.get(Context.current());
        String user = RoutingContext.USER_CTX_KEY.get(Context.current());

        log.info("ServiceB.process: name={}, unit={}, idc={}, user={}", request.getName(), unit, idc, user);

        // Call Service C — HeaderPropagationClientInterceptor auto-attaches routing headers
        ServiceResponse cResponse = serviceCStub.process(request);

        ServiceResponse response = ServiceResponse.newBuilder()
                .setMessage("[B received] -> " + cResponse.getMessage())
                .setFromService("service-b")
                .setTrace("B -> " + cResponse.getTrace())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

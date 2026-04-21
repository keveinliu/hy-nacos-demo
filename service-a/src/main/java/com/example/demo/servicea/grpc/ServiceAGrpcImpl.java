package com.example.demo.servicea.grpc;

import com.example.demo.common.routing.RoutingContext;
import com.example.demo.grpc.ServiceAGrpc;
import com.example.demo.grpc.ServiceBGrpc;
import com.example.demo.grpc.ServiceRequest;
import com.example.demo.grpc.ServiceResponse;
import io.grpc.Context;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class ServiceAGrpcImpl extends ServiceAGrpc.ServiceAImplBase {

    private static final Logger log = LoggerFactory.getLogger(ServiceAGrpcImpl.class);

    @GrpcClient("service-b")
    private ServiceBGrpc.ServiceBBlockingStub serviceBStub;

    @Override
    public void greeting(ServiceRequest request, StreamObserver<ServiceResponse> responseObserver) {
        String unit = RoutingContext.UNIT_CTX_KEY.get(Context.current());
        String idc  = RoutingContext.IDC_CTX_KEY.get(Context.current());

        log.info("ServiceA.greeting(gRPC): name={}, unit={}, idc={}", request.getName(), unit, idc);

        try {
            ServiceResponse bResponse = serviceBStub.process(request);

            ServiceResponse response = ServiceResponse.newBuilder()
                    .setMessage(bResponse.getMessage())
                    .setFromService("service-a -> " + bResponse.getFromService())
                    .setTrace("A -> " + bResponse.getTrace())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.warn("ServiceA: downstream call failed: {}", e.getStatus());
            responseObserver.onError(e);
        }
    }
}

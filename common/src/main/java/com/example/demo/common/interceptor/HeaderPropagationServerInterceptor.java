package com.example.demo.common.interceptor;

import com.example.demo.common.routing.RoutingContext;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

/**
 * Server-side gRPC interceptor that extracts routing labels from incoming
 * Metadata and stores them in the gRPC Context for downstream use.
 * Runs at @Order(20), after UnitRoutingServerInterceptor (@Order(10)).
 */
@GrpcGlobalServerInterceptor
@Order(20)
public class HeaderPropagationServerInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HeaderPropagationServerInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String unit = headers.get(RoutingContext.UNIT_METADATA_KEY);
        String idc = headers.get(RoutingContext.IDC_METADATA_KEY);

        log.debug("HeaderPropagationServer: unit={}, idc={}", unit, idc);

        Context ctx = Context.current()
                .withValue(RoutingContext.UNIT_CTX_KEY, unit)
                .withValue(RoutingContext.IDC_CTX_KEY, idc);

        return Contexts.interceptCall(ctx, call, headers, next);
    }
}

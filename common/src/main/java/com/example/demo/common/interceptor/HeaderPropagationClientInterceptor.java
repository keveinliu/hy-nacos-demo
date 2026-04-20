package com.example.demo.common.interceptor;

import com.example.demo.common.routing.RoutingContext;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Context;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side gRPC interceptor that reads routing labels from the current
 * gRPC Context and attaches them to outgoing Metadata headers.
 * Works for both HTTP->gRPC and gRPC->gRPC origin calls.
 */
@GrpcGlobalClientInterceptor
public class HeaderPropagationClientInterceptor implements ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HeaderPropagationClientInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String unit = RoutingContext.UNIT_CTX_KEY.get(Context.current());
                String idc = RoutingContext.IDC_CTX_KEY.get(Context.current());

                log.debug("HeaderPropagationClient: unit={}, idc={}", unit, idc);

                if (unit != null) {
                    headers.put(RoutingContext.UNIT_METADATA_KEY, unit);
                }
                if (idc != null) {
                    headers.put(RoutingContext.IDC_METADATA_KEY, idc);
                }

                super.start(responseListener, headers);
            }
        };
    }
}

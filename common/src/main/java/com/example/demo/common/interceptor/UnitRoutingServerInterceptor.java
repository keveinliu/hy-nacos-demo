package com.example.demo.common.interceptor;

import com.example.demo.common.routing.RoutingContext;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

/**
 * Server-side gRPC interceptor that enforces unit-based traffic routing.
 * Only processes requests whose x-routing-unit header matches the service's
 * own ROUTING_UNIT environment variable. Requests with no unit header are allowed.
 * Health check and reflection service calls are excluded from unit checking.
 * Runs at @Order(10), before HeaderPropagationServerInterceptor (@Order(20)).
 */
@GrpcGlobalServerInterceptor
@Order(10)
public class UnitRoutingServerInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(UnitRoutingServerInterceptor.class);

    // Service names exempt from unit checking (K8s health probes use these)
    private static final String HEALTH_SERVICE = "grpc.health.v1.Health";
    private static final String REFLECTION_SERVICE_V1 = "grpc.reflection.v1alpha.ServerReflection";
    private static final String REFLECTION_SERVICE_V1B = "grpc.reflection.v1.ServerReflection";

    private final String serviceUnit;

    public UnitRoutingServerInterceptor() {
        this.serviceUnit = System.getenv("ROUTING_UNIT");
        log.info("UnitRoutingServerInterceptor initialized with serviceUnit={}", serviceUnit);
    }

    // Package-private for testing
    UnitRoutingServerInterceptor(String serviceUnit) {
        this.serviceUnit = serviceUnit;
        log.info("UnitRoutingServerInterceptor initialized (test) with serviceUnit={}", serviceUnit);
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String serviceName = call.getMethodDescriptor().getServiceName();
        if (HEALTH_SERVICE.equals(serviceName)
                || REFLECTION_SERVICE_V1.equals(serviceName)
                || REFLECTION_SERVICE_V1B.equals(serviceName)) {
            return next.startCall(call, headers);
        }

        String requestUnit = headers.get(RoutingContext.UNIT_METADATA_KEY);

        if (requestUnit == null || requestUnit.isEmpty()) {
            return next.startCall(call, headers);
        }

        if (serviceUnit == null || serviceUnit.isEmpty() || serviceUnit.equals(requestUnit)) {
            return next.startCall(call, headers);
        }

        String msg = String.format("Unit mismatch: request unit=[%s], service unit=[%s]",
                requestUnit, serviceUnit);
        log.warn("Rejecting request: {}", msg);

        Status status = Status.PERMISSION_DENIED.withDescription(msg);
        call.close(status, new Metadata());
        return new ServerCall.Listener<ReqT>() {
            // noop listener after rejection
        };
    }
}

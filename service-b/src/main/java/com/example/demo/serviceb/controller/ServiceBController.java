package com.example.demo.serviceb.controller;

import com.example.demo.common.routing.RoutingContext;
import com.example.demo.grpc.ServiceCGrpc;
import com.example.demo.grpc.ServiceRequest;
import com.example.demo.grpc.ServiceResponse;
import io.grpc.Context;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ServiceBController {

    private static final Logger log = LoggerFactory.getLogger(ServiceBController.class);

    @GrpcClient("service-c")
    private ServiceCGrpc.ServiceCBlockingStub serviceCStub;

    @GetMapping("/process")
    public ResponseEntity<Map<String, String>> process(
            @RequestParam(defaultValue = "world") String name,
            HttpServletRequest request) {

        String unit = request.getHeader(RoutingContext.UNIT_HEADER);
        String idc  = request.getHeader(RoutingContext.IDC_HEADER);

        log.info("ServiceB HTTP: name={}, unit={}, idc={}", name, unit, idc);

        String serviceUnit = System.getenv("ROUTING_UNIT");
        if (unit != null && !unit.isEmpty() && serviceUnit != null && !serviceUnit.isEmpty()
                && !serviceUnit.equals(unit)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unit mismatch: request unit=[" + unit + "], service unit=[" + serviceUnit + "]");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Bridge HTTP context to gRPC Context for downstream call
        Context ctx = Context.current()
                .withValue(RoutingContext.UNIT_CTX_KEY, unit)
                .withValue(RoutingContext.IDC_CTX_KEY, idc);
        Context previousCtx = ctx.attach();
        ServiceResponse cResponse;
        try {
            ServiceRequest grpcRequest = ServiceRequest.newBuilder().setName(name).build();
            cResponse = serviceCStub.process(grpcRequest);
        } finally {
            ctx.detach(previousCtx);
        }

        Map<String, String> result = new HashMap<>();
        result.put("message", "[B received] -> " + cResponse.getMessage());
        result.put("fromService", "service-b");
        result.put("trace", "B -> " + cResponse.getTrace());
        result.put("routingUnit", unit != null ? unit : "");
        result.put("routingIdc", idc != null ? idc : "");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "service-b");
        info.put("unit", System.getenv().getOrDefault("ROUTING_UNIT", ""));
        info.put("idc",  System.getenv().getOrDefault("ROUTING_IDC", ""));
        return ResponseEntity.ok(info);
    }
}

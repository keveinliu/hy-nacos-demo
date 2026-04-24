package com.example.demo.serviceb.controller;

import com.example.demo.common.api.ServiceCApi;
import com.example.demo.common.proto.ServiceRequest;
import com.example.demo.common.proto.ServiceResponse;
import com.example.demo.common.routing.RoutingConstants;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
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

    @DubboReference(check = false)
    private ServiceCApi serviceCApi;

    @GetMapping("/process")
    public ResponseEntity<Map<String, String>> process(
            @RequestParam(defaultValue = "world") String name,
            HttpServletRequest request) {

        String unit = request.getHeader(RoutingConstants.UNIT_KEY);
        String idc = request.getHeader(RoutingConstants.IDC_KEY);
        log.info("ServiceB HTTP: name={}, unit={}, idc={}", name, unit, idc);

        String serviceUnit = System.getenv("ROUTING_UNIT");
        if (unit != null && !unit.isEmpty() && serviceUnit != null && !serviceUnit.isEmpty()
                && !serviceUnit.equals(unit)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unit mismatch");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        RpcContext.getContext().setAttachment(RoutingConstants.UNIT_KEY, unit);
        RpcContext.getContext().setAttachment(RoutingConstants.IDC_KEY, idc);

        try {
            ServiceRequest req = ServiceRequest.newBuilder().setName(name).build();
            ServiceResponse cResponse = serviceCApi.process(req);
            Map<String, String> result = new HashMap<>();
            result.put("message", "[B received] -> " + cResponse.getMessage());
            result.put("fromService", "service-b");
            result.put("trace", "B -> " + cResponse.getTrace());
            return ResponseEntity.ok(result);
        } catch (RpcException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            HttpStatus status = e.getCode() == RpcException.FORBIDDEN_EXCEPTION
                    ? HttpStatus.FORBIDDEN : HttpStatus.BAD_GATEWAY;
            return ResponseEntity.status(status).body(error);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "service-b");
        info.put("unit", System.getenv().getOrDefault("ROUTING_UNIT", ""));
        info.put("idc", System.getenv().getOrDefault("ROUTING_IDC", ""));
        return ResponseEntity.ok(info);
    }
}

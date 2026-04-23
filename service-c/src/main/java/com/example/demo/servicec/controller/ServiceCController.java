package com.example.demo.servicec.controller;

import com.example.demo.common.routing.RoutingConstants;
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
public class ServiceCController {

    private static final Logger log = LoggerFactory.getLogger(ServiceCController.class);

    @GetMapping("/process")
    public ResponseEntity<Map<String, String>> process(
            @RequestParam(defaultValue = "world") String name,
            HttpServletRequest request) {

        String unit = request.getHeader(RoutingConstants.UNIT_KEY);
        String idc = request.getHeader(RoutingConstants.IDC_KEY);
        log.info("ServiceC HTTP: name={}, unit={}, idc={}", name, unit, idc);

        String serviceUnit = System.getenv("ROUTING_UNIT");
        if (unit != null && !unit.isEmpty() && serviceUnit != null && !serviceUnit.isEmpty()
                && !serviceUnit.equals(unit)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unit mismatch");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        Map<String, String> result = new HashMap<>();
        result.put("message", "Hello from Service C, name=" + name);
        result.put("fromService", "service-c");
        result.put("trace", "C");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "service-c");
        info.put("unit", System.getenv().getOrDefault("ROUTING_UNIT", ""));
        info.put("idc", System.getenv().getOrDefault("ROUTING_IDC", ""));
        return ResponseEntity.ok(info);
    }
}

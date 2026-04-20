package com.example.demo.common.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.stream.Collectors;

public class UnitFilteringDiscoveryClient implements DiscoveryClient {

    private static final Logger log = LoggerFactory.getLogger(UnitFilteringDiscoveryClient.class);

    private final DiscoveryClient delegate;
    private final String serviceUnit;

    public UnitFilteringDiscoveryClient(DiscoveryClient delegate, Environment env) {
        this.delegate = delegate;
        this.serviceUnit = env.getProperty("ROUTING_UNIT", env.getProperty("routing.unit", ""));
        log.info("UnitFilteringDiscoveryClient initialized with serviceUnit={}", this.serviceUnit);
    }

    @Override
    public String description() {
        return "Unit-filtering wrapper over " + delegate.description();
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        List<ServiceInstance> all = delegate.getInstances(serviceId);
        if (serviceUnit == null || serviceUnit.isEmpty()) {
            return all;
        }

        List<ServiceInstance> filtered = all.stream()
                .filter(instance -> {
                    String instanceUnit = instance.getMetadata().get("unit");
                    return instanceUnit == null || instanceUnit.isEmpty() || serviceUnit.equals(instanceUnit);
                })
                .collect(Collectors.toList());

        log.debug("getInstances({}): total={}, filtered(unit={})={}", serviceId, all.size(), serviceUnit, filtered.size());

        if (filtered.isEmpty()) {
            log.warn("No instances of {} match unit={}. Falling back to all {} instances.", serviceId, serviceUnit, all.size());
            return all;
        }

        return filtered;
    }

    @Override
    public List<String> getServices() {
        return delegate.getServices();
    }
}

package com.example.demo.common.discovery;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

@Configuration
public class UnitFilteringDiscoveryConfig {

    @Bean
    @Primary
    public DiscoveryClient unitFilteringDiscoveryClient(@Lazy DiscoveryClient discoveryClient, Environment env) {
        return new UnitFilteringDiscoveryClient(discoveryClient, env);
    }
}

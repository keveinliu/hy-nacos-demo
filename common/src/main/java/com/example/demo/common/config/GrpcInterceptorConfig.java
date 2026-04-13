package com.example.demo.common.config;

import com.example.demo.common.interceptor.HeaderPropagationClientInterceptor;
import com.example.demo.common.interceptor.HeaderPropagationServerInterceptor;
import com.example.demo.common.interceptor.UnitRoutingServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for gRPC interceptors.
 * Interceptors are annotated with @GrpcGlobalServerInterceptor and
 * @GrpcGlobalClientInterceptor for automatic registration by grpc-spring-boot-starter.
 */
@Configuration
public class GrpcInterceptorConfig {

    @Bean
    public UnitRoutingServerInterceptor unitRoutingServerInterceptor() {
        return new UnitRoutingServerInterceptor();
    }

    @Bean
    public HeaderPropagationServerInterceptor headerPropagationServerInterceptor() {
        return new HeaderPropagationServerInterceptor();
    }

    @Bean
    public HeaderPropagationClientInterceptor headerPropagationClientInterceptor() {
        return new HeaderPropagationClientInterceptor();
    }
}

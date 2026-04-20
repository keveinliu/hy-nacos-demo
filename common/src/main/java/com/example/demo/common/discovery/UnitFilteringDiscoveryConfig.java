package com.example.demo.common.discovery;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class UnitFilteringDiscoveryConfig implements BeanPostProcessor {

    private final Environment env;

    public UnitFilteringDiscoveryConfig(Environment env) {
        this.env = env;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof CompositeDiscoveryClient) {
            return new UnitFilteringDiscoveryClient((DiscoveryClient) bean, env);
        }
        return bean;
    }
}

package com.example.demo.common.metadata;

import com.example.demo.common.routing.RoutingConstants;
import org.apache.dubbo.registry.client.ServiceInstance;
import org.apache.dubbo.registry.client.ServiceInstanceCustomizer;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 应用级注册自定义扩展：将环境变量中的 unit/idc 以及协议信息注入到 Nacos 服务实例的 metadata 中。
 *
 * <p>由于 Dubbo 3 采用应用级注册（ServiceDiscovery），{@code dubbo.provider.parameters} 中的参数
 * 仅作用于接口级 URL，不会自动写入 Nacos 的 instance metadata。本扩展通过 SPI 机制在实例注册前插入自定义属性。</p>
 */
public class EnvMetadataCustomizer implements ServiceInstanceCustomizer {

    private static final Logger log = LoggerFactory.getLogger(EnvMetadataCustomizer.class);

    public static final String UNIT_KEY = "unit";
    public static final String IDC_KEY = "idc";
    public static final String PROTOCOL_KEY = "protocol";

    @Override
    public void customize(ServiceInstance serviceInstance, ApplicationModel applicationModel) {
        String unit = System.getenv("ROUTING_UNIT");
        String idc = System.getenv("ROUTING_IDC");
        String protocol = "grpc";

        if (unit != null && !unit.isEmpty()) {
            serviceInstance.getMetadata().put(UNIT_KEY, unit);
        }
        if (idc != null && !idc.isEmpty()) {
            serviceInstance.getMetadata().put(IDC_KEY, idc);
        }
        serviceInstance.getMetadata().put(PROTOCOL_KEY, protocol);

        log.info("Customized Nacos instance metadata for app=[{}]: unit={}, idc={}, protocol={}",
                applicationModel.getApplicationName(), unit, idc, protocol);
    }

    @Override
    public int getPriority() {
        return 100;
    }
}

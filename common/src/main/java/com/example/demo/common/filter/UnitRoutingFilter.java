package com.example.demo.common.filter;

import com.example.demo.common.routing.RoutingConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

@Activate(group = PROVIDER, order = -1000)
public class UnitRoutingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(UnitRoutingFilter.class);
    private final String serviceUnit = System.getenv("ROUTING_UNIT");

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String requestUnit = invocation.getAttachment(RoutingConstants.UNIT_KEY);

        if (requestUnit == null || requestUnit.isEmpty()) {
            return invoker.invoke(invocation);
        }

        if (serviceUnit == null || serviceUnit.isEmpty() || serviceUnit.equals(requestUnit)) {
            return invoker.invoke(invocation);
        }

        String msg = "Unit mismatch: request unit=[" + requestUnit + "], service unit=[" + serviceUnit + "]";
        log.warn("Rejecting request: {}", msg);
        throw new RpcException(RpcException.FORBIDDEN_EXCEPTION, msg);
    }
}

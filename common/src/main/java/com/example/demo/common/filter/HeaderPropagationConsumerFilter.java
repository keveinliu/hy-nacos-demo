package com.example.demo.common.filter;

import com.example.demo.common.routing.RoutingConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

@Activate(group = CONSUMER)
public class HeaderPropagationConsumerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String unit = RpcContext.getContext().getAttachment(RoutingConstants.UNIT_KEY);
        String idc = RpcContext.getContext().getAttachment(RoutingConstants.IDC_KEY);

        if (unit != null) {
            invocation.setAttachment(RoutingConstants.UNIT_KEY, unit);
        }
        if (idc != null) {
            invocation.setAttachment(RoutingConstants.IDC_KEY, idc);
        }

        return invoker.invoke(invocation);
    }
}

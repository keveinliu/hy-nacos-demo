package com.example.demo.common.filter;

import com.example.demo.common.routing.RoutingConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

@Activate(group = PROVIDER)
public class HeaderPropagationProviderFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String unit = invocation.getAttachment(RoutingConstants.UNIT_KEY);
        String idc = invocation.getAttachment(RoutingConstants.IDC_KEY);

        if (unit != null) {
            RpcContext.getContext().setAttachment(RoutingConstants.UNIT_KEY, unit);
        }
        if (idc != null) {
            RpcContext.getContext().setAttachment(RoutingConstants.IDC_KEY, idc);
        }

        return invoker.invoke(invocation);
    }
}

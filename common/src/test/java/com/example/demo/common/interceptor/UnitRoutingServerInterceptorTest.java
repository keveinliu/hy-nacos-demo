package com.example.demo.common.interceptor;

import com.example.demo.common.routing.RoutingContext;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitRoutingServerInterceptorTest {

    private static final MethodDescriptor.Marshaller<Object> NOOP_MARSHALLER = new MethodDescriptor.Marshaller<Object>() {
        @Override public InputStream stream(Object value) { return null; }
        @Override public Object parse(InputStream stream) { return null; }
    };

    private static MethodDescriptor<Object, Object> descriptorFor(String serviceName) {
        return MethodDescriptor.<Object, Object>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(serviceName + "/TestMethod")
                .setRequestMarshaller(NOOP_MARSHALLER)
                .setResponseMarshaller(NOOP_MARSHALLER)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static ServerCall<Object, Object> mockCallWithService(String serviceName) {
        ServerCall<Object, Object> mockCall = mock(ServerCall.class);
        when(mockCall.getMethodDescriptor()).thenReturn(descriptorFor(serviceName));
        return mockCall;
    }

    @Test
    @SuppressWarnings("unchecked")
    void matchingUnitAllowsThrough() {
        UnitRoutingServerInterceptor interceptor = new UnitRoutingServerInterceptor("unit-1");
        ServerCall<Object, Object> mockCall = mockCallWithService("com.example.demo.grpc.ServiceC");
        ServerCallHandler<Object, Object> mockHandler = mock(ServerCallHandler.class);
        when(mockHandler.startCall(any(), any())).thenReturn(new ServerCall.Listener<Object>() {});

        Metadata headers = new Metadata();
        headers.put(RoutingContext.UNIT_METADATA_KEY, "unit-1");

        interceptor.interceptCall(mockCall, headers, mockHandler);

        verify(mockHandler).startCall(same(mockCall), any(Metadata.class));
        verify(mockCall, never()).close(any(Status.class), any(Metadata.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void mismatchedUnitRejects() {
        UnitRoutingServerInterceptor interceptor = new UnitRoutingServerInterceptor("unit-1");
        ServerCall<Object, Object> mockCall = mockCallWithService("com.example.demo.grpc.ServiceC");
        ServerCallHandler<Object, Object> mockHandler = mock(ServerCallHandler.class);

        Metadata headers = new Metadata();
        headers.put(RoutingContext.UNIT_METADATA_KEY, "unit-2");

        interceptor.interceptCall(mockCall, headers, mockHandler);

        ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);
        verify(mockCall).close(statusCaptor.capture(), any(Metadata.class));
        assertEquals(Status.Code.PERMISSION_DENIED, statusCaptor.getValue().getCode());
        verify(mockHandler, never()).startCall(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void absentUnitHeaderAllowsThrough() {
        UnitRoutingServerInterceptor interceptor = new UnitRoutingServerInterceptor("unit-1");
        ServerCall<Object, Object> mockCall = mockCallWithService("com.example.demo.grpc.ServiceC");
        ServerCallHandler<Object, Object> mockHandler = mock(ServerCallHandler.class);
        when(mockHandler.startCall(any(), any())).thenReturn(new ServerCall.Listener<Object>() {});

        interceptor.interceptCall(mockCall, new Metadata(), mockHandler);

        verify(mockHandler).startCall(same(mockCall), any(Metadata.class));
        verify(mockCall, never()).close(any(Status.class), any(Metadata.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void emptyUnitHeaderAllowsThrough() {
        UnitRoutingServerInterceptor interceptor = new UnitRoutingServerInterceptor("unit-1");
        ServerCall<Object, Object> mockCall = mockCallWithService("com.example.demo.grpc.ServiceC");
        ServerCallHandler<Object, Object> mockHandler = mock(ServerCallHandler.class);
        when(mockHandler.startCall(any(), any())).thenReturn(new ServerCall.Listener<Object>() {});

        Metadata headers = new Metadata();
        headers.put(RoutingContext.UNIT_METADATA_KEY, "");

        interceptor.interceptCall(mockCall, headers, mockHandler);

        verify(mockHandler).startCall(same(mockCall), any(Metadata.class));
        verify(mockCall, never()).close(any(Status.class), any(Metadata.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void healthCheckServiceSkipsUnitCheck() {
        UnitRoutingServerInterceptor interceptor = new UnitRoutingServerInterceptor("unit-1");
        ServerCall<Object, Object> mockCall = mockCallWithService("grpc.health.v1.Health");
        ServerCallHandler<Object, Object> mockHandler = mock(ServerCallHandler.class);
        when(mockHandler.startCall(any(), any())).thenReturn(new ServerCall.Listener<Object>() {});

        Metadata headers = new Metadata();
        headers.put(RoutingContext.UNIT_METADATA_KEY, "unit-2");

        interceptor.interceptCall(mockCall, headers, mockHandler);

        verify(mockHandler).startCall(same(mockCall), any(Metadata.class));
        verify(mockCall, never()).close(any(Status.class), any(Metadata.class));
    }
}

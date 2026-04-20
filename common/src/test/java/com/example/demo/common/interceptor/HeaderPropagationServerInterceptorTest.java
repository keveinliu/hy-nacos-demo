package com.example.demo.common.interceptor;

import com.example.demo.common.routing.RoutingContext;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeaderPropagationServerInterceptorTest {

    private final HeaderPropagationServerInterceptor interceptor = new HeaderPropagationServerInterceptor();

    @Test
    @SuppressWarnings("unchecked")
    void propagatesHeadersToContext() {
        ServerCall<Object, Object> mockCall = mock(ServerCall.class);

        Metadata headers = new Metadata();
        headers.put(RoutingContext.UNIT_METADATA_KEY, "unit-1");
        headers.put(RoutingContext.IDC_METADATA_KEY, "idc-1");

        AtomicReference<String> capturedUnit = new AtomicReference<>();
        AtomicReference<String> capturedIdc = new AtomicReference<>();

        ServerCallHandler<Object, Object> capturingHandler = (call, hdrs) -> {
            capturedUnit.set(RoutingContext.UNIT_CTX_KEY.get(Context.current()));
            capturedIdc.set(RoutingContext.IDC_CTX_KEY.get(Context.current()));
            return new ServerCall.Listener<Object>() {};
        };

        interceptor.interceptCall(mockCall, headers, capturingHandler);

        assertEquals("unit-1", capturedUnit.get());
        assertEquals("idc-1", capturedIdc.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    void emptyMetadataDoesNotThrow() {
        ServerCall<Object, Object> mockCall = mock(ServerCall.class);
        ServerCallHandler<Object, Object> mockHandler = mock(ServerCallHandler.class);
        when(mockHandler.startCall(any(), any())).thenReturn(new ServerCall.Listener<Object>() {});

        Metadata emptyHeaders = new Metadata();

        assertDoesNotThrow(() -> interceptor.interceptCall(mockCall, emptyHeaders, mockHandler));
        verify(mockHandler).startCall(same(mockCall), any(Metadata.class));
    }
}

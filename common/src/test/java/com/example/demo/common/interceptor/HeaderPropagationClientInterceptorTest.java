package com.example.demo.common.interceptor;

import com.example.demo.common.routing.RoutingContext;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeaderPropagationClientInterceptorTest {

    private final HeaderPropagationClientInterceptor interceptor = new HeaderPropagationClientInterceptor();

    private static final MethodDescriptor.Marshaller<Object> NOOP_MARSHALLER = new MethodDescriptor.Marshaller<Object>() {
        @Override public InputStream stream(Object value) { return null; }
        @Override public Object parse(InputStream stream) { return null; }
    };

    private static final MethodDescriptor<Object, Object> TEST_METHOD = MethodDescriptor.<Object, Object>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName("com.example.demo.grpc.ServiceC/Process")
            .setRequestMarshaller(NOOP_MARSHALLER)
            .setResponseMarshaller(NOOP_MARSHALLER)
            .build();

    @Test
    @SuppressWarnings("unchecked")
    void attachesContextValuesToMetadata() {
        AtomicReference<Metadata> capturedHeaders = new AtomicReference<>();

        ClientCall<Object, Object> capturingCall = new ClientCall<Object, Object>() {
            @Override public void start(Listener<Object> responseListener, Metadata headers) { capturedHeaders.set(headers); }
            @Override public void request(int numMessages) {}
            @Override public void cancel(String message, Throwable cause) {}
            @Override public void halfClose() {}
            @Override public void sendMessage(Object message) {}
        };

        Channel mockChannel = mock(Channel.class);
        when(mockChannel.newCall(any(), any())).thenReturn(capturingCall);

        Context ctx = Context.current()
                .withValue(RoutingContext.UNIT_CTX_KEY, "unit-1")
                .withValue(RoutingContext.IDC_CTX_KEY, "idc-1");

        ctx.run(() -> {
            ClientCall<Object, Object> call = interceptor.interceptCall(TEST_METHOD, CallOptions.DEFAULT, mockChannel);
            call.start(new ClientCall.Listener<Object>() {}, new Metadata());
        });

        Metadata outgoing = capturedHeaders.get();
        assertNotNull(outgoing);
        assertEquals("unit-1", outgoing.get(RoutingContext.UNIT_METADATA_KEY));
        assertEquals("idc-1", outgoing.get(RoutingContext.IDC_METADATA_KEY));
    }

    @Test
    @SuppressWarnings("unchecked")
    void emptyContextDoesNotAddHeaders() {
        AtomicReference<Metadata> capturedHeaders = new AtomicReference<>();

        ClientCall<Object, Object> capturingCall = new ClientCall<Object, Object>() {
            @Override public void start(Listener<Object> responseListener, Metadata headers) { capturedHeaders.set(headers); }
            @Override public void request(int numMessages) {}
            @Override public void cancel(String message, Throwable cause) {}
            @Override public void halfClose() {}
            @Override public void sendMessage(Object message) {}
        };

        Channel mockChannel = mock(Channel.class);
        when(mockChannel.newCall(any(), any())).thenReturn(capturingCall);

        ClientCall<Object, Object> call = interceptor.interceptCall(TEST_METHOD, CallOptions.DEFAULT, mockChannel);
        call.start(new ClientCall.Listener<Object>() {}, new Metadata());

        Metadata outgoing = capturedHeaders.get();
        assertNotNull(outgoing);
        assertNull(outgoing.get(RoutingContext.UNIT_METADATA_KEY));
        assertNull(outgoing.get(RoutingContext.IDC_METADATA_KEY));
    }
}

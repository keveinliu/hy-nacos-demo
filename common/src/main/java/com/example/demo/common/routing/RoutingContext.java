package com.example.demo.common.routing;

import io.grpc.Context;
import io.grpc.Metadata;

/**
 * Holds gRPC Context keys, Metadata keys, and HTTP header name constants
 * for routing labels (unit/idc) used in traffic scheduling.
 */
public final class RoutingContext {

    public static final Context.Key<String> UNIT_CTX_KEY = Context.key("routing-unit");
    public static final Context.Key<String> IDC_CTX_KEY = Context.key("routing-idc");

    public static final Metadata.Key<String> UNIT_METADATA_KEY =
            Metadata.Key.of("x-routing-unit", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> IDC_METADATA_KEY =
            Metadata.Key.of("x-routing-idc", Metadata.ASCII_STRING_MARSHALLER);

    public static final String UNIT_HEADER = "x-routing-unit";
    public static final String IDC_HEADER = "x-routing-idc";

    private RoutingContext() {
    }
}

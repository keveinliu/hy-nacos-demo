package com.example.demo.common.api;

public class ServiceResponse implements java.io.Serializable {
    private String message;
    private String fromService;
    private String trace;

    public ServiceResponse() {}

    public ServiceResponse(String message, String fromService, String trace) {
        this.message = message;
        this.fromService = fromService;
        this.trace = trace;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFromService() { return fromService; }
    public void setFromService(String fromService) { this.fromService = fromService; }

    public String getTrace() { return trace; }
    public void setTrace(String trace) { this.trace = trace; }
}

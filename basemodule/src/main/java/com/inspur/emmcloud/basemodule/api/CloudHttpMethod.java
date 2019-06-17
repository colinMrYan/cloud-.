package com.inspur.emmcloud.basemodule.api;

/**
 * Created by yufuchang on 2018/5/3.
 */

public enum CloudHttpMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    HEAD("HEAD"),
    MOVE("MOVE"),
    COPY("COPY"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE"),
    CONNECT("CONNECT");

    private final String value;

    CloudHttpMethod(String value) {
        this.value = value;
    }

    public static boolean permitsRetry(CloudHttpMethod method) {
        return method == GET;
    }

    public static boolean permitsCache(CloudHttpMethod method) {
        return method == GET || method == POST;
    }

    public static boolean permitsRequestBody(CloudHttpMethod method) {
        return method == POST || method == PUT || method == PATCH || method == DELETE;
    }

    public String toString() {
        return this.value;
    }
}
package com.example.common.network.http;

public final class ResponseCode {

    public static final int SUCCESS = 0;
    public static final int TOKEN_TIMEOUT = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int NETWORK_ERROR = -1;

    private ResponseCode() {
    }
}

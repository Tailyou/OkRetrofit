package com.hengda.zwf.httputil.request;

public class HttpResponse<T> {

    public static final String HTTP_STATUS_SUCCESS = "1";

    public String status;
    public String msg;
    public T data;

    public String getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

}

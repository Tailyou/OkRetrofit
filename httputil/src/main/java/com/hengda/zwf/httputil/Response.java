package com.hengda.zwf.httputil;

public class Response<T> {

    private String status;
    private String msg;
    private T data;

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

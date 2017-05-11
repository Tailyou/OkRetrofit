package com.hengda.zwf.httputil.request;

/**
 * 自定义-Retrofit网络请求异常
 *
 * @author 祝文飞（Tailyou）
 * @time 2017/5/10 10:52
 */
public class HttpException extends RuntimeException {

    public HttpException(String detailMessage) {
        super(detailMessage);
    }

}

package com.hengda.zwf.httputil.httprequest;

/**
 * 自定义-Retrofit网络请求异常
 *
 * @author 祝文飞（Tailyou）
 * @time 2017/5/10 10:52
 */
public class ApiException extends RuntimeException {

    public ApiException(String detailMessage) {
        super(detailMessage);
    }

}

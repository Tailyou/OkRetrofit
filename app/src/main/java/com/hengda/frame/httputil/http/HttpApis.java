package com.hengda.frame.httputil.http;

import com.hengda.zwf.httputil.httprequest.HttpResponse;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * 作者：祝文飞（Tailyou）
 * 邮箱：tailyou@163.com
 * 时间：2017/2/15 14:02
 * 描述：
 */

public interface HttpApis {

    /**
     * 请求机器号
     *
     * @param app_kind
     * @return
     */
    @FormUrlEncoded
    @POST("index.php?a=request_deviceno")
    Observable<HttpResponse<String>> reqDeviceNo(@Field("app_kind") int app_kind);

}

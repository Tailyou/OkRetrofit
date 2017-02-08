package com.hengda.frame.httputil.http;


import com.hengda.frame.httputil.update.CheckResponse;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


/**
 * 作者：Tailyou （祝文飞）
 * 时间：2016/6/11 16:17
 * 邮箱：tailyou@163.com
 * 描述：
 */
public interface IHttpService {

    /**
     * 检查App版本更新
     *
     * @param appKey
     * @param appSecret
     * @param appKind
     * @param versionCode
     * @param deviceId
     * @return
     */
    @FormUrlEncoded
    @POST("index.php?a=checkVersion")
    Observable<CheckResponse> checkUpdate(@Field("appKey") String appKey,
                                          @Field("appSecret") String appSecret,
                                          @Field("appKind") int appKind,
                                          @Field("versionCode") int versionCode,
                                          @Field("deviceId") String deviceId);

}

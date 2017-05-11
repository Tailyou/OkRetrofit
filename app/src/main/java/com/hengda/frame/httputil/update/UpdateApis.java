package com.hengda.frame.httputil.update;

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

public interface UpdateApis {

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
    Observable<UpdateResponse> checkUpdate(@Field("appKey") String appKey,
                                           @Field("appSecret") String appSecret,
                                           @Field("appKind") int appKind,
                                           @Field("versionCode") int versionCode,
                                           @Field("deviceId") String deviceId);

}

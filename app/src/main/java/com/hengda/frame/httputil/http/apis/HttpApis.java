package com.hengda.frame.httputil.http.apis;

import com.hengda.frame.httputil.bean.DataBean;
import com.hengda.zwf.httputil.request.HttpResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * 作者：祝文飞（Tailyou）
 * 邮箱：tailyou@163.com
 * 时间：2017/2/15 14:02
 * 描述：
 */

public interface HttpApis {

    /**
     * 获取数据
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/12/21 14:23
     */
    @GET("index.php?g=mapi&m=appdatas&a=datas")
    Observable<HttpResponse<DataBean>> loadDatas();

}

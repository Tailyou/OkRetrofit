package com.hengda.frame.httputil.http;

import com.hengda.frame.httputil.app.HdAppConfig;
import com.hengda.frame.httputil.bean.DataBean;
import com.hengda.frame.httputil.bean.UpdateBean;
import com.hengda.frame.httputil.http.apis.HttpApis;
import com.hengda.frame.httputil.http.apis.UpdateApis;
import com.hengda.zwf.httputil.request.BaseRetrofit;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;

/**
 * 作者：祝文飞（Tailyou）
 * 邮箱：tailyou@163.com
 * 时间：2017/2/15 14:06
 * 描述：
 */
public class RetrofitHelper extends BaseRetrofit {

    public static final String APP_UPDATE_URL = "http://101.200.234.14/APPCloud/";
    private static Hashtable<String, RetrofitHelper> retrofitHelperHashtable = new Hashtable<>();
    private static HttpApis httpApis = null;
    public static UpdateApis updateApis = null;
    private volatile static RetrofitHelper instance;

    /**
     * 单例模式
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:31
     */
    private RetrofitHelper() {
        super();
        updateApis = getApiService(APP_UPDATE_URL, UpdateApis.class);
        httpApis = getApiService(setupBaseHttpUrl(), HttpApis.class);
    }

    /**
     * 获取实例-单例
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:32
     */
    public static RetrofitHelper getInstance() {
        String ipPort = HdAppConfig.getDefaultIpPort();
        instance = retrofitHelperHashtable.get(ipPort);
        if (instance == null) {
            synchronized (RetrofitHelper.class) {
                if (instance == null) {
                    instance = new RetrofitHelper();
                    retrofitHelperHashtable.clear();
                    retrofitHelperHashtable.put(ipPort, instance);
                }
            }
        }
        return instance;
    }

    /**
     * 组装网络请求基地址
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:38
     */
    public static String setupBaseHttpUrl() {
        return "http://" + HdAppConfig.getDefaultIpPort() + "/hnbwy/";
    }

    /**
     * 在此配置超时，缓存，日志等
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/5/10 11:10
     */
    @Override
    public OkHttpClient initOkHttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        return builder.build();
    }

    /**
     * 检查更新
     *
     * @param appKey
     * @param appSecret
     * @param appKind
     * @param verCode
     * @param deviceNo
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:37
     */
    public Observable<UpdateBean> checkUpdate(String appKey, String appSecret, int appKind, int verCode, String deviceNo) {
        return updateApis.checkUpdate(appKey, appSecret, appKind, verCode, deviceNo)
                .compose(rxSchedulerHelper()).compose(handleResult());
    }

    /**
     * 获取数据
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/1/3 11:57
     */
    public Observable<DataBean> loadDatas() {
        return httpApis.loadDatas().compose(rxSchedulerHelper()).compose(handleResult());
    }

}

package com.hengda.frame.httputil.http;

import com.hengda.frame.httputil.BuildConfig;
import com.hengda.frame.httputil.app.HdAppConfig;
import com.hengda.frame.httputil.app.HdApplication;
import com.hengda.frame.httputil.app.HdConstants;
import com.hengda.frame.httputil.update.CheckResponse;
import com.hengda.zwf.commonutil.AppUtil;
import com.hengda.zwf.httputil.http_request.HttpApi;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;


/**
 * 作者：Tailyou （祝文飞）
 * 时间：2016/6/11 16:17
 * 邮箱：tailyou@163.com
 * 描述：
 */
public class HttpRequester extends HttpApi {

    private IHttpService iHttpService;
    private volatile static HttpRequester instance;
    private static Hashtable<String, HttpRequester> mRequestApiTable;

    static {
        mRequestApiTable = new Hashtable<>();
    }

    /**
     * 单例模式
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:31
     */
    private HttpRequester(String baseUrl) {
        super(baseUrl);
        iHttpService = retrofit.create(IHttpService.class);
    }

    /**
     * 获取实例-单例
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:32
     */
    public static HttpRequester getInstance(String baseHttpUrl) {
        instance = mRequestApiTable.get(baseHttpUrl);
        if (instance == null) {
            synchronized (HttpRequester.class) {
                if (instance == null) {
                    instance = new HttpRequester(baseHttpUrl);
                    mRequestApiTable.put(baseHttpUrl, instance);
                }
            }
        }
        return instance;
    }

    /**
     * 检查更新
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:37
     */
    public Observable<CheckResponse> checkUpdate() {
        return iHttpService.checkUpdate(HdConstants.APP_KEY, HdConstants.APP_SECRET, 3,
                AppUtil.getVersionCode(HdApplication.mContext), HdAppConfig.getDeviceNo())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /*public <T> void doSubscribe(Observable<HttpResponse<T>> observable, Observer<T> observer) {
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (TextUtils.equals(HttpResponse.HTTP_STATUS_SUCCESS, response.getStatus())) {
                        return response.getData();
                    } else {
                        throw new HttpException(response.getMsg());
                    }
                })
                .subscribe(observer);
    }*/

    @Override
    public OkHttpClient initOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(10, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        return builder.build();
    }

}

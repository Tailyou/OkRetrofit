package com.hengda.frame.httputil.http;

import com.hengda.frame.httputil.BuildConfig;
import com.hengda.frame.httputil.app.HdAppConfig;
import com.hengda.frame.httputil.app.HdApplication;
import com.hengda.frame.httputil.app.HdConstants;
import com.hengda.frame.httputil.update.CheckResponse;
import com.hengda.zwf.commonutil.AppUtil;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 作者：Tailyou （祝文飞）
 * 时间：2016/6/11 16:17
 * 邮箱：tailyou@163.com
 * 描述：
 */
public class HttpRequester{

    private Retrofit retrofit;
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
        retrofit = new Retrofit.Builder()
                .client(initOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();
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

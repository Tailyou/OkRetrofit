package com.hengda.zwf.httputil.request;

import com.hengda.zwf.httputil.update.UpdateApis;
import com.hengda.zwf.httputil.update.UpdateResponse;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 作者：祝文飞（Tailyou）
 * 邮箱：tailyou@163.com
 * 时间：2017/2/15 14:06
 * 描述：Retrofit基类
 */
public abstract class BaseRetrofit {

    public static final String APP_UPDATE_URL = "http://101.200.234.14/APPCloud/";
    public static OkHttpClient okHttpClient = null;
    public static UpdateApis updateApis = null;

    public abstract OkHttpClient initOkHttp();

    public BaseRetrofit() {
        okHttpClient = initOkHttp();
        updateApis = getApiService(APP_UPDATE_URL, UpdateApis.class);
    }

    public <T> T getApiService(String baseUrl, Class<T> clz) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(clz);
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
    public Observable<UpdateResponse> checkUpdate(String appKey, String appSecret, int appKind, int verCode, String deviceNo) {
        return updateApis.checkUpdate(appKey, appSecret, appKind, verCode, deviceNo).compose(rxSchedulerHelper());
    }

    /**
     * 统一线程处理
     *
     * @param <T>
     * @return
     */
    public <T> ObservableTransformer<T, T> rxSchedulerHelper() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 统一返回结果处理
     *
     * @param <T>
     * @return
     */
    public <T> ObservableTransformer<HttpResponse<T>, T> handleResult() {
        return new ObservableTransformer<HttpResponse<T>, T>() {
            @Override
            public ObservableSource<T> apply(Observable<HttpResponse<T>> httpResponseObservable) {
                return httpResponseObservable.map(new Function<HttpResponse<T>, T>() {
                    @Override
                    public T apply(HttpResponse<T> httpResponse) throws Exception {
                        if (httpResponse.getStatus().equals(HttpResponse.HTTP_STATUS_SUCCESS)) {
                            return httpResponse.getData();
                        } else {
                            throw new HttpException(httpResponse.getMsg());
                        }
                    }
                });
            }
        };
    }

}

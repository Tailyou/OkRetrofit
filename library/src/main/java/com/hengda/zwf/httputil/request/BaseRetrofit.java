package com.hengda.zwf.httputil.request;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
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

    public static OkHttpClient okHttpClient = null;

    public abstract OkHttpClient initOkHttp();

    public BaseRetrofit() {
        okHttpClient = initOkHttp();
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
     * 统一线程处理
     *
     * @param <T>
     * @return
     */
    public <T> ObservableTransformer<T, T> rxSchedulerHelper() {
        return upstream -> upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 统一返回结果处理
     *
     * @param <T>
     * @return
     */
    public <T> ObservableTransformer<HttpResponse<T>, T> handleResult() {
        return httpResponseObservable -> httpResponseObservable.map(httpResponse -> {
            if (httpResponse.getStatus().equals(HttpResponse.HTTP_STATUS_SUCCESS)) {
                return httpResponse.getData();
            } else {
                throw new HttpException(httpResponse.getMsg());
            }
        });
    }

}

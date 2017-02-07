package com.hengda.zwf.httputil;

import android.text.TextUtils;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RequestApi {

    public static final String HTTP_STATUS_SUCCEED = "1";
    public Retrofit retrofit;

    public RequestApi(String baseUrl) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();
    }

    public <T> void doSubscribe(Subscriber<T> subscriber, Observable<Response<T>> observable) {
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {
                    if (TextUtils.equals(HTTP_STATUS_SUCCEED, response.getStatus())) {
                        return response.getData();
                    } else {
                        throw new RequestException(response.getMsg());
                    }
                })
                .subscribe(subscriber);
    }

}

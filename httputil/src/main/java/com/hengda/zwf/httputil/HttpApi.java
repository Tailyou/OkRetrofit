package com.hengda.zwf.httputil;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.Observable;
import io.reactivex.Observer;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class HttpApi {

    public Retrofit retrofit;

    public HttpApi(String baseUrl) {
        retrofit = new Retrofit.Builder()
                .client(initOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();
    }

    public abstract <T> void doSubscribe(Observable<HttpResponse<T>> observable, Observer<T> observer);

    public abstract OkHttpClient initOkHttpClient();

}

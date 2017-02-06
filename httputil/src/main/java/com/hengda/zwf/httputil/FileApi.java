package com.hengda.zwf.httputil;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;

public class FileApi {

    public Retrofit retrofit;

    public FileApi(String baseUrl) {
        retrofit = new Retrofit.Builder()
                .client(initOkHttpClient())
                .baseUrl(baseUrl)
                .build();
    }

    public OkHttpClient initOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.networkInterceptors().add(chain -> {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse
                    .newBuilder()
                    .body(new FileResponseBody(originalResponse))
                    .build();
        });
        return builder.build();
    }

}

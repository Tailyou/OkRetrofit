package com.hengda.zwf.httputil;


import rx.Subscriber;

public class RequestSubscriber<T> extends Subscriber<T> {

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        failed(e);
    }

    @Override
    public void onNext(T t) {
        succeed(t);
    }

    public void succeed(T t) {
    }

    public void failed(Throwable e) {
    }

}

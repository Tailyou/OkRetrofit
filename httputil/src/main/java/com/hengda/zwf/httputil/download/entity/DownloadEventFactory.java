package com.hengda.zwf.httputil.download.entity;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;


public class DownloadEventFactory {

    private volatile static DownloadEventFactory singleton;
    private Map<String, DownloadEvent> map = new HashMap<>();

    private DownloadEventFactory() {
    }

    public static DownloadEventFactory getSingleton() {
        if (singleton == null) {
            synchronized (DownloadEventFactory.class) {
                if (singleton == null) {
                    singleton = new DownloadEventFactory();
                }
            }
        }
        return singleton;
    }

    public DownloadEvent normal(String url) {
        return create(url, DownloadFlag.NORMAL, null);
    }

    public DownloadEvent waiting(String url) {
        return create(url, DownloadFlag.WAITING, null);
    }

    public DownloadEvent waiting(String url, DownloadStatus status) {
        return create(url, DownloadFlag.WAITING, status);
    }

    public DownloadEvent started(String url, DownloadStatus status) {
        return create(url, DownloadFlag.STARTED, status);
    }

    public DownloadEvent completed(String url, DownloadStatus status) {
        return create(url, DownloadFlag.COMPLETED, status);
    }

    public DownloadEvent failed(String url, DownloadStatus status, Throwable throwable) {
        return create(url, DownloadFlag.FAILED, status, throwable);
    }

    public DownloadEvent create(String url, int flag, DownloadStatus status) {
        DownloadEvent event = createEvent(url, flag, status);
        event.setError(null);
        return event;
    }

    public DownloadEvent create(String url, int flag, DownloadStatus status, Throwable throwable) {
        DownloadEvent event = createEvent(url, flag, status);
        event.setError(throwable);
        return event;
    }

    @NonNull
    private DownloadEvent createEvent(String url, int flag, DownloadStatus status) {
        DownloadEvent event = map.get(url);
        if (event == null) {
            event = new DownloadEvent();
            map.put(url, event);
        }
        event.setDownloadStatus(status == null ? new DownloadStatus() : status);
        event.setFlag(flag);
        return event;
    }

}

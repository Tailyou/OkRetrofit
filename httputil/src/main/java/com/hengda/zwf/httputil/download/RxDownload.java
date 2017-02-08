package com.hengda.zwf.httputil.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hengda.zwf.httputil.download.dbase.DbManager;
import com.hengda.zwf.httputil.download.entity.DownloadEvent;
import com.hengda.zwf.httputil.download.entity.DownloadMission;
import com.hengda.zwf.httputil.download.entity.DownloadRecord;
import com.hengda.zwf.httputil.download.entity.DownloadStatus;
import com.hengda.zwf.httputil.download.function.Constant;
import com.hengda.zwf.httputil.download.function.DownloadHelper;
import com.hengda.zwf.httputil.download.function.DownloadService;
import com.hengda.zwf.httputil.download.function.Utils;

import java.io.File;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import retrofit2.Retrofit;


public class RxDownload {

    private static DownloadService mDownloadService;
    private static Object object = new Object();
    private static boolean bound = false;

    private DownloadHelper mDownloadHelper;
    private Context mContext;
    private int MAX_DOWNLOAD_NUMBER = 5;

    static {
        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof InterruptedException) {
                Utils.log("Thread interrupted");
            } else if (throwable instanceof InterruptedIOException) {
                Utils.log("Io interrupted");
            } else if (throwable instanceof SocketException) {
                Utils.log("Socket error");
            }
        });
    }

    private RxDownload() {
        mDownloadHelper = new DownloadHelper();
    }

    public static RxDownload getInstance() {
        return new RxDownload();
    }

    public File[] getRealFiles(String saveName, String savePath) {
        String[] filePaths = mDownloadHelper.getRealFilePaths(saveName, savePath);
        return new File[]{new File(filePaths[0]), new File(filePaths[1]), new File(filePaths[2])};
    }

    public RxDownload context(Context context) {
        this.mContext = context;
        return this;
    }

    public RxDownload defaultSavePath(String savePath) {
        mDownloadHelper.setDefaultSavePath(savePath);
        return this;
    }

    public RxDownload retrofit(Retrofit retrofit) {
        mDownloadHelper.setRetrofit(retrofit);
        return this;
    }

    public RxDownload maxThread(int max) {
        mDownloadHelper.setMaxThreads(max);
        return this;
    }

    public RxDownload maxRetryCount(int max) {
        mDownloadHelper.setMaxRetryCount(max);
        return this;
    }

    public RxDownload maxDownloadNumber(int max) {
        this.MAX_DOWNLOAD_NUMBER = max;
        return this;
    }

    /**
     * 获取全部下载记录
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/2/8 12:22
     */
    public Observable<List<DownloadRecord>> getTotalDownloadRecords() {
        if (mContext == null) {
            return Observable.error(new Throwable(Constant.CONTEXT_NULL_HINT));
        }
        DbManager dbManager = DbManager.getSingleton(mContext.getApplicationContext());
        return dbManager.readAllRecords();
    }

    /**
     * 根据 url 获取指定下载记录
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/2/8 12:22
     */
    public Observable<DownloadRecord> getDownloadRecord(String url) {
        if (mContext == null) {
            return Observable.error(new Throwable(Constant.CONTEXT_NULL_HINT));
        }
        DbManager dbManager = DbManager.getSingleton(mContext.getApplicationContext());
        return dbManager.readRecord(url);
    }

    public <Upstream> ObservableTransformer<Upstream, DownloadStatus> transform(
            @NonNull final String url,
            @NonNull final String saveName,
            @Nullable final String savePath) {
        return upstream -> upstream.flatMap(new Function<Upstream, ObservableSource<DownloadStatus>>() {
            @Override
            public ObservableSource<DownloadStatus> apply(Upstream upstream) throws Exception {
                return download(url, saveName, savePath);
            }
        });
    }

    public Observable<DownloadStatus> download(
            @NonNull final String url,
            @NonNull final String saveName,
            @Nullable final String savePath) {
        return mDownloadHelper.downloadDispatcher(url, saveName, savePath);
    }

    public <Upstream> ObservableTransformer<Upstream, Object> transformService(
            @NonNull final String url,
            @NonNull final String saveName,
            @Nullable final String savePath) {
        return upstream -> upstream.flatMap(new Function<Upstream, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Upstream upstream) throws Exception {
                return serviceDownload(url, saveName, savePath);
            }
        });
    }

    public Observable<?> serviceDownload(
            @NonNull final String url,
            @NonNull final String saveName,
            @Nullable final String savePath) {
        return createGeneralObservable(() -> addDownloadTask(url, saveName, savePath));
    }

    private void addDownloadTask(@NonNull String url,
                                 @NonNull String saveName,
                                 @Nullable String savePath) {
        DownloadMission mission = new DownloadMission.Builder()
                .setRxDownload(RxDownload.this)
                .setUrl(url)
                .setSaveName(saveName)
                .setSavePath(savePath)
                .build();
        mDownloadService.addDownloadMission(mission);
    }

    /**
     * 获取下载状态，ServiceDownload
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/2/8 12:21
     */
    public Observable<DownloadEvent> receiveDownloadStatus(final String url) {
        return Single.create(e -> {
            if (!bound) {
                startBindServiceAndDo(() -> e.onSuccess(object));
            } else {
                e.onSuccess(object);
            }
        }).flatMapObservable(o -> mDownloadService.processor(RxDownload.this, url).toObservable())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 暂停下载，ServiceDownload
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/2/8 12:23
     */
    public Observable<?> pauseServiceDownload(final String url) {
        return createGeneralObservable(() -> mDownloadService.pauseDownload(url));
    }

    /**
     * 取消下载，ServiceDownload
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/2/8 12:24
     */
    public Observable<?> cancelServiceDownload(final String url) {
        return createGeneralObservable(() -> mDownloadService.cancelDownload(url));
    }

    /**
     * 删除下载，ServiceDownload
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/2/8 12:25
     */
    public Observable<?> deleteServiceDownload(final String url, final boolean deleteFile) {
        return createGeneralObservable(() -> mDownloadService.deleteDownload(url, deleteFile, RxDownload.this));
    }

    private Observable<?> createGeneralObservable(final GeneralObservableCallback callback) {
        return Observable.create(emitter -> {
            if (!bound) {
                startBindServiceAndDo(() -> {
                    callback.call();
                    emitter.onNext(object);
                    emitter.onComplete();
                });
            } else {
                callback.call();
                emitter.onNext(object);
                emitter.onComplete();
            }
        });
    }

    private void startBindServiceAndDo(final ServiceConnectedCallback callback) {
        if (mContext == null) {
            throw new IllegalArgumentException(Constant.CONTEXT_NULL_HINT);
        }
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(DownloadService.INTENT_KEY, MAX_DOWNLOAD_NUMBER);
        mContext.startService(intent);
        mContext.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                DownloadService.DownloadBinder downloadBinder
                        = (DownloadService.DownloadBinder) binder;
                mDownloadService = downloadBinder.getService();
                mContext.unbindService(this);
                bound = true;
                callback.call();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        }, Context.BIND_AUTO_CREATE);
    }

    private interface GeneralObservableCallback {
        void call();
    }

    private interface ServiceConnectedCallback {
        void call();
    }

}

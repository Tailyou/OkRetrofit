package com.hengda.frame.httputil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hengda.frame.httputil.app.HdAppConfig;
import com.hengda.frame.httputil.update.CheckCallback;
import com.hengda.frame.httputil.update.CheckResponse;
import com.hengda.frame.httputil.update.CheckUpdateActivity;
import com.hengda.zwf.httputil.file_download.RxDownload;
import com.hengda.zwf.httputil.file_download.entity.DownloadEvent;
import com.hengda.zwf.httputil.file_download.entity.DownloadFlag;
import com.orhanobut.logger.Logger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends CheckUpdateActivity {

    RxDownload rxDownload = RxDownload.getInstance().context(this);
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    String url = "http://192.168.10.20/hnbwy/resource/class_img/class_info.zip";
    String saveName = url.substring(url.lastIndexOf("/") + 1);
    String savePath = HdAppConfig.getDefaultFileDir();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //检查更新
        findViewById(R.id.btnUpdate).setOnClickListener(view -> checkNewVersion(new CheckCallback() {
            @Override
            public void hasNewVersion(CheckResponse checkResponse) {
                showHasNewVersionDialog(checkResponse);
            }

            @Override
            public void isAlreadyLatestVersion() {
                showVersionInfoDialog();
            }
        }));

        //正常下载
        findViewById(R.id.btnNormalDown).setOnClickListener(view ->
                rxDownload.download(url, saveName, savePath)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(d -> compositeDisposable.add(d))
                        .doOnNext(status -> Logger.e(status.getFormatStatusString()))
                        .doOnError(throwable -> Logger.e("下载失败：" + throwable.getMessage()))
                        .doOnComplete(() -> {

                        })
                        .subscribe());

        //在Service中下载
        findViewById(R.id.btnServiceDown).setOnClickListener(view ->
                rxDownload.serviceDownload(url, saveName, savePath)
                        .subscribe());
    }

    @Override
    protected void onResume() {
        super.onResume();
        rxDownload.receiveDownloadStatus(url)
                .subscribe(downloadEvent -> {
                    Logger.e(downloadEvent.getDownloadStatus().getFormatStatusString());
                    if (downloadEvent.getFlag() == DownloadFlag.FAILED) {
                        Throwable throwable = downloadEvent.getError();
                        Log.w("Error", throwable);
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        rxDownload.pauseServiceDownload(url).subscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消下载
        compositeDisposable.dispose();
    }

}

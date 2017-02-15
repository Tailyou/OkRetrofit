package com.hengda.frame.httputil;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.hengda.frame.httputil.app.HdAppConfig;
import com.hengda.frame.httputil.update.CheckCallback;
import com.hengda.frame.httputil.update.CheckResponse;
import com.hengda.frame.httputil.update.CheckUpdateActivity;
import com.hengda.zwf.httputil.RxDownload;
import com.hengda.zwf.httputil.entity.DownloadFlag;
import com.hengda.zwf.httputil.function.Utils;
import com.orhanobut.logger.Logger;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends CheckUpdateActivity {

    RxDownload rxDownload = RxDownload.getInstance().context(this);
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    String url = "http://hengdawb-res.oss-cn-hangzhou.aliyuncs.com/GuangXiTech_Res/0002.zip";
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
                        .doOnComplete(() -> unzip())
                        .subscribe());

        //在Service中下载
        findViewById(R.id.btnServiceDown).setOnClickListener(view ->
                rxDownload.serviceDownload(url, saveName, savePath)
                        .subscribe());
    }

    private void unzip() {
        Logger.e("下载完成");
        String filePath = TextUtils.concat(savePath, saveName).toString();
        new Thread(() -> {
            try {
                Utils.unzip(new File(filePath), savePath, () -> Logger.e("解压完成"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rxDownload.receiveDownloadStatus(url)
                .subscribe(downloadEvent -> {
                    Logger.e(downloadEvent.getDownloadStatus().getFormatStatusString());
                    switch (downloadEvent.getFlag()) {
                        case DownloadFlag.COMPLETED:
                            unzip();
                            break;
                        case DownloadFlag.FAILED:
                            Throwable throwable = downloadEvent.getError();
                            Log.w("Error", throwable);
                            break;
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

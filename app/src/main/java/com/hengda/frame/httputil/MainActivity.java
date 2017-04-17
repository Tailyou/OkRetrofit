package com.hengda.frame.httputil;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hengda.frame.httputil.app.HdAppConfig;
import com.hengda.frame.httputil.update.CheckCallback;
import com.hengda.frame.httputil.update.CheckResponse;
import com.hengda.frame.httputil.update.CheckUpdateActivity;
import com.hengda.zwf.httputil.RxDownload;
import com.hengda.zwf.httputil.entity.DownloadFlag;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends CheckUpdateActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    String url = "http://hengdawb-res.oss-cn-hangzhou.aliyuncs.com/HuLuDao_Res/CHINESE.zip";
    String saveName = "CHINESE.zip";
    String savePath = HdAppConfig.getDefaultFileDir();
    TextView tvDownloadStatus;
    TextView tvDownloadPrg;
    RxDownload rxDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rxDownload = RxDownload.getInstance().context(this).maxThread(16).maxRetryCount(3);

        tvDownloadStatus = (TextView) findViewById(R.id.tvDownloadStatus);
        tvDownloadPrg = (TextView) findViewById(R.id.tvDownloadPrg);
        //检查更新
        findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNewVersion(new CheckCallback() {
                    @Override
                    public void hasNewVersion(CheckResponse checkResponse) {
                        showHasNewVersionDialog(checkResponse);
                    }

                    @Override
                    public void isAlreadyLatestVersion() {
                        showVersionInfoDialog();
                    }
                });
            }
        });

        //正常下载
        findViewById(R.id.btnNormalDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download();
            }
        });

        //在Service中下载
        findViewById(R.id.btnServiceDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File root = new File(Environment.getExternalStorageDirectory() + File.separator + "myDir" + File.separator);
                root.mkdirs();
                rxDownload.serviceDownload(url, saveName, savePath).subscribe();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消下载
        compositeDisposable.dispose();
    }

    private void download() {
        rxDownload.download(url, saveName, savePath)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> {
                    compositeDisposable.add(d);
                    tvDownloadStatus.setText("下载地址：" + url + "\n");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
                    tvDownloadStatus.setText(tvDownloadStatus.getText() + "\n开始下载：" + sdf.format(new Date()));
                })
                .doOnNext(status -> tvDownloadPrg.setText("下载进度：" + status.getFormatStatusString()))
                .doOnError(throwable -> tvDownloadStatus.setText("下载失败"))
                .doOnComplete(() -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
                    tvDownloadPrg.setText(tvDownloadPrg.getText() + "\n下载完成：" + sdf.format(new Date()));
                    File file = new File(savePath, saveName);
                    file.delete();
                })
                .subscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rxDownload.receiveDownloadStatus(url)
                .subscribe(downloadEvent -> {
                    switch (downloadEvent.getFlag()) {
                        case DownloadFlag.COMPLETED:
                            Log.e("download status", "COMPLETED");
                            break;
                        case DownloadFlag.FAILED:
                            Log.e("download status", "FAILED");
                            break;
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        rxDownload.pauseServiceDownload(url).subscribe();
    }

}

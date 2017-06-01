package com.hengda.frame.httputil;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hengda.frame.httputil.app.HdAppConfig;
import com.hengda.frame.httputil.http.RetrofitHelper;
import com.hengda.zwf.httputil.download.RxDownload;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    String url = "http://dldir1.qq.com/weixin/android/weixin6330android920.apk";
    String saveName = "weixin6330android920.apk";
    String savePath = HdAppConfig.getDefaultFileDir();
    TextView tvDownloadStatus;
    TextView tvDownloadPrg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDownloadStatus = (TextView) findViewById(R.id.tvDownloadStatus);
        tvDownloadPrg = (TextView) findViewById(R.id.tvDownloadPrg);
        //获取数据
        findViewById(R.id.btnGetData).setOnClickListener(view -> loadData());
        //正常下载
        findViewById(R.id.btnDownload).setOnClickListener(view -> downloadNormal());
    }

    private void downloadNormal() {
        RxDownload.getInstance().context(MainActivity.this)
                .maxThread(4).maxRetryCount(3)
                .download(url, saveName, savePath)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    compositeDisposable.add(disposable);
                    tvDownloadStatus.setText("下载地址：" + url + "\n");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
                    tvDownloadStatus.setText(tvDownloadStatus.getText() + "\n开始下载：" + sdf.format(new Date()));
                })
                .doOnNext(downloadStatus -> {
                    //此处更新下载进度
                    String formatStatusString = downloadStatus.getFormatStatusString();
                    tvDownloadPrg.setText("下载进度：" + formatStatusString);
                })
                .doOnError(throwable -> {
                    //此处处理下载异常
                    tvDownloadStatus.setText("下载失败:" + throwable.getMessage());
                })
                .doOnComplete(() -> {
                    //下载完成，解压或安装
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
                    tvDownloadPrg.setText(tvDownloadPrg.getText() + "\n下载完成：" + sdf.format(new Date()));
                    File file = new File(savePath, saveName);
                    file.delete();
                })
                .subscribe();
    }

    private void loadData() {
        RetrofitHelper.getInstance()
                .loadDatas()
                .doOnSubscribe(disposable -> compositeDisposable.add(disposable))
                .doOnNext(dataBean -> Toast.makeText(MainActivity.this, new Gson().toJson(dataBean), Toast.LENGTH_SHORT).show())
                .doOnError(throwable -> Logger.e(throwable.getMessage()))
                .subscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

}

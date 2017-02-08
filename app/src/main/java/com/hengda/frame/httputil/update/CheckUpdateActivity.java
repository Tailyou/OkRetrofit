package com.hengda.frame.httputil.update;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hengda.frame.httputil.DialogCenter;
import com.hengda.frame.httputil.R;
import com.hengda.frame.httputil.app.HdAppConfig;
import com.hengda.frame.httputil.app.HdConstants;
import com.hengda.frame.httputil.http.HttpRequester;
import com.hengda.zwf.commonutil.AppUtil;
import com.hengda.zwf.commonutil.DataManager;
import com.hengda.zwf.commonutil.HdTool;
import com.hengda.zwf.commonutil.NetUtil;
import com.hengda.zwf.hddialog.DialogClickListener;
import com.hengda.zwf.httputil.download.entity.DownloadStatus;
import com.hengda.zwf.httputil.download.function.RxDownload;
import com.hengda.zwf.httputil.download.function.Utils;
import com.orhanobut.logger.Logger;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.text.TextUtils.concat;
import static java.io.File.separator;


/**
 * 作者：Tailyou （祝文飞）
 * 时间：2016/10/9 10:27
 * 邮箱：tailyou@163.com
 * 描述：
 */
public class CheckUpdateActivity extends Activity {

    private TextView txtProgress;
    private TextView txtUpdateLog;
    private Disposable disposable;

    /**
     * 检查更新
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/30 11:44
     */
    public void checkNewVersion(CheckCallback callback) {
        if (NetUtil.isConnected(CheckUpdateActivity.this)) {
            HttpRequester.getInstance(HdConstants.APP_UPDATE_URL).checkUpdate()
                    .doOnNext(checkResponse -> dealCheckResponse(callback, checkResponse))
                    .doOnError(throwable -> Logger.e(throwable.getMessage()))
                    .subscribe();
        }
    }

    /**
     * 处理检查更新响应
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/2/8 16:05
     */
    private void dealCheckResponse(CheckCallback callback, CheckResponse checkResponse) {
        Logger.e(checkResponse.getMsg());
        switch (checkResponse.getStatus()) {
            case "2001":
                callback.isAlreadyLatestVersion();
                break;
            case "2002":
                callback.hasNewVersion(checkResponse);
                break;
            case "4041":
                break;
        }
    }

    /**
     * 没有更新，显示当前版本信息
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/30 11:46
     */
    public void showVersionInfoDialog() {
        DialogCenter.showDialog(CheckUpdateActivity.this, new DialogClickListener() {
            @Override
            public void p() {
                DialogCenter.hideDialog();
            }
        }, new String[]{"版本更新", "当前已是最新版：" + AppUtil.getVersionName(CheckUpdateActivity.this), "取消"});
    }

    /**
     * 检查到新版本
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/30 11:44
     */
    public void showHasNewVersionDialog(final CheckResponse checkResponse) {
        ScrollView scrollView = (ScrollView) View.inflate(CheckUpdateActivity.this,
                R.layout.dialog_custom_view_scroll_txt, null);
        txtUpdateLog = HdTool.getView(scrollView, R.id.tvUpdateLog);
        txtUpdateLog.setText("检查到新版本：" + checkResponse.getVersionInfo().getVersionName() + "\n更新日志：\n"
                + checkResponse.getVersionInfo().getVersionLog());
        DialogCenter.showDialog(CheckUpdateActivity.this, scrollView, new DialogClickListener() {
            @Override
            public void p() {
                showDownloadingDialog();
                loadAndInstall(checkResponse);
            }

            @Override
            public void n() {
                DialogCenter.hideDialog();
            }
        }, new String[]{"版本更新", "更新", "取消"});
    }

    /**
     * 显示下载进度对话框
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/30 11:45
     */
    private void showDownloadingDialog() {
        txtProgress = (TextView) View.inflate(CheckUpdateActivity.this, R.layout.dialog_custom_view_txt, null);
        txtProgress.setText("下载安装包...");
        DialogCenter.showDialog(CheckUpdateActivity.this, txtProgress, new DialogClickListener() {
            @Override
            public void p() {
                DialogCenter.hideDialog();
                Utils.dispose(disposable);
            }
        }, new String[]{"下载更新", "取消"});
    }

    /**
     * 下载并安装
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/30 11:47
     */
    private void loadAndInstall(CheckResponse checkResponse) {
        String url = checkResponse.getVersionInfo().getVersionUrl();
        String saveName = url.substring(url.lastIndexOf("/") + 1);
        String savePath = HdAppConfig.getDefaultFileDir();

        RxDownload.getInstance()
                .download(url, saveName, savePath)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> disposable = d)
                .doOnNext(status -> updateProgress(status))
                .doOnError(throwable -> Logger.e("下载失败：" + throwable.getMessage()))
                .doOnComplete(() -> installApk(saveName, savePath))
                .subscribe();
    }

    private void installApk(String saveName, String savePath) {
        String apkPath = TextUtils.concat(savePath, saveName).toString();
        AppUtil.installApk(CheckUpdateActivity.this, apkPath);
    }

    private void updateProgress(DownloadStatus status) {
        txtProgress.setText(String.format("正在下载(%s/%s)",
                DataManager.getFormatSize(status.getDownloadSize()),
                DataManager.getFormatSize(status.getTotalSize())));
    }

}

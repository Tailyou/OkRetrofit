package com.hengda.frame.httputil.update;

import android.app.Activity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hengda.frame.httputil.DialogCenter;
import com.hengda.frame.httputil.R;
import com.hengda.frame.httputil.app.HdAppConfig;
import com.hengda.frame.httputil.app.HdConstants;
import com.hengda.frame.httputil.http.FileRequester;
import com.hengda.frame.httputil.http.HttpRequester;
import com.hengda.zwf.commonutil.AppUtil;
import com.hengda.zwf.commonutil.DataManager;
import com.hengda.zwf.commonutil.HdTool;
import com.hengda.zwf.commonutil.NetUtil;
import com.hengda.zwf.hddialog.DialogClickListener;
import com.hengda.zwf.httputil.FileCallback;
import com.hengda.zwf.httputil.RequestSubscriber;
import com.orhanobut.logger.Logger;

import java.io.File;

import okhttp3.ResponseBody;
import retrofit2.Call;


/**
 * 作者：Tailyou （祝文飞）
 * 时间：2016/10/9 10:27
 * 邮箱：tailyou@163.com
 * 描述：
 */
public class CheckUpdateActivity extends Activity {

    private TextView txtProgress;
    private TextView txtUpdateLog;

    /**
     * 检查更新
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/30 11:44
     */
    public void checkNewVersion(CheckCallback callback) {
        if (NetUtil.isConnected(CheckUpdateActivity.this)) {
            HttpRequester.getInstance(HdConstants.APP_UPDATE_URL)
                    .checkUpdate(new RequestSubscriber<CheckResponse>() {
                        @Override
                        public void succeed(CheckResponse checkResponse) {
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

                        @Override
                        public void failed(Throwable e) {
                            Logger.e(e.getMessage());
                        }
                    });
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
        txtProgress = (TextView) View.inflate(CheckUpdateActivity.this,
                R.layout.dialog_custom_view_txt, null);
        txtProgress.setText("下载安装包...");
        DialogCenter.showDialog(CheckUpdateActivity.this, txtProgress, new DialogClickListener() {
            @Override
            public void p() {
                DialogCenter.hideDialog();
                FileRequester.cancel();
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
        String apkUrl = checkResponse.getVersionInfo().getVersionUrl();
        String baseUrl = apkUrl.substring(0, apkUrl.lastIndexOf("/") + 1);
        String fileName = apkUrl.substring(apkUrl.lastIndexOf("/") + 1);
        String fileStoreDir = HdAppConfig.getDefaultFileDir();

        FileRequester.getInstance(baseUrl).loadFileByName(fileName,
                new FileCallback(fileStoreDir, fileName) {
                    @Override
                    public void progress(long progress, long total) {
                        txtProgress.setText(String.format("正在下载(%s/%s)",
                                DataManager.getFormatSize(progress),
                                DataManager.getFormatSize(total)));
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        DialogCenter.hideDialog();
                    }

                    @Override
                    public void onSuccess(File file) {
                        DialogCenter.hideDialog();
                        AppUtil.installApk(CheckUpdateActivity.this, file.getAbsolutePath());
                    }
                });
    }

}

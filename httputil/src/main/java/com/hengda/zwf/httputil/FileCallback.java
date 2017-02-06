package com.hengda.zwf.httputil;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscription;

/**
 * 文件下载回调
 *
 * @author 祝文飞（Tailyou）
 * @time 2017/2/6 13:43
 */
public abstract class FileCallback implements Callback<ResponseBody> {

    private String destFileDir;//存储目录
    private String destFileName;//文件名

    public FileCallback(String destFileDir, String destFileName) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
        subscribeLoadProgress();
    }

    public abstract void onSuccess(File file);

    public abstract void progress(long progress, long total);

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        new Thread(() -> {
            try {
                saveFile(response);
            } catch (Throwable throwable) {
                onFailure(call, throwable);
            }
        }).start();
    }

    public void saveFile(Response<ResponseBody> response) throws Throwable {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len;
        FileOutputStream fos = null;
        try {
            is = response.body().byteStream();
            File dir = new File(destFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, destFileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            unsubscribe();
            if (destFileName.endsWith(".zip"))
                unzipFile(file);
            else
                onSuccess(file);
        } finally {
            if (is != null) is.close();
            if (fos != null) fos.close();
        }
    }

    /**
     * 订阅下载进度
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/2/6 13:44
     */
    private void subscribeLoadProgress() {
        Subscription subscription = RxBus.getInstance().doSubscribe(FileLoadEvent.class,
                fileLoadEvent -> progress(fileLoadEvent.getProgress(), fileLoadEvent.getTotal()),
                throwable -> Logger.e(throwable.getMessage()));
        RxBus.getInstance().addSubscription(this, subscription);
    }

    /**
     * 取消订阅
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/2/6 13:45
     */
    private void unsubscribe() {
        RxBus.getInstance().unSubscribe(this);
    }

    /**
     * 解压
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/2/6 13:45
     */
    private void unzipFile(File file) throws Throwable {
        ZipUtil.unzipFolder(file, destFileDir, () -> onSuccess(file));
    }

}

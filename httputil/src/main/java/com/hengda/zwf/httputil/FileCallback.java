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


public abstract class FileCallback implements Callback<ResponseBody> {

    private String destFileDir;
    private String destFileName;

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

    private void subscribeLoadProgress() {
        Subscription subscription = RxBus.getInstance().doSubscribe(FileLoadEvent.class,
                fileLoadEvent -> progress(fileLoadEvent.getProgress(), fileLoadEvent.getTotal()),
                throwable -> Logger.e(throwable.getMessage()));
        RxBus.getInstance().addSubscription(this, subscription);
    }

    private void unsubscribe() {
        RxBus.getInstance().unSubscribe(this);
    }

    private void unzipFile(File file) throws Throwable {
        ZipUtil.unzipFolder(file, destFileDir, () -> onSuccess(file));
    }

}

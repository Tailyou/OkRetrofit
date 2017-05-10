package com.hengda.zwf.httputil.httpload.function;


import com.hengda.zwf.httputil.httpload.entity.DownloadRange;
import com.hengda.zwf.httputil.httpload.entity.DownloadStatus;
import com.hengda.zwf.httputil.httpload.entity.DownloadType;
import com.hengda.zwf.httputil.httpload.entity.DownloadTypeFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.FlowableEmitter;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.hengda.zwf.httputil.httpload.function.Constant.DOWNLOAD_RECORD_FILE_DAMAGED;
import static com.hengda.zwf.httputil.httpload.function.Constant.DOWNLOAD_URL_EXISTS;
import static com.hengda.zwf.httputil.httpload.function.Constant.TEST_RANGE_SUPPORT;
import static com.hengda.zwf.httputil.httpload.function.Utils.contentLength;
import static com.hengda.zwf.httputil.httpload.function.Utils.lastModify;
import static com.hengda.zwf.httputil.httpload.function.Utils.log;
import static com.hengda.zwf.httputil.httpload.function.Utils.notSupportRange;
import static com.hengda.zwf.httputil.httpload.function.Utils.requestRangeNotSatisfiable;
import static com.hengda.zwf.httputil.httpload.function.Utils.serverFileChanged;
import static com.hengda.zwf.httputil.httpload.function.Utils.serverFileNotChange;


public class DownloadHelper {

    private DownloadApi mDownloadApi;
    private FileHelper mFileHelper;
    private DownloadTypeFactory typeFactory;
    private Map<String, String[]> mDownloadRecord;
    private int MAX_RETRY_COUNT = 3;

    public DownloadHelper() {
        mDownloadApi = RetrofitProvider.getInstance().create(DownloadApi.class);
        mDownloadRecord = new HashMap<>();
        mFileHelper = new FileHelper();
        typeFactory = new DownloadTypeFactory(this);
    }

    public void setRetrofit(Retrofit retrofit) {
        mDownloadApi = retrofit.create(DownloadApi.class);
    }

    public void setDefaultSavePath(String defaultSavePath) {
        mFileHelper.setDefaultSavePath(defaultSavePath);
    }

    public int getMaxRetryCount() {
        return MAX_RETRY_COUNT;
    }

    public void setMaxRetryCount(int MAX_RETRY_COUNT) {
        this.MAX_RETRY_COUNT = MAX_RETRY_COUNT;
    }

    public String[] getFileSavePaths(String savePath) {
        return mFileHelper.getRealDirectoryPaths(savePath);
    }

    public String[] getRealFilePaths(String saveName, String savePath) {
        return mFileHelper.getRealFilePaths(saveName, savePath);
    }

    public DownloadApi getDownloadApi() {
        return mDownloadApi;
    }

    public int getMaxThreads() {
        return mFileHelper.getMaxThreads();
    }

    public void setMaxThreads(int MAX_THREADS) {
        mFileHelper.setMaxThreads(MAX_THREADS);
    }

    public void prepareNormalDownload(String url, long fileLength, String lastModify)
            throws IOException, ParseException {
        mFileHelper.prepareDownload(getLastModifyFile(url), getFile(url), fileLength, lastModify);
    }

    public void saveNormalFile(FlowableEmitter<DownloadStatus> emitter, String url, Response<ResponseBody> resp) {
        mFileHelper.saveFile(emitter, getFile(url), resp);
    }

    public DownloadRange readDownloadRange(String url, int i) throws IOException {
        return mFileHelper.readDownloadRange(getTempFile(url), i);
    }

    public void prepareMultiThreadDownload(
            String url, long fileLength, String lastModify)
            throws IOException, ParseException {
        mFileHelper.prepareDownload(getLastModifyFile(url),
                getTempFile(url), getFile(url),
                fileLength, lastModify);
    }

    public void saveRangeFile(
            FlowableEmitter<DownloadStatus> emitter,
            int i, long start, long end,
            String url, ResponseBody response) {
        mFileHelper.saveFile(emitter, i, start, end,
                getTempFile(url), getFile(url), response);
    }

    public Observable<DownloadStatus> downloadDispatcher(
            final String url,
            final String saveName,
            final String savePath) {
        try {
            beforeDownload(url, saveName, savePath);
        } catch (IOException e) {
            return Observable.error(e);
        }
        return getDownloadType(url)
                .flatMap(new Function<DownloadType, ObservableSource<DownloadStatus>>() {
                    @Override
                    public ObservableSource<DownloadStatus> apply(DownloadType downloadType) throws Exception {
                        downloadType.prepareDownload();
                        return downloadType.startDownload();
                    }
                })
                .doOnError(throwable -> {
                    if (throwable instanceof CompositeException) {
                        CompositeException realException = (CompositeException) throwable;
                        List<Throwable> exceptions = realException.getExceptions();
                        for (Throwable each : exceptions) {
                            log(each);
                        }
                    } else {
                        log(throwable);
                    }
                })
                .doFinally(() -> deleteDownloadRecord(url));
    }

    public Observable<DownloadType> notSupportHead(final String url) throws IOException {
        return mDownloadApi
                .GET_withIfRange(TEST_RANGE_SUPPORT, readLastModify(url), url)
                .map(response -> {
                    if (serverFileNotChange(response)) {
                        return getWhenServerFileNotChange(response, url);
                    } else if (serverFileChanged(response)) {
                        return getWhenServerFileChanged(response, url);
                    } else {
                        return typeFactory.unable();
                    }
                })
                .compose(Utils.retry(MAX_RETRY_COUNT));
    }

    private void beforeDownload(String url, String saveName, String savePath) throws IOException {
        if (recordExists(url)) {
            throw new IOException(DOWNLOAD_URL_EXISTS);
        }
        addDownloadRecord(url, saveName, savePath);
    }

    private void addDownloadRecord(String url, String saveName, String savePath) throws IOException {
        mFileHelper.createDownloadDirs(savePath);
        mDownloadRecord.put(url, getRealFilePaths(saveName, savePath));
    }

    private boolean recordExists(String url) {
        return mDownloadRecord.get(url) != null;
    }

    private void deleteDownloadRecord(String url) {
        mDownloadRecord.remove(url);
    }

    private String readLastModify(String url) throws IOException {
        return mFileHelper.getLastModify(getLastModifyFile(url));
    }

    private boolean downloadNotComplete(String url) throws IOException {
        return mFileHelper.downloadNotComplete(getTempFile(url));
    }

    private boolean downloadNotComplete(String url, long contentLength) {
        return getFile(url).length() != contentLength;
    }

    private boolean needReDownload(String url, long contentLength) throws IOException {
        return tempFileNotExists(url) || tempFileDamaged(url, contentLength);
    }

    private boolean downloadFileExists(String url) {
        return getFile(url).exists();
    }

    private boolean tempFileDamaged(String url, long fileLength) throws IOException {
        return mFileHelper.tempFileDamaged(getTempFile(url), fileLength);
    }

    private boolean tempFileNotExists(String url) {
        return !getTempFile(url).exists();
    }

    private File getFile(String url) {
        return new File(mDownloadRecord.get(url)[0]);
    }

    private File getTempFile(String url) {
        return new File(mDownloadRecord.get(url)[1]);
    }

    private File getLastModifyFile(String url) {
        return new File(mDownloadRecord.get(url)[2]);
    }

    private Observable<DownloadType> getDownloadType(String url) {
        if (downloadFileExists(url)) {
            try {
                return getWhenFileExists(url);
            } catch (IOException e) {
                return getWhenFileNotExists(url);
            }
        } else {
            return getWhenFileNotExists(url);
        }
    }

    private Observable<DownloadType> getWhenFileNotExists(final String url) {
        return mDownloadApi
                .HEAD(TEST_RANGE_SUPPORT, url)
                .map(response -> {
                    if (notSupportRange(response)) {
                        return typeFactory.normal(url, contentLength(response),
                                lastModify(response));
                    } else {
                        return typeFactory.multiThread(url, contentLength(response),
                                lastModify(response));
                    }
                })
                .compose(Utils.retry(MAX_RETRY_COUNT));
    }

    private Observable<DownloadType> getWhenFileExists(final String url) throws IOException {
        return mDownloadApi
                .HEAD_WithIfRange(TEST_RANGE_SUPPORT, readLastModify(url), url)
                .map(response -> {
                    if (serverFileNotChange(response)) {
                        return getWhenServerFileNotChange(response, url);
                    } else if (serverFileChanged(response)) {
                        return getWhenServerFileChanged(response, url);
                    } else if (requestRangeNotSatisfiable(response)) {
                        return typeFactory.needGET(url,
                                contentLength(response), lastModify(response));
                    } else {
                        return typeFactory.unable();
                    }
                })
                .compose(Utils.retry(MAX_RETRY_COUNT));
    }

    private DownloadType getWhenServerFileChanged(Response<Void> resp, String url) {
        if (notSupportRange(resp)) {
            return typeFactory.normal(url,
                    contentLength(resp), lastModify(resp));
        } else {
            return typeFactory.multiThread(url,
                    contentLength(resp), lastModify(resp));
        }
    }

    private DownloadType getWhenServerFileNotChange(Response<Void> resp, String url) {
        if (notSupportRange(resp)) {
            return getWhenNotSupportRange(resp, url);
        } else {
            return getWhenSupportRange(resp, url);
        }
    }

    private DownloadType getWhenSupportRange(Response<Void> resp, String url) {
        long contentLength = contentLength(resp);
        try {
            if (needReDownload(url, contentLength)) {
                return typeFactory.multiThread(url, contentLength, lastModify(resp));
            }
            if (downloadNotComplete(url)) {
                return typeFactory.continued(url, contentLength, lastModify(resp));
            }
        } catch (IOException e) {
            log(DOWNLOAD_RECORD_FILE_DAMAGED);
            return typeFactory.multiThread(url, contentLength, lastModify(resp));
        }
        return typeFactory.already(contentLength);
    }

    private DownloadType getWhenNotSupportRange(Response<Void> resp, String url) {
        long contentLength = contentLength(resp);
        if (downloadNotComplete(url, contentLength)) {
            return typeFactory.normal(url, contentLength, lastModify(resp));
        } else {
            return typeFactory.already(contentLength);
        }
    }

}

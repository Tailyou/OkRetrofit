package com.hengda.zwf.httputil.entity;

import com.hengda.zwf.httputil.function.Constant;
import com.hengda.zwf.httputil.function.DownloadHelper;
import com.hengda.zwf.httputil.function.Utils;

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static com.hengda.zwf.httputil.function.Utils.log;
import static java.lang.Thread.currentThread;

public abstract class DownloadType {

    String mUrl;
    long mFileLength;
    String mLastModify;
    DownloadHelper mDownloadHelper;

    public abstract void prepareDownload() throws IOException, ParseException;

    public abstract Observable<DownloadStatus> startDownload() throws IOException;

    static class NormalDownload extends DownloadType {
        @Override
        public void prepareDownload() throws IOException, ParseException {
            log(Constant.NORMAL_DOWNLOAD_PREPARE);
            mDownloadHelper.prepareNormalDownload(mUrl, mFileLength, mLastModify);
        }

        @Override
        public Observable<DownloadStatus> startDownload() {
            return mDownloadHelper
                    .getDownloadApi()
                    .download(null, mUrl)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(subscription -> log(Constant.NORMAL_DOWNLOAD_STARTED))
                    .doOnError(throwable -> log(Constant.NORMAL_DOWNLOAD_FAILED))
                    .doOnComplete(() -> log(Constant.NORMAL_DOWNLOAD_COMPLETED))
                    .flatMap(new Function<Response<ResponseBody>, Publisher<DownloadStatus>>() {
                        @Override
                        public Publisher<DownloadStatus> apply(Response<ResponseBody> response)
                                throws Exception {
                            return normalSave(response);
                        }
                    })
                    .compose(Utils.retry2(mDownloadHelper.getMaxRetryCount()))
                    .toObservable();
        }

        private Publisher<DownloadStatus> normalSave(final Response<ResponseBody> response) {
            return Flowable
                    .create(new FlowableOnSubscribe<DownloadStatus>() {
                        @Override
                        public void subscribe(FlowableEmitter<DownloadStatus> e)
                                throws Exception {
                            mDownloadHelper.saveNormalFile(e, mUrl, response);
                        }
                    }, BackpressureStrategy.LATEST);
        }
    }

    static class ContinueDownload extends DownloadType {
        @Override
        public void prepareDownload() throws IOException, ParseException {
            log(prepareLog());
        }

        @Override
        public Observable<DownloadStatus> startDownload() throws IOException {
            List<Publisher<DownloadStatus>> tasks = new ArrayList<>();
            for (int i = 0; i < mDownloadHelper.getMaxThreads(); i++) {
                tasks.add(rangeDownloadTask(i));
            }
            return Flowable
                    .mergeDelayError(tasks)
                    .doOnSubscribe(subscription -> log(startLog()))
                    .doOnComplete(() -> log(completeLog()))
                    .doOnError(throwable -> log(errorLog()))
                    .toObservable();
        }

        protected String prepareLog() {
            return Constant.CONTINUE_DOWNLOAD_PREPARE;
        }

        protected String startLog() {
            return Constant.CONTINUE_DOWNLOAD_STARTED;
        }

        protected String completeLog() {
            return Constant.CONTINUE_DOWNLOAD_COMPLETED;
        }

        protected String errorLog() {
            return Constant.CONTINUE_DOWNLOAD_FAILED;
        }

        private Publisher<DownloadStatus> rangeDownloadTask(final int index) {
            return Flowable
                    .create(new FlowableOnSubscribe<DownloadRange>() {
                        @Override
                        public void subscribe(FlowableEmitter<DownloadRange> emitter)
                                throws Exception {
                            DownloadRange mRange = mDownloadHelper.readDownloadRange(mUrl, index);
                            if (mRange.legal()) {
                                emitter.onNext(mRange);
                            }
                            emitter.onComplete();
                        }
                    }, BackpressureStrategy.ERROR)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(range -> log(Constant.RANGE_DOWNLOAD_STARTED, currentThread().getName(), range.start, range.end))
                    .flatMap(new Function<DownloadRange, Publisher<DownloadStatus>>() {
                        @Override
                        public Publisher<DownloadStatus> apply(final DownloadRange range)
                                throws Exception {

                            return startRangeDownload(range, index);
                        }
                    })
                    .doOnComplete(() -> log(Constant.RANGE_DOWNLOAD_COMPLETED, currentThread().getName()))
                    .compose(Utils.retry2(mDownloadHelper.getMaxRetryCount()));
        }

        private Publisher<DownloadStatus> startRangeDownload(final DownloadRange range, final int index) {
            String rangeStr = "bytes=" + range.start + "-" + range.end;
            return mDownloadHelper
                    .getDownloadApi()
                    .download(rangeStr, mUrl)
                    .flatMap(new Function<Response<ResponseBody>, Publisher<DownloadStatus>>() {
                        @Override
                        public Publisher<DownloadStatus> apply(Response<ResponseBody> resp)
                                throws Exception {
                            return rangeSave(range.start, range.end, index, resp.body());
                        }
                    });
        }

        private Publisher<DownloadStatus> rangeSave(
                final long start, final long end,
                final int index, final ResponseBody response) {
            return Flowable.create(new FlowableOnSubscribe<DownloadStatus>() {
                @Override
                public void subscribe(FlowableEmitter<DownloadStatus> emitter)
                        throws Exception {
                    mDownloadHelper.saveRangeFile(emitter,
                            index, start, end, mUrl, response);
                }
            }, BackpressureStrategy.LATEST);
        }
    }

    static class AlreadyDownloaded extends DownloadType {
        @Override
        public void prepareDownload() throws IOException, ParseException {
            log(Constant.ALREADY_DOWNLOAD_HINT);
        }

        @Override
        public Observable<DownloadStatus> startDownload() throws IOException {
            return Observable.just(new DownloadStatus(mFileLength, mFileLength));
        }
    }

    static class NotSupportHEAD extends DownloadType {
        @Override
        public void prepareDownload() throws IOException, ParseException {
            log(Constant.NOT_SUPPORT_HEAD_HINT);
        }

        @Override
        public Observable<DownloadStatus> startDownload() throws IOException {
            return mDownloadHelper
                    .notSupportHead(mUrl)
                    .flatMap(new Function<DownloadType, ObservableSource<DownloadStatus>>() {
                        @Override
                        public ObservableSource<DownloadStatus> apply(DownloadType downloadType)
                                throws Exception {
                            downloadType.prepareDownload();
                            return downloadType.startDownload();
                        }
                    });
        }
    }

    static class UnableDownload extends DownloadType {
        @Override
        public void prepareDownload() throws IOException, ParseException {
            log(Constant.UNABLE_DOWNLOAD_HINT);
        }

        @Override
        public Observable<DownloadStatus> startDownload() throws IOException {
            return Observable.error(new UnableDownloadException(Constant.UNABLE_DOWNLOAD_HINT));
        }
    }

    static class MultiThreadDownload extends ContinueDownload {
        @Override
        public void prepareDownload() throws IOException, ParseException {
            super.prepareDownload();
            mDownloadHelper.prepareMultiThreadDownload(mUrl, mFileLength, mLastModify);
        }

        @Override
        public Observable<DownloadStatus> startDownload() throws IOException {
            return super.startDownload();
        }

        @Override
        protected String prepareLog() {
            return Constant.MULTITHREADING_DOWNLOAD_PREPARE;
        }

        @Override
        protected String startLog() {
            return Constant.MULTITHREADING_DOWNLOAD_STARTED;
        }

        @Override
        protected String completeLog() {
            return Constant.MULTITHREADING_DOWNLOAD_COMPLETED;
        }

        @Override
        protected String errorLog() {
            return Constant.MULTITHREADING_DOWNLOAD_FAILED;
        }
    }

}

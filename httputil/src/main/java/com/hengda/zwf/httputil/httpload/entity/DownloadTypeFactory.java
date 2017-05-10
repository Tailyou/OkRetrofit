package com.hengda.zwf.httputil.httpload.entity;

import com.hengda.zwf.httputil.httpload.function.DownloadHelper;

public class DownloadTypeFactory {

    private DownloadHelper mDownloadHelper;

    public DownloadTypeFactory(DownloadHelper downloadHelper) {
        this.mDownloadHelper = downloadHelper;
    }

    public DownloadType normal(String url, long fileLength, String lastModify) {
        DownloadType type = new DownloadType.NormalDownload();
        type.mUrl = url;
        type.mFileLength = fileLength;
        type.mLastModify = lastModify;
        type.mDownloadHelper = this.mDownloadHelper;
        return type;
    }

    public DownloadType continued(String url, long fileLength, String lastModify) {
        DownloadType type = new DownloadType.ContinueDownload();
        type.mUrl = url;
        type.mFileLength = fileLength;
        type.mLastModify = lastModify;
        type.mDownloadHelper = this.mDownloadHelper;
        return type;
    }

    public DownloadType multiThread(String url, long fileLength, String lastModify) {
        DownloadType type = new DownloadType.MultiThreadDownload();
        type.mUrl = url;
        type.mFileLength = fileLength;
        type.mLastModify = lastModify;
        type.mDownloadHelper = this.mDownloadHelper;
        return type;
    }

    public DownloadType already(long fileLength) {
        DownloadType type = new DownloadType.AlreadyDownloaded();
        type.mFileLength = fileLength;
        type.mDownloadHelper = this.mDownloadHelper;
        return type;
    }

    public DownloadType needGET(String url, long fileLength, String lastModify) {
        DownloadType type = new DownloadType.NotSupportHEAD();
        type.mUrl = url;
        type.mFileLength = fileLength;
        type.mLastModify = lastModify;
        type.mDownloadHelper = this.mDownloadHelper;
        return type;
    }

    public DownloadType unable(){
        return new DownloadType.UnableDownload();
    }

}

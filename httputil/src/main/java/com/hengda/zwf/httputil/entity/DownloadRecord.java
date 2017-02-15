package com.hengda.zwf.httputil.entity;

public class DownloadRecord {

    private String url;
    private String saveName;
    private String savePath;
    private DownloadStatus mStatus;
    private int flag = DownloadFlag.NORMAL;
    private long date;

    public DownloadRecord() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public DownloadStatus getStatus() {
        return mStatus;
    }

    public void setStatus(DownloadStatus status) {
        mStatus = status;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

}

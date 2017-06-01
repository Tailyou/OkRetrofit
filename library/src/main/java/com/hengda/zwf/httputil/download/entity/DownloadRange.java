package com.hengda.zwf.httputil.download.entity;

public class DownloadRange {

    public long start;
    public long end;
    public long size;

    public DownloadRange(long start, long end) {
        this.start = start;
        this.end = end;
        this.size = end - start + 1;
    }

    public boolean legal() {
        return start <= end;
    }

}

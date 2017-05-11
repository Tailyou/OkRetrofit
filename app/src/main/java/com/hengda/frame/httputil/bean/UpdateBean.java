package com.hengda.frame.httputil.bean;

/**
 * 作者：Tailyou （祝文飞）
 * 时间：2016/7/4 13:18
 * 邮箱：tailyou@163.com
 * 描述：App检查更新返回
 */
public class UpdateBean {

    private String versionNo;
    private String versionName;
    private String versionUrl;
    private String versionLog;

    public String getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionUrl() {
        return versionUrl;
    }

    public void setVersionUrl(String versionUrl) {
        this.versionUrl = versionUrl;
    }

    public String getVersionLog() {
        return versionLog;
    }

    public void setVersionLog(String versionLog) {
        this.versionLog = versionLog;
    }

}

package com.hengda.zwf.httputil.httprequest;

/**
 * 作者：Tailyou （祝文飞）
 * 时间：2016/7/4 13:18
 * 邮箱：tailyou@163.com
 * 描述：App检查更新返回
 */
public class UpdateResponse {

    private String status;
    private String msg;
    private VersionInfoEntity versionInfo;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public VersionInfoEntity getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(VersionInfoEntity versionInfo) {
        this.versionInfo = versionInfo;
    }

    public static class VersionInfoEntity {

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

}

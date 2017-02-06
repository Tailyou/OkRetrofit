package com.hengda.frame.httputil.update;

/**
 * 作者：Tailyou （祝文飞）
 * 时间：2016/7/4 13:18
 * 邮箱：tailyou@163.com
 * 描述：
 */
public class CheckResponse {

    /**
     * status : 2002
     * msg : 有新的版本
     * versionInfo : {"versionNo":"1.0.1","versionName":"第一版","versionUrl":"http://www.baidu
     * .com","versionLog":"此版本已过期，为保证功能的使用，请升级成最新版本!"}
     */

    private String status;
    private String msg;
    /**
     * versionNo : 1.0.1
     * versionName : 第一版
     * versionUrl : http://www.baidu.com
     * versionLog : 此版本已过期，为保证功能的使用，请升级成最新版本!
     */

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

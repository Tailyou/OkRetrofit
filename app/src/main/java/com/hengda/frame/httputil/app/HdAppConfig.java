package com.hengda.frame.httputil.app;


import com.hengda.zwf.commonutil.SDCardUtil;
import com.hengda.zwf.commonutil.SharedPrefUtil;


/**
 * 作者：Tailyou
 * 时间：2016/1/11 10:05
 * 邮箱：tailyou@163.com
 * 描述：恒达App配置文件
 */
public class HdAppConfig {

    private static SharedPrefUtil appConfig = new SharedPrefUtil(HdApplication.mContext, HdConstants.APP_SHARED_PREF_NAME);

    //    SharedPref字段
    public static final String IP_PORT = "IP_PORT";//服务器IP和端口

    public static void setDefaultIpPort(String ipPort) {
        appConfig.setPrefString(IP_PORT, ipPort);
    }

    public static String getDefaultIpPort() {
        return appConfig.getPrefString(IP_PORT, HdConstants.DEFAULT_IP_PORT);
    }

    //    获取默认文件存储目录
    public static String getDefaultFileDir() {
        return SDCardUtil.getSDCardPath() + "Hd_Smart_Res/";
    }

}

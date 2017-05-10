package com.hengda.frame.httputil.app;


import android.text.TextUtils;

import com.hengda.zwf.commonutil.FileUtils;
import com.hengda.zwf.commonutil.SDCardUtil;
import com.hengda.zwf.commonutil.SharedPrefUtil;


/**
 * 作者：Tailyou
 * 时间：2016/1/11 10:05
 * 邮箱：tailyou@163.com
 * 描述：恒达App配置文件
 */
public class HdAppConfig {

    private static SharedPrefUtil appConfig = new SharedPrefUtil(HdApplication.mContext,
            HdConstants.APP_SHARED_PREF_NAME);

    //    SharedPref字段
    public static final String LANGUAGE = "LANGUAGE";//当前语种
    public static final String PASSWORD = "PASSWORD";//管理员密码
    public static final String RSSI = "RSSI";//RSSI门限
    public static final String AUTO_FLAG = "AUTO_FLAG";//自动讲解：0关闭，1开启
    public static final String SMART_SERVICE = "SMART_SERVICE";//智慧服务：0关闭，1开启
    public static final String AUTO_MODE = "AUTO_MODE";//讲解方式：0隔一，1连续
    public static final String STC_MODE = "STC_MODE";//报警方式：0直接报警，1间接报警
    public static final String RECEIVE_NO_MODE = "RECEIVE_NO_MODE";//收号方式：0蓝牙，1RFID，2混合
    public static final String SCREEN_MODE = "SCREEN_MODE";//节能模式：0关闭，1开启
    public static final String POWER_MODE = "POWER_MODE";//关机权限：0禁止，1允许
    public static final String POWER_PERMI = "POWER_PERMI";//禁止关机下是否获取到关机权限：0无，1有
    public static final String IP_PORT = "IP_PORT";//服务器IP和端口

    public static void setDefaultIpPort(String ipPort) {
        appConfig.setPrefString(IP_PORT, ipPort);
    }

    public static String getDefaultIpPort() {
        return appConfig.getPrefString(IP_PORT, HdConstants.DEFAULT_IP_PORT);
    }

    public static void setDeviceNo(String deviceNo) {
        FileUtils.writeStringToFile(SDCardUtil.getSDCardPath() + "DeviceNo.txt", deviceNo, false);
    }

    public static String getDeviceNo() {
        StringBuilder deviceNo = FileUtils.readStringFromFile(SDCardUtil.getSDCardPath() + "DeviceNo.txt", "UTF-8");
        return TextUtils.isEmpty(deviceNo) ? HdConstants.DEFAULT_DEVICE_NO : deviceNo.toString();
    }

    //    获取默认文件存储目录
    public static String getDefaultFileDir() {
        return SDCardUtil.getSDCardPath() + "Hd_Smart_Res/";
    }

}

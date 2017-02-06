package com.hengda.frame.httputil.app;

import android.app.Application;
import android.content.Context;

import com.orhanobut.logger.Logger;

/**
 * 作者：Tailyou
 * 时间：2016/1/8 10:42
 * 邮箱：tailyou@163.com
 * 描述：
 */
public class HdApplication extends Application {
    /**
     * 全局上下文环境
     */
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init("HD_SMART");
        mContext = getApplicationContext();
    }

}

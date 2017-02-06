package com.hengda.frame.httputil.http;


import com.hengda.frame.httputil.app.HdAppConfig;
import com.hengda.frame.httputil.app.HdApplication;
import com.hengda.frame.httputil.app.HdConstants;
import com.hengda.frame.httputil.update.CheckResponse;
import com.hengda.zwf.commonutil.AppUtil;
import com.hengda.zwf.httputil.RequestApi;

import java.util.Hashtable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：Tailyou （祝文飞）
 * 时间：2016/6/11 16:17
 * 邮箱：tailyou@163.com
 * 描述：
 */
public class HttpRequester extends RequestApi {

    private IHttpService iHttpService;
    private volatile static HttpRequester instance;
    private static Hashtable<String, HttpRequester> mRequestApiTable;

    static {
        mRequestApiTable = new Hashtable<>();
    }

    /**
     * 单例模式
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:31
     */
    private HttpRequester(String baseHttpUrl) {
        super(baseHttpUrl);
        iHttpService = retrofit.create(IHttpService.class);
    }

    /**
     * 获取实例-单例
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:32
     */
    public static HttpRequester getInstance(String baseHttpUrl) {
        instance = mRequestApiTable.get(baseHttpUrl);
        if (instance == null) {
            synchronized (HttpRequester.class) {
                if (instance == null) {
                    instance = new HttpRequester(baseHttpUrl);
                    mRequestApiTable.put(baseHttpUrl, instance);
                }
            }
        }
        return instance;
    }

    /**
     * 检查更新
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:37
     */
    public void checkUpdate(Subscriber<CheckResponse> subscriber) {
        Observable<CheckResponse> observable = iHttpService.checkUpdate(HdConstants.APP_KEY,
                HdConstants.APP_SECRET, 3, AppUtil.getVersionCode(HdApplication.mContext),
                HdAppConfig.getDeviceNo());

        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

}

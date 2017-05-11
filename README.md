OkRetrofit
==================

# 概述

OkRetrofit是一个基于RxJava2+Retrofit2封装的网络库，包含文件下载和网络请求两部分，其中文件下载参考RxDownload修改，网络请求做了适当的封装，使用起来特别简单。

#### 文件下载

- 智能判断服务器是否支持断点续传并适配相应下载方式；
- 智能判断同一地址对应的文件在服务端是否有改变并重新下载；
- 支持多线程下载，可设置下载线程数；
- 支持下载状态、下载进度监听；
- 支持在Service中下载文件，内置DownloadService；

#### 网络请求

- 内置`BaseRetrofit`,提供了抽象方法`initOkHttp`供上层实现，可在此方法中配置日志、缓存、超时等；
- 内置服务器统一返回`HttpResponse`和请求异常`HttpException`；
- 内置统一线程处理和统一返回结果转换方法；

# 使用

#### Gradle

```groovy
dependencies {
    compile 'com.hengda.zwf:OkRetrofit:0.0.1'
}
```

#### Maven

```groovy
<dependency>
  <groupId>com.hengda.zwf</groupId>
  <artifactId>OkRetrofit</artifactId>
  <version>0.0.1</version>
  <type>pom</type>
</dependency>
```


#### 文件下载

```java
RxDownload.getInstance().context(MainActivity.this)
        .maxThread(4).maxRetryCount(3)
        .download(url, saveName, savePath)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                compositeDisposable.add(disposable);
                tvDownloadStatus.setText("下载地址：" + url + "\n");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
                tvDownloadStatus.setText(tvDownloadStatus.getText() + "\n开始下载：" + sdf.format(new Date()));
            }
        })
        .doOnNext(new Consumer<DownloadStatus>() {
            @Override
            public void accept(DownloadStatus downloadStatus) throws Exception {
                tvDownloadPrg.setText("下载进度：" + downloadStatus.getFormatStatusString());
            }
        })
        .doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                tvDownloadStatus.setText("下载失败:" + throwable.getMessage());
            }
        })
        .doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
                tvDownloadPrg.setText(tvDownloadPrg.getText() + "\n下载完成：" + sdf.format(new Date()));
                File file = new File(savePath, saveName);
                file.delete();
            }
        })
        .subscribe();     
```

#### 网络请求

1、新建定义方法的接口

```java
public interface HttpApis {

    /**
     * 获取数据
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/12/21 14:23
     */
    @GET("index.php?g=mapi&m=appdatas&a=datas")
    Observable<HttpResponse<DataBean>> loadDatas();

}
```

2、实现BaseRetrofit

```java
package com.hengda.frame.httputil.http;

import com.hengda.frame.httputil.app.HdAppConfig;
import com.hengda.frame.httputil.bean.DataBean;
import com.hengda.zwf.httputil.request.BaseRetrofit;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;

/**
 * 作者：祝文飞（Tailyou）
 * 邮箱：tailyou@163.com
 * 时间：2017/2/15 14:06
 * 描述：
 */
public class RetrofitHelper extends BaseRetrofit {

    private static Hashtable<String, RetrofitHelper> retrofitHelperHashtable = new Hashtable<>();
    private static HttpApis httpApis = null;
    private volatile static RetrofitHelper instance;

    /**
     * 单例模式
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:31
     */
    private RetrofitHelper() {
        super();
        httpApis = getApiService(setupBaseHttpUrl(), HttpApis.class);
    }

    /**
     * 获取实例-单例
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:32
     */
    public static RetrofitHelper getInstance() {
        String baseUrl = setupBaseHttpUrl();
        instance = retrofitHelperHashtable.get(baseUrl);
        if (instance == null) {
            synchronized (RetrofitHelper.class) {
                if (instance == null) {
                    instance = new RetrofitHelper();
                    retrofitHelperHashtable.clear();
                    retrofitHelperHashtable.put(baseUrl, instance);
                }
            }
        }
        return instance;
    }

    /**
     * 组装网络请求基地址
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:38
     */
    public static String setupBaseHttpUrl() {
        return "http://" + HdAppConfig.getDefaultIpPort() + "/hnbwy/";
    }

    /**
     * 在此配置超时，缓存，日志等
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/5/10 11:10
     */
    @Override
    public OkHttpClient initOkHttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        return builder.build();
    }

    /**
     * 获取数据
     *
     * @author 祝文飞（Tailyou）
     * @time 2017/1/3 11:57
     */
    public Observable<DataBean> loadDatas() {
        return httpApis.loadDatas().compose(rxSchedulerHelper()).compose(handleResult());
    }

}
```

3、使用

```java
RetrofitHelper.getInstance()
        .loadDatas()
        .doOnSubscribe(new Consumer<Disposable>() {
            @Override public void accept(Disposable disposable) throws Exception {
                compositeDisposable.add(disposable);
            }
        })
        .subscribe(new Consumer<DataBean>() {
            @Override
            public void accept(DataBean dataBean) throws Exception {
                Toast.makeText(MainActivity.this, new Gson().toJson(dataBean), Toast.LENGTH_SHORT).show();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Logger.e(throwable.getMessage());
            }
        });                
```



## 一、概述

Retrofit+RxJava是当前最流行的Android网络交互解决方案。OkRetrofit是一个基于Retrofit2+RxJava2封装的文件下载和网络请求库，
其中文件下载部分参考了RxDownload，去掉了RxPermission相关的代码，网络请求部分做了适当的抽象和封装，方便使用的同时也不影响相关部分的定制。

### 1.1 文件下载

1. 智能判断服务器是否支持断点续传并适配相应下载方式；
2. 智能判断同一地址对应的文件在服务端是否有改变并重新下载；
3. 支持多线程下载，可设置下载线程数；
4. 支持下载状态、下载进度监听；
5. 支持在Service中下载文件，内置DownloadService；

<br/>

### 1.2 网络请求

1. 内置`BaseRetrofit`,提供了抽象方法`initOkHttp`供上层实现，可在此方法中配置日志、缓存、超时等；
2. 内置服务器统一返回`HttpResponse`和请求异常`HttpException`；
3. 内置统一线程处理和统一返回结果转换方法；
  
<br/>

## 二、使用

### 2.1 Gradle
OkRetrofit已上传到jcenter，在gradle中直接引用即可。

```groovy
dependencies {
    compile 'com.hengda.zwf:OkRetrofit:0.0.1'
}
```


### 2.2 文件下载

```java
		RxDownload.getInstance().context(MainActivity.this)
                .maxThread(4).maxRetryCount(3)
                .download(url, saveName, savePath)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    compositeDisposable.add(disposable);
                    tvDownloadStatus.setText("下载地址：" + url + "\n");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
                    tvDownloadStatus.setText(tvDownloadStatus.getText() + "\n开始下载：" + sdf.format(new Date()));
                })
                .doOnNext(downloadStatus -> {
                    //此处更新下载进度
                    String formatStatusString = downloadStatus.getFormatStatusString();
                    tvDownloadPrg.setText("下载进度：" + formatStatusString);
                })
                .doOnError(throwable -> {
                    //此处处理下载异常
                    tvDownloadStatus.setText("下载失败:" + throwable.getMessage());
                })
                .doOnComplete(() -> {
                    //下载完成，解压或安装
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
                    tvDownloadPrg.setText(tvDownloadPrg.getText() + "\n下载完成：" + sdf.format(new Date()));
                    File file = new File(savePath, saveName);
                    file.delete();
                })
                .subscribe();
```

### 2.3 网络请求

2.3.1 新建声明网络请求方法的接口

```java
public interface HttpApis {

    @GET("index.php?g=mapi&m=appdatas&a=datas")
    Observable<HttpResponse<DataBean>> loadDatas();

}
```

2.3.2 继承BaseRetrofit，实现initOkHttp方法，在此方法中可配置超时、日志、缓存等。

```java
public class RetrofitHelper extends BaseRetrofit {
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
     * 组装网络请求基地址
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:38
     */
    public static String setupBaseHttpUrl() {
        return "http://" + HdAppConfig.getDefaultIpPort() + "/hnbwy/";
    }
    /**
     * 获取实例-单例
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:32
     */
    public static RetrofitHelper getInstance() {
        if (instance == null) {
            synchronized (RetrofitHelper.class) {
                if (instance == null) {
                    instance = new RetrofitHelper();
                }
            }
        }
        return instance;
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

2.3.3 使用

```java
        RetrofitHelper.getInstance()
                .loadDatas()
                .doOnSubscribe(disposable -> compositeDisposable.add(disposable))
                .doOnNext(dataBean -> Toast.makeText(MainActivity.this, new Gson().toJson(dataBean), Toast.LENGTH_SHORT).show())
                .doOnError(throwable -> Logger.e(throwable.getMessage()))
                .subscribe();                
```

详细用法参见Demo，地址：https://github.com/Tailyou/OkRetrofit
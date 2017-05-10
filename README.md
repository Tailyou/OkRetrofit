## 一、概述

HttpUtil是一个二合一的网络功能库，包含文件下载和网络请求。

文件下载基于RxDownload修改
- 智能判断服务器是否支持断点续传并适配相应下载方式；
- 智能判断同一地址对应的文件在服务端是否有改变并重新下载；
- 支持多线程下载，可设置下载线程数；
- 支持下载状态、下载进度监听；
- 支持在Service中下载文件，内置DownloadService；

网络请求
- 提供了抽象方法`initOkHttp`供上层实现，可在此方法中配置日志、缓存、超时等；
- 内置统一线程处理和统一返回结果转换；
- 内置统一返回实体和异常类；
- 内置了App检查更新的方法；

## 二、版本
已在多个项目中使用，且已上传jCenter，最新版本2.0.0，直接在gradle中添加即可。

compile 'com.hengda.zwf:HttpUtil:2.0.0'

## 三、使用
具体用法参见demo，项目地址：https://git.oschina.net/tailyou/HD_Frame_HttpUtil 。

### 1、文件下载
```
RxDownload.getInstance().context(MainActivity.this)
                        .maxThread(16).maxRetryCount(3)
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

### 2、网络请求

#### 提供网络请求接口

```
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

#### 实现BaseRetrofit

主要实现 `initOkHttp`

```
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
        String ipPort = HdAppConfig.getDefaultIpPort();
        instance = retrofitHelperHashtable.get(ipPort);
        if (instance == null) {
            synchronized (RetrofitHelper.class) {
                if (instance == null) {
                    instance = new RetrofitHelper();
                    retrofitHelperHashtable.clear();
                    retrofitHelperHashtable.put(ipPort, instance);
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
        return httpApis.loadDatas()
                .compose(rxSchedulerHelper()).compose(handleResult());
    }

}
```



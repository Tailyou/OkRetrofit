# OkRetrofit

OkRetrofit是一个基于Retrofit2+RxJava2封装的文件下载和网络请求库，其中文件下载部分参考了RxDownload，去掉了RxPermission相关的代码，
网络请求部分做了适当的抽象和封装，方便使用的同时也不影响相关部分的定制。

### 文件下载

- 智能判断服务器是否支持断点续传并适配相应下载方式；
- 智能判断同一地址对应的文件在服务端是否有改变并重新下载；
- 支持多线程下载，可设置下载线程数；
- 支持下载状态、下载进度监听；
- 支持在Service中下载文件，内置DownloadService；

### 网络请求

- 内置`BaseRetrofit`,提供了抽象方法`initOkHttp`供上层实现，可在此方法中配置日志、缓存、超时等；
- 内置服务器统一返回`HttpResponse`和请求异常`HttpException`；
- 内置统一线程处理和统一返回结果转换方法；

# Usage

### Gradle

```groovy
dependencies {
    compile 'com.hengda.zwf:OkRetrofit:0.0.1'
}
```

### Maven

```groovy
<dependency>
  <groupId>com.hengda.zwf</groupId>
  <artifactId>OkRetrofit</artifactId>
  <version>0.0.1</version>
  <type>pom</type>
</dependency>
```


### 文件下载

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

### 网络请求

1、新建声明网络请求方法的接口

```java
public interface HttpApis {

    @GET("index.php?g=mapi&m=appdatas&a=datas")
    Observable<HttpResponse<DataBean>> loadDatas();

}
```

2、继承BaseRetrofit，实现initOkHttp方法，在此方法中可配置超时、日志、缓存等。

```java
public class RetrofitHelper extends BaseRetrofit {

    private static Hashtable<String, RetrofitHelper> retrofitHelperHashtable = new Hashtable<>();
    private static HttpApis httpApis = null;
    private volatile static RetrofitHelper instance;

    private RetrofitHelper() {
        super();
        httpApis = getApiService(setupBaseHttpUrl(), HttpApis.class);
    }

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

    public static String setupBaseHttpUrl() {
        return "http://" + HdAppConfig.getDefaultIpPort() + "/hnbwy/";
    }

    @Override
    public OkHttpClient initOkHttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        return builder.build();
    }

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



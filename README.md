## 一、概述

HttpUtil是一个二合一的网络功能库，包含文件下载和网络请求。

文件下载基于RxDownload修改
- 智能判断服务器是否支持断点续传并适配相应下载方式；
- 智能判断同一地址对应的文件在服务端是否有改变并重新下载；
- 支持多线程下载，可设置下载线程数；
- 支持下载状态、下载进度监听；
- 支持在Service中下载文件，内置DownloadService；

网络请求
- 提供了抽象方法`initOkHttp`供实现，可在此方法中配置日志、缓存、超时等；
- 内置统一线程处理和统一返回结果转换；
- 内置统一返回实体和异常类；
- 内置了App检查更新的方法；


## 二、版本
已在多个项目中使用，且已上传jCenter，最新版本1.0.0，直接在gradle中添加即可。
compile 'com.hengda.zwf:HttpUtil:1.0.0'

## 三、使用
具体用法参见demo，demo以检查版本更新和安装包下载为例。

```
private void loadAndInstall(CheckResponse checkResponse) {
        String url = checkResponse.getVersionInfo().getVersionUrl();
        String saveName = url.substring(url.lastIndexOf("/") + 1);
        String savePath = HdAppConfig.getDefaultFileDir();

        RxDownload.getInstance()
                .download(url, saveName, savePath)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> disposable = d)
                .doOnNext(status -> updateProgress(status))
                .doOnError(throwable -> Logger.e("下载失败：" + throwable.getMessage()))
                .doOnComplete(() -> installApk(saveName, savePath))
                .subscribe();
    }

    private void installApk(String saveName, String savePath) {
        String apkPath = TextUtils.concat(savePath, saveName).toString();
        AppUtil.installApk(CheckUpdateActivity.this, apkPath);
    }

    private void updateProgress(DownloadStatus status) {
        txtProgress.setText(String.format("正在下载(%s/%s)",
                DataManager.getFormatSize(status.getDownloadSize()),
                DataManager.getFormatSize(status.getTotalSize())));
    }
```
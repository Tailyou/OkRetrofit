package com.hengda.frame.httputil.http;


import com.hengda.zwf.httputil.FileApi;
import com.hengda.zwf.httputil.FileCallback;

import java.util.Hashtable;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * 作者：Tailyou （祝文飞）
 * 时间：2016/4/1 16:47
 * 邮箱：tailyou@163.com
 * 描述：Retrofit文件下载API
 */
public class FileRequester extends FileApi {

    private IFileService IFileService;
    private volatile static FileRequester instance;
    private static Call<ResponseBody> call;

    private static Hashtable<String, FileRequester> mFileApiTable;

    static {
        mFileApiTable = new Hashtable<>();
    }

    /**
     * 单例模式
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 8:42
     */
    private FileRequester(String baseUrl) {
        super(baseUrl);
        IFileService = retrofit.create(IFileService.class);
    }

    /**
     * 获取实例-单例
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 8:44
     */
    public static FileRequester getInstance(String baseUrl) {
        instance = mFileApiTable.get(baseUrl);
        if (instance == null) {
            synchronized (FileRequester.class) {
                if (instance == null) {
                    instance = new FileRequester(baseUrl);
                    mFileApiTable.put(baseUrl, instance);
                }
            }
        }
        return instance;
    }


    /**
     * 下载文件-数据库、资源、Apk
     *
     * @param fileName
     * @param callback
     */
    public void loadFileByName(String fileName, FileCallback callback) {
        call = IFileService.loadFile(fileName);
        call.enqueue(callback);
    }

    /**
     * 取消下载
     *
     * @author 祝文飞（Tailyou）
     * @time 2016/11/12 11:34
     */
    public static void cancel() {
        if (call != null && call.isCanceled() == false) {
            call.cancel();
        }
    }

}

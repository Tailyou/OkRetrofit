## 一、概述
基于Retrofit的网络请求和文件下载工具，其中文件下载为最基础版，包括下载进度、解压回调等。
## 二、版本
已在多个项目中使用，且已上传jCenter，最新版本0.1.6，直接在gradle中添加即可。
compile 'com.hengda.zwf:HttpUtil:0.1.6'
## 三、使用
具体用法参见demo，demo以检查版本更新和安装包下载为例。

- 下载大文件需防止内存溢出，增加@Streaming即可

```
public interface IFileService {

    /**
     * 下载数据库、资源
     *
     * @param fileName
     * @return
     */
    @Streaming
    @GET("{fileName}")
    Call<ResponseBody> loadFile(@Path("fileName") String fileName);

}
```

- 解压工具类，异步解压，增加解压完成回调

```
public class ZipUtil {

        /**
         * 解压
         *
         * @author 祝文飞（Tailyou）
         * @time 2017/2/6 13:46
         */
        public static void unzipFolder(File zipFile, String unzipToDirPath,
                                       IUnzipCallback callback) throws Exception {
            ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry;
            String szName;
            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(unzipToDirPath + File.separator + szName);
                    folder.mkdirs();
                } else {
                    File file = new File(unzipToDirPath + File.separator + szName);
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = inZip.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                }
            }
            callback.completed();
            zipFile.delete();
            inZip.close();
        }

        /**
         * 解压完成回调
         *
         * @author 祝文飞（Tailyou）
         * @time 2017/2/6 13:46
         */
        public interface IUnzipCallback {
            void completed();
        }

    }
```
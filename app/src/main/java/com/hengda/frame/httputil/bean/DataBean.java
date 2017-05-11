package com.hengda.frame.httputil.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 作者：祝文飞（Tailyou）
 * 邮箱：tailyou@163.com
 * 时间：2017/1/3 11:51
 * 描述：数据库信息
 */
public class DataBean implements Serializable {

    /**
     * zip_url : http://192.168.10.20/hnbwy/resource/class_img/class_info.zip
     * class_version : 3
     * class_list : [{"class_id":"9","class_title":"什么是考古学？"},{"class_id":"10","class_title":"什么是考古"}]
     * class_info : [{"class_id":"9","file_name":"1.png","page":"1"},{"class_id":"10","file_name":"10.png","page":"10"}]
     */

    private String zip_url;
    private int class_version;
    private List<ClassListBean> class_list;
    private List<ClassInfoBean> class_info;
    private List<Mp4InfoBean> mp4_info;

    public String getZip_url() {
        return zip_url;
    }

    public void setZip_url(String zip_url) {
        this.zip_url = zip_url;
    }

    public int getClass_version() {
        return class_version;
    }

    public void setClass_version(int class_version) {
        this.class_version = class_version;
    }

    public List<ClassListBean> getClass_list() {
        return class_list;
    }

    public void setClass_list(List<ClassListBean> class_list) {
        this.class_list = class_list;
    }

    public List<ClassInfoBean> getClass_info() {
        return class_info;
    }

    public void setClass_info(List<ClassInfoBean> class_info) {
        this.class_info = class_info;
    }

    public List<Mp4InfoBean> getMp4_info() {
        return mp4_info;
    }

    public void setMp4_info(List<Mp4InfoBean> mp4_info) {
        this.mp4_info = mp4_info;
    }

    public static class ClassListBean implements Serializable {
        /**
         * class_id : 9
         * class_title : 什么是考古学？
         */

        private int class_id;
        private int class_type;
        private String class_title;
        private int group_id;
        private String md5;
        private String zip_url;
        private String zip_size;
        private int dbFlag;

        public int getClass_id() {
            return class_id;
        }

        public void setClass_id(int class_id) {
            this.class_id = class_id;
        }

        public int getClass_type() {
            return class_type;
        }

        public void setClass_type(int class_type) {
            this.class_type = class_type;
        }

        public String getClass_title() {
            return class_title;
        }

        public void setClass_title(String class_title) {
            this.class_title = class_title;
        }

        public int getGroup_id() {
            return group_id;
        }

        public void setGroup_id(int group_id) {
            this.group_id = group_id;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        public String getZip_url() {
            return zip_url;
        }

        public void setZip_url(String zip_url) {
            this.zip_url = zip_url;
        }

        public String getZip_size() {
            return zip_size;
        }

        public void setZip_size(String zip_size) {
            this.zip_size = zip_size;
        }

        public int getDbFlag() {
            return dbFlag;
        }

        public void setDbFlag(int dbFlag) {
            this.dbFlag = dbFlag;
        }
    }

    public static class ClassInfoBean implements Serializable{


        /**
         * class_id : 19
         * file_name : 1.png
         * page : 1
         * is_mp3 : 0
         * mp3 :
         */

        private int class_id;
        private int page;
        private int is_mp3;
        private String file_name;
        private String mp3;

        public int getClass_id() {
            return class_id;
        }

        public void setClass_id(int class_id) {
            this.class_id = class_id;
        }

        public String getFile_name() {
            return file_name;
        }

        public void setFile_name(String file_name) {
            this.file_name = file_name;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getIs_mp3() {
            return is_mp3;
        }

        public void setIs_mp3(int is_mp3) {
            this.is_mp3 = is_mp3;
        }

        public String getMp3() {
            return mp3;
        }

        public void setMp3(String mp3) {
            this.mp3 = mp3;
        }
    }

    public static class Mp4InfoBean implements Serializable{

        /**
         * class_id : 24
         * file_name : 1.mp4
         * page : 1
         * mp4_img : 1.png
         * filename : 1、主题_1.mp4
         */

        private int class_id;
        private String file_name;
        private int page;
        private String mp4_img;
        private String filename;

        public int getClass_id() {
            return class_id;
        }

        public void setClass_id(int class_id) {
            this.class_id = class_id;
        }

        public String getFile_name() {
            return file_name;
        }

        public void setFile_name(String file_name) {
            this.file_name = file_name;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public String getMp4_img() {
            return mp4_img;
        }

        public void setMp4_img(String mp4_img) {
            this.mp4_img = mp4_img;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
    }

}

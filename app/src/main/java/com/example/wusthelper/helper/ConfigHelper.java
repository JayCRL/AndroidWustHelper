package com.example.wusthelper.helper;

import android.util.Log;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.bean.javabean.data.ConfigData;
import com.example.wusthelper.bean.javabean.TermBean;
import com.example.wusthelper.utils.AppVersionUtils;

import com.example.wusthelper.helper.SharePreferenceLab;

public class ConfigHelper {
    private static ConfigData configData;
    private final static String TAG ="qzytest";

    public static ConfigData getConfigBean(){
        if (!if_make_request()){
            return new ConfigData();
        }else {
            return configData;
        }
    }

    public static void setConfigBean(ConfigData bean){//设置配置文件
        Log.d(TAG, "setConfigBean: configData code=" + bean.getCode() + ", msg=" + bean.getMsg() + ", currentTerm=" + bean.getData().getCurrentTerm());
        configData =bean;
    }

    /**
     * 优先返回 basic 后端缓存的学期起始日（yyyy-MM-dd 或毫秒时间戳字符串）；为空则 fallback 旧 config。
     */
    public static String getTermStartDate() {
        String cached = SharePreferenceLab.getTermStartDate();
        if (cached != null && !cached.trim().isEmpty()) {
            return cached.trim();
        }
        return get_now_Term_startDate();
    }

    public static void setTermStartDate(int year, int month, int day) {
        SharePreferenceLab.setTermStartDate(year, month, day);
    }

    public static void setTermStartDate(String value) {
        SharePreferenceLab.setTermStartDate(value);
    }

    public static String get_now_Term_startDate(){//获取当前学期的开始日期
        if (!if_make_request()){
            return "";
        }else {
            return search_the_startDate(configData.getData().getCurrentTerm());
        }
    }

    public static String get_currentTerm(){//返回当前学期
        if (!if_make_request()){
            return "";
        }else {
            return configData.data.getCurrentTerm();
        }
    }

    public static String get_isUpdate(){//获取更新状态
        if (!if_make_request()){
            return "";
        }else {
            return configData.data.getIsUpdate();
        }
    }
    public static String get_version(){//获取版本号
        if (!if_make_request()){
            return "";
        }else {
            return configData.data.getVersion();
        }
    }
    public static String get_apkUrl(){//获取apk下载地址
        if (!if_make_request()){
            return "";
        }else {
            return configData.data.getApkUrl();
        }
    }
    public static String get_updateContent(){//获取更新内容
        if (!if_make_request()){
            return "";
        }else {
            return configData.data.getUpdateContent();
        }
    }
    public static String get_jwcConfig(){//获取教务处访问情况
        if (!if_make_request()){
            return "";
        }else {
            return configData.data.getJwcConfig();
        }
    }

    public static boolean getIfHasNewVersion(){
        if (!if_make_request()){
            return false;
        }else {
            return isUpdate(configData.data.getVersion());
        }
    }


    private static boolean if_make_request() {
        if(configData ==null){
            return false;
        }else if(!configData.getCode().equals("0")&&!configData.getMsg().equals("成功")){
            return false;
        }else {
            return true;
        }
    }

    private static String search_the_startDate(String currentTerm) {
        if(!if_make_request()){
            return "";
        }else {
            for(TermBean termBean : configData.getData().termSetting){
                if(termBean.term.equals(currentTerm)){
                    return termBean.startDate;
                }
            }
            return "";
        }

    }

    private static boolean isUpdate(String newVersion) {
        Log.d(TAG, "isUpdate: "+AppVersionUtils.getVersionName(MyApplication.getContext()));
        int result = compareVersion(newVersion, AppVersionUtils.getVersionName(MyApplication.getContext()));
        return result > 0;
    }


    /**
     * 版本号比较
     *
     * @param version1
     * @param version2
     * @return
     * 0代表相等，1代表version1大于version2，-1代表version1小于version2
     */
    private static int compareVersion(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }
        String[] version1Array = version1.split("\\.");
        String[] version2Array = version2.split("\\.");
        Log.d("UpdateHelper", "version1Array=="+version1Array.length);
        Log.d("UpdateHelper", "version2Array=="+version2Array.length);
        int index = 0;
        // 获取最小长度值
        int minLen = Math.min(version1Array.length, version2Array.length);
        int diff = 0;
        // 循环判断每位的大小
        Log.d("UpdateHelper", "verTag2="+version1Array[index]);
        while (index < minLen
                && (diff = Integer.parseInt(version1Array[index])
                - Integer.parseInt(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            // 如果位数不一致，比较多余位数
            for (int i = index; i < version1Array.length; i++) {
                if (Integer.parseInt(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Integer.parseInt(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }
}

package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
/**
 * ConfigData的子类
 * */
public class ConfigBean {

    @SerializedName("apkUrl")//app下载地址
    public String apkUrl;

    @SerializedName("currentTerm")//当前学期
    public String currentTerm;

    @SerializedName("jwcConfig")//教务处情况
    public String jwcConfig;

    @SerializedName("updateContent")//更新公告
    public String updateContent;

    @SerializedName("version")//app版本号
    public String version;

    @SerializedName("isUpdate")//更新类型 0为不强制更新，1为强制更新
    public String isUpdate;

    @SerializedName("termSetting")//学期数据
    public List<TermBean> termSetting =new ArrayList<>();

    public ConfigBean() {
        apkUrl= "";
        currentTerm= "";
        jwcConfig= "";
        updateContent= "加载数据错误，可能是服务器后台出现了问题，请进行反馈";
        version= "";
        isUpdate= "";

    }

    public String getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(String currentTerm) {
        this.currentTerm = currentTerm;
    }

    public String getJwcConfig() {
        return jwcConfig;
    }

    public void setJwcConfig(String jwcConfig) {
        this.jwcConfig = jwcConfig;
    }

    public String getIsUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(String isUpdate) {
        this.isUpdate = isUpdate;
    }

    public List<TermBean> getTermSetting() {
        return termSetting;
    }

    public void setTermSetting(List<TermBean> termSetting) {
        this.termSetting = termSetting;
    }
    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public String getUpdateContent() {
        return updateContent;
    }

    public void setUpdateContent(String updateContent) {
        this.updateContent = updateContent;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ConfigBean{" +
                "apkUrl='" + apkUrl + '\'' +
                ", currentTerm='" + currentTerm + '\'' +
                ", jwcConfig='" + jwcConfig + '\'' +
                ", updateContent='" + updateContent + '\'' +
                ", version='" + version + '\'' +
                ", isUpdate='" + isUpdate + '\'' +
                ", termSetting=" + termSetting +
                '}';
    }
}

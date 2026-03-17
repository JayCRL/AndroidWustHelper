package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

import org.litepal.crud.LitePalSupport;

public class CountDownBean extends LitePalSupport {
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("comment")
    private String note;
    private long createTimes;
    @SerializedName("time")
    private String targetTimeString;
    @SerializedName("createTime")
    private String crateTimeString;
    private long targetTime;
    private int colorId;
    private boolean isFinish;
    @SerializedName("uuid")
    private String onlyId;
    private boolean isOnNet;
    private boolean isDelete = false;
    private boolean isChange = false;
    private boolean isCheck = false;

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public boolean isChange() {
        return isChange;
    }

    public void setChange(boolean change) {
        isChange = change;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public CountDownBean(String name, String note, long createTimes, String onlyId, long targetTime){
        this.name = name;
        this.note = note;
        this.createTimes = createTimes;
        this.onlyId = onlyId;
        this.targetTime = targetTime;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public long getCreateTimes() {
        return createTimes;
    }

    public void setCreateTimes(long createTimes) {
        this.createTimes = createTimes;
    }

    public String getTargetTimeString() {
        return targetTimeString;
    }

    public void setTargetTimeString(String targetTimeString) {
        this.targetTimeString = targetTimeString;
    }

    public String getCrateTimeString() {
        return crateTimeString;
    }

    public void setCrateTimeString(String crateTimeString) {
        this.crateTimeString = crateTimeString;
    }

    public boolean isOnNet() {
        return isOnNet;
    }

    public void setOnNet(boolean onNet) {
        isOnNet = onNet;
    }

    public String getOnlyId() {
        return onlyId;
    }

    public void setOnlyId(String onlyId) {
        this.onlyId = onlyId;
    }

    public CountDownBean() {
    }

    public CountDownBean(int id, String name, String note, long createTimes, long targetTime, int colorId) {
        this.id = id;
        this.name = name;
        this.note = note;
        this.createTimes = createTimes;
        this.targetTime = targetTime;
        this.colorId = colorId;
    }

    public CountDownBean(String name, String note, long createTimes, long targetTime) {
        this.name = name;
        this.note = note;
        this.createTimes = createTimes;
        this.targetTime = targetTime;
    }

    public CountDownBean(int id, String name, String note, long createTimes, long targetTime, int colorId, boolean isFinish) {
        this.id = id;
        this.name = name;
        this.note = note;
        this.createTimes = createTimes;
        this.targetTime = targetTime;
        this.colorId = colorId;
        this.isFinish = isFinish;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCreateTime() {
        return createTimes;
    }

    public void setCreateTime(long createTime) {
        this.createTimes = createTime;
    }

    public long getTargetTime() {
        return targetTime;
    }

    public void setTargetTime(long targetTime) {
        this.targetTime = targetTime;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    @Override
    public boolean equals(Object obj) {
        CountDownBean countDownBean = (CountDownBean) obj;
        if(this.getOnlyId().equals(countDownBean.onlyId)){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "CountDownBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", note='" + note + '\'' +
                ", createTimes=" + createTimes +
                ", targetTimeString='" + targetTimeString + '\'' +
                ", crateTimeString='" + crateTimeString + '\'' +
                ", targetTime=" + targetTime +
                ", colorId=" + colorId +
                ", isFinish=" + isFinish +
                ", onlyId='" + onlyId + '\'' +
                ", isOnNet=" + isOnNet +
                ", isDelete=" + isDelete +
                ", isChange=" + isChange +
                ", isCheck=" + isCheck +
                '}';
    }
}

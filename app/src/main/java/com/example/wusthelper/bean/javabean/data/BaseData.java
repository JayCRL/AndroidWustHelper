package com.example.wusthelper.bean.javabean.data;

import com.google.gson.annotations.SerializedName;

/**
 * JavaBean的简单封装，用于解析网络请求的解析,
 * 首先就定义了 code和 msg两个比较常用的量
 * 后续网络解析类希望继承自BaseBean */
public class BaseData {

    @SerializedName("code")
    private Integer code;
    @SerializedName(value = "msg", alternate = {"message"})
    private String msg;

    /**
     * 兼容：历史代码大量以字符串方式比较 code
     */
    public String getCode() {
        return code == null ? "" : String.valueOf(code);
    }

    public Integer getCodeInt() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        if (code == null) {
            return false;
        }
        return code == 200 || code == 0 || code == 10000 || code == 11000;
    }

    @Override
    public String toString() {
        return "BaseData{" +
                "code='" + getCode() + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}

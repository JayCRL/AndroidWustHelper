/*
 * @author yuandalai
 * @date 2018/11/16
 * @email yuanlai0611@gmail.com
 * @github https://github.com/yuanlai0611
 * @blog https://yuanlai0611.github.io/
 */

package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

public class LibraryCollectionBean {

    @SerializedName("callNo")
    private String callNumber;

    @SerializedName("barCode")
    private String barCode;

    @SerializedName("location")
    private String location;

    @SerializedName("status")
    private String status;

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

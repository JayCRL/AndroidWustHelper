package com.example.wusthelper.bean.javabean.data;

public class SecondHandPageData extends BaseData {
    private PageCommodity data;
    private Boolean ok;

    public PageCommodity getData() {
        return data;
    }

    public void setData(PageCommodity data) {
        this.data = data;
    }

    public Boolean getOk() {
        return ok;
    }

    public void setOk(Boolean ok) {
        this.ok = ok;
    }
}

package com.example.wusthelper.bean.javabean.data;

import java.util.List;

public class CatCommentListData extends BaseData {
    private List<CatComment> data;

    public List<CatComment> getData() {
        return data;
    }

    public void setData(List<CatComment> data) {
        this.data = data;
    }
}

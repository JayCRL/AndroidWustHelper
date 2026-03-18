package com.example.wusthelper.bean.javabean.data;

public class CompetitionResponsePageData extends BaseData {
    private CompetitionPageData<ResponsePost> data;

    public CompetitionPageData<ResponsePost> getData() {
        return data;
    }

    public void setData(CompetitionPageData<ResponsePost> data) {
        this.data = data;
    }
}

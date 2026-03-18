package com.example.wusthelper.bean.javabean.data;

public class CompetitionPostPageData extends BaseData {
    private CompetitionPageData<CompetitionPost> data;

    public CompetitionPageData<CompetitionPost> getData() {
        return data;
    }

    public void setData(CompetitionPageData<CompetitionPost> data) {
        this.data = data;
    }
}

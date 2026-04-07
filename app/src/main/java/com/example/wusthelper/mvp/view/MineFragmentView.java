package com.example.wusthelper.mvp.view;

import com.example.wusthelper.base.BaseMvpView;

public interface MineFragmentView extends BaseMvpView {

    void showName(String name);
    void showStudentId(String id);
    /**
     * 显示一张用户图片
     * 按照String路径显示
     * */
    void showUserImage(String path);
}

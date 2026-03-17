package com.example.wusthelper.bean.javabean;

import com.contrarywind.interfaces.IPickerViewData;

/**
 * @author: Gong Yunhao
 * @version: V1.0
 * @date: 2018/10/26
 * @github https://github.com/Roman-Gong
 * @blog https://www.jianshu.com/u/52a8fa1f29fb
 */
public class CampusBean implements IPickerViewData {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CampusBean(String name) {
        this.name = name;
    }

    @Override
    public String getPickerViewText() {
        return name;
    }
}

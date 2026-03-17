package com.example.wusthelper.helper;

import android.content.Context;
import android.graphics.Color;

import androidx.fragment.app.FragmentActivity;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.example.wusthelper.R;
import com.example.wusthelper.utils.ResourcesUtils;

public class OptionPickHelper {

    /**
     * @param context
     * @param masterSemester
     * @param onOptionsSelectListener
 * 用于生成SemesterOptionPick
 * 第二个参数是顶部显示当前学期数据  */
    public static OptionsPickerView<String> getSemesterOption(Context context,
                                                              String masterSemester,
                                                              OnOptionsSelectListener onOptionsSelectListener) {
        return new OptionsPickerBuilder(context,onOptionsSelectListener)
                .setTitleText(masterSemester)
                .setContentTextSize(20)
                .setDividerColor(Color.LTGRAY)
                .setBgColor(Color.WHITE)
                .setTitleBgColor(Color.WHITE)
                .setTitleColor(Color.GRAY)
                .setCancelColor(Color.GRAY)
                .setOutSideCancelable(true)
                .setSubmitColor(ResourcesUtils.getRealColor(R.color.green_ok))
                .setTextColorCenter(ResourcesUtils.getRealColor(R.color.green_ok))
                .build();
    }

    public static OptionsPickerView<String> getCampusOption(Context context,
                                                            OnOptionsSelectListener onOptionsSelectListener) {
        return new OptionsPickerBuilder(context,onOptionsSelectListener)
                .setContentTextSize(20)
                .setDividerColor(Color.LTGRAY)
                .setBgColor(Color.WHITE)
                .setTitleBgColor(Color.WHITE)
                .setTitleColor(Color.GRAY)
                .setCancelColor(Color.GRAY)
                .setOutSideCancelable(true)
                .setSubmitColor(ResourcesUtils.getRealColor(R.color.green_ok))
                .setTextColorCenter(ResourcesUtils.getRealColor(R.color.green_ok))
                .build();
    }
}

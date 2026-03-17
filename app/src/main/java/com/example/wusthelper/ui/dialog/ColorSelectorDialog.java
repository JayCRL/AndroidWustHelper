package com.example.wusthelper.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.example.wusthelper.R;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

public class ColorSelectorDialog {
    private final Context mContext;
    private final View mDialogView;
    private ColorPicker mColorPickerView;
    private SVBar mSVBar;
    private OpacityBar mOpacityBar;
    private OnColorChangeListener mListenr;
    private OnColorOverListener mOnColorOverListener;
    private AlertDialog mDialog;

    public ColorSelectorDialog(Context context,OnColorChangeListener listener,OnColorOverListener Overlistener) {
        this.mContext = context;
        this.mListenr = listener;
        this.mOnColorOverListener = Overlistener;
        mDialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_color_picker_view,null);
        mColorPickerView = mDialogView.findViewById(R.id.color_picker_view);
        //饱和度Bar
        mSVBar = mDialogView.findViewById(R.id.color_picker_SVB_bar);
        //不透明度Bar
        mOpacityBar = mDialogView.findViewById(R.id.color_picker_opacity_bar);

        //将以上两种Bar和mColorPickerView连接起来
        mColorPickerView.addOpacityBar(mOpacityBar);
        mColorPickerView.addSVBar(mSVBar);

        //监听ColorPicker颜色改变
        mColorPickerView.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                //通知外面当前选择的颜色
                mListenr.onColorChange(color);
            }
        });

        mColorPickerView.setOnColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                mOnColorOverListener.onColorOver(color);
            }
        });
        //创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("颜色选择器")//对话框标题
                .setIcon(R.drawable.icon_colorpicker)//对话框图标
                .setView(mDialogView)//对话框中显示的View
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        //也可以在这里调用回调方法来获取最后选中的颜色

                    }
                })
                .create();
        mDialog = builder.create();
    }

    public void showDialog(){
        //显示对话框
        mDialog.show();
        //下面这句代码用于设置对话框的大小，调整大小使颜色选择器能够完全显示不被遮挡
        mDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
    }

    //颜色改变时的回调接口
    public interface OnColorChangeListener{
        void onColorChange(int color);
    }
    //颜色改变时的回调接口
    public interface OnColorOverListener{
        void onColorOver(int color);
    }



    public int getColor(){
        //获取当前选中颜色
        return mColorPickerView.getColor();
    }

}
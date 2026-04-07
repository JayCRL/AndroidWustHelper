package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityCountdownShareBinding;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.utils.Base64Util;
import com.example.wusthelper.utils.ScreenUtils;

import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;

public class CountDownShareActivity extends BaseActivity<ActivityCountdownShareBinding> implements View.OnClickListener {

    //传进来的唯一id
    private String onlyId;

    //生成的图片的bitmap
    private Bitmap savedBitmap;

    //头像的本地路径
    private String headPath;

    //学生名字
    private String  stuNameString;

    public static Intent newInstance(Context context,String onlyId,String countdownName){
        Intent intent = new Intent(context,CountDownShareActivity.class);
        intent.putExtra("QRCode",onlyId);
        intent.putExtra("countdown_name",countdownName);
        return intent;
    }



    @Override
    public void initView() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        onlyId = getIntent().getStringExtra("QRCode");
        getBinding().shareCountdownName.setText(getIntent().getStringExtra("countdown_name"));
        String context = Base64Util.encode(onlyId);
        generateLogoBitmap(context);
        savedBitmap = changeBitmapSize(savedBitmap);
        Glide.with(this).load(savedBitmap).into(getBinding().countdownShareImage);

        headPath = SharePreferenceLab.getInstance().getHeadPath(MyApplication.getContext());
        Glide.with(this)
                .load(headPath)
                .apply(RequestOptions.placeholderOf(R.drawable.user_img_holder))
                .apply(RequestOptions.centerCropTransform())
                .into(getBinding().shareCountdownStuHead);

        stuNameString = SharePreferenceLab.getDisplayName();
        getBinding().shareCountdownStuName.setText(stuNameString);
        getBinding().countdownBack.setOnClickListener(this);
    }

    public void generateLogoBitmap(String content) {

        Bitmap logoBitmap = BitmapFactory.decodeResource(CountDownShareActivity.this.getResources(), R.mipmap.logo);
        savedBitmap = QRCodeEncoder.syncEncodeQRCode(content, ScreenUtils.dp2px(400), Color.parseColor("#000000"), logoBitmap);
    }

    public Bitmap changeBitmapSize(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = ScreenUtils.dp2px(240);
        int newHeight = ScreenUtils.dp2px(240);
        float scaleWidth = ((float)newWidth)/width;
        float scaleHeight = ((float)newHeight)/height;
        Matrix matrix = new Matrix();
        matrix.setScale(scaleWidth, scaleHeight);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.countdown_back:
                finish();
                break;
        }
    }
}


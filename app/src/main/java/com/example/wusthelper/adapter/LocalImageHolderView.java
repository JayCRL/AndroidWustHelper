package com.example.wusthelper.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.holder.Holder;
import com.example.wusthelper.R;

/**
 * 首页轮播图
 * //B、本地图片
 * */
public class LocalImageHolderView extends Holder<Bitmap> {
    private final String TAG ="NetImageHolderView";

    private ImageView mImageView;

    private Context mContext;

    public LocalImageHolderView(View itemView, Context context) {
        super(itemView);
        mContext = context;
    }

    @Override
    protected void initView(View itemView) {
        mImageView = itemView.findViewById(R.id.iv_card_app);
    }

    @Override
    public void updateUI(Bitmap data) {
        Log.d(TAG, "initCycleImage: 1111: ");
        mImageView.setImageBitmap(data);
    }
}
package com.example.wusthelper.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bigkoo.convenientbanner.holder.Holder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.CycleImageBean;

/**
 * 首页轮播图
 * //A、网络图片
 */
public class NetImageHolderView extends Holder<CycleImageBean> {
    private final String TAG = "NetImageHolderView";

    private ImageView mImageView;

    private Context mContext;

    public NetImageHolderView(View itemView) {
        super(itemView);
    }

    public NetImageHolderView(View itemView, Context context) {
        super(itemView);
        mContext = context;
    }

    @Override
    protected void initView(View itemView) {
        mImageView = itemView.findViewById(R.id.iv_card_app);
    }

    @SuppressLint("CheckResult")
    @Override
    public void updateUI(CycleImageBean data) {
        // ConvenientBanner 在部分实现里可能不在主线程回调 updateUI；Glide 需要在主线程启动请求。
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            try {
                mImageView.post(() -> updateUI(data));
            } catch (Exception ignore) {
            }
            return;
        }

        mImageView.setImageResource(R.mipmap.default_bg);

        RequestOptions options = new RequestOptions()
                .placeholder(R.mipmap.default_bg)
                .error(R.mipmap.default_bg)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop();

        final String url = (data == null || data.getImgUrl() == null) ? "" : data.getImgUrl().trim();
        Log.e(TAG, "updateUI thread=" + Thread.currentThread().getName() + " imageView=" + mImageView.hashCode() + " url=" + url);

        if (url.isEmpty()) {
            mImageView.setImageResource(R.mipmap.default_bg);
            return;
        }

        // 用 ApplicationContext 发起请求，避免 View/Fragment lifecycle 抖动导致 request 直接被 cancel。
        com.bumptech.glide.RequestManager rm = Glide.with(mImageView.getContext().getApplicationContext());
        try {
            rm.clear(mImageView);
        } catch (Exception ignore) {
        }

        try {
            rm.load(url)
                    .apply(options)
                    .listener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Glide failed imageView=" + mImageView.hashCode() + " url=" + url, e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            Log.e(TAG, "Glide ready imageView=" + mImageView.hashCode() + " url=" + url + " source=" + dataSource);
                            return false;
                        }
                    })
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .into(new DrawableImageViewTarget(mImageView) {
                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            Log.e(TAG, "target onLoadFailed imageView=" + mImageView.hashCode() + " url=" + url);
                            super.onLoadFailed(errorDrawable);
                        }

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            Log.e(TAG, "target onResourceReady imageView=" + mImageView.hashCode() + " url=" + url);
                            super.onResourceReady(resource, transition);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            Log.e(TAG, "target onLoadCleared imageView=" + mImageView.hashCode() + " url=" + url);
                            super.onLoadCleared(placeholder);
                        }
                    });
        } catch (Throwable t) {
            // 兜底：如果 Glide 因为线程/生命周期等原因抛异常，这里能看到堆栈
            Log.e(TAG, "Glide start exception imageView=" + mImageView.hashCode() + " url=" + url, t);
        }
    }
}

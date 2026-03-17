package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import java.net.InetAddress;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.SchoolCalendarData;
import com.example.wusthelper.databinding.ActivitySchoolCalendarBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.utils.ResourcesUtils;
import com.example.wusthelper.utils.ToastUtil;

public class SchoolCalendarActivity extends BaseActivity<ActivitySchoolCalendarBinding> {

    private static final String TAG = "SchoolCalendarActivity";

    public static Intent newInstance(Context context){
        return new Intent(context,SchoolCalendarActivity.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initView(){
        getWindow().setStatusBarColor(ResourcesUtils.getRealColor(R.color.white));
        changeStatusBarTextColor(true);
        getBinding().ivBack.setOnClickListener(view -> finish());
        loadCalendar();
    }

    private void loadCalendar() {
        getBinding().progressBar.setVisibility(View.VISIBLE);
        NewApiHelper.getSchoolCalendar(new com.example.wusthelper.request.okhttp.listener.DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                SchoolCalendarData data = (SchoolCalendarData) responseObj;
                if ("401".equals(data.getCode())) {
                    getBinding().progressBar.setVisibility(View.GONE);
                    NewApiHelper.clearLoginState();
                    ToastUtil.show(getMessage(data.getMsg(), "登录已失效，请重新登录"));
                    startActivity(LoginMvpActivity.newInstance(SchoolCalendarActivity.this));
                    finish();
                    return;
                }

                if (data.isSuccess() && data.getData() != null) {
                    String url = data.getData();
                    android.util.Log.e("SchoolCalendarActivity", "Original data from server: " + url);

                    // 兼容：极少数部署会把整个 JSON 作为字符串塞进 data（双重包装）
                    if (url != null && url.trim().startsWith("{")) {
                        try {
                            org.json.JSONObject obj = new org.json.JSONObject(url);
                            url = obj.optString("data", url);
                        } catch (Exception ignore) {
                            // keep original
                        }
                    }

                    if (url != null) {
                        url = url.trim();
                        if (url.startsWith("//")) {
                            url = "https:" + url;
                        }
                    }

                    android.util.Log.e("SchoolCalendarActivity", "Final url to load: " + url);

                    if (url != null && !url.isEmpty()) {
                        final String finalUrl = url;
                        getBinding().ivCalendar.setVisibility(View.VISIBLE);
                        getBinding().tvEmpty.setVisibility(View.GONE);
                        getBinding().progressBar.setVisibility(View.GONE);

                        // 绑定 Activity 的生命周期，让 Glide 自动管理请求，
                        // 去除跳过缓存的选项以提高大图加载成功率。
                        Glide.with(SchoolCalendarActivity.this)
                                .load(finalUrl)
                                .placeholder(R.drawable.schoolcalendar)
                                .error(R.drawable.schoolcalendar)
                                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                        android.util.Log.e(TAG, "日历网络图片加载失败: " + finalUrl, e);
                                        // 确保能在主线程执行 UI 更新
                                        getBinding().tvEmpty.post(() -> {
                                            getBinding().tvEmpty.setVisibility(View.VISIBLE);
                                            getBinding().tvEmpty.setText("最新校历加载失败，当前显示为本地默认版本");
                                        });
                                        return false; // 返回 false 让 error placeholder 继续生效
                                    }

                                    @Override
                                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                })
                                .into(getBinding().ivCalendar);
                        return;
                    }
                }

                // 如果没有获取到有效 URL，直接展示本地内置的校历图片
                getBinding().progressBar.setVisibility(View.GONE);
                getBinding().ivCalendar.setVisibility(View.VISIBLE);
                getBinding().tvEmpty.setVisibility(View.GONE);
                Glide.with(getApplicationContext()).load(R.drawable.schoolcalendar).into(getBinding().ivCalendar);
            }

            @Override
            public void onFailure(Object reasonObj) {
                // 网络请求失败，也展示本地内置的校历图片
                getBinding().progressBar.setVisibility(View.GONE);
                getBinding().ivCalendar.setVisibility(View.VISIBLE);
                getBinding().tvEmpty.setVisibility(View.GONE);
                Glide.with(getApplicationContext()).load(R.drawable.schoolcalendar).into(getBinding().ivCalendar);
            }
        });
    }

    private String getMessage(String msg, String fallback) {
        return msg == null || msg.trim().isEmpty() ? fallback : msg;
    }
}

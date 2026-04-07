package com.example.wusthelper.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.bean.javabean.data.Commodity;
import com.example.wusthelper.bean.javabean.data.CommodityDetailData;
import com.example.wusthelper.bean.javabean.data.PageCommodity;
import com.example.wusthelper.bean.javabean.data.SecondHandPageData;
import com.example.wusthelper.databinding.ActivitySecondHandDetailBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import java.util.ArrayList;
import java.util.List;

public class SecondHandDetailActivity extends BaseActivity<ActivitySecondHandDetailBinding> {

    private static final String EXTRA_PID = "pid";
    private static final String EXTRA_CAN_DELETE = "can_delete";

    private int pid;
    private Commodity commodity;
    private boolean isFavorite;
    private boolean canDelete;

    public static Intent newInstance(Context context, int pid) {
        Intent intent = new Intent(context, SecondHandDetailActivity.class);
        intent.putExtra(EXTRA_PID, pid);
        return intent;
    }

    public static Intent newOwnerInstance(Context context, int pid) {
        Intent intent = newInstance(context, pid);
        intent.putExtra(EXTRA_CAN_DELETE, true);
        return intent;
    }

    @Override
    public void initView() {
        pid = getIntent().getIntExtra(EXTRA_PID, 0);
        canDelete = getIntent().getBooleanExtra(EXTRA_CAN_DELETE, false);
        getBinding().tbTitle.tvTitleTitle.setText("商品详情");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
        getBinding().btnFavorite.setOnClickListener(v -> toggleFavorite());
        getBinding().btnDelete.setOnClickListener(v -> confirmDelete());
        getBinding().btnDelete.setVisibility(canDelete ? android.view.View.VISIBLE : android.view.View.GONE);
        loadFavoriteState();
        loadDetail();
    }

    private void loadFavoriteState() {
        NewApiHelper.getSecondHandCollectionList(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                SecondHandPageData response = (SecondHandPageData) responseObj;
                PageCommodity pageData = response.getData();
                List<Commodity> records = pageData == null || pageData.records == null ? new ArrayList<>() : pageData.records;
                boolean favorite = false;
                for (Commodity item : records) {
                    if (item != null && item.pid == pid) {
                        favorite = true;
                        break;
                    }
                }
                boolean finalFavorite = favorite;
                runOnUiThread(() -> {
                    isFavorite = finalFavorite;
                    updateFavoriteButton();
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
            }
        });
    }

    private void loadDetail() {
        if (pid <= 0) {
            Toast.makeText(this, "商品信息无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        NewApiHelper.getSecondHandDetail(pid, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                CommodityDetailData data = (CommodityDetailData) responseObj;
                commodity = data.getData();
                runOnUiThread(() -> bindCommodity());
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(SecondHandDetailActivity.this, "详情加载失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void bindCommodity() {
        if (commodity == null) {
            Toast.makeText(this, "暂无详情数据", Toast.LENGTH_SHORT).show();
            return;
        }
        getBinding().tvTitle.setText(safeText(commodity.name, "未命名商品"));
        getBinding().tvPrice.setText("¥ " + String.format("%.2f", commodity.price));
        getBinding().tvDescription.setText(safeText(commodity.introduce, "暂无描述"));
        getBinding().tvContact.setText("联系方式：" + safeText(commodity.contact, "未提供"));
        getBinding().tvTime.setText("发布时间：" + formatDate(commodity.date));
        bindStatus(getBinding().tvStatus, commodity.status);
        getBinding().tvCategory.setText(getCategoryLabel(commodity.type));
        updateFavoriteButton();
    }

    private void toggleFavorite() {
        if (commodity == null) {
            return;
        }
        DisposeDataListener listener = new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                runOnUiThread(() -> {
                    if (data.isSuccess()) {
                        isFavorite = !isFavorite;
                        updateFavoriteButton();
                        setResult(RESULT_OK);
                    }
                    Toast.makeText(SecondHandDetailActivity.this,
                            safeText(data.getMsg(), isFavorite ? "已收藏" : "已取消收藏"), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(SecondHandDetailActivity.this, "操作失败", Toast.LENGTH_SHORT).show());
            }
        };
        if (isFavorite) {
            NewApiHelper.removeSecondHandCollection(commodity.pid, listener);
        } else {
            NewApiHelper.addSecondHandCollection(commodity.pid, listener);
        }
    }

    private void confirmDelete() {
        if (commodity == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("删除商品")
                .setMessage("确认删除这条商品发布吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteCommodity())
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteCommodity() {
        NewApiHelper.deleteSecondHand(pid, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                runOnUiThread(() -> {
                    Toast.makeText(SecondHandDetailActivity.this,
                            data.isSuccess() ? "删除成功" : safeText(data.getMsg(), "删除失败"), Toast.LENGTH_SHORT).show();
                    if (data.isSuccess()) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(SecondHandDetailActivity.this, "删除失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateFavoriteButton() {
        getBinding().btnFavorite.setText(isFavorite ? "取消收藏" : "收藏");
    }

    private void bindStatus(TextView textView, int status) {
        if (status == 0) {
            textView.setText("出售");
            textView.getBackground().setTint(Color.parseColor("#4CAF50"));
        } else if (status == 1) {
            textView.setText("求购");
            textView.getBackground().setTint(Color.parseColor("#FF9800"));
        } else {
            textView.setText("其他");
            textView.getBackground().setTint(Color.parseColor("#9E9E9E"));
        }
    }

    private String getCategoryLabel(int type) {
        switch (type) {
            case 0:
                return "电子商品";
            case 1:
                return "生活用品";
            case 2:
                return "虚拟商品";
            case 3:
                return "学习用品";
            case 4:
                return "跑腿服务";
            default:
                return "其他分类";
        }
    }

    private String formatDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return "未知";
        }
        return date.length() > 19 ? date.substring(0, 19) : date;
    }

    private String safeText(String text, String fallback) {
        return text == null || text.trim().isEmpty() ? fallback : text.trim();
    }
}

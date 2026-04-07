package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.bean.javabean.data.Commodity;
import com.example.wusthelper.databinding.ActivitySecondHandPublishBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

public class SecondHandPublishActivity extends BaseActivity<ActivitySecondHandPublishBinding> {

    private static final String EXTRA_COMMODITY = "commodity";

    private final String[] categories = {"电子商品", "生活用品", "虚拟商品", "学习用品", "跑腿服务"};
    private final String[] statuses = {"出售", "求购", "其它"};

    private Commodity editCommodity;

    public static Intent newInstance(Context context) {
        return new Intent(context, SecondHandPublishActivity.class);
    }

    public static Intent newEditInstance(Context context, Commodity commodity) {
        Intent intent = new Intent(context, SecondHandPublishActivity.class);
        intent.putExtra(EXTRA_COMMODITY, commodity);
        return intent;
    }

    @Override
    public void initView() {
        editCommodity = (Commodity) getIntent().getSerializableExtra(EXTRA_COMMODITY);
        getBinding().tbTitle.tvTitleTitle.setText(editCommodity == null ? "发布商品" : "编辑商品");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getBinding().spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getBinding().spinnerStatus.setAdapter(statusAdapter);

        bindEditData();
        getBinding().btnSubmit.setText(editCommodity == null ? "发布" : "保存并重新发布");
        getBinding().btnSubmit.setOnClickListener(v -> submit());
    }

    private void bindEditData() {
        if (editCommodity == null) {
            return;
        }
        getBinding().etName.setText(safeText(editCommodity.name, ""));
        getBinding().etPrice.setText(String.valueOf(editCommodity.price));
        getBinding().etContact.setText(safeText(editCommodity.contact, ""));
        getBinding().etIntroduce.setText(safeText(editCommodity.introduce, ""));
        getBinding().spinnerCategory.setSelection(normalizeCategory(editCommodity.type));
        getBinding().spinnerStatus.setSelection(normalizeStatus(editCommodity.status));
    }

    private void submit() {
        String name = getBinding().etName.getText().toString().trim();
        String priceText = getBinding().etPrice.getText().toString().trim();
        String contact = getBinding().etContact.getText().toString().trim();
        String introduce = getBinding().etIntroduce.getText().toString().trim();
        int type = getBinding().spinnerCategory.getSelectedItemPosition();
        int status = getBinding().spinnerStatus.getSelectedItemPosition();

        if (name.isEmpty()) {
            Toast.makeText(this, "请输入商品标题", Toast.LENGTH_SHORT).show();
            return;
        }
        if (priceText.isEmpty()) {
            Toast.makeText(this, "请输入价格", Toast.LENGTH_SHORT).show();
            return;
        }
        if (contact.isEmpty()) {
            Toast.makeText(this, "请输入联系方式", Toast.LENGTH_SHORT).show();
            return;
        }
        if (introduce.isEmpty()) {
            Toast.makeText(this, "请输入商品描述", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "价格格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        if (price < 0) {
            Toast.makeText(this, "价格不能为负数", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editCommodity == null) {
            publish(name, price, contact, status, type, introduce);
        } else {
            republishAfterDelete(name, price, contact, status, type, introduce);
        }
    }

    private void publish(String name, double price, String contact, int status, int type, String introduce) {
        NewApiHelper.publishSecondHand(name, price, contact, status, type, introduce, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                runOnUiThread(() -> {
                    Toast.makeText(SecondHandPublishActivity.this,
                            data.isSuccess() ? "发布成功" : safeText(data.getMsg(), "发布失败"), Toast.LENGTH_SHORT).show();
                    if (data.isSuccess()) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(SecondHandPublishActivity.this, "发布失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void republishAfterDelete(String name, double price, String contact, int status, int type, String introduce) {
        NewApiHelper.deleteSecondHand(editCommodity.pid, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                if (!data.isSuccess()) {
                    runOnUiThread(() -> Toast.makeText(SecondHandPublishActivity.this,
                            safeText(data.getMsg(), "删除旧商品失败"), Toast.LENGTH_SHORT).show());
                    return;
                }
                publish(name, price, contact, status, type, introduce);
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(SecondHandPublishActivity.this, "删除旧商品失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private int normalizeCategory(int category) {
        if (category < 0 || category >= categories.length) {
            return 0;
        }
        return category;
    }

    private int normalizeStatus(int status) {
        if (status < 0 || status >= statuses.length) {
            return 0;
        }
        return status;
    }

    private String safeText(String text, String fallback) {
        return text == null || text.trim().isEmpty() ? fallback : text.trim();
    }
}

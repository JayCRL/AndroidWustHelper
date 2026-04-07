package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.bean.javabean.data.CampusMateActivity;
import com.example.wusthelper.bean.javabean.data.CampusMateActivityDetailData;
import com.example.wusthelper.bean.javabean.data.CampusMateActivityStats;
import com.example.wusthelper.bean.javabean.data.CampusMateActivityStatsData;
import com.example.wusthelper.bean.javabean.data.CampusMateApplication;
import com.example.wusthelper.bean.javabean.data.CampusMateApplicationListData;
import com.example.wusthelper.bean.javabean.data.CampusMateUserInfo;
import com.example.wusthelper.bean.javabean.data.CampusMateUserInfoData;
import com.example.wusthelper.bean.javabean.data.SimpleIdListData;
import com.example.wusthelper.databinding.ActivityCampusPartnerDetailBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import java.util.ArrayList;
import java.util.List;

public class CampusPartnerDetailActivity extends BaseActivity<ActivityCampusPartnerDetailBinding> {

    private static final String EXTRA_ACTIVITY_ID = "activity_id";

    private int activityId;
    private CampusMateActivity activity;
    private CampusMateUserInfo userInfo;
    private CampusMateActivityStats stats;
    private boolean liked;
    private boolean favorited;
    private CampusMateApplication myApplication;

    public static Intent newInstance(Context context, int activityId) {
        Intent intent = new Intent(context, CampusPartnerDetailActivity.class);
        intent.putExtra(EXTRA_ACTIVITY_ID, activityId);
        return intent;
    }

    @Override
    public void initView() {
        activityId = getIntent().getIntExtra(EXTRA_ACTIVITY_ID, 0);
        getBinding().tbTitle.tvTitleTitle.setText("活动详情");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
        getBinding().btnLike.setOnClickListener(v -> toggleLike());
        getBinding().btnFavorite.setOnClickListener(v -> toggleFavorite());
        getBinding().btnApply.setOnClickListener(v -> toggleApply());
        loadAll();
    }

    private void loadAll() {
        if (activityId <= 0) {
            Toast.makeText(this, "活动信息无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadDetail();
        loadStats();
        loadLikeIds();
        loadFavoriteIds();
        loadApplications();
    }

    private void loadDetail() {
        NewApiHelper.getCampusMateActivityDetail(activityId, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                CampusMateActivityDetailData data = (CampusMateActivityDetailData) responseObj;
                activity = data.getData();
                runOnUiThread(() -> bindActivity());
                loadUserInfo();
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CampusPartnerDetailActivity.this, "详情加载失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadUserInfo() {
        NewApiHelper.getCampusMateUserInfo(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                CampusMateUserInfoData data = (CampusMateUserInfoData) responseObj;
                userInfo = data.getData();
                runOnUiThread(() -> bindUserInfo());
            }

            @Override
            public void onFailure(Object reasonObj) {
            }
        });
    }

    private void loadStats() {
        NewApiHelper.getCampusMateActivityStats(activityId, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                CampusMateActivityStatsData data = (CampusMateActivityStatsData) responseObj;
                stats = data.getData();
                runOnUiThread(() -> bindStats());
            }

            @Override
            public void onFailure(Object reasonObj) {
            }
        });
    }

    private void loadLikeIds() {
        NewApiHelper.getCampusMateLikedIds(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                SimpleIdListData data = (SimpleIdListData) responseObj;
                List<Integer> ids = data.getData() == null ? new ArrayList<>() : data.getData();
                liked = ids.contains(activityId);
                runOnUiThread(() -> updateActionButtons());
            }

            @Override
            public void onFailure(Object reasonObj) {
            }
        });
    }

    private void loadFavoriteIds() {
        NewApiHelper.getCampusMateFavoriteIds(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                SimpleIdListData data = (SimpleIdListData) responseObj;
                List<Integer> ids = data.getData() == null ? new ArrayList<>() : data.getData();
                favorited = ids.contains(activityId);
                runOnUiThread(() -> updateActionButtons());
            }

            @Override
            public void onFailure(Object reasonObj) {
            }
        });
    }

    private void loadApplications() {
        NewApiHelper.getCampusMateMyApplications(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                CampusMateApplicationListData data = (CampusMateApplicationListData) responseObj;
                List<CampusMateApplication> list = data.getData() == null ? new ArrayList<>() : data.getData();
                myApplication = null;
                for (CampusMateApplication item : list) {
                    if (item.activityId == activityId) {
                        myApplication = item;
                        break;
                    }
                }
                runOnUiThread(() -> updateActionButtons());
            }

            @Override
            public void onFailure(Object reasonObj) {
            }
        });
    }

    private void bindActivity() {
        if (activity == null) {
            return;
        }
        getBinding().tvTitle.setText(safe(activity.title, "未命名活动"));
        getBinding().tvType.setText(safe(activity.type, "未分类"));
        getBinding().tvStatus.setText("状态：" + safe(activity.status, "未知"));
        getBinding().tvDescription.setText(safe(activity.description, "暂无描述"));
        getBinding().tvTime.setText("活动时间：" + safe(activity.activityTime, "未知"));
        getBinding().tvLocation.setText("活动地点：" + safe(activity.location, "未填写"));
        getBinding().tvPeople.setText("人数范围：" + activity.minPeople + " - " + activity.maxPeople + " 人");
        getBinding().tvCampus.setText("校区：" + safe(activity.campus, "未填写"));
        getBinding().tvCollege.setText("学院：" + safe(activity.college, "未填写"));
        getBinding().tvTags.setText("标签：" + safe(activity.tags, "无"));
        getBinding().tvCreatedAt.setText("发布时间：" + safe(activity.createdAt, "未知"));
        tintType(getBinding().tvType);
    }

    private void bindUserInfo() {
        getBinding().tvCreatorId.setText("发起人：" + safe(activity == null ? null : String.valueOf(activity.creatorId), "未知"));
        getBinding().tvUserCollege.setText("学院：" + safe(userInfo == null ? null : userInfo.college, "未填写"));
        getBinding().tvUserCampus.setText("校区：" + safe(userInfo == null ? null : userInfo.campus, "未填写"));
        getBinding().tvUserMajor.setText("专业：" + safe(userInfo == null ? null : userInfo.major, "未填写"));
        getBinding().tvUserSignature.setText("签名：" + safe(userInfo == null ? null : userInfo.signature, "这个人很低调"));
        String contact = "未填写";
        if (userInfo != null) {
            if (notBlank(userInfo.phone)) {
                contact = userInfo.phone;
            } else if (notBlank(userInfo.wechat)) {
                contact = "微信：" + userInfo.wechat;
            } else if (notBlank(userInfo.qq)) {
                contact = "QQ：" + userInfo.qq;
            }
        }
        getBinding().tvUserContact.setText("联系方式：" + contact);
    }

    private void bindStats() {
        int likeCount = stats == null || stats.likeCount == null ? 0 : stats.likeCount;
        int favoriteCount = stats == null || stats.favoriteCount == null ? 0 : stats.favoriteCount;
        getBinding().tvLikeCount.setText("点赞 " + likeCount);
        getBinding().tvFavoriteCount.setText("收藏 " + favoriteCount);
    }

    private void toggleLike() {
        NewApiHelper.toggleCampusMateLike(activityId, new ActionListener("操作失败") {
            @Override
            protected void onSuccess() {
                liked = !liked;
                loadStats();
                updateActionButtons();
            }
        });
    }

    private void toggleFavorite() {
        NewApiHelper.toggleCampusMateFavorite(activityId, new ActionListener("操作失败") {
            @Override
            protected void onSuccess() {
                favorited = !favorited;
                loadStats();
                updateActionButtons();
            }
        });
    }

    private void toggleApply() {
        if (myApplication == null) {
            NewApiHelper.applyCampusMateActivity(activityId, "我想参加这个活动", new ActionListener("申请失败") {
                @Override
                protected void onSuccess() {
                    loadApplications();
                }
            });
            return;
        }
        NewApiHelper.cancelCampusMateApplication(myApplication.id, new ActionListener("取消申请失败") {
            @Override
            protected void onSuccess() {
                myApplication = null;
                updateActionButtons();
            }
        });
    }

    private void updateActionButtons() {
        getBinding().btnLike.setText(liked ? "已点赞" : "点赞");
        getBinding().btnFavorite.setText(favorited ? "已收藏" : "收藏");
        getBinding().btnApply.setText(myApplication == null ? "申请参加" : "取消申请");
    }

    private void tintType(TextView textView) {
        textView.getBackground().setTint(Color.parseColor("#03A9F4"));
    }

    private String safe(String text, String fallback) {
        return text == null || text.trim().isEmpty() ? fallback : text.trim();
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private abstract class ActionListener implements DisposeDataListener {
        private final String failText;

        ActionListener(String failText) {
            this.failText = failText;
        }

        @Override
        public void onSuccess(Object responseObj) {
            BaseData data = (BaseData) responseObj;
            runOnUiThread(() -> {
                Toast.makeText(CampusPartnerDetailActivity.this, data.isSuccess() ? safe(data.getMsg(), "操作成功") : safe(data.getMsg(), failText), Toast.LENGTH_SHORT).show();
                if (data.isSuccess()) {
                    onSuccess();
                }
            });
        }

        protected abstract void onSuccess();

        @Override
        public void onFailure(Object reasonObj) {
            runOnUiThread(() -> Toast.makeText(CampusPartnerDetailActivity.this, failText, Toast.LENGTH_SHORT).show());
        }
    }
}

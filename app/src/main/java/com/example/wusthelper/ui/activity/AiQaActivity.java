package com.example.wusthelper.ui.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.AiQaAnswerBean;
import com.example.wusthelper.bean.javabean.data.AiQaData;
import com.example.wusthelper.bean.javabean.data.AiSubmitData;
import com.example.wusthelper.databinding.ActivityAiQaBinding;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.utils.CountDownUtils;
import com.example.wusthelper.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AiQaActivity extends BaseActivity<ActivityAiQaBinding> {

    private static final String TAG = "AiQaActivity";

    private final List<ChatMessage> msgList = new ArrayList<>();
    private ChatAdapter adapter;
    private int pendingIndex = -1;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private Runnable loadingStageRunnable;
    private long loadingMessageId = -1L;

    private Runnable typewriterRunnable;
    private long typewriterMessageId = -1L;

    private final List<String> quickQuestions = Arrays.asList(
            "宿舍限电多少瓦",
            "本周计算机学院有哪些重要通知",
            "武汉最近天气适合穿什么衣服"
    );

    private final List<String> loadingStageTips = Arrays.asList(
            "正在检索知识库…",
            "正在分析问题…",
            "正在组织回答…",
            "正在生成结果…"
    );

    public static Intent newInstance(Context context) {
        return new Intent(context, AiQaActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("WustChat");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());

        adapter = new ChatAdapter(msgList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(false);
        getBinding().rvChat.setLayoutManager(layoutManager);
        getBinding().rvChat.setAdapter(adapter);

        showWelcomeState();
        bindQuickQuestions();

        // 投稿入口：仅在已登录且不是研究生时展示
        getBinding().tvSubmit.setVisibility((!TextUtils.isEmpty(SharePreferenceLab.getToken()) && !SharePreferenceLab.getIsGraduate())
                ? android.view.View.VISIBLE : android.view.View.GONE);
        getBinding().tvSubmit.setOnClickListener(v -> showSubmitDialog());

        getBinding().btnSend.setOnClickListener(v -> sendQuestion(getBinding().etInput.getText().toString()));
        getBinding().etInput.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                sendQuestion(v.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void showWelcomeState() {
        getBinding().layoutWelcome.setVisibility(msgList.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void bindQuickQuestions() {
        getBinding().tvSuggestionOne.setText(quickQuestions.get(0));
        getBinding().tvSuggestionTwo.setText(quickQuestions.get(1));
        getBinding().tvSuggestionThree.setText(quickQuestions.get(2));
        getBinding().tvSuggestionOne.setOnClickListener(v -> fillOrSend(quickQuestions.get(0), true));
        getBinding().tvSuggestionTwo.setOnClickListener(v -> fillOrSend(quickQuestions.get(1), true));
        getBinding().tvSuggestionThree.setOnClickListener(v -> fillOrSend(quickQuestions.get(2), false));
    }

    private void fillOrSend(String question, boolean fillOnly) {
        getBinding().etInput.setText(question);
        getBinding().etInput.setSelection(question.length());
        if (!fillOnly) {
            sendQuestion(question);
        }
    }

    private void showSubmitDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ai_submit, null);
        EditText etText = view.findViewById(R.id.et_text);
        AutoCompleteTextView etTag = view.findViewById(R.id.et_tag);
        AutoCompleteTextView etSource = view.findViewById(R.id.et_source);
        EditText etExpireAt = view.findViewById(R.id.et_expire_at);

        List<String> tagSuggestions = Arrays.asList(
                "图书馆",
                "选课",
                "宿舍",
                "教务",
                "考试",
                "校车",
                "校历",
                "空教室",
                "志愿",
                "奖学金",
                "一卡通"
        );
        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tagSuggestions);
        etTag.setAdapter(tagAdapter);
        etTag.setThreshold(0);
        etTag.setOnClickListener(v -> etTag.showDropDown());

        List<String> sourceSuggestions = Arrays.asList(
                "移动端学生投稿",
                "学校官网",
                "学院通知",
                "教务处",
                "个人经验"
        );
        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sourceSuggestions);
        etSource.setAdapter(sourceAdapter);
        etSource.setThreshold(0);
        etSource.setOnClickListener(v -> etSource.showDropDown());

        etSource.setText("移动端学生投稿");

        etExpireAt.setOnClickListener(v -> showExpireAtPicker(etExpireAt));

        new AlertDialog.Builder(this)
                .setTitle("投稿知识")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("提交", (dialog, which) -> {
                    String text = etText.getText() == null ? "" : etText.getText().toString().trim();
                    String tag = etTag.getText() == null ? "" : etTag.getText().toString().trim();
                    String source = etSource.getText() == null ? "" : etSource.getText().toString().trim();
                    String expireAt = etExpireAt.getText() == null ? "" : etExpireAt.getText().toString().trim();

                    if (TextUtils.isEmpty(text)) {
                        ToastUtil.show("请输入投稿内容");
                        return;
                    }

                    Log.i(TAG, "submitAiCorpus: tag=" + tag + ", source=" + source + ", expireAt=" + expireAt);

                    NewApiHelper.submitAiCorpus(text, source, tag, expireAt, new DisposeDataListener() {
                        @Override
                        public void onSuccess(Object responseObj) {
                            AiSubmitData data = (AiSubmitData) responseObj;
                            if ("401".equals(data.getCode())) {
                                handleUnauthorized(data.getMsg());
                                return;
                            }
                            if (data.isSuccess()) {
                                ToastUtil.show(TextUtils.isEmpty(data.getMsg()) ? "已提交审核" : data.getMsg());
                            } else {
                                ToastUtil.show(TextUtils.isEmpty(data.getMsg()) ? "投稿失败" : data.getMsg());
                            }
                        }

                        @Override
                        public void onFailure(Object reasonObj) {
                            ToastUtil.show("投稿失败，可能是网络未连接或请求超时");
                        }
                    });
                })
                .show();
    }

    private void showExpireAtPicker(EditText etExpireAt) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                String formatted = CountDownUtils.format.format(new Date(calendar.getTimeInMillis()));
                etExpireAt.setText(formatted);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void sendQuestion(String input) {
        String question = input == null ? "" : input.trim();
        if (TextUtils.isEmpty(question)) {
            ToastUtil.show("请输入问题");
            return;
        }

        Log.i(TAG, "sendQuestion: " + question);

        stopTypewriter();
        stopLoadingStageLoop();

        addMsg(ChatMessage.user(question));
        getBinding().etInput.setText("");

        pendingIndex = addMsg(ChatMessage.loading());
        startLoadingStageLoop(pendingIndex);

        NewApiHelper.chatWithAi(question, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                AiQaData aiQaData = (AiQaData) responseObj;
                Log.i(TAG, "chatWithAi onSuccess: code=" + aiQaData.getCode() + ", msg=" + aiQaData.getMsg());

                if ("401".equals(aiQaData.getCode())) {
                    handleUnauthorized(aiQaData.getMsg());
                    return;
                }

                if (aiQaData.isSuccess() && aiQaData.getData() != null && !TextUtils.isEmpty(aiQaData.getData().getAnswer())) {
                    ChatMessage aiMsg = ChatMessage.aiTyping(aiQaData.getData());
                    int index = replacePending(aiMsg);
                    startTypewriter(index, aiMsg.messageId);
                } else {
                    replacePending(ChatMessage.error(TextUtils.isEmpty(aiQaData.getMsg()) ? "暂时没有可用回答，请稍后再试" : aiQaData.getMsg()));
                    ToastUtil.show(TextUtils.isEmpty(aiQaData.getMsg()) ? "AI 回答失败" : aiQaData.getMsg());
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                Log.w(TAG, "chatWithAi onFailure: " + reasonObj);
                replacePending(ChatMessage.error("网络较差或服务暂时不可用，请稍后重试"));
                ToastUtil.show("请求失败，可能是网络未连接或请求超时");
            }
        });
    }

    private void startLoadingStageLoop(int index) {
        if (index < 0 || index >= msgList.size()) {
            return;
        }
        ChatMessage msg = msgList.get(index);
        if (msg.role != ChatMessage.ROLE_LOADING) {
            return;
        }

        loadingMessageId = msg.messageId;

        // 立即更新一次，避免用户看到“正在思考中…”太久
        updateLoadingText(index, 0);

        loadingStageRunnable = new Runnable() {
            int stageIndex = 1;

            @Override
            public void run() {
                if (!updateLoadingText(index, stageIndex)) {
                    stopLoadingStageLoop();
                    return;
                }
                stageIndex = (stageIndex + 1) % loadingStageTips.size();
                uiHandler.postDelayed(this, 800);
            }
        };
        uiHandler.postDelayed(loadingStageRunnable, 800);
    }

    private boolean updateLoadingText(int index, int stageIndex) {
        if (index < 0 || index >= msgList.size()) {
            return false;
        }
        ChatMessage msg = msgList.get(index);
        if (msg.role != ChatMessage.ROLE_LOADING || msg.messageId != loadingMessageId) {
            return false;
        }
        String tip = loadingStageTips.get(stageIndex % loadingStageTips.size());
        msg.content = tip;
        adapter.notifyItemChanged(index);
        return true;
    }

    private void stopLoadingStageLoop() {
        loadingMessageId = -1L;
        if (loadingStageRunnable != null) {
            uiHandler.removeCallbacks(loadingStageRunnable);
            loadingStageRunnable = null;
        }
    }

    private void startTypewriter(int index, long messageId) {
        if (index < 0 || index >= msgList.size()) {
            return;
        }

        ChatMessage msg = msgList.get(index);
        if (msg.role != ChatMessage.ROLE_AI || msg.messageId != messageId || TextUtils.isEmpty(msg.fullContent)) {
            return;
        }

        stopTypewriter();

        typewriterMessageId = messageId;
        final String full = msg.fullContent;
        final int totalCodePoints = full.codePointCount(0, full.length());

        typewriterRunnable = new Runnable() {
            int shownCodePoints = 0;

            @Override
            public void run() {
                if (index < 0 || index >= msgList.size()) {
                    stopTypewriter();
                    return;
                }
                ChatMessage current = msgList.get(index);
                if (current.role != ChatMessage.ROLE_AI || current.messageId != typewriterMessageId) {
                    stopTypewriter();
                    return;
                }

                int step = 2;
                shownCodePoints = Math.min(totalCodePoints, shownCodePoints + step);
                int endIndex = full.offsetByCodePoints(0, shownCodePoints);
                current.content = full.substring(0, endIndex);
                adapter.notifyItemChanged(index);

                if (shownCodePoints >= totalCodePoints) {
                    current.content = full;
                    current.fullContent = null;
                    adapter.notifyItemChanged(index);
                    stopTypewriter();
                    scrollToBottom();
                    return;
                }

                if (index == msgList.size() - 1) {
                    scrollToBottom();
                }
                uiHandler.postDelayed(this, 20);
            }
        };

        uiHandler.post(typewriterRunnable);
    }

    private void stopTypewriter() {
        typewriterMessageId = -1L;
        if (typewriterRunnable != null) {
            uiHandler.removeCallbacks(typewriterRunnable);
            typewriterRunnable = null;
        }
    }

    private void handleUnauthorized(String msg) {
        NewApiHelper.handleUnauthorized(this, TextUtils.isEmpty(msg) ? "登录已失效，请重新登录" : msg);
    }

    private int addMsg(ChatMessage msg) {
        msgList.add(msg);
        showWelcomeState();
        adapter.notifyItemInserted(msgList.size() - 1);
        scrollToBottom();
        return msgList.size() - 1;
    }

    private int replacePending(ChatMessage message) {
        stopLoadingStageLoop();

        if (pendingIndex >= 0 && pendingIndex < msgList.size()) {
            int index = pendingIndex;
            msgList.set(index, message);
            adapter.notifyItemChanged(index);
            pendingIndex = -1;
            scrollToBottom();
            return index;
        } else {
            return addMsg(message);
        }
    }

    private void scrollToBottom() {
        if (msgList.isEmpty()) {
            return;
        }
        new Handler().post(() -> getBinding().rvChat.smoothScrollToPosition(msgList.size() - 1));
    }

    static class ChatMessage {
        static final int ROLE_USER = 1;
        static final int ROLE_AI = 2;
        static final int ROLE_LOADING = 3;
        static final int ROLE_ERROR = 4;

        long messageId;
        int role;
        String content;
        String subtitle;

        // 打字机效果用：只在 ROLE_AI 时使用
        String fullContent;

        static ChatMessage user(String content) {
            ChatMessage message = new ChatMessage();
            message.messageId = System.nanoTime();
            message.role = ROLE_USER;
            message.content = content;
            return message;
        }

        static ChatMessage loading() {
            ChatMessage message = new ChatMessage();
            message.messageId = System.nanoTime();
            message.role = ROLE_LOADING;
            message.content = "正在思考中…";
            return message;
        }

        static ChatMessage error(String content) {
            ChatMessage message = new ChatMessage();
            message.messageId = System.nanoTime();
            message.role = ROLE_ERROR;
            message.content = content;
            message.subtitle = "可稍后重试或换个问法";
            return message;
        }

        static ChatMessage ai(AiQaAnswerBean bean) {
            ChatMessage message = new ChatMessage();
            message.messageId = System.nanoTime();
            message.role = ROLE_AI;
            message.content = bean.getAnswer();
            // 简化展示：不向用户展示任何“标签/命中投稿/缓存命中”等副信息
            message.subtitle = null;
            return message;
        }

        static ChatMessage aiTyping(AiQaAnswerBean bean) {
            ChatMessage message = new ChatMessage();
            message.messageId = System.nanoTime();
            message.role = ROLE_AI;
            message.content = "";
            message.fullContent = bean.getAnswer();
            message.subtitle = null;
            return message;
        }
    }

    static class ChatAdapter extends BaseQuickAdapter<ChatMessage, BaseViewHolder> {
        ChatAdapter(List<ChatMessage> data) {
            super(R.layout.item_chat, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, ChatMessage item) {
            boolean showLeft = item.role != ChatMessage.ROLE_USER;
            helper.setGone(R.id.layout_left, !showLeft);
            helper.setGone(R.id.layout_right, showLeft);
            if (showLeft) {
                helper.setText(R.id.tv_msg_left, item.content);

                // 简化展示：默认隐藏 subtitle；仅 error 时显示固定提示
                boolean showSubtitle = item.role == ChatMessage.ROLE_ERROR && !TextUtils.isEmpty(item.subtitle);
                helper.setText(R.id.tv_msg_left_subtitle, showSubtitle ? item.subtitle : "");
                helper.setGone(R.id.tv_msg_left_subtitle, !showSubtitle);

                int bgRes = item.role == ChatMessage.ROLE_ERROR ? R.drawable.bg_ai_bubble_error : R.drawable.bg_ai_bubble_left;
                helper.setBackgroundResource(R.id.tv_msg_left, bgRes);
            } else {
                helper.setText(R.id.tv_msg_right, item.content);
                helper.setGone(R.id.tv_msg_right_subtitle, true);
                helper.setBackgroundResource(R.id.tv_msg_right, R.drawable.bg_ai_bubble_right);
            }
        }
    }
}

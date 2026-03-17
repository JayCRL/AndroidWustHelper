package com.example.wusthelper.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wusthelper.R;
import com.example.wusthelper.adapter.LibraryHistoryAdapter;
import com.example.wusthelper.bean.javabean.LibraryHistoryBean;
import com.example.wusthelper.bean.javabean.data.LibraryHistoryData;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.utils.NetWorkUtils;
import com.example.wusthelper.utils.ToastUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LibraryHistoryActivity extends AppCompatActivity {

    private RecyclerView mLibraryHistoryRecyclerView;
    private SmartRefreshLayout historyRefreshLayout;
    private List<LibraryHistoryBean> mLibraryHistoryBeanList = new ArrayList<>();
    private LibraryHistoryAdapter mLibraryHistoryAdapter;
    private boolean isFirstLoaded = false;
    private static final String TAG = "LibraryHistoryFragment";
    private FrameLayout contentFrameLayout;
    private FrameLayout noInternetFrameLayout;
    private ImageView ivBack;
    private AlertDialog dialog;

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context,LibraryHistoryActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_history);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        initView();
        dialog = loadingDialog("正在加载图书", false);
        isNetWorkConnect();
        mLibraryHistoryAdapter = new LibraryHistoryAdapter(mLibraryHistoryBeanList, LibraryHistoryActivity.this);
        mLibraryHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(LibraryHistoryActivity.this));
        mLibraryHistoryRecyclerView.setAdapter(mLibraryHistoryAdapter);
        if (!isFirstLoaded) {
            // login();
            getBorrowingBooks();
            isFirstLoaded = true;
        }

        historyRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                if (NetWorkUtils.isConnected(LibraryHistoryActivity.this)){
//                    try_to_login();
                    // login();
                    getBorrowingBooks();
                    historyRefreshLayout.finishRefresh(500);
                }
                else {
                    isNetWorkConnect();
                    historyRefreshLayout.finishRefresh(500);
                }
            }
        });
    }

    private void isNetWorkConnect() {

        if (!NetWorkUtils.isConnected(this)) {

            contentFrameLayout.setVisibility(View.GONE);
            noInternetFrameLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initView() {

        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mLibraryHistoryRecyclerView = findViewById(R.id.rv_library_history);
        historyRefreshLayout = findViewById(R.id.sr_book_history);
        contentFrameLayout = findViewById(R.id.fl_content);
        noInternetFrameLayout = findViewById(R.id.fl_no_internet);


    }

    private void getHistoryBooks() {
        NewApiHelper.getHistoryBook(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                LibraryHistoryData libraryHistoryData = (LibraryHistoryData) responseObj;
                if(libraryHistoryData.getCode().equals("10000") || libraryHistoryData.getCode().equals("11000")){
                    for(LibraryHistoryBean libraryHistoryBean : libraryHistoryData.getData()){
                        libraryHistoryBean.setTitle(LibraryHistoryBean.HISTORY_BOOK);
                        libraryHistoryBean.setType(LibraryHistoryBean.CONTENT);
                        libraryHistoryBean.setFunction("已归还");
                    }
                    FragmentActivity activity = LibraryHistoryActivity.this;
                    if(activity != null)
                        mLibraryHistoryBeanList.add(new LibraryHistoryBean(LibraryHistoryBean.HISTORY_BOOK, LibraryHistoryBean.TITLE, activity.getDrawable(R.drawable.history_book), "历史图书", libraryHistoryData.getData().size()));
                    mLibraryHistoryBeanList.addAll(libraryHistoryData.getData());
                    mLibraryHistoryAdapter.notifyDataSetChanged();
                }else if(libraryHistoryData.getCode().equals("40002")){
                    ToastUtil.show("图书馆登录已过期，请重新登录");
                    SharePreferenceLab.setIsLibraryLogin(false);
                    finish();
                } else {
                    ToastUtil.show(libraryHistoryData.getMsg());

                }
                historyRefreshLayout.finishRefresh();
            }

            @Override
            public void onFailure(Object reasonObj) {
                historyRefreshLayout.finishRefresh();
                ToastUtil.show("请求失败，可能是网络未链接或请求超时");
            }
        });
    }

    private void getBorrowingBooks() {
        dialog.show();
        NewApiHelper.getRentInfo(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                LibraryHistoryData libraryHistoryData = (LibraryHistoryData) responseObj;

                if(libraryHistoryData.getCode().equals("10000") || libraryHistoryData.getCode().equals("11000")){
                    mLibraryHistoryBeanList.clear();
                    contentFrameLayout.setVisibility(View.VISIBLE);
                    noInternetFrameLayout.setVisibility(View.GONE);
                    List<LibraryHistoryBean> beyondTimeList = new ArrayList<>();
                    List<LibraryHistoryBean> borrowingList = new ArrayList<>();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    for(LibraryHistoryBean libraryHistoryBean : libraryHistoryData.getData()){
                        try {
                            Date currentDate = new Date();
                            Date returnDate = format.parse(libraryHistoryBean.getReturnDate());
                            if(currentDate.getTime() > returnDate.getTime()) {

                                libraryHistoryBean.setTitle(LibraryHistoryBean.BEYOND_TIME);
                                libraryHistoryBean.setType(LibraryHistoryBean.CONTENT);
                                libraryHistoryBean.setFunction("还书");
                                beyondTimeList.add(libraryHistoryBean);
                            } else {
                                libraryHistoryBean.setTitle(LibraryHistoryBean.BORROWING);
                                libraryHistoryBean.setType(LibraryHistoryBean.CONTENT);
                                libraryHistoryBean.setFunction("续借");
                                borrowingList.add(libraryHistoryBean);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    historyRefreshLayout.finishRefresh();
                                }
                            });
                        } catch (Exception e){
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    historyRefreshLayout.finishRefresh();
                                }
                            });

                        }
                    }
                    mLibraryHistoryBeanList.add(new LibraryHistoryBean(LibraryHistoryBean.BORROWING, LibraryHistoryBean.TITLE, getApplication().getDrawable(R.drawable.borrowing), "在借的图书", borrowingList.size()));
                    mLibraryHistoryBeanList.addAll(borrowingList);
                    mLibraryHistoryBeanList.add(new LibraryHistoryBean(LibraryHistoryBean.BEYOND_TIME, LibraryHistoryBean.TITLE, getApplication().getDrawable(R.drawable.beyond_time), "超期的图书", beyondTimeList.size()));
                    mLibraryHistoryBeanList.addAll(beyondTimeList);
                    getHistoryBooks();
                    dialog.cancel();
                }else if(libraryHistoryData.getCode().equals("40002")){
                    ToastUtil.show("图书馆登录已过期，请重新登录");
                    SharePreferenceLab.setIsLibraryLogin(false);
                    finish();
                    dialog.cancel();
                }
                else {
                    historyRefreshLayout.finishRefresh();
                    ToastUtil.show(libraryHistoryData.getMsg());
                    dialog.cancel();
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                dialog.cancel();
                historyRefreshLayout.finishRefresh();
                ToastUtil.show("请求失败，可能是网络未链接或请求超时");
                Log.e(TAG, "getBorrowingBooks: failed!"  );
            }
        });

    }

    private AlertDialog loadingDialog(String text, boolean cancelable){

        View view = LayoutInflater.from(LibraryHistoryActivity.this).inflate(R.layout.toast_loading, null);

        AVLoadingIndicatorView avl = (AVLoadingIndicatorView) view.findViewById(R.id.avl);
        avl.show();
        TextView tv = view.findViewById(R.id.tv);
        tv.setText(text);
        AlertDialog dialog = new AlertDialog.Builder(LibraryHistoryActivity.this, R.style.LoadingDialog)
                .setView(view)
                .setCancelable(cancelable)
                .create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d(TAG, "调用了OnCancelListener");
            }
        });

        return dialog;

    }
}
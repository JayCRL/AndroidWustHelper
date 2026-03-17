package com.example.wusthelper.ui.fragment.libraryviewpager;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wusthelper.R;
import com.example.wusthelper.adapter.LibraryHistoryAdapter;
import com.example.wusthelper.bean.javabean.LibraryHistoryBean;
import com.example.wusthelper.bean.javabean.data.LibraryHistoryData;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.ui.activity.LibraryLoginActivity;
import com.example.wusthelper.utils.NetWorkUtils;
import com.example.wusthelper.utils.ToastUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LibraryHistoryFragment extends Fragment {

    private RecyclerView mLibraryHistoryRecyclerView;
    private SmartRefreshLayout historyRefreshLayout;
    private List<LibraryHistoryBean> mLibraryHistoryBeanList = new ArrayList<>();
    private LibraryHistoryAdapter mLibraryHistoryAdapter;
    private boolean isFirstLoaded = false;
    private static final String TAG = "LibraryHistoryFragment";
    private FrameLayout contentFrameLayout;
    private FrameLayout noInternetFrameLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_history, container, false);
        initView(view);
        isNetWorkConnect();
        mLibraryHistoryAdapter = new LibraryHistoryAdapter(mLibraryHistoryBeanList, getActivity());
        mLibraryHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLibraryHistoryRecyclerView.setAdapter(mLibraryHistoryAdapter);
        if (!isFirstLoaded) {
            // login();
            getBorrowingBooks();
            isFirstLoaded = true;
        }

        historyRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                if (NetWorkUtils.isConnected(getActivity())){
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
        return view;
    }

    private void isNetWorkConnect() {

        if (!NetWorkUtils.isConnected(getActivity())) {

            contentFrameLayout.setVisibility(View.GONE);
            noInternetFrameLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initView(View view) {

        mLibraryHistoryRecyclerView = view.findViewById(R.id.rv_library_history);
        historyRefreshLayout = view.findViewById(R.id.sr_book_history);
        contentFrameLayout = view.findViewById(R.id.fl_content);
        noInternetFrameLayout = view.findViewById(R.id.fl_no_internet);


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
                    FragmentActivity activity = getActivity();
                    if(activity != null)
                        mLibraryHistoryBeanList.add(new LibraryHistoryBean(LibraryHistoryBean.HISTORY_BOOK, LibraryHistoryBean.TITLE, activity.getDrawable(R.drawable.history_book), "历史图书", libraryHistoryData.getData().size()));
                    mLibraryHistoryBeanList.addAll(libraryHistoryData.getData());
                    mLibraryHistoryAdapter.notifyDataSetChanged();
                }else if(libraryHistoryData.getCode().equals("40002")){
                    ToastUtil.show("图书馆登录已过期，请重新登录");
                    SharePreferenceLab.setIsLibraryLogin(false);
                    startActivity(LibraryLoginActivity.newInstance(getContext()));
                    getActivity().finish();
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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    historyRefreshLayout.finishRefresh();
                                }
                            });
                        } catch (Exception e){
                            e.printStackTrace();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    historyRefreshLayout.finishRefresh();
                                }
                            });

                        }
                    }
                    mLibraryHistoryBeanList.add(new LibraryHistoryBean(LibraryHistoryBean.BORROWING, LibraryHistoryBean.TITLE, getContext().getDrawable(R.drawable.borrowing), "在借的图书", borrowingList.size()));
                    mLibraryHistoryBeanList.addAll(borrowingList);
                    mLibraryHistoryBeanList.add(new LibraryHistoryBean(LibraryHistoryBean.BEYOND_TIME, LibraryHistoryBean.TITLE, getContext().getDrawable(R.drawable.beyond_time), "超期的图书", beyondTimeList.size()));
                    mLibraryHistoryBeanList.addAll(beyondTimeList);
                    getHistoryBooks();
                }else if(libraryHistoryData.getCode().equals("40002")){
                    ToastUtil.show("图书馆登录已过期，请重新登录");
                    SharePreferenceLab.setIsLibraryLogin(false);
                    startActivity(LibraryLoginActivity.newInstance(getContext()));
                    getActivity().finish();
                }
                else {
                    historyRefreshLayout.finishRefresh();
                    ToastUtil.show(libraryHistoryData.getMsg());
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                historyRefreshLayout.finishRefresh();
                ToastUtil.show("请求失败，可能是网络未链接或请求超时");
                Log.e(TAG, "getBorrowingBooks: failed!"  );
            }
        });

    }
}

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wusthelper.R;
import com.example.wusthelper.adapter.MyLibraryCollectionAdapter;
import com.example.wusthelper.bean.javabean.CollectionBookBean;
import com.example.wusthelper.bean.javabean.data.LibCollectData;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.ui.activity.LibraryLoginActivity;
import com.example.wusthelper.utils.NetWorkUtils;
import com.example.wusthelper.utils.ToastUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class LibraryCollectionFragment extends Fragment {
    private List<CollectionBookBean> mCollectionBookBeanList = new ArrayList<>();
    private LibCollectData lib_collect_data;
    private RecyclerView mLibraryCollectionRecyclerView;
    private SmartRefreshLayout collectionRefreshLayout;
    private MyLibraryCollectionAdapter mLibraryCollectionAdapter;
    private static final String KEY = "NewWustHelperWithAndroid2018";
    private static final String TAG = "LibraryCollection";
    private String studentId;
    private FrameLayout contentFrameLayout;
    private FrameLayout noContentFrameLayout;
    private FrameLayout noInternetFrameLayout;

    private int page ;
    private Boolean if_make_question=false ;//是否在进行网络请求，默认否
    private static String pageSize="10";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onResume() {
        super.onResume();
        page = 1;
        mCollectionBookBeanList.clear();
        isNetWorkConnect();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_collection, container, false);
        initView(view);

        mLibraryCollectionAdapter = new MyLibraryCollectionAdapter(mCollectionBookBeanList, getActivity());
        mLibraryCollectionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLibraryCollectionRecyclerView.setAdapter(mLibraryCollectionAdapter);

        collectionRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                if (NetWorkUtils.isConnected(getActivity())){
                    if(!if_make_question){
                        if_make_question=true;
                        refreshLayout.finishRefresh(1000);
                        refreshLayoutAllList();
                    }else{
                        refreshLayout.finishRefresh(1000);
                    }
                }else {
                    refreshLayout.finishRefresh(false);//结束刷新（刷新失败）
                    isNetWorkConnect();
                }
            }
        });



        collectionRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (NetWorkUtils.isConnected(getActivity())){
                    if(!if_make_question){
                        if_make_question=true;
                        refreshLayout.finishLoadMore(1000);
                        getCollectionBooks(page);
                    }else{
                        refreshLayout.finishLoadMore();
                    }
                }else {
                    refreshLayout.finishLoadMore(false);//结束加载（加载失败）
                    isNetWorkConnect();
                }
            }
        });
        return view;
    }

    private void refreshLayoutAllList() {
        page=1;
        mCollectionBookBeanList.clear();
        getCollectionBooks(page);
    }

    private void initView(View view) {

        mLibraryCollectionRecyclerView = (RecyclerView)view.findViewById(R.id.rv_library_collection);
        collectionRefreshLayout = (SmartRefreshLayout)view.findViewById(R.id.sr_book_collection);
        contentFrameLayout = (FrameLayout)view.findViewById(R.id.fl_content);
        noContentFrameLayout = (FrameLayout)view.findViewById(R.id.fl_no_content);
        noInternetFrameLayout = (FrameLayout)view.findViewById(R.id.fl_no_internet);

    }

    private void isNetWorkConnect() {

        if (!NetWorkUtils.isConnected(getActivity())) {
            noInternetFrameLayout.setVisibility(View.VISIBLE);
            contentFrameLayout.setVisibility(View.GONE);
            noContentFrameLayout.setVisibility(View.GONE);
        }else {
            getCollectionBooks(page);
        }

    }

    private void getCollectionBooks(int this_page) {
        NewApiHelper.getLibListCollection(this_page + "", new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                LibCollectData libCollectData = (LibCollectData) responseObj;
                if(libCollectData.getCode().equals("10000")) {
                    addDataToList(libCollectData);
                }else if(libCollectData.getCode().equals("40002")){
                    ToastUtil.show("图书馆登录已过期，请重新登录");
                    SharePreferenceLab.setIsLibraryLogin(false);
                    startActivity(LibraryLoginActivity.newInstance(getContext()));
                    getActivity().finish();
                } else{
                    ToastUtil.show(libCollectData.getMsg());
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                ToastUtil.show("请求失败，可能是网络未链接或请求超时");
            }
        });
//        NewApiHelper.getLibListCollection(this_page+"", pageSize, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String Json = response.body().string();
//                Log.d(TAG, "onResponse: "+Json);
//                try {
//                    lib_collect_data=new Gson().fromJson(Json, LibCollectData.class);
//                    if(lib_collect_data.getCode()==10000){
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                add_data_to_List(lib_collect_data);
//                            }
//                        });
//                    }else {
//
//                    }
//                } catch (JsonSyntaxException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    private void addDataToList(LibCollectData libCollectData) {
        mCollectionBookBeanList.addAll(libCollectData.getData());
        mLibraryCollectionAdapter.notifyDataSetChanged();
        if_make_question=false;
        page++;
        if(!(mCollectionBookBeanList.size()>0)){
            Log.d(TAG,"mCollectionBookBeanList:"+mCollectionBookBeanList.toString());
            Log.d(TAG,"无收藏信息");
            contentFrameLayout.setVisibility(View.GONE);
            noInternetFrameLayout.setVisibility(View.GONE);
            noContentFrameLayout.setVisibility(View.VISIBLE);
        }else{
            Log.d(TAG,"有收藏信息");
            contentFrameLayout.setVisibility(View.VISIBLE);
            noInternetFrameLayout.setVisibility(View.GONE);
            noContentFrameLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!SharePreferenceLab.getIsLibraryLogin()){
            getActivity().finish();
        }
    }
}

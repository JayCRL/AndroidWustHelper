package com.example.wusthelper.ui.fragment.libraryviewpager;

import static com.xuexiang.xutil.XUtil.runOnUiThread;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wusthelper.R;
import com.example.wusthelper.adapter.LibraryNotificationAdapter;
import com.example.wusthelper.bean.javabean.AnnouncementBean;
import com.example.wusthelper.bean.javabean.LibraryAnnouncementBean;
import com.example.wusthelper.bean.javabean.LibraryNotificationBean;
import com.example.wusthelper.bean.javabean.data.AnnouncementData;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.ui.activity.LibraryLoginActivity;
import com.example.wusthelper.utils.NetWorkUtils;
import com.example.wusthelper.utils.ThreadPoolManager;
import com.example.wusthelper.utils.ToastUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LibraryNotificationFragment extends Fragment {

    private static final String TAG = "LibraryNotific";
    private AlertDialog loadingView;
    private RecyclerView mLibraryNotificationRecyclerView;
    private LibraryNotificationAdapter mLibraryNotificationAdapter;
    private List<LibraryNotificationBean> mLibraryNotificationBeanList = new ArrayList<>();
    private boolean isFirstLoaded = false;
//    private static final String NOTIFICATION_URL = "http://www.lib.wust.edu.cn/bullet/bullet.aspx";
    private static final String NOTIFICATION_URL = "http://122.205.143.141:9999/article/list";
    private FrameLayout contentFrameLayout;
    private FrameLayout noInternetFrameLayout;
    private SmartRefreshLayout notificationSmartLayout;
    private  int PAGE_COUNT;
    private LibraryAnnouncementBean libraryannouncementBean;//每次请求获得的图书馆公告数据
    private List<AnnouncementBean> announcementBeanList = new ArrayList<>();//用于显示的数组
    private static int is_load=0;//判断是否是正在下拉刷新
    private static int totalPage = 0;
    int number_page=1;//请求的页数
    private String firstRequest = "__EVENTTARGET";
    private String firstString = "ctl00$ContentPlaceHolder2$DataPager1$ctl00$PageJump";
    private String secondRequest = "__VIEWSTATE";
    private String secondString = "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";
    private String thirdRequest = "__VIEWSTATEGENERATOR";
    private String thirdString = "1F08F0C3";
    private String forthRequest = "__EVENTVALIDATION";
    private String forthString = "/wEdABPBdz94PnFwMgqItCpYMSbaB5QOEw618qtcv9QsXljPsKQdlehrqbOESQBKM/DpUrrjt6dGkhfDfauW2/XRDfHe+PA/Ism1WYXcmb6V2ldBkxL1RL734rMk/KxG+cabLTqzpaCN4fOH3CMHgjBcr6jQkZ0V9v0UTey0AhOtC2IoB+OBO5BvU9Kgd3PsVJSdXcZXcV6fiOLwqQBJGFawFsLmIX/GD7ZaIMw/rXLITYqKuXyzfOM8spkBd1LqWo515oCIc0HJRZfjjn+KEMGbQegHQOf0x8momQZl2W9i5Ai1ddZZ1qRzOBC66EmTJo6JFbqiskMYEumDzMva8hJUL/O5G04qD9+cVTjRAyHpno2XpGrjRa6DggaRJkILjCMLXqrJ84toGGVz24KltJoxh97NvAHdgxzOFiMg7ZG/CQMdw76SORnFSALowCm5GKK1kws=";



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_notification, container, false);
        initView(view);
        isNetWorkConnect();
        if (!isFirstLoaded) {
            if (NetWorkUtils.isConnected(getActivity()))
                isFirstLoaded = true;
        }
//        initLoadingView();
        mLibraryNotificationAdapter = new LibraryNotificationAdapter(announcementBeanList, getActivity());
        mLibraryNotificationRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLibraryNotificationRecyclerView.setAdapter(mLibraryNotificationAdapter);
        notificationSmartLayout.setRefreshFooter(new ClassicsFooter(requireContext()));
        notificationSmartLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                if(NetWorkUtils.isConnected(getActivity())){
                    if(is_load == 0){
                        number_page++;
//                        getLibGetAnnouncementList(number_page+"");
                        getNewLibGetAnnouncementList(number_page+"");
                        //  updateRecyclerView(mLibraryNotificationAdapter.getRealLastPosition(), mLibraryNotificationAdapter.getRealLastPosition() + PAGE_COUNT);
                        refreshlayout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
                    }else {
                        refreshlayout.finishLoadMore(100);
                    }
                }else {
                    refreshlayout.finishLoadMore(false);//结束加载（加载失败）
                }

            }
        });

        notificationSmartLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                if(NetWorkUtils.isConnected(getActivity())){
                    contentFrameLayout.setVisibility(View.VISIBLE);
                    noInternetFrameLayout.setVisibility(View.GONE);
                    refreshLayout.finishRefresh(100);
                }else {
                    refreshLayout.finishRefresh(false);//结束刷新（刷新失败）
                }
            }
        });
        if(NetWorkUtils.isConnected(getActivity())){
//            getLibGetAnnouncementList(""+number_page);
            getNewLibGetAnnouncementList(number_page+"");
        }
        return view;
    }

    private void isNetWorkConnect() {

        if (!NetWorkUtils.isConnected(getActivity())) {
            contentFrameLayout.setVisibility(View.GONE);
            noInternetFrameLayout.setVisibility(View.VISIBLE);
        }

    }

    private void initView(View view) {

        mLibraryNotificationRecyclerView = (RecyclerView)view.findViewById(R.id.rv_library_notification);
        contentFrameLayout = (FrameLayout)view.findViewById(R.id.fl_content);
        noInternetFrameLayout = (FrameLayout)view.findViewById(R.id.fl_no_internet);
        notificationSmartLayout = (SmartRefreshLayout)view.findViewById(R.id.sr_book_notification);

    }

    //爬虫获取
//    private void getLibGetAnnouncementList(String pageNum) {
//        String pageSize = "10";
//        is_load=1;
//        ThreadPoolManager.getInstance().addExecuteTask(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Document document = Jsoup.connect(NOTIFICATION_URL)
//                            .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 SLBrowser/8.0.0.3161 SLBChan/30")
//                            .header("Content-Type","application/x-www-form-urlencoded")
////                            .header("Connection","keep-alive")
////                            .header("Accept-Encoding","gzip, deflate, br")
////                            .header("Host","www.lib.wust.edu.cn")
////                            .header("Accept","*/*")
//                            .data("ctl00$ContentPlaceHolder2$DataPager1$ctl00$PageJump",pageNum)
//                            .post();
////                    Log.d(TAG,"document " + document);
////                    .data(firstRequest,firstString)
////                            .data(secondRequest,secondString)
////                            .data(thirdRequest,thirdString)
////                            .data(forthRequest,forthString)
//
//                    Element element = document.getElementById("content");
//                    Log.d(TAG,"element " + element);
////                    Elements elements = element.select("tbody");
//////                    Log.d(TAG,"elements " + elements);
////                    Elements elements1 = elements.get(0).select("tr");
//////                    Log.d(TAG,"elements1 " + elements1);
////                    Elements elements2 = elements1.get(0).select("div");
//////                    Log.d(TAG,"elements2 " + elements2);
////                    Elements elements3 = elements2.get(6).select("div");
////                    Log.d(TAG,"elements3 " + elements3);
////                    for(int i = 0; i<elements3.size(); i++) {
////                        Log.e(TAG,"elements3.get(i) " + elements3.get(i));
////                    }
//                    if(number_page == 1) {
//                        Elements elementPages = element.select("option");
//                        Log.e(TAG,"elementPages" + elementPages);
//                        totalPage = elementPages.size();
//                    }
//
//                    Elements elementsName = element.getElementsByAttributeValue("style"," float: left; width:75%;");
//                    Log.d(TAG,"elementsName " + elementsName + elementsName.size());
//                    Elements elementsTime = element.getElementsByAttributeValue("style","float: left");
//                    Log.d(TAG,"elementsTime " + elementsTime + elementsTime.size());
//                    for(int i = 0 ; i < elementsName.size(); i++) {
//                        String announcementId = "http://www.lib.wust.edu.cn/bullet/" + elementsName.get(i).select("a").attr("href");
//                        Log.d(TAG,"announcementId " + announcementId);
//                        String announcementTitle = elementsName.get(i).select("a").text();
//                        Log.d(TAG,"announcementTitle " + announcementTitle);
//                        String announcementCreatetime = elementsTime.get(i).select("div").text();
//                        Log.d(TAG,"announcementCreatetime " + announcementCreatetime);
//                        AnnouncementBean announcement = new AnnouncementBean();
//                        announcement.setAnnouncementId(announcementId);
//                        announcement.setAnnouncementTitle(announcementTitle);
//                        announcement.setAnnouncementCreatetime(announcementCreatetime);
//                        announcementBeanList.add(announcement);
//                    }
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            loadingView.cancel();
//                            mLibraryNotificationAdapter.notifyDataSetChanged();
//                        }
//                    });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    private void getLibGetAnnouncementList(String pageNum) {
        String pageSize = "10";
        is_load=1;
        ThreadPoolManager.getInstance().addExecuteTask(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = "http://122.205.143.141:9999/article/list?categoryId=30&currentPage="+pageNum+"&pageNum=10&isDatedGroups=true";
                    Document document = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 SLBrowser/8.0.0.3161 SLBChan/30")
                            .ignoreContentType(true)
                            .get();
                    Log.d(TAG, "run: 公告解析"+document);
                    Element body = document.body();
                    JSONObject jsonData = new JSONObject(body.toString());
                    Log.d(TAG, "jsonData: 公告解析"+jsonData);
                    JSONArray dataList = jsonData.getJSONArray("data");
                    Log.d(TAG, "data: 搜索解析"+dataList);
                    for(int i = 0; i<dataList.length(); i++){
                        JSONArray articles = dataList.getJSONObject(i).getJSONArray("articles");
                        for(int j = 0; j<articles.length(); j++){
                            String announcementId = articles.getJSONObject(j).getString("href");
                            Log.d(TAG, "announcementId: 搜索解析"+announcementId);
                            if(announcementId == null || announcementId.equals("null")){
                                try {
                                    Document documentDetail = Jsoup.connect("http://122.205.143.141:9999/article/index?id="+articles.getJSONObject(j).getString("id"))
                                            .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 SLBrowser/8.0.0.3161 SLBChan/30")
                                            .ignoreContentType(true)
                                            .get();
                                    Element bodyDetail = documentDetail.body();
                                    Log.d(TAG, "run: 公告解析"+bodyDetail);
                                    JSONObject jsonDataDetail = new JSONObject(bodyDetail.text());
                                    Log.d(TAG, "run: 公告解析"+jsonDataDetail);
                                    announcementId = "<!DOCTYPE html>\n" +
                                            "<html lang=\"en\">\n" +
                                            "<head>\n" +
                                            "    <meta charset=\"UTF-8\">\n" +
                                            "    <title>公告详情</title>\n" +
                                            "</head>\n" +
                                            "<body>\n" +jsonDataDetail.getJSONObject("data").getString("content")+
                                            "</body>\n" +
                                            "</html>"
                                            ;
                                    Log.d(TAG, "announcementId: 搜索解析"+announcementId);
                                }catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            Log.d(TAG,"announcementId " + announcementId);
                            String announcementTitle = articles.getJSONObject(j).getString("title");
                            Log.d(TAG,"announcementTitle " + announcementTitle);
                            String announcementCreatetime = articles.getJSONObject(j).getString("createdAt");
                            Log.d(TAG,"announcementCreatetime " + announcementCreatetime);
                            AnnouncementBean announcement = new AnnouncementBean();
                            announcement.setAnnouncementId(announcementId);
                            announcement.setAnnouncementTitle(announcementTitle);
                            announcement.setAnnouncementCreatetime(announcementCreatetime);
                            announcementBeanList.add(announcement);
                        }
                    }

//                    Elements elementsName = element.getElementsByAttributeValue("style"," float: left; width:75%;");
//                    Log.d(TAG,"elementsName " + elementsName + elementsName.size());
//                    Elements elementsTime = element.getElementsByAttributeValue("style","float: left");
//                    Log.d(TAG,"elementsTime " + elementsTime + elementsTime.size());
//                    for(int i = 0 ; i < elementsName.size(); i++) {
//                        String announcementId = "http://www.lib.wust.edu.cn/bullet/" + elementsName.get(i).select("a").attr("href");
//                        Log.d(TAG,"announcementId " + announcementId);
//                        String announcementTitle = elementsName.get(i).select("a").text();
//                        Log.d(TAG,"announcementTitle " + announcementTitle);
//                        String announcementCreatetime = elementsTime.get(i).select("div").text();
//                        Log.d(TAG,"announcementCreatetime " + announcementCreatetime);
//                        AnnouncementBean announcement = new AnnouncementBean();
//                        announcement.setAnnouncementId(announcementId);
//                        announcement.setAnnouncementTitle(announcementTitle);
//                        announcement.setAnnouncementCreatetime(announcementCreatetime);
//                        announcementBeanList.add(announcement);
//                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            loadingView.cancel();
                            mLibraryNotificationAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //后台获取
    private void getNewLibGetAnnouncementList(String pageNum) {
        NewApiHelper.getLibraryAnnouncement(pageNum, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                AnnouncementData announcementData = (AnnouncementData) responseObj;
                if(announcementData.getCode().equals("10000")) {
                    announcementBeanList.addAll(announcementData.getData());
                    refresh_recyclerView();
                }else if(announcementData.getCode().equals("40002")){
                    ToastUtil.show("图书馆登录已过期，请重新登录");
                    SharePreferenceLab.setIsLibraryLogin(false);
                    startActivity(LibraryLoginActivity.newInstance(getContext()));
                    getActivity().finish();
                }
                else{
                    ToastUtil.show(announcementData.getMsg());
                }
//                loadingView.cancel();
            }

            @Override
            public void onFailure(Object reasonObj) {
//                loadingView.cancel();
                ToastUtil.show("请求失败，可能是网络未链接或请求超时");
            }
        });
    }

    private void show_libraryannouncementList(LibraryAnnouncementBean libraryannouncementBean) {
        announcementBeanList.addAll(libraryannouncementBean.data);
        refresh_recyclerView();
    }

    private void refresh_recyclerView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                is_load=0;
                mLibraryNotificationAdapter.notifyDataSetChanged();
            }
        });
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }




    private List<AnnouncementBean> getDatas(final int firstIndex, final int lastIndex) {
        List<AnnouncementBean> resList = new ArrayList<>();
        for (int i = firstIndex; i < lastIndex; i++) {
            if (i < announcementBeanList.size()) {
                resList.add(announcementBeanList.get(i));
            }
        }
        return resList;
    }

    private void initLoadingView() {
        loadingView = MyDialogHelper.createLoadingDialog(getActivity(),"正在获取通知...",false);
        loadingView.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!SharePreferenceLab.getIsLibraryLogin()){
            getActivity().finish();
        }
    }
}

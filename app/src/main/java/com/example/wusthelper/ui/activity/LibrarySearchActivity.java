package com.example.wusthelper.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.example.wusthelper.R;
import com.example.wusthelper.adapter.SearchBookAdapter;
import com.example.wusthelper.bean.javabean.SearchBookBean;
import com.example.wusthelper.bean.javabean.data.SearchBookData;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.utils.ThreadPoolManager;
import com.example.wusthelper.utils.ToastUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LibrarySearchActivity extends AppCompatActivity {

    private Toolbar librarySearchToolbar;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private List<SearchBookBean> searchBookBeanList = new ArrayList<>();
    private static final String TAG = "LibrarySearchActivity";
    private SearchBookAdapter searchBookAdapter;
    private RecyclerView libraryRecyclerView;
    private SmartRefreshLayout smartRefreshLayout;
    private int page;
    private int totalPage;
    //是否有更多
    private boolean isMore = true;
    private String queryContent;
    private AlertDialog dialog;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        setContentView(R.layout.activity_library_search);

        librarySearchToolbar = (Toolbar)findViewById(R.id.tb_library_search);
        libraryRecyclerView = (RecyclerView)findViewById(R.id.rv_library_search);
        smartRefreshLayout = (SmartRefreshLayout)findViewById(R.id.sr_search);
        dialog = loadingDialog("正在查找图书", false);
        libraryRecyclerView.setLayoutManager(new LinearLayoutManager(LibrarySearchActivity.this));
        searchBookAdapter = new SearchBookAdapter(LibrarySearchActivity.this, searchBookBeanList);
        libraryRecyclerView.setAdapter(searchBookAdapter);
        searchBookAdapter.setOnItemClickListener(new SearchBookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, searchBookBeanList.get(position).getDetailUrl()+ searchBookBeanList.get(position).getBookName()+searchBookBeanList.get(position).getAuthor());
                Intent intent = BookDetailActivity.newInstance(LibrarySearchActivity.this, searchBookBeanList.get(position).getDetailUrl(), searchBookBeanList.get(position).getBookName(),searchBookBeanList.get(position).getAuthor());
                startActivity(intent);
            }
        });

        setSupportActionBar(librarySearchToolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        librarySearchToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        smartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

                if (isMore && page<totalPage) {

                    page++;
//                    newSearchBook(queryContent, page);
                    searchBook(queryContent, page);

                } else {

                    smartRefreshLayout.finishLoadMore(500);

                }

            }
        });
    }

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, LibrarySearchActivity.class);
        return intent;
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_library_search, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);

        searchView.setQueryHint("输入书本信息查找");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setIconified(true);
                Log.e(TAG,"search");

                page = 1;
                dialog.show();
                searchBookBeanList.clear();
                queryContent = query;
                searchBook(query, page);
//                page++;
//                newSearchBook(query, page);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        mSearchAutoComplete.setHintTextColor(getResources().getColor(R.color.color_alpha_white));
        mSearchAutoComplete.setTextColor(getResources().getColor(R.color.color_white));
        searchView.setIconifiedByDefault(true);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        return super.onCreateOptionsMenu(menu);

    }

//    public void searchBook(String bookName, final int page) {
//        //?是分隔符,&是参数之间的分隔符
//        //final String queryUrl = "http://122.205.143.145:8780/opac/openlink.php?strSearchType=title&match_flag=forward&historyCount=1&strText=%E9%BE%99%E6%97%8F&doctype=ALL&with_ebook=on&displaypg=20&showmode=list&sort=CATA_DATE&orderby=desc&location=ALL&csrf_token=rVhSRUayJ5";
//        final String queryUrl = "https://libsys.wust.edu.cn/meta-local/opac/search/";
//        ThreadPoolManager.getInstance().addExecuteTask(new Runnable() {
//            String json = "{\"page\":" + page +",\"pageSize\":20,\"indexName\":\"idx.opac\",\"sortField\":\"relevance\",\"sortType\":\"desc\",\"collapseField\":\"groupId\",\"queryFieldList\":[{\"logic\":0,\"field\":\"all\",\"values\":[\" "+ bookName + "\"],\"operator\":\"*\"}],\"filterFieldList\":[]}";
//            @Override
//            public void run() {
//                try {
//
//                    Document document = Jsoup.connect(queryUrl)
//                            .ignoreContentType(true)
//                            .header("Content-Type", "application/json; charset=UTF-8")
//                            .requestBody(json)
//                            .post();
//                    Log.d(TAG, "run: 搜索解析"+document);
//                    Element body = document.body();
//                    JSONObject jsonData = new JSONObject(body.text());
//                    Log.d(TAG, "jsonData: 搜索解析"+jsonData);
//                    JSONObject data = jsonData.getJSONObject("data");
//                    if(page == 1) {
//                        if(data.getInt("total") == 0) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    ToastUtil.show("没有符合条件的书！！");
////                                Toast.makeText(LibrarySearchActivity.this, "没有符合条件的书！！", Toast.LENGTH_SHORT).show();
//                                    Log.e(TAG,"没有找到书");
//                                    dialog.cancel();
//                                }
//                            });
//                            return;
//                        } else {
//                            totalPage = data.getInt("total")/data.getInt("limit");
//                            Log.e(TAG,"totalPage = " +totalPage);
//                        }
//                    }
//
//                    Log.d(TAG, "data: 搜索解析"+data);
//                    JSONArray dataList = data.getJSONArray("dataList");
//                    Log.d(TAG, "data: 搜索解析"+dataList);
////                    JSONObject dataList1 = dataList.getJSONObject(0);
////                    Log.d(TAG, "dataList1："+dataList1);
////                    String bibId = dataList1.getString("bibId");
////                    Log.e(TAG, "bibId："+bibId);
//
//
//                    for (int i = 0 ; i < dataList.length(); i++){
//
//                        SearchBookBean searchBookBean = new SearchBookBean();
//                        String bookName = dataList.getJSONObject(i).getString("title");
//                        Log.d(TAG, "bookName = " + bookName);
//                        String detailUrl = dataList.getJSONObject(i).getString("bibId");
//                        searchBookBean.setBookName(bookName);
//                        detailUrl = "https://libsys.wust.edu.cn/meta-local/opac/bibs/" + detailUrl;
//                        searchBookBean.setDetailUrl(detailUrl);
//                        String author = dataList.getJSONObject(i).getString("author");
////                            Log.d(TAG, author);
//                        searchBookBean.setAuthor(author);
//                        String press = dataList.getJSONObject(i).getString("publisher");
////                            Log.d(TAG, press);
//                        searchBookBean.setPress(press);
//                        int sum = dataList.getJSONObject(i).getInt("itemCount");
////                            Log.d(TAG, sum);
//                        searchBookBean.setSum(Integer.toString(sum));
//
//                        int borrowableNumBeginIndex = dataList.getJSONObject(i).getInt("circCount");
//                        searchBookBean.setBorrowableNum(Integer.toString(borrowableNumBeginIndex));
//                        String imageUrl;
//                        try {
//                            imageUrl = dataList.getJSONObject(i).getString("isbn");
//                        }catch (Exception exception) {
//                            imageUrl = "";
//                        }
//                        Log.d(TAG,"imageUrl =" + imageUrl);
//                        imageUrl = "https://libsys.wust.edu.cn/meta-local/opac/third_api/douban/" +imageUrl +"/info";
//                        searchBookBean.setImageUrl(imageUrl);
//                        searchBookBeanList.add(searchBookBean);
//
//                    }
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dialog.cancel();
//                            smartRefreshLayout.finishLoadMore();
//                            searchBookAdapter.notifyDataSetChanged();
//                        }
//                    });
////                    Log.d(TAG, "------------------------------------------------------");
//
//
//                } catch (IOException | JSONException e) {
//                    e.printStackTrace();
//                    dialog.cancel();
//                }
//
//            }
//        });
//
//    }

    public void searchBook(String bookName, final int page) {
        //?是分隔符,&是参数之间的分隔符
        //final String queryUrl = "http://122.205.143.145:8780/opac/openlink.php?strSearchType=title&match_flag=forward&historyCount=1&strText=%E9%BE%99%E6%97%8F&doctype=ALL&with_ebook=on&displaypg=20&showmode=list&sort=CATA_DATE&orderby=desc&location=ALL&csrf_token=rVhSRUayJ5";
        final String queryUrl = "https://libsys.wust.edu.cn/meta-local/opac/search/";
        ThreadPoolManager.getInstance().addExecuteTask(new Runnable() {
            String json = "{\"page\":" + page +",\"pageSize\":20,\"indexName\":\"idx.opac\",\"sortField\":\"relevance\",\"sortType\":\"desc\",\"collapseField\":\"groupId\",\"queryFieldList\":[{\"logic\":0,\"field\":\"all\",\"values\":[\" "+ bookName + "\"],\"operator\":\"*\"}],\"filterFieldList\":[]}";
            @Override
            public void run() {
                try {

                    Document document = Jsoup.connect(queryUrl)
                            .ignoreContentType(true)
                            .header("Content-Type", "application/json; charset=UTF-8")
                            .requestBody(json)
                            .post();
                    Log.d(TAG, "run: 搜索解析"+document);
                    Element body = document.body();
                    JSONObject jsonData = new JSONObject(body.text());
                    Log.d(TAG, "jsonData: 搜索解析"+jsonData);
                    JSONObject data = jsonData.getJSONObject("data");
                    if(page == 1) {
                        if(data.getInt("total") == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    searchBookAdapter.notifyDataSetChanged();
                                    ToastUtil.show("没有符合条件的书！！");
//                                Toast.makeText(LibrarySearchActivity.this, "没有符合条件的书！！", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG,"没有找到书");
                                    dialog.cancel();
                                }
                            });
                            return;
                        } else {
                            if(data.getInt("total")%data.getInt("limit") == 0){
                                totalPage = data.getInt("total")/data.getInt("limit");
                            }else {
                                totalPage = data.getInt("total")/data.getInt("limit")+1;
                            }
                            Log.e(TAG,"totalPage = " +totalPage);
                        }
                    }

                    Log.d(TAG, "data: 搜索解析"+data);
                    JSONArray dataList = data.getJSONArray("dataList");
                    Log.d(TAG, "data: 搜索解析"+dataList);
//                    JSONObject dataList1 = dataList.getJSONObject(0);
//                    Log.d(TAG, "dataList1："+dataList1);
//                    String bibId = dataList1.getString("bibId");
//                    Log.e(TAG, "bibId："+bibId);


                    for (int i = 0 ; i < dataList.length(); i++){

                        SearchBookBean searchBookBean = new SearchBookBean();
                        String bookName = dataList.getJSONObject(i).getString("title");
                        Log.d(TAG, "bookName = " + bookName);
                        String detailUrl = dataList.getJSONObject(i).getString("bibId");
                        searchBookBean.setBookName(bookName);
                        detailUrl = "https://libsys.wust.edu.cn/meta-local/opac/bibs/" + detailUrl+"/infos";
                        searchBookBean.setDetailUrl(detailUrl);
                        String author;
                        try {
                            author = dataList.getJSONObject(i).getString("author");
                        }catch (Exception e){
                            author = "暂无内容";
                        }

//                            Log.d(TAG, author);
                        searchBookBean.setAuthor(author);
                        String press = dataList.getJSONObject(i).getString("publisher");
//                            Log.d(TAG, press);
                        searchBookBean.setPress(press);
                        int sum = dataList.getJSONObject(i).getInt("itemCount");
//                            Log.d(TAG, sum);
                        searchBookBean.setSum(Integer.toString(sum));

                        int borrowableNumBeginIndex = dataList.getJSONObject(i).getInt("circCount");
                        searchBookBean.setBorrowableNum(Integer.toString(borrowableNumBeginIndex));
                        String imageUrl;
                        String bookCoverUrl;
                        try {
                            imageUrl = dataList.getJSONObject(i).getString("isbn");
                        }catch (Exception exception) {
                            imageUrl = "";
                        }
                        Log.d(TAG,"imageUrl =" + imageUrl);
                        imageUrl = "https://libsys.wust.edu.cn/meta-local/opac/third_api/douban/" +imageUrl +"/info";
                        try {
                            Document documentImage = Jsoup.connect(imageUrl)
                                    .ignoreContentType(true)
                                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 SLBrowser/8.0.0.3161 SLBChan/30")
                                    .get();
                            Element bodyImage  = documentImage.body();
                            JSONObject jsonObjectImage = new JSONObject(bodyImage.text());
                            Log.d(TAG,"jsonObjectImage" + jsonObjectImage);
                            JSONObject jsonDataImage = jsonObjectImage.getJSONObject("data");


                            bookCoverUrl = "https:"+jsonDataImage.getString("imageUrl");
                        }catch (Exception e) {
                            bookCoverUrl = "";
                        }
                        searchBookBean.setImageUrl(bookCoverUrl);
                        searchBookBeanList.add(searchBookBean);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.cancel();
                            smartRefreshLayout.finishLoadMore();
                            searchBookAdapter.notifyDataSetChanged();
                        }
                    });
//                    Log.d(TAG, "------------------------------------------------------");


                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    dialog.cancel();
                }

            }
        });

    }

    public void newSearchBook(String bookName, final int page){
        NewApiHelper.searchLibraryBook(page + "", bookName, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                SearchBookData searchBookData = (SearchBookData) responseObj;
                if(searchBookData.getCode().equals("10000")) {
                    searchBookBeanList.addAll(searchBookData.getData());
                    dialog.cancel();
                    smartRefreshLayout.finishLoadMore();
                    searchBookAdapter.notifyDataSetChanged();
                }else if(searchBookData.getCode().equals("40601")){
                    if(page == 1) {
                        ToastUtil.show("没有符合条件的书！！");
                        Log.e(TAG,"没有找到书");
                        dialog.cancel();
                    }else {
                        isMore = false;
                        smartRefreshLayout.finishLoadMore();
                        ToastUtil.show("没有更多了噢！！");
                    }
                }else if(searchBookData.getCode().equals("40002")){
                    SharePreferenceLab.setIsLibraryLogin(false);
                    ToastUtil.show(searchBookData.getMsg());
                    dialog.cancel();
                }else {
                    ToastUtil.show(searchBookData.getMsg());
                    dialog.cancel();
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                dialog.cancel();
                ToastUtil.show("请求失败，可能是网络未链接或请求超时");
            }
        });
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dialog.cancel();
    }

    private AlertDialog loadingDialog(String text, boolean cancelable){

        View view = LayoutInflater.from(LibrarySearchActivity.this).inflate(R.layout.toast_loading, null);

        AVLoadingIndicatorView avl = (AVLoadingIndicatorView) view.findViewById(R.id.avl);
        avl.show();
        TextView tv = view.findViewById(R.id.tv);
        tv.setText(text);
        AlertDialog dialog = new AlertDialog.Builder(LibrarySearchActivity.this, R.style.LoadingDialog)
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
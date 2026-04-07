package com.example.wusthelper.ui.activity;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;
import static com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.wusthelper.R;
import com.example.wusthelper.adapter.LibraryCollectionAdapter;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.bean.javabean.data.BookData;
import com.example.wusthelper.bean.javabean.LibraryCollectionBean;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.exception.OkHttpException;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.utils.NetWorkUtils;
import com.example.wusthelper.utils.ThreadPoolManager;
import com.example.wusthelper.utils.ToastUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class BookDetailActivity extends AppCompatActivity {

    private static final String BOOK_DETAIL_URL = "bookDetailUrl";
    private static final String BOOK_NAME = "bookName";
    private static final String BOOK_AUTHOR = "author";
    private static final String IS_COLLECT = "isCollect";
    private static final String BOOK_IMAGE_URL = "bookImageUrl";
    private static final String IS_HTTP_REQUEST = "isHttpRequest";
    private String bookDetailUrl;
    private String bookName;
    private String author;
    private String isCollect;
    private String publisher;
    private String introduction;
    private String imgUrl;
    private String isbn;
    private String bookImageUrl;
    private ImageView bookBackgroundImageView;
    private ImageView bookCoverImageView;
    private Toolbar bookDetailToolbar;
    private FloatingActionButton bookCollectionFloatingButton;
    private TextView nameAuthorTextView;
    private TextView pressTextView;
    private TextView isbnTextView;
    private TextView introTextView;
    private LinearLayout libraryCollectionLinearLayout;
    private HashMap<String, Object> titleMap = new HashMap<>();
    private String bookCoverUrl;
    private RecyclerView recyclerView;
    private LibraryCollectionAdapter libraryCollectionAdapter;
    private List<LibraryCollectionBean> libraryCollectionBeanList = new ArrayList<>();
    private static final String TAG = "BookDetailActivity";
    private String[] titles = {"题名/责任者", "出版发行项", "ISBN及定价", "提要文摘附注"};
    private static final String BORROW_CONDITION = "borrowCondition";
    private static final String KEY = "NewWustHelperWithAndroid2018";
    private String studentId;
    private boolean is_make_question =false;//是否在进行网络请求，默认否
    private long re_question_time=0;
    private int bookDetailId;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View view = getWindow().getDecorView();
        final int options = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        view.setSystemUiVisibility(options);
        setContentView(R.layout.activity_book_detail);
        initData();
        initView();
        // getBookDetail();
        if (NetWorkUtils.isConnected(this)){
            dialog = loadingDialog("正在加载详情", false);
            dialog.show();
            getBookDetailHttp();
//            bookDetailAsyncTask = new BookDetailAsyncTask();
//            bookDetailAsyncTask.execute();
        }else {
            ToastUtil.show("无网络");
        }
        bookDetailToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

//    public static Intent newInstance(Context context, String bookDetailUrl, String bookName, String is_collect, int bookid, String imageUrl,boolean isHttpRequest) {
//        Intent intent = new Intent(context, BookDetailActivity.class);
//        Log.d(TAG, "newInstance: "+bookDetailUrl);
//        intent.putExtra(BOOK_DETAIL_URL, bookDetailUrl);
//        intent.putExtra(BOOK_NAME, bookName);
//        intent.putExtra(IS_COLLECT, is_collect);
//        intent.putExtra(BOOKDETAILID,bookid);
//        intent.putExtra(BOOK_IMAGE_URL,imageUrl);
//        intent.putExtra(IS_HTTP_REQUEST,isHttpRequest);
//        Log.d(TAG, "newInstance: "+is_collect);
//        return intent;
//    }

    public static Intent newInstance(Context context, String bookDetailUrl, String bookName,String author) {
        Intent intent = new Intent(context, BookDetailActivity.class);
        Log.d(TAG, "newInstance: "+bookDetailUrl);
        intent.putExtra(BOOK_DETAIL_URL, bookDetailUrl);
        intent.putExtra(BOOK_NAME, bookName);
        intent.putExtra(BOOK_AUTHOR,author);
//        intent.putExtra(IS_COLLECT, is_collect);
//        intent.putExtra(BOOKDETAILID,bookid);
//        Log.d(TAG, "newInstance: "+is_collect);
        return intent;
    }

    private void initView() {
        bookDetailToolbar = (Toolbar) findViewById(R.id.tb_book_detail);
        nameAuthorTextView = (TextView) findViewById(R.id.tv_name_author);
        pressTextView = (TextView) findViewById(R.id.tv_press);
        isbnTextView = (TextView) findViewById(R.id.tv_isbn);
        introTextView = (TextView) findViewById(R.id.tv_intro);
        libraryCollectionLinearLayout = (LinearLayout) findViewById(R.id.ll_library_collection_container);
        bookCoverImageView = (ImageView) findViewById(R.id.iv_book_cover);
        bookBackgroundImageView = (ImageView) findViewById(R.id.iv_book_background);
        recyclerView = (RecyclerView) findViewById(R.id.book_location);
        recyclerView.setLayoutManager(new LinearLayoutManager(BookDetailActivity.this));
        libraryCollectionAdapter = new LibraryCollectionAdapter(libraryCollectionBeanList);
        recyclerView.setAdapter(libraryCollectionAdapter);
        bookCollectionFloatingButton = (FloatingActionButton) findViewById(R.id.fb_collect);
        if(isCollect.equals("1")){
            bookCollectionFloatingButton.setBackgroundTintList(getResources().getColorStateList(R.color.color_book_collection));
            bookCollectionFloatingButton.setSelected(true);
        }else {
            bookCollectionFloatingButton.setSelected(false);
        }

        bookCollectionFloatingButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                if(System.currentTimeMillis()>re_question_time){
                    if(!is_make_question){
                        if (!bookCollectionFloatingButton.isSelected()) {
                            is_make_question=true;
                            tryMakeRequestCollect();
                        } else {
                            is_make_question=true;
                            tryDelRequestCollect();
                        }
                    }else {
                        ToastUtil.show("正在上传数据，请稍等");
                    }
                }else {
                    ToastUtil.show("操作过于频繁，稍后重试");
                }

            }
        });



    }


    private void initData() {
        bookDetailUrl = getIntent().getStringExtra(BOOK_DETAIL_URL);
        bookImageUrl = getIntent().getStringExtra(BOOK_IMAGE_URL);
        Log.e(TAG, bookDetailUrl);
        bookName = getIntent().getStringExtra(BOOK_NAME);
        author = getIntent().getStringExtra(BOOK_AUTHOR);
        studentId = SharePreferenceLab.getInstance().getStudentId(this);
//        isCollect = getIntent().getStringExtra(IS_COLLECT);
//        bookDetailId = getIntent().getIntExtra(BOOKDETAILID,0);
        titleMap.put(titles[0], "");
        titleMap.put(titles[1], "");
        titleMap.put(titles[2], "");
        titleMap.put(titles[3], "");
        publisher = "";
        introduction = "";
        imgUrl = "";
        isbn = "";
        isCollect = "0";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (NetWorkUtils.isConnected(this)){
//            bookDetailAsyncTask.cancel(true);
        }
    }




    private boolean isExist(String[] titles, String title) {

        for (String str : titles) {

            if (str.equals(title))
                return true;

        }

        return false;

    }

    /**
     * 爬虫获取
     */
    private void getBookDetail() {
        ThreadPoolManager.getInstance().addExecuteTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(bookDetailUrl+"/infos")
                            .ignoreContentType(true)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 SLBrowser/8.0.0.3161 SLBChan/30")
                            .get();
                    Log.d(TAG,"document" + document);
                    Element body  = document.body();
                    JSONObject jsonObject = new JSONObject(body.text());
                    Log.d(TAG,"jsonObject" + jsonObject);
                    JSONObject jsonData = jsonObject.getJSONObject("data");
                    Log.d(TAG,"jsonData" + jsonData);
                    JSONObject jsonMap = jsonData.getJSONObject("map");
                    Log.d(TAG,"jsonMap" + jsonMap);
                    JSONObject detailInfo = jsonMap.getJSONObject("detailInfo");
                    Log.d(TAG,"detailInfo" + detailInfo);
                    JSONObject map = detailInfo.getJSONObject("map");
                    try {
                        titleMap.put(titles[0],map.getString(titles[0]));
                    }catch (Exception e){
                        titleMap.put(titles[0],"无详细内容");
                    }
                    try {
                        titleMap.put(titles[1],map.getString(titles[1]));
                    }catch (Exception e){
                        titleMap.put(titles[1],"无详细内容");
                    }
                    try {
                        titleMap.put(titles[2],map.getString(titles[2]));
                    }catch (Exception e){
                        titleMap.put(titles[2],"无详细内容");
                    }
                    try {
                        titleMap.put(titles[3],map.getString(titles[3]));
                    }catch (Exception e){
                        titleMap.put(titles[3],"无详细内容");
                    }




                    Log.d(TAG,titleMap.toString());
                    try {
                        Document documentImage = Jsoup.connect(bookImageUrl)
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

                    try {
                        Document documentHoldings = Jsoup.connect(bookDetailUrl+"/holdings")
                                .ignoreContentType(true)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 SLBrowser/8.0.0.3161 SLBChan/30")
                                .get();
                        Log.d(TAG,"documentHoldings" + documentHoldings);
                        JSONObject jsonObjectHoldings = new JSONObject(documentHoldings.body().text());
                        Log.d(TAG,"jsonObjectHoldings" + jsonObjectHoldings);
                        String jsonArray = jsonObjectHoldings.getJSONObject("data").getString("holdings");
                        Log.d(TAG,"jsonArray" + jsonArray);
                        JSONArray jsonArray1 = new JSONArray(jsonArray);
                        Log.d(TAG,"jsonArray1" + jsonArray1);
                        for(int i = 0; i< jsonArray1.length(); i++) {
                            LibraryCollectionBean libraryCollectionBean = new LibraryCollectionBean();
                            String bookCallNo = jsonArray1.getJSONObject(i).getString("callNo");
                            libraryCollectionBean.setCallNumber(bookCallNo);
                            String bookBarCode = jsonArray1.getJSONObject(i).getString("barCode");
                            libraryCollectionBean.setBarCode(bookBarCode);
                            String bookLocation = jsonArray1.getJSONObject(i).getString("location");
                            libraryCollectionBean.setLocation(bookLocation);
                            String bookStatus = jsonArray1.getJSONObject(i).getString("status");
                            libraryCollectionBean.setStatus(bookStatus);
                            libraryCollectionBeanList.add(libraryCollectionBean);
                            Log.d(TAG,bookCallNo +bookBarCode+ bookLocation+bookStatus);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            bookDetailToolbar.setTitle((String) titleMap.get(bookName));

                            Log.e(TAG, "onPostExecute: " + bookCoverUrl);

                            if(!bookCoverUrl.equals("")) {
                                Glide.with(BookDetailActivity.this)
                                        .load(bookCoverUrl)
                                        .into(bookCoverImageView);

                                Glide.with(BookDetailActivity.this)
                                        .load(bookCoverUrl)
                                        .apply(RequestOptions.centerCropTransform())
                                        .apply(diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
                                        .apply(bitmapTransform(new MultiTransformation<>(new CenterCrop(), new BlurTransformation(65))))
                                        .into(bookBackgroundImageView);
                            }else {
                                Glide.with(BookDetailActivity.this)
                                        .load(getResources().getDrawable(R.drawable.book_default))
                                        .into(bookCoverImageView);
                                Glide.with(BookDetailActivity.this)
                                        .load(getResources().getDrawable(R.drawable.book_default))
                                        .apply(RequestOptions.centerCropTransform())
                                        .apply(diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
                                        .apply(bitmapTransform(new MultiTransformation<>(new CenterCrop(), new BlurTransformation(30))))
                                        .into(bookBackgroundImageView);
                            }
                            if (((String) titleMap.get(titles[0])).isEmpty()) {
                                nameAuthorTextView.setText("获取不到信息");
                            } else {
                                nameAuthorTextView.setText((String) titleMap.get(titles[0]));
                            }
                            if (((String) titleMap.get(titles[1])).isEmpty()) {
                                pressTextView.setText("获取不到信息");
                            } else {
                                pressTextView.setText((String) titleMap.get(titles[1]));
                            }
                            if (((String) titleMap.get(titles[2])).isEmpty()) {
                                isbnTextView.setText("获取不到信息");
                            } else {
                                isbnTextView.setText((String) titleMap.get(titles[2]));
                            }
                            if (((String) titleMap.get(titles[3])).isEmpty()) {
                                introTextView.setText("获取不到信息");
                            } else {
                                introTextView.setText((String) titleMap.get(titles[3]));
                            }

                            libraryCollectionAdapter.notifyDataSetChanged();
                        }
                    });

                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                }

            }
        });
    }

    /**
     * 接口获取
     */
    private void getBookDetailHttp() {
        NewApiHelper.getBookDetail(bookDetailUrl, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BookData baseData = (BookData) responseObj;
                if(baseData.getCode().equals("10000")){
                    Log.d(TAG,"baseData:=" + baseData.getData().toString());
                    isCollect = baseData.getData().getIsCollection();
                    if(isCollect.equals("1")){
                        bookCollectionFloatingButton.setBackgroundTintList(getResources().getColorStateList(R.color.color_book_collection));
                        bookCollectionFloatingButton.setSelected(true);
                    }else {
                        bookCollectionFloatingButton.setSelected(false);
                    }
                    titleMap.put(titles[0],baseData.getData().getBookNameAndAuth().equals("") ? "无详细内容/无详细内容" : baseData.getData().getBookNameAndAuth());
                    titleMap.put(titles[1],baseData.getData().getPublisher().equals("") ? "无详细内容" : baseData.getData().getPublisher());
                    titleMap.put(titles[2],baseData.getData().getIsbn().equals("") ? "无详细内容" : baseData.getData().getIsbn());
                    titleMap.put(titles[3],baseData.getData().getIntroduction().equals("") ? "无详细内容" : baseData.getData().getIntroduction());
                    nameAuthorTextView.setText(baseData.getData().getBookNameAndAuth().equals("") ? "无详细内容" : baseData.getData().getBookNameAndAuth());
                    pressTextView.setText(baseData.getData().getPublisher().equals("") ? "无详细内容" : baseData.getData().getPublisher());
                    isbnTextView.setText(baseData.getData().getIsbn().equals("") ? "无详细内容" : baseData.getData().getIsbn());
                    introTextView.setText(baseData.getData().getIntroduction().equals("") ? "无详细内容" : baseData.getData().getIntroduction());
                    if(!baseData.getData().getImgUrl().equals("https://")) {
                        Glide.with(BookDetailActivity.this)
                                .load(baseData.getData().getImgUrl())
                                .into(bookCoverImageView);

                        Glide.with(BookDetailActivity.this)
                                .load(baseData.getData().getImgUrl())
                                .apply(RequestOptions.centerCropTransform())
                                .apply(diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
                                .apply(bitmapTransform(new MultiTransformation<>(new CenterCrop(), new BlurTransformation(65))))
                                .into(bookBackgroundImageView);
                    }else {
                        Glide.with(BookDetailActivity.this)
                                .load(getResources().getDrawable(R.drawable.book_default))
                                .into(bookCoverImageView);
                        Glide.with(BookDetailActivity.this)
                                .load(getResources().getDrawable(R.drawable.book_default))
                                .apply(RequestOptions.centerCropTransform())
                                .apply(diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
                                .apply(bitmapTransform(new MultiTransformation<>(new CenterCrop(), new BlurTransformation(30))))
                                .into(bookBackgroundImageView);
                    }
                    for(LibraryCollectionBean libraryCollectionBean: baseData.getData().getLibraryCollectionBeanList()){
                        libraryCollectionBeanList.add(libraryCollectionBean);
                    }
                    libraryCollectionAdapter.notifyDataSetChanged();
                }else if(baseData.getCode().equals("40002")){
                    SharePreferenceLab.setIsLibraryLogin(false);
                    ToastUtil.show(baseData.getMsg());
                }else {
                    ToastUtil.show(baseData.getMsg());
                }
                dialog.cancel();
            }
            @Override
            public void onFailure(Object reasonObj) {
                dialog.cancel();
                OkHttpException e = (OkHttpException) reasonObj;
                String msg = e.getEmsg().toString();
                Log.d(TAG, "onFailure: " + msg);
                ToastUtil.show("请求失败，可能是网络未链接或请求超时");
            }
        });
    }
    /**
     * 取消收藏
     * @throws FileNotFoundException
     */
    private void tryDelRequestCollect()  {
        isbn = (String) titleMap.get(titles[2]);
        NewApiHelper.getDelCollection(isbn, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData baseData = (BaseData) responseObj;
                if(baseData.getCode().equals("10000")) {
                    is_make_question=false;
                    re_question_time=System.currentTimeMillis()+5000;
                    ToastUtil.show(baseData.getMsg());
                    bookCollectionFloatingButton.setBackgroundTintList(getResources().getColorStateList(R.color.color_book_no_collection));
                    bookCollectionFloatingButton.setSelected(false);
                }else{
                    ToastUtil.show(baseData.getMsg());
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                ToastUtil.show("请求失败，可能是网络未链接或请求超时");
            }
        });
//        NewApiHelper.getDelCollection(bookDetailId, new okhttp3.Callback() {
//            @Override
//            public void onFailure(okhttp3.Call call, IOException e) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d(TAG, "run: "+"无网络连接");
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
//                String body = response.body().string();
//                Log.d(TAG, "onResponse: "+body);
//                try {
//                    JSONObject jsonObject = new JSONObject(body);
//                    int code = jsonObject.getInt("code");
//                    final String msg = jsonObject.getString("msg");
//                    if (code == 10000){
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                is_make_question=false;
//                                re_question_time=System.currentTimeMillis()+5000;
//                                ToastUtil.showToast(msg);
//                                bookCollectionFloatingButton.setBackgroundTintList(getResources().getColorStateList(R.color.color_book_no_collection));
//                                bookCollectionFloatingButton.setSelected(false);
//                            }
//                        });
//                    }else {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                ToastUtil.showToast("失败，因为"+msg);
//                            }
//                        });
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    /**
     * 添加收藏
     */
    private void tryMakeRequestCollect() {
//        String str = (String) titleMap.get(titles[0]);
//        Log.d(TAG,"titleMap.get(题名/责任者:)" + titleMap.get("题名/责任者:"));
//        String[] strs = str.split("/");
//        String author;
//        String title = strs[0];
//        if (strs.length < 2) {
//            author = "无法获取";
//        } else {
//            author = strs[1];
//        }
        String publisher = (String) titleMap.get(titles[1]);
        isbn = (String) titleMap.get(titles[2]);
        NewApiHelper.getLibMakeAddCollection(bookName,isbn, author, publisher, bookDetailUrl, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData baseData = (BaseData) responseObj;
                if(baseData.getCode().equals("10000")) {
                    ToastUtil.show(baseData.getMsg());
                    is_make_question=false;
                    re_question_time=System.currentTimeMillis()+5000;
                    bookCollectionFloatingButton.setBackgroundTintList(getResources().getColorStateList(R.color.color_book_collection));
                    bookCollectionFloatingButton.setSelected(true);
                }else{
                    ToastUtil.show(baseData.getMsg());
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                ToastUtil.show("请求失败，可能是网络未链接或请求超时");
            }
        });

    }


    private AlertDialog loadingDialog(String text, boolean cancelable){

        View view = LayoutInflater.from(BookDetailActivity.this).inflate(R.layout.toast_loading, null);

        AVLoadingIndicatorView avl = (AVLoadingIndicatorView) view.findViewById(R.id.avl);
        avl.show();
        TextView tv = view.findViewById(R.id.tv);
        tv.setText(text);
        AlertDialog dialog = new AlertDialog.Builder(BookDetailActivity.this, R.style.LoadingDialog)
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
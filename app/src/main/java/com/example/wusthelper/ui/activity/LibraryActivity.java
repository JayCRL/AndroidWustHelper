package com.example.wusthelper.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.wusthelper.R;
import com.example.wusthelper.adapter.LibraryViewPagerAdapter;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityLibraryBinding;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.ui.dialog.LoadDialog;
import com.example.wusthelper.ui.fragment.libraryviewpager.LibraryCollectionFragment;
import com.example.wusthelper.ui.fragment.libraryviewpager.LibraryHistoryFragment;
import com.example.wusthelper.ui.fragment.libraryviewpager.LibraryNotificationFragment;
import com.example.wusthelper.ui.fragment.libraryviewpager.LibraryTrainNotificationFragment;
import com.example.wusthelper.ui.fragment.libraryviewpager.NewLibraryHistoryFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class LibraryActivity extends BaseActivity<ActivityLibraryBinding> {

    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private Toolbar mLibraryToolbar;
    private TabLayout mLibraryTabLayout;
    private ViewPager mLibraryViewPager;
    private List<Fragment> mFragmentList = new ArrayList<>();
    private List<String> mTitleList = new ArrayList<>();
    private LibraryViewPagerAdapter mLibraryViewPagerAdapter;
    private String TAG = "LibraryActivity";
    private LoadDialog loadDialog;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        mFragmentList.add(new NewLibraryHistoryFragment());

//        mFragmentList.add(new LibraryHistoryFragment());
//        mFragmentList.add(new LibraryTrainNotificationFragment());
        mFragmentList.add(new LibraryCollectionFragment());
        mFragmentList.add(new LibraryNotificationFragment());
//        mTitleList.add("收藏的图书");
//        mTitleList.add("借阅图书");
        mTitleList.add("图书查询");
        mTitleList.add("我的收藏");
        mTitleList.add("公告馆讯");


//        mTitleList.add("试用动态");


//        mLibraryTabLayout.addTab(mLibraryTabLayout.newTab());
//        mLibraryTabLayout.addTab(mLibraryTabLayout.newTab());
        mLibraryTabLayout.addTab(mLibraryTabLayout.newTab());
        mLibraryTabLayout.addTab(mLibraryTabLayout.newTab());
        mLibraryTabLayout.addTab(mLibraryTabLayout.newTab());

        mLibraryTabLayout.setupWithViewPager(mLibraryViewPager, false);
        mLibraryViewPagerAdapter = new LibraryViewPagerAdapter(getSupportFragmentManager(), mTitleList, mFragmentList);
        mLibraryViewPager.setAdapter(mLibraryViewPagerAdapter);
        mLibraryTabLayout.setupWithViewPager(mLibraryViewPager);
//        mLibraryTabLayout.setTabsFromPagerAdapter(mLibraryViewPagerAdapter);
//        mLibraryViewPager.setOffscreenPageLimit(2);
        setSupportActionBar(mLibraryToolbar);
        mLibraryToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }


    @Override
    public void initView() {
        mLibraryToolbar = (Toolbar)findViewById(R.id.toolbar);
        mLibraryTabLayout = (TabLayout)findViewById(R.id.tl_library);
        mLibraryViewPager = (ViewPager)findViewById(R.id.vp_library);
        loadDialog = new LoadDialog(this);
    }

    public static Intent newInstance(Context context) {
        return new Intent(context, LibraryActivity.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
//                Log.d(TAG, "点击了搜索");
                Intent intent = LibrarySearchActivity.newInstance(LibraryActivity.this);
                startActivity(intent);
                overridePendingTransition(R.anim.library_search_down_in, R.anim.library_search_down_out);
                //startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_library, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!SharePreferenceLab.getIsLibraryLogin()){
            startActivity(LibraryLoginActivity.newInstance(LibraryActivity.this));
            finish();
        }
    }
}
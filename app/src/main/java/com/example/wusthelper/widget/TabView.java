package com.example.wusthelper.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.wusthelper.R;
import com.example.wusthelper.helper.SharePreferenceLab;

import java.util.ArrayList;
import java.util.List;

public class TabView extends View {

    // 实际的宽度
    private float mWidth;
    // 实际的高度
    private float mHeight;

    // 没课的状态，为浅灰色
    private Paint mDefaultPaint;
    // 有课的状态，颜色可以自定义；
    private Paint mColorPaint;

    //这里的 +1 操作是为了给最右边留一份item
    private  float item;

    //使用二维数组记录哪些点需要画绿色
    private int[][] point;

    //行数
    private static final int LINE = 6;
    //列数
    private static final int COLUMN = 7;

    //均分的份数
    private static final int AVERAGE = 5;
    //左边空白区的份数(必须小于均分份数)  其余的份数全部用来画圆
    private static final int BLANK = 1;
    private String TAG = "TabView";

    public TabView(Context context) {
        super(context);
        init();
    }

    public TabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TabView(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TabView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initPaint();
        //Log.e(TAG, "onDraw: " );
        //此方法为本View的核心
        drawOval(canvas);

    }

    /**
     * 此方法用来实现画圆
     * 原理比较简单   就是两层for循环  判断如果有课 则画绿色  反之  画黑色
     * 比较麻烦的是坐标计算   这里放一个关于RectF的讲解网站  https://www.jianshu.com/p/af1cbba4645a
     * 如果此网站失效   记得查找到新的RectF的讲解网站放入
     * 基本思想是将宽度均分   然后分别算每一个点的四个坐标
     * @param canvas
     */
    private void drawOval(Canvas canvas) {
     //   Log.e(TAG, "drawOval: " );
        for(int i = 0 ;i<LINE ;i++){
            float top = (i * AVERAGE + BLANK) * item;
            float bottom = (i + 1) * AVERAGE * item;
            for(int j = 0 ;j<COLUMN;j++){
                float left = (j * AVERAGE + BLANK) * item;
                float right = (j +1) * AVERAGE * item;
                @SuppressLint("DrawAllocation")
                RectF rectF = new RectF(left,top,right,bottom);
                if(point[i][j] == 1){
                    //画实心绿色圆
               //     Log.e(TAG, "drawOval:绿色" );
                    canvas.drawOval(rectF,mColorPaint);
                }else{
                    //画实心黑色圆
                //    Log.e(TAG, "drawOval: 黑色" );
                    canvas.drawOval(rectF,mDefaultPaint);
                }
            }
        }
    }

    private void init() {
      //  Log.e(TAG, "init: " );
        mDefaultPaint = new Paint();
        mColorPaint = new Paint();
        point = new int[LINE][COLUMN];
    }

    /**
     * 设置数据
     * @param colorPoint
     */
    public void setColorPoint(List<Integer> colorPoint) {
        /**
         * 0 6  12 18 24 30 36
         * 1 7  13 19 25 31 37
         * 2 8  14 20 26 32 38
         * 3 9  15 21 27 33 39
         * 4 10 16 22 28 34 40
         * 5 11 17 23 29 35 41
         * 对应的二维数组
         */
    //    Log.d(TAG, "setColorPoint: "+colorPoint);
        for(Integer c : colorPoint){
            if(SharePreferenceLab.getIsChooseSundayFirst()){
                c = (c + 6)%42;
            }
            //防止传进来的数据太大  导致数组越界
            if(c > LINE * COLUMN - 1)
                continue;
            point[c%LINE][c/LINE] = 1;
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        item = mWidth / (COLUMN * AVERAGE + 1);
      //  Log.e(TAG, "onSizeChanged: "+mWidth);
    }

    /**
     * 初始化画笔
     */
    public void initPaint() {
        //Log.e(TAG, "initPaint: " );
        mDefaultPaint.setColor(getResources().getColor(R.color.colorTabViewDefault));
        mColorPaint.setColor(getResources().getColor(R.color.colorBlock));
        mColorPaint.setStyle(Paint.Style.FILL);
        mDefaultPaint.setStyle(Paint.Style.FILL);

    }
}
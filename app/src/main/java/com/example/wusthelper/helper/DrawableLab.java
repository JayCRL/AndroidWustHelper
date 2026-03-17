package com.example.wusthelper.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.wusthelper.R;

import java.util.HashMap;

public class DrawableLab {

    private static DrawableLab mDrawableLab;
    private static HashMap<String, Integer> drawableDictionary = new HashMap<>();
    private static int drawableIndex;
    private static Drawable[]drawables = new Drawable[20];
    private static int[] drawabId = new int[20];
    private static final String TAG = "DrawableLab";

    public static synchronized DrawableLab getInstance(Context context) {
        if (mDrawableLab == null) return new DrawableLab(context);
        return mDrawableLab;
    }

    @SuppressLint("NewApi")
    private DrawableLab(Context context) {
        drawables[0] = context.getDrawable(R.drawable.layer_color_class_1);
        drawables[1] = context.getDrawable(R.drawable.layer_color_class_2);
        drawables[2] = context.getDrawable(R.drawable.layer_color_class_3);
        drawables[3] = context.getDrawable(R.drawable.layer_color_class_4);
        drawables[4] = context.getDrawable(R.drawable.layer_color_class_5);
        drawables[5] = context.getDrawable(R.drawable.layer_color_class_6);
        drawables[6] = context.getDrawable(R.drawable.layer_color_class_7);
        drawables[7] = context.getDrawable(R.drawable.layer_color_class_8);
        drawables[8] = context.getDrawable(R.drawable.layer_color_class_9);
        drawables[9] = context.getDrawable(R.drawable.layer_color_class_10);

        drawables[10] = context.getDrawable(R.drawable.bg_round_class1);
        drawables[11] = context.getDrawable(R.drawable.bg_round_class2);
        drawables[12] = context.getDrawable(R.drawable.bg_round_class3);
        drawables[13] = context.getDrawable(R.drawable.bg_round_class4);
        drawables[14] = context.getDrawable(R.drawable.bg_round_class5);
        drawables[15] = context.getDrawable(R.drawable.bg_round_class6);
        drawables[16] = context.getDrawable(R.drawable.bg_round_class7);
        drawables[17] = context.getDrawable(R.drawable.bg_round_class8);
        drawables[18] = context.getDrawable(R.drawable.bg_round_class9);
        drawables[19] = context.getDrawable(R.drawable.bg_round_class10);

    }

    public Drawable getDrawable(int drawableIndex) {
        return drawables[drawableIndex];
    }

    public static int getResourceID(int drawableIndex){

        drawabId[0] = R.drawable.layer_color_class_1;
        drawabId[1] = R.drawable.layer_color_class_2;
        drawabId[2] = R.drawable.layer_color_class_3;
        drawabId[3] = R.drawable.layer_color_class_4;
        drawabId[4] = R.drawable.layer_color_class_5;
        drawabId[5] = R.drawable.layer_color_class_6;
        drawabId[6] = R.drawable.layer_color_class_7;
        drawabId[7] = R.drawable.layer_color_class_8;
        drawabId[8] = R.drawable.layer_color_class_9;
        drawabId[9] = R.drawable.layer_color_class_10;

        drawabId[10] = R.drawable.bg_round_class1;
        drawabId[11] = R.drawable.bg_round_class2;
        drawabId[12] = R.drawable.bg_round_class3;
        drawabId[13] = R.drawable.bg_round_class4;
        drawabId[14] = R.drawable.bg_round_class5;
        drawabId[15] = R.drawable.bg_round_class6;
        drawabId[16] = R.drawable.bg_round_class7;
        drawabId[17] = R.drawable.bg_round_class8;
        drawabId[18] = R.drawable.bg_round_class9;
        drawabId[19] = R.drawable.bg_round_class10;


        return drawabId[drawableIndex];
    }

    public static int getDrawableIndex(String name) {

        if (drawableDictionary.containsKey(name)) {
            return drawableDictionary.get(name);
        } else {
            int temp = drawableIndex;
            drawableDictionary.put(name, temp);
            drawableIndex = (drawableIndex + 1) % 10;
            Log.d(TAG, "" + drawableIndex);
            return temp;
        }

    }

}

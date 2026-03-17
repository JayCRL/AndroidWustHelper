package com.example.wusthelper.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * 描述：
 * SharedPreferences 存储的工具类，封装了一些对于 SharedPreferences的操作方法
 * 全局使用需要在Application 里面进行初始化
 * */
public class SPTool {
    private static SharedPreferences sp;


    private SPTool() {
    }

    public static void init(Context context, String SpaceName) {
        sp = context.getSharedPreferences(SpaceName, Context.MODE_PRIVATE);
    }

    public static boolean contains(String key) {
        return sp.contains(key);
    }


    public static void remove(String key) {
        sp.edit().remove(key).apply();
    }


    public static void clear() {
        sp.edit().clear().apply();
    }

    public static void put(String key, String value) {
        if (value == null) {
            sp.edit().remove(key).apply();
        } else {
            sp.edit().putString(key, value).apply();
        }
    }

    public static void put(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public static void put(String key, float value) {
        sp.edit().putFloat(key, value).apply();
    }

    public static void put(String key, long value) {
        sp.edit().putLong(key, value).apply();
    }

    public static void put(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public static void put(String key, String[] value) {
        SharedPreferences.Editor mEditor = sp.edit();
        mEditor.putInt(key + "_size", value.length);
        for (int i = 0; i < value.length; i++) {
            mEditor.remove(key + "_" + i);
            mEditor.putString(key + "_" + i, value[i]);
        }
        mEditor.apply();
    }

    public static void put(String key, Set<String> value) {
        sp.edit().putStringSet(key, value).apply();
    }

    public static String get(String key) {
        return sp.getString(key, "");
    }

    public static String get(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public static boolean get(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public static float get(String key, float defValue) {
        return sp.getFloat(key, defValue);
    }

    public static int get(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public static long get(String key, long defValue) {
        return sp.getLong(key, defValue);
    }


    public static Set<String> get(String key, Set<String> defValue) {
        return sp.getStringSet(key, defValue);
    }

    public static String[] getArray(String key, String[] defValue) {
        int size = sp.getInt(key + "_size", 0);
        String[] value = new String[size];
        for (int i = 0; i < size; i++) {
            value[i] = sp.getString(key + "_" + i, null);
        }
        return value;
    }

}
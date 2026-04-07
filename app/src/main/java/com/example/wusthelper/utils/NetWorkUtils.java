package com.example.wusthelper.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * @author: Gong Yunhao
 * @version: V1.0
 * @date: 2018/9/30
 * @github https://github.com/Roman-Gong
 * @blog https://www.jianshu.com/u/52a8fa1f29fb
 */
public class NetWorkUtils {

    /**
     * 判断网络是否连接
     */
    public static boolean isConnected(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) {
                return false;
            }
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            if (caps == null) {
                return false;
            }
            // 仅判断是否具备联网能力（不强依赖 VALIDATED，避免部分校园网/认证网关误判）
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * 判断是否是wifi连接
     */
    public static boolean isWifi(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) {
                return false;
            }
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            return caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }

        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 打开网络设置界面
     */
    public static void openSetting(Activity activity) {

        Intent intent;
        if (android.os.Build.VERSION.SDK_INT > 10) {
            intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        } else {
            intent = new Intent();
            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
            intent.setComponent(component);
            intent.setAction("android.intent.action.VIEW");
        }
        activity.startActivity(intent);
    }

}

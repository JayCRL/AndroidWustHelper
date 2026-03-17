package com.example.wusthelper.request.okhttp;

import android.util.Log;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.request.TokenInterceptor;
import com.example.wusthelper.request.okhttp.cookie.SimpleCookieJar;
import com.example.wusthelper.request.okhttp.https.HttpsUtils;
import com.example.wusthelper.request.okhttp.listener.DisposeDataHandle;
import com.example.wusthelper.request.okhttp.response.CommonFileCallback;
import com.example.wusthelper.request.okhttp.response.CommonJsonCallback;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Dns;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author qndroid
 * @function 用来发送get, post请求的工具类，包括设置一些请求的共用参数
 */
public class CommonOkHttpClient {
    private static final int TIME_OUT = 25;
    private static OkHttpClient mOkHttpClient;

    static {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        /**
         *  为所有请求添加请求头，看个人需求
         */
        okHttpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request =
                        chain.request().newBuilder().addHeader("Platform", "android") // 标明发送本次请求的客户端
                                .build();
                return chain.proceed(request);
            }
        });
        okHttpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Log.d("HTTP", "-> " + request.method() + " " + request.url());
                try {
                    Response response = chain.proceed(request);
                    Log.d("HTTP", "<- " + response.code() + " " + request.url());
                    return response;
                } catch (Exception e) {
                    Log.e("HTTP", "xx " + request.url() + " " + e.getMessage(), e);
                    throw e;
                }
            }
        });

        /**
         * 为请求添加token拦截器
         */
        okHttpClientBuilder.addInterceptor(new TokenInterceptor());
        okHttpClientBuilder.cookieJar(new SimpleCookieJar());
        // 禁用系统代理，避免被本机/抓包代理端口拖死（参考 test_server_student.py）
        okHttpClientBuilder.proxy(Proxy.NO_PROXY);
        // 强制使用 IPv4（部分校园网/IPv6 环境下访问外网/特定域名会卡住）
        okHttpClientBuilder.dns(new Dns() {
            @Override
            public List<InetAddress> lookup(String hostname) throws UnknownHostException {
                List<InetAddress> addresses = Dns.SYSTEM.lookup(hostname);
                Log.d("DNS", hostname + " -> " + addresses);
                List<InetAddress> v4 = new ArrayList<>();
                for (InetAddress address : addresses) {
                    if (address instanceof Inet4Address) {
                        v4.add(address);
                    }
                }
                if (!v4.isEmpty()) {
                    Log.d("DNS", hostname + " use IPv4 -> " + v4);
                }
                return v4.isEmpty() ? addresses : v4;
            }
        });
        okHttpClientBuilder.connectTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpClientBuilder.readTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpClientBuilder.writeTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpClientBuilder.followRedirects(true);
        /**
         * trust all the https point
         */
        okHttpClientBuilder.sslSocketFactory(HttpsUtils.initSSLSocketFactory(),
                HttpsUtils.initTrustManager());
        mOkHttpClient = okHttpClientBuilder
                .cache(new Cache(new File(MyApplication.getContext().getExternalCacheDir(),"cache"),1024*1024*10))//接收两个参数，1：私有缓存目录，2：缓存空间大小
                .build();
    }

    public static OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }


    /**
     * 通过构造好的Request,Callback去发送请求
     */
    public static Call get(Request request, DisposeDataHandle handle) {
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(handle));
        return call;
    }

    public static Call post(Request request, DisposeDataHandle handle) {
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(handle));
        return call;
    }

    public static Response postExecute(Request request, DisposeDataHandle handle) throws IOException {
        return mOkHttpClient.newCall(request).execute();
    }

    public static Call downloadFile(Request request, DisposeDataHandle handle) {
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new CommonFileCallback(handle));
        return call;
    }
}
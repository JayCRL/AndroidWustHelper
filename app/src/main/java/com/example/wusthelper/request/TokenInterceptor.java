package com.example.wusthelper.request;

import android.text.TextUtils;
import android.util.Log;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.helper.SharePreferenceLab;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * token拦截器
 * 每次网络请求，都会拦截通过token进行拦截
 * 如果过期
 * 就会重新请求一个新的token,然后再以新的token进行请求
 */
public class TokenInterceptor implements Interceptor {
    public static final int CODE_JWC_CHANGE_PWD_ERR = 30001;
    public static final int CODE_JWC_FIN_INFO_ERR = 30101;
    public static final int CODE_JWC_MOD_DEF_PWD = 30102;
    public static final int CODE_JWC_ERR_INFO_ERR = 30103;
    public static final int CODE_YJS_FIN_INFO_ERR = 70002;
    public static final int CODE_YJS_MOD_DEF_PWD = 70003;
    public static final int CODE_YJS_ERR_INFO_ERR = 70005;

    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int BODY_UNAUTHORIZED = 401;

    final String TAG = "TokenTimeoutIntercept: ";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // 登录接口本身不做刷新，避免递归/死循环
        if (isLoginRequest(request)) {
            return chain.proceed(request);
        }

        Response response = chain.proceed(request);
        if (isUnauthorized(response)) {
            Log.d(TAG, "token invalid, try refresh");
            String oldToken = RequestCenter.getToken();
            boolean refreshed = false;

            synchronized (TokenInterceptor.class) {
                // 如果当前 token 已经被其他线程刷新过，则直接使用新 token 重试
                String currentToken = SharePreferenceLab.getInstance().getToken(MyApplication.getContext());
                if (currentToken != null && !currentToken.equals(oldToken) && !currentToken.trim().isEmpty()) {
                    refreshed = true;
                } else {
                    refreshed = refreshToken();
                }
            }

            if (refreshed) {
                String token = SharePreferenceLab.getInstance().getToken(MyApplication.getContext());
                RequestCenter.setToken(token);

                Request newRequest = request.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .header("Token", token)
                        .build();

                response.close();
                return chain.proceed(newRequest);
            }
        }
        return response;
    }

    private boolean isLoginRequest(Request request) {
        String url = request.url().toString();
        return url.contains("/login") || url.contains("/combine-login");
    }

    private boolean isUnauthorized(Response response) {
        if (response.code() == HTTP_UNAUTHORIZED) {
            return true;
        }
        if (response.body() == null) {
            return false;
        }
        try {
            String body = response.peekBody(1024 * 1024).string();
            JSONObject jsonObject = new JSONObject(body);
            int code = jsonObject.optInt("code", 0);
            String msg = getUnauthorizedMessage(jsonObject);
            if (code == BODY_UNAUTHORIZED) {
                return true;
            }
            return isUnauthorizedMessage(msg);
        } catch (Exception ignore) {
            return false;
        }
    }

    private String getUnauthorizedMessage(JSONObject jsonObject) {
        String msg = jsonObject.optString("msg", "");
        if (TextUtils.isEmpty(msg)) {
            msg = jsonObject.optString("message", "");
        }
        if (TextUtils.isEmpty(msg)) {
            msg = jsonObject.optString("data", "");
        }
        return msg;
    }

    private boolean isUnauthorizedMessage(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return false;
        }
        return msg.contains("教务系统会话失效，请重新登录")
                || msg.contains("教务系统会话失效")
                || msg.contains("会话失效，请重新登录")
                || msg.contains("会话失效")
                || msg.contains("cookie失效")
                || msg.contains("登录已失效")
                || msg.contains("请先登录")
                || msg.contains("未登录");
    }

    private boolean refreshToken() {
        String studentId = SharePreferenceLab.getInstance().getStudentId(MyApplication.getContext());
        String psd = SharePreferenceLab.getInstance().getPassword(MyApplication.getContext());
        Log.d(TAG, "studentId ：" + studentId);

        if (studentId == null || studentId.trim().isEmpty() || psd == null || psd.trim().isEmpty()) {
            return false;
        }

        try {
            Response response;
            if (SharePreferenceLab.getIsGraduate()) {
                response = NewApiHelper.loginGraduate(studentId, psd);
            } else {
                response = NewApiHelper.login(studentId, psd);
            }
            String res = response.body() == null ? "" : response.body().string();
            final JSONObject jsonObject = new JSONObject(res);
            int code = jsonObject.optInt("code", 0);
            final String message = jsonObject.optString("msg", jsonObject.optString("message", ""));
            Log.d(TAG, "refreshToken code: " + code);

            if (code == CODE_JWC_CHANGE_PWD_ERR || code == CODE_JWC_FIN_INFO_ERR || code == CODE_JWC_MOD_DEF_PWD || code == CODE_JWC_ERR_INFO_ERR
                    || code == CODE_YJS_FIN_INFO_ERR || code == CODE_YJS_MOD_DEF_PWD || code == CODE_YJS_ERR_INFO_ERR) {
                return false;
            }

            if (jsonObject.has("data") && !jsonObject.isNull("data")) {
                String token = jsonObject.optString("data", "");
                if (token != null && !token.trim().isEmpty() && !"null".equalsIgnoreCase(token)) {
                    RequestCenter.setToken(token);
                    SharePreferenceLab.setToken(token);
                    SharePreferenceLab.getInstance().setMessage(MyApplication.getContext(), message);
                    return true;
                }
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "refreshToken error: " + e.getMessage(), e);
        }
        return false;
    }
}

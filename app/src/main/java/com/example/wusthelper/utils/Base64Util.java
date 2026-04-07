/*
 * @author yuandalai
 * @date 2018/11/24
 * @email yuanlai0611@gmail.com
 * @github https://github.com/yuanlai0611
 * @blog https://yuanlai0611.github.io/
 */

package com.example.wusthelper.utils;

import android.util.Base64;
import android.util.Log;

import java.util.Random;

public class Base64Util {

    private static final String TAG = "Base64Util";

    public static String encode(String studentName, String studentId, String token, String semester) {

        String content = "kjbk" + "?+/" + studentName + "?+/" + studentId + "?+/" + token + "?+/" + semester;
        Log.d(TAG, content);
        content = Base64.encodeToString(content.getBytes(), Base64.NO_WRAP);
        Log.d(TAG, content);
        StringBuilder builder = new StringBuilder(content);
        Random random = new Random();
        for (int i=7; i<10; i++)
            builder.insert(i, random.nextInt(10));
        for (int i=0; i<3; i++)
            builder.insert(i, random.nextInt(10));
        Log.d(TAG, builder.toString());
        content=builder.toString();
//        content = Base64.encodeToString(builder.toString().getBytes(), Base64.NO_WRAP);
        return content;
    }
    public static String encode(String onlyId) {

        onlyId = Base64.encodeToString(onlyId.getBytes(), Base64.NO_WRAP);
        Log.d(TAG, onlyId);
        StringBuilder builder = new StringBuilder(onlyId);
        Random random = new Random();
        for (int i=7; i<10; i++)
            builder.insert(i, random.nextInt(10));
        for (int i=0; i<3; i++)
            builder.insert(i, random.nextInt(10));
        Log.d(TAG, builder.toString());
        onlyId = Base64.encodeToString(builder.toString().getBytes(), Base64.NO_WRAP);
        return onlyId;

    }


    public static String decode(String code)  {

        try {
            code = new String(Base64.decode(code, Base64.DEFAULT));
//            Log.d(TAG, code);
            StringBuilder builder = new StringBuilder(code);
            builder.delete(0, 3);
            builder.delete(7, 10);
            code = builder.toString();
            Log.d(TAG, code);
//            return new String(Base64.decode(code, Base64.DEFAULT));
            return code;
        } catch (IllegalArgumentException e) {

            return null;
        }

    }

}

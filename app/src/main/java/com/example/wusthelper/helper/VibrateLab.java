package com.example.wusthelper.helper;

import android.os.Vibrator;

import com.example.wusthelper.MyApplication;

import static android.content.Context.VIBRATOR_SERVICE;

public class VibrateLab {

    public static void vibrate(int millisecond){
        Vibrator vibrator = (Vibrator)  MyApplication.getContext().getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(millisecond);
        }
    }


}

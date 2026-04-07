package com.example.wusthelper.utils;

import android.graphics.Color;

import java.util.Random;

public class ColorUtil {
        /**
         * 获取一个随机的rgb颜色
         * @return
         */
        public static int getRandomColor(){
            Random random = new Random();
            int red = random.nextInt(50)+100;//0-190
            int green = random.nextInt(40)+115;
            int blue = random.nextInt(35)+200;
            return Color.rgb(red, green, blue);
        }
}

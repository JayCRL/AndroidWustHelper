package com.example.wusthelper.utils;

public class NumDisposeUtil {
    public static String doubleToString(double d)
    {
        java.text.DecimalFormat myformat=new java.text.DecimalFormat("0.0");
        String str = myformat.format(d);
        return str;
    }

    public static float NumberFormatFloat(float f,int m){
        String strfloat = NumberFormat(f,m);
        return Float.parseFloat(strfloat);
    }
    public static String NumberFormat(float f,int m){
        return String.format("%."+m+"f",f);
    }
}

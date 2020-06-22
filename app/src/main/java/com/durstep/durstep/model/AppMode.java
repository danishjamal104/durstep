package com.durstep.durstep.model;

import android.content.Context;
import android.content.SharedPreferences;

public class AppMode {
    public static int ADMIN = 1;
    public static int CLIENT = 0;
    public static int DISTRIBUTOR= -1;

    private static int currentAppMode=-100;

    private static String pref_db_name = "APP_MODE";
    private static String pref_mode_key = "MODE";

    public static void updateAppMode(Context context, int appMode){
        SharedPreferences pref = context.getSharedPreferences(pref_db_name, Context.MODE_PRIVATE);
        pref.edit().putInt(pref_mode_key, appMode).apply();
    }

    private static int __getAppMode(Context context){
        return context.getSharedPreferences(pref_db_name, Context.MODE_PRIVATE).getInt(pref_mode_key, CLIENT);
    }

    public static int getAppMode(Context context){
        if(currentAppMode==-100){
            currentAppMode = __getAppMode(context);
        }
        return currentAppMode;
    }

}

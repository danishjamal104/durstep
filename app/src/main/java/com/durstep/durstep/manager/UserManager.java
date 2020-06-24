package com.durstep.durstep.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.durstep.durstep.model.User;

public class UserManager {

    private final static String DB = "USER";
    private final static String KEY_NAME = "NAME";
    private final static String KEY_NUMBER = "NUMBER";

    public static void setUser(Context context, User user){
        SharedPreferences pref = getUserPref(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_NAME, user.getName());
        editor.putString(KEY_NUMBER, user.getNumber());
        editor.apply();
    }

    public static void setNumber(Context context, String number){
        SharedPreferences pref = getUserPref(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_NUMBER, number);
        editor.apply();
    }

    public static void setName(Context context, String name){
        SharedPreferences pref = getUserPref(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    public static User getCurrentUser(Context context){
        SharedPreferences pref = getUserPref(context);
        User user = new User();
        user.setName(pref.getString(KEY_NAME, null));
        user.setNumber(pref.getString(KEY_NUMBER, null));
        return user;
    }

    public static String getName(Context context){
        SharedPreferences pref = getUserPref(context);
        return pref.getString(KEY_NAME, null);
    }

    public static String getNumber(Context context){
        SharedPreferences pref = getUserPref(context);
        return pref.getString(KEY_NUMBER, null);
    }


    private static SharedPreferences getUserPref(Context context){
        return context.getSharedPreferences(DB, Context.MODE_PRIVATE);
    }

}

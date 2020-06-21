package com.durstep.durstep.manager;


import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private final static String CURRENT_TOKEN = "CURRENT";
    private final static String NEW_TOKEN = "NEW";

    public static String getPushToken(Context context){
        return getToken(context, CURRENT_TOKEN);
    }

    public static void setNewPushToken(Context context, String token){
        setToken(context, token, NEW_TOKEN);
    }

    private static String getToken(Context context, String key){
        SharedPreferences pref = getTokenPref(context);
        return pref.getString(key, null);
    }

    private static void setToken(Context context, String token, String key){
        SharedPreferences pref = getTokenPref(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, token);
        editor.apply();
    }

    public static void handleOnLoginSignUp(Context context){
        String curr = getToken(context, TokenManager.CURRENT_TOKEN);
        String latest = getToken(context, TokenManager.NEW_TOKEN);

        if(curr!=null && latest==null){
            DbManager.updatePushToken(context);
        }else if(curr!=null && !curr.equals(latest)){
            // updated token
            refreshToken(context, latest);
            DbManager.updatePushToken(context);
        }

    }

    public static void handle(Context context){
        String curr = getToken(context, TokenManager.CURRENT_TOKEN);
        String latest = getToken(context, TokenManager.NEW_TOKEN);

        if(curr==null && latest!=null){
            // first launch
            refreshToken(context, latest);
            DbManager.updatePushToken(context);
        }else if(curr!=null && latest!=null && !curr.equals(latest)){
            // updated token
            refreshToken(context, latest);
            DbManager.updatePushToken(context);
        }

    }

    private static void refreshToken(Context context, String token){
        setToken(context, null, TokenManager.NEW_TOKEN);
        setToken(context, token, TokenManager.CURRENT_TOKEN);
    }

    private static SharedPreferences getTokenPref(Context context){
        return context.getSharedPreferences("token", Context.MODE_PRIVATE);
    }

}
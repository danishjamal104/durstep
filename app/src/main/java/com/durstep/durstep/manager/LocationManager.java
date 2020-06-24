package com.durstep.durstep.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.GeoPoint;

public class LocationManager {

    private final static String DB = "LOCATION";
    private final static String KEY_LATITUDE = "LAT";
    private final static String KEY_LONGITUDE = "LONG";

    public static void updateLocation(Context context, GeoPoint geoPoint){
        SharedPreferences pref = getUserPref(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(KEY_LATITUDE, (float)geoPoint.getLatitude());
        editor.putFloat(KEY_LONGITUDE, (float)geoPoint.getLongitude());
        editor.apply();
    }

    public static GeoPoint getLastLocation(Context context){
        SharedPreferences pref = getUserPref(context);
        double lat = pref.getFloat(KEY_LATITUDE, 0f);
        double lon = pref.getFloat(KEY_LONGITUDE, 0f);
        GeoPoint geoPoint = new GeoPoint(lat, lon);
        return geoPoint;
    }

    private static SharedPreferences getUserPref(Context context){
        return context.getSharedPreferences(DB, Context.MODE_PRIVATE);
    }


}

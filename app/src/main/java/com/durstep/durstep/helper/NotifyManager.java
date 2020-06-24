package com.durstep.durstep.helper;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.durstep.durstep.manager.UserManager;
import com.durstep.durstep.model.Subscription;
import com.durstep.durstep.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotifyManager {

    public final static String URL = "https://us-central1-durstep-7e7a8.cloudfunctions.net/notify?to=%s&title=%s&msg=%s";

    public final static String DELIVERY_START_TITLE = "Out For Delivery";
    public final static String DELIVERY_START_MSG = "%s is on his way to deliver your milk. Feel free to contact him at %s.";

    public static void send(Context context, String to, String title, String msg){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url =getUrl(to, title, msg);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Utils.log(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.log(error.getLocalizedMessage());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public static void senMultiple(Context context, List<String> to, String title, String msg){
        RequestQueue queue = Volley.newRequestQueue(context);

        for(String uId: to){
            String url = getUrl(uId, title, msg);
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Utils.log(response);
                }
            },null);
            queue.add(request);
        }
    }

    private static String getUrl(String to, String title, String msg){
        return String.format(URL, to, title, msg);
    }

    public static void sendDeliveryStartNotification(Context context, List<String> to){
        User distributor = UserManager.getCurrentUser(context);
        String title = DELIVERY_START_TITLE;
        String msg = String.format(DELIVERY_START_MSG, distributor.getName(), distributor.getNumber());
        senMultiple(context, to, title, msg);
    }

}

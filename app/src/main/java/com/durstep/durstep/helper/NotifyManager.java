package com.durstep.durstep.helper;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class NotifyManager {

    public final static String URL = "https://us-central1-durstep-7e7a8.cloudfunctions.net/notify?to=%s&title=%s&msg=%s";

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

    private static String getUrl(String to, String title, String msg){
        return String.format(URL, to, title, msg);
    }

}

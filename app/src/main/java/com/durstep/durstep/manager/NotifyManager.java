package com.durstep.durstep.manager;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.model.ActiveDelivery;
import com.durstep.durstep.model.Subscription;
import com.durstep.durstep.model.User;
import com.google.firebase.firestore.DocumentReference;
import java.util.ArrayList;
import java.util.List;

public class NotifyManager {

    public final static String DELIVERY_START_TITLE = "Out For Delivery";
    public final static String DELIVERY_START_MSG = "%s is on his way to deliver your milk. Feel free to contact him at %s.";

    public final static String DELIVERY_CONFIRM_TITLE = "Delivery Successful";
    public final static String DELIVERY_CONFIRM_MSG = "%s litre milk successfully delivered to";

    public final static String NEW_USER_REGISTER_TITLE = "New Client";
    public final static String NEW_USER_REGISTER_MSG = "You Have A New Client: %s, %s";

    public final static String NEW_SUBSCRIPTION_TITLE = "%s subscribed to new service";
    public final static String NEW_SUBSCRIPTION_MSG = "%s litres to be delivered at %s hours. For more details contact %s";

    public static void send(Context context, String url){
        RequestQueue queue = Volley.newRequestQueue(context);
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
                if(error.getLocalizedMessage()!=null){
                    Utils.log(error.getLocalizedMessage());
                }
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
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
    public static void senMultiple(Context context, List<String> to, String title, String msg, int type){
        RequestQueue queue = Volley.newRequestQueue(context);

        for(String uId: to){
            String url = getUrl(uId, title, msg);
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Utils.log(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error.getLocalizedMessage()!=null){
                        Utils.log(error.getLocalizedMessage());
                    }
                }
            });
            queue.add(request);
        }
    }

    private static String getUrl(String to, String title, String msg){
        return String.format(Utils.NOTIFICATION_URL, to, title, msg, 0);
    }
    private static String getType1Url(String to, String title, String msg, String name, String number){
        return String.format(String.format(Utils.NOTIFICATION_URL, to, title, msg, 1)+"&extra=%s, %s", name, number);
    }

    public static void sendDeliveryStartNotification(Context context, List<String> to){
        User distributor = UserManager.getCurrentUser(context);
        String msg = String.format(DELIVERY_START_MSG, distributor.getName(), distributor.getNumber());
        senMultiple(context, to, DELIVERY_START_TITLE, msg, 1);
    }
    public static void sendLocationUpdate(Context context, ActiveDelivery delivery){
        List<String> to = new ArrayList<>();
        String title = "Delivery Update";
        String msg = "Location updated. Click here to track";
        for(DocumentReference subs_ref: delivery.getSubscription_list()){
            to.add(Utils.getUserIdFromSubscriptionRef(subs_ref));
        }
        senMultiple(context, to, title, msg, 0);
    }
    public static void sendDeliveryConfirmation(Context context, String distId, double amount){
        String msg = String.format(DELIVERY_CONFIRM_MSG, amount);

        String url = getType1Url(distId, DELIVERY_CONFIRM_TITLE,
                msg, UserManager.getName(context), UserManager.getNumber(context));

        send(context, url);
    }

    public static void notifyAdmin_newUser(Context context, String name, String number){
        send(context, Utils.ADMIN_ID, NEW_USER_REGISTER_TITLE, String.format(NEW_USER_REGISTER_MSG, name, number));
    }

    public static void notifyAdmin_newSubscription(Context context, Subscription s){
        String name = UserManager.getName(context);
        String number = UserManager.getNumber(context);
        String title = String.format(NEW_SUBSCRIPTION_TITLE, name);
        String msg = String.format(NEW_SUBSCRIPTION_MSG, s.getAmount(), s.getDeliveryTime(), number);
        send(context, Utils.ADMIN_ID, title, msg);
    }

}

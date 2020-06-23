package com.durstep.durstep.model;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.durstep.durstep.adapter.SubscriptionAdapter;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.interfaces.ListItemClickListener;
import com.durstep.durstep.manager.DbManager;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class User {
    public final static int CLIENT = 0;
    public final static int ADMIN = 1;
    public final static int DISTRIBUTOR = -1;

    String name;
    String number;
    int type = 0;
    int totalOrder = 0;
    String uid;

    // below are the fields used for subscription list in expandable format
    @Exclude
    RecyclerView subscription_rv;
    @Exclude
    SubscriptionAdapter subscriptionAdapter;
    @Exclude
    Context context;

    public User() {
    }

    public User(String name, String number, int type, String uid) {
        this.name = name;
        this.number = number;
        this.type = type;
        this.uid = uid;
    }

    public User(String name, String number, int type) {
        this.name = name;
        this.number = number;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public int getTotalOrder() {
        return totalOrder;
    }

    public void setTotalOrder(int totalOrder) {
        this.totalOrder = totalOrder;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Exclude
    public boolean isAdmin(){
        return type==User.ADMIN;
    }

    @Exclude
    public void setUpSubscription(Context context, RecyclerView recyclerView){
        this.context = context;
        this.subscription_rv = recyclerView;
    }

    @Exclude
    public void loadSubscription(ListItemClickListener<Subscription, User> subscriptionListItemClickListener){
        if(subscriptionAdapter!=null){
            return;
        }

        subscription_rv.setHasFixedSize(false);
        subscription_rv.setLayoutManager(new LinearLayoutManager(context));

        subscriptionAdapter = new SubscriptionAdapter(new ArrayList<>(), context, subscriptionListItemClickListener);

        subscription_rv.setAdapter(subscriptionAdapter);

        DbManager.getSubscription(uid, new FirebaseTask<Subscription>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if(isSuccess){
                    Utils.toast(context, "No Subscription");
                }else{
                    Utils.longToast(context, error);
                }
            }
            @Override
            public void onSingleDataLoaded(Subscription object) {

            }

            @Override
            public void onMultipleDataLoaded(List<Subscription> objects) {
                subscriptionAdapter.addAll(objects);
            }
        });
    }
}

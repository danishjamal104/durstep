package com.durstep.durstep.model;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.durstep.durstep.R;
import com.durstep.durstep.adapter.SubscriptionAdapter;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.interfaces.ListItemClickListener;
import com.durstep.durstep.interfaces.MenuClickListener;
import com.durstep.durstep.manager.DbManager;
import com.google.firebase.firestore.Exclude;

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
    String push_token;

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

    public String getPush_token() {
        return push_token;
    }

    public void setPush_token(String push_token) {
        this.push_token = push_token;
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

        subscriptionAdapter = new SubscriptionAdapter(context, subscriptionListItemClickListener);

        subscription_rv.setAdapter(subscriptionAdapter);

        subscriptionAdapter.setMenuClickListener(new MenuClickListener<Subscription>() {
            @Override
            public void onMenuItemClick(int id, Subscription subscription) {
                switch (id){
                    case R.id.subs_menu_add_instruction:
                        Utils.log("Add ins");
                        break;
                    case R.id.subs_menu_track:
                        Utils.log("Track");
                        break;
                }
            }
        });

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
            public void onSingleDataLoaded(Subscription object) {}
            @Override
            public void onMultipleDataLoaded(List<Subscription> objects) {
                subscriptionAdapter.addAll(objects);
            }
        });
    }
}

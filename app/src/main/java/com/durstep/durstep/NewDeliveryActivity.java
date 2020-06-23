package com.durstep.durstep;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import com.durstep.durstep.adapter.UserAdapter;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.interfaces.ListItemClickListener;
import com.durstep.durstep.interfaces.SubscriptionLoadingTask;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.model.ActiveDelivery;
import com.durstep.durstep.model.Subscription;
import com.durstep.durstep.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class NewDeliveryActivity extends BaseActivity {

    TextInputLayout search_til;
    ChipGroup selected_subscription_cg;
    MaterialButton confirm_bt;

    SwipeRefreshLayout loadPrevious_srl;

    RecyclerView user_rv;
    UserAdapter userAdapter;


    List<Subscription> subscriptionList = new ArrayList<>();
    List<Chip> chips=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_delivery);
        init();
    }
    void init(){
        search_til = findViewById(R.id.new_delivery_search_number_name_til);
        selected_subscription_cg = findViewById(R.id.new_delivery_selected_subscription_cg);
        confirm_bt = findViewById(R.id.new_delivery_confirm_mbt);
        user_rv = findViewById(R.id.new_delivery_user_list_rv);
        loadPrevious_srl = findViewById(R.id.new_delivery_loadPrev_srl);
        setUp();
    }
    void setUp(){
        user_rv.setHasFixedSize(false);
        user_rv.setLayoutManager(new LinearLayoutManager(this));

        userAdapter  = new UserAdapter(this, new ListItemClickListener<Subscription, User>() {
            @Override
            public void onItemClicked(Subscription object1, User object2) {
                toast(""+object1.getAmount());
                log("Click sub: "+object1.getAmount());
                updateList(object1, object2);
            }
        });
        user_rv.setAdapter(userAdapter);

        search_til.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                userAdapter.applyFilter(s.toString().trim());
            }
        });

        loadPrevious_srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPreviousDelivery();
            }
        });

        confirm_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDelivery();
            }
        });

        loadUser();
    }
    void loadPreviousDelivery(){
        DbManager.getPreviousDeliveryOfDistributor(DbManager.getUid(), new SubscriptionLoadingTask() {
            @Override
            public void onSubscriptionLoaded(List<Pair<Subscription, User>> pairList) {
                for(Pair<Subscription, User> p: pairList){
                    updateList(p.first, p.second);
                }
                loadPrevious_srl.setRefreshing(false);
            }

            @Override
            public void onError(String error) {
                longToast(error);
            }
        });
    }
    void loadUser(){
        DbManager.getAllUserOfType(User.CLIENT, new FirebaseTask<User>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if(isSuccess){

                }else{

                }
            }
            @Override
            public void onSingleDataLoaded(User object) {

            }
            @Override
            public void onMultipleDataLoaded(List<User> objects) {
                userAdapter.addAll(objects);
            }
        });
    }
    void createDelivery(){
        if(subscriptionList.size()==0){
            return;
        }
        ActiveDelivery activeDelivery = new ActiveDelivery();
        activeDelivery.setSubscription_list(Utils.getSubsRefFromObject(subscriptionList));
        activeDelivery.setPending(subscriptionList.size());
        activeDelivery.setTotal(subscriptionList.size());
        activeDelivery.setDelivered(0);
        activeDelivery.setDelivered_list(new ArrayList<>());
        activeDelivery.setLocation(null);

        DbManager.createDelivery(DbManager.getUid(), activeDelivery, new FirebaseTask<Void>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if(isSuccess){
                    toast(getString(R.string.success));
                    onBackPressed();
                }else{
                    toast(error);
                }
            }
            @Override
            public void onSingleDataLoaded(Void object) {

            }
            @Override
            public void onMultipleDataLoaded(List<Void> objects) {

            }
        });

    }

    void updateList(Subscription subscription, User user){
        for(Subscription subs: subscriptionList){
            if(subs.getsId().equals(subscription.getsId())){
                return;
            }
        }
        subscriptionList.add(subscription);
        addChip(subscription, user.getName(), user.getNumber());
    }
    void addChip(Subscription subscription,String name, String number){
        String chipText = ""+subscription.getAmount()+" "+name+" "+number;
        Chip chip = new Chip(this);
        chip.setLayoutParams(new ChipGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        chip.setText(chipText);
        chip.setChipIcon(getDrawable(R.drawable.ic_baseline_cancel_24));
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscriptionList.remove(chips.indexOf(v));
                chips.remove(chips.indexOf(v));
                refreshChip();
            }
        });
        chips.add(chip);
        refreshChip();
    }
    void refreshChip(){
        selected_subscription_cg.removeAllViews();
        for(Chip chip: chips){
            selected_subscription_cg.addView(chip);
        }
    }
}
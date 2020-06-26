package com.durstep.durstep.fragment.home;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.durstep.durstep.R;
import com.durstep.durstep.adapter.SubscriptionAdapter;
import com.durstep.durstep.helper.TrackDialog;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.interfaces.ListItemClickListener;
import com.durstep.durstep.interfaces.SubsMenuClickListener;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.model.Subscription;
import com.durstep.durstep.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    FloatingActionButton newSubscription_fab;
    TextView background_tv;

    SwipeRefreshLayout refreshLayout;
    RecyclerView subs_rv;
    SubscriptionAdapter subs_adapter;

    ProgressBar progressBar;

    public HomeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        newSubscription_fab = view.findViewById(R.id.home_addSubscription_fab);
        background_tv = view.findViewById(R.id.home_noSubsMessage_tv);
        refreshLayout = view.findViewById(R.id.home_refreshLayout_srl);
        progressBar = view.findViewById(R.id.home_progress_pb);
        subs_rv = view.findViewById(R.id.home_subs_list_rv);
        subs_rv.setHasFixedSize(false);
        subs_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newSubscription_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewSubscriptionDialog(false);
            }
        });
        subs_adapter = new SubscriptionAdapter(getActivity(), getContext(), new ListItemClickListener<Subscription, User>() {
            @Override
            public void onItemClicked(Subscription object1, User object2) {

            }
        });
        subs_adapter.setSubsMenuClickListener(new SubsMenuClickListener() {
            @Override
            public void onMenuItemClick(int id, Subscription subscription) {
                switch (id){
                    case R.id.subs_menu_add_instruction:
                        Utils.log("Add ins");
                        break;
                    case R.id.subs_menu_track:
                        Utils.log("Track");
                        TrackDialog trackDialog = new TrackDialog(getContext(), getActivity(), subscription);
                        trackDialog.start(progressBar);
                        break;
                }
            }
        });
        subs_rv.setAdapter(subs_adapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadSubscriptions();
            }
        });
        loadSubscriptions();
    }
    void loadSubscriptions(){
        refreshLayout.setRefreshing(true);
        DbManager.getAllSubscriptionOfCurrentUser(new FirebaseTask<Subscription>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if(isSuccess){
                    background_tv.setVisibility(View.VISIBLE);
                }else{
                    Utils.longToast(getContext(), error);
                }
                refreshLayout.setRefreshing(false);
            }
            @Override
            public void onSingleDataLoaded(Subscription object) {

            }
            @Override
            public void onMultipleDataLoaded(List<Subscription> objects) {
                if(objects!=null){
                    subs_adapter.addAll(objects);
                    background_tv.setVisibility(View.GONE);
                }
                refreshLayout.setRefreshing(false);
            }
        });
    }
    void showNewSubscriptionDialog(boolean error){
        View v = getLayoutInflater().inflate(R.layout.new_subscription_layout, null);
        Spinner spinner = v.findViewById(R.id.new_subscription_amount_sp);
        TextInputLayout time = v.findViewById(R.id.new_subscription_time_til);
        EditText time_et = time.getEditText();
        time_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    TimePickerDialog dialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            time_et.setText(Utils.formatTime(hourOfDay, minute));
                            if(error){
                                time.setErrorEnabled(false);
                            }
                        }
                    }, 00, 00, false);

                    dialog.show();
                }
            }
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.litres, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if(error){
            time.setError(getString(R.string.invalid_time_format));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.new_subs_alert_title));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String time = time_et.getText().toString().trim();
                if(!Utils.isValidTime(time)){
                    showNewSubscriptionDialog(true);
                    return;
                }
                double amount = Double.parseDouble(spinner.getSelectedItem().toString().split(" ")[0]);
                subscribe(time, amount);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setView(v);
        builder.show();
    }
    void subscribe(String time, double amount){
        String[] hh_mm = time.split(":");
        String hh = hh_mm[0];
        String mm = hh_mm[1];
        Utils.log("Hours: ".concat(hh));
        Utils.log("Minutes: ".concat(mm));
        Utils.log("Amount: ".concat(""+amount));

        Subscription subscription = new Subscription();
        subscription.setsId(DbManager.getNewSubsId());
        subscription.setAmount(amount);
        subscription.setuId(DbManager.getUid());
        subscription.setDeliveryTime(time);
        subscription.setsDate(new Timestamp(new Date()));

        DbManager.createNewSubscription(subscription, new FirebaseTask<Void>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if(isSuccess){

                }else{
                    Utils.toast(getContext(), error);
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
    private void enableLoading(){
        progressBar.setVisibility(View.VISIBLE);
    }
    private void disableLoading(){
        progressBar.setVisibility(View.GONE);
    }
}
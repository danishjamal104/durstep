package com.durstep.durstep.fragment.stats;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.durstep.durstep.R;
import com.durstep.durstep.adapter.OrderAdapter;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.StatsLoadingTask;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.model.Order;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class Distributor_StatsFragment extends Fragment {

    TextView total_order_tv, consumption_tv, payment_due_tv, payment_paid_tv;
    Chip month_chip;
    ProgressBar progressBar;

    RecyclerView order_rv;
    OrderAdapter adapter;
    Map<String, Object> md;
    String month=null;


    public Distributor_StatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats, container, false);
        total_order_tv = v.findViewById(R.id.stats_totalOrder_tv);
        consumption_tv = v.findViewById(R.id.stats_consumed_tv);
        payment_paid_tv = v.findViewById(R.id.stats_amountPayed_tv);
        payment_due_tv = v.findViewById(R.id.stats_amountDue_tv);
        month_chip = v.findViewById(R.id.stats_month_chip);
        progressBar = v.findViewById(R.id.stats_progress_pb);
        order_rv = v.findViewById(R.id.stats_orderList_rv);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        order_rv.setHasFixedSize(false);
        order_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdapter(new ArrayList<>(), getContext());
        order_rv.setAdapter(adapter);

        month_chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthMenu();
            }
        });

        loadOrderOfMonth(null);
    }
    void showMonthMenu(){
        PopupMenu menu = new PopupMenu(getContext(), month_chip);
        menu.inflate(R.menu.month_menu);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String title = item.getTitle().toString();
                month_chip.setText(title.substring(0, 3).toUpperCase());
                loadOrderOfMonth(title);
                adapter.allClear();
                return false;
            }
        });
        menu.show();
    }

    void loadOrderOfMonth(String month){
        progressBar.setVisibility(View.VISIBLE);
        if(month==null){
            this.month = Utils.getDateTimeInFormat(new Timestamp(new Date()), "MMM");
        }else{
            this.month = month;
        }
        String uid = DbManager.getUid();
        DbManager.loadMonthOrder(uid, new StatsLoadingTask<Order>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if (isSuccess && error==null){
                    Utils.toast(getContext(), "No orders this month");

                }else{
                    Utils.longToast(getContext(), error);
                }
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onMetaDataLoaded(Map<String, Object> md) {
                if(md==null){
                    emptyMetaData();
                }else{
                    setMetaData(md);
                }

            }
            @Override
            public void onListLoaded(List<Order> objects) {
                adapter.addAll(objects);
                progressBar.setVisibility(View.GONE);
            }
        }, month);
    }

    void setMetaData(Map<String, Object> metaData){
        this.md = metaData;
        String orderSize = ""+((List<DocumentReference>) md.get("orders")).size();
        String allotted_delivery = (md.get("allotted_delivery").toString());
        String litre_delivered = (md.get("litre_delivered").toString());
        String order_delivered = (md.get("order_delivered").toString());

        total_order_tv.setText(String.format("%s: %s", getString(R.string.allotted_delivery), allotted_delivery));
        consumption_tv.setText(String.format("%s: %s %s", getString(R.string.litre_delivered), litre_delivered, getString(R.string.litre_abbreviation)));
        payment_due_tv.setText(String.format("%s: %s", getString(R.string.order_delivered), order_delivered));
        payment_paid_tv.setText(String.format("%s: %s", getString(R.string.pending_delivery), orderSize));
        month_chip.setText(month.substring(0, 3).toUpperCase());
}

    void emptyMetaData(){
        total_order_tv.setText(String.format("%s: %s", getString(R.string.allotted_delivery), ""));
        consumption_tv.setText(String.format("%s: %s", getString(R.string.litre_delivered), ""));
        payment_due_tv.setText(String.format("%s: %s", getString(R.string.order_delivered), ""));
        payment_paid_tv.setText(String.format("%s: %s", getString(R.string.pending_delivery), ""));
    }
}
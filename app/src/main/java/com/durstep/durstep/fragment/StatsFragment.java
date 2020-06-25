package com.durstep.durstep.fragment;

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
import com.durstep.durstep.interfaces.OrderLoadingTask;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.model.Order;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class StatsFragment extends Fragment {

    TextView total_order_tv, consumption_tv, payment_due_tv, payment_paid_tv;
    Chip month_chip;
    ProgressBar progressBar;

    RecyclerView order_rv;
    OrderAdapter adapter;
    Map<String, Object> md;


    public StatsFragment() {
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
        DbManager.loadMonthOrder(DbManager.getUid(), new OrderLoadingTask() {
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
                setMetaData(md);
            }
            @Override
            public void onOrdersLoaded(List<Order> objects) {
                adapter.addAll(objects);
                progressBar.setVisibility(View.GONE);
            }
        }, month);
    }

    void setMetaData(Map<String, Object> metaData){
        this.md = metaData;
        int orderSize = ((List<DocumentReference>) md.get("orders")).size();
        double consumption = (double) md.get("consumption");
        double due = (double) md.get("due");
        String payment;
        try{
            payment = md.get("paid").toString();
        }catch (Exception e){
            payment = "No payment made";
        }

        total_order_tv.setText(String.format("%s: %s", getString(R.string.total_order), orderSize));
        consumption_tv.setText(String.format("%s: %s %s", getString(R.string.total_consumption), consumption, getString(R.string.litre_abbreviation)));
        payment_due_tv.setText(String.format("%s: ₹ %s", getString(R.string.total_amount_due), due));
        payment_paid_tv.setText(String.format("%s: ₹ %s", getString(R.string.total_amount_paid), payment));
        month_chip.setText(Utils.getDateTimeInFormat(new Timestamp(new Date()), "MMM").toUpperCase());
    }
}
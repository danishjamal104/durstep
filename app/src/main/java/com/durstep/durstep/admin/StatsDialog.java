package com.durstep.durstep.admin;

import android.app.Dialog;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.durstep.durstep.R;
import com.durstep.durstep.adapter.OrderAdapter;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.StatsLoadingTask;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.model.Order;
import com.durstep.durstep.model.User;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StatsDialog {

    TextView total_order_tv, consumption_tv, payment_due_tv, payment_paid_tv;
    Chip month_chip;
    ProgressBar progressBar;

    RecyclerView order_rv;
    OrderAdapter adapter;
    Map<String, Object> md;

    String month=null;

    Dialog v;
    Context context;
    User user;

    public StatsDialog(Context context, User user) {

        this.context = context;
        this.user = user;
        v = new Dialog(context);
        v.setContentView(R.layout.for_admin_stats_layout);
        Objects.requireNonNull(v.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        v.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        total_order_tv = v.findViewById(R.id.for_admin_stats_totalOrder_tv);
        consumption_tv = v.findViewById(R.id.for_admin_stats_consumed_tv);
        payment_paid_tv = v.findViewById(R.id.for_admin_stats_amountPayed_tv);
        payment_due_tv = v.findViewById(R.id.for_admin_stats_amountDue_tv);
        month_chip = v.findViewById(R.id.for_admin_stats_month_chip);
        progressBar = v.findViewById(R.id.for_admin_stats_progress_pb);
        order_rv = v.findViewById(R.id.for_admin_stats_orderList_rv);
        setUp();
    }

    void setUp(){
        order_rv.setHasFixedSize(false);
        order_rv.setLayoutManager(new LinearLayoutManager(context));
        adapter = new OrderAdapter(new ArrayList<>(), context);
        order_rv.setAdapter(adapter);

        month_chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthMenu();
            }
        });

        loadOrderOfMonth(null);
        v.show();
    }

    void showMonthMenu(){
        PopupMenu menu = new PopupMenu(context, month_chip);
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
        String uid = user.getUid();
        DbManager.loadMonthOrder(uid, new StatsLoadingTask<Order>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if (isSuccess && error==null){
                    emptyMetaData();
                }else if(!isSuccess && error==null){
                    Utils.toast(context, "No orders this month");
                }else{
                    Utils.longToast(context, error);
                }
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onMetaDataLoaded(Map<String, Object> md) {
                setMetaData(md);
            }
            @Override
            public void onListLoaded(List<Order> objects) {
                adapter.addAll(objects);
                progressBar.setVisibility(View.GONE);
            }
        }, month);
    }
    void setMetaData(Map<String, Object> metaData){
        if(user.getType()==User.CLIENT){
            setUserMetaData(metaData);
        }else if(user.getType()==User.DISTRIBUTOR){
            setDistributorMetaData(metaData);
        }
    }


    void setUserMetaData(Map<String, Object> metaData){
        this.md = metaData;
        int orderSize = ((List<DocumentReference>) md.get("orders")).size();
        double consumption = Double.parseDouble(md.get("consumption").toString());
        double due = Double.parseDouble(md.get("due").toString());
        Double payment = 0.0;
        List<Double> payments;
        try{
            payments = ((List<Double>) md.get("payments"));
            for(Double d: payments){
                payment+=d;
            }
        }catch (Exception e){
            payment = 0.0;
        }

        total_order_tv.setText(String.format("%s: %s", context.getString(R.string.total_order), orderSize));
        consumption_tv.setText(String.format("%s: %s %s", context.getString(R.string.total_consumption), consumption, context.getString(R.string.litre_abbreviation)));
        payment_due_tv.setText(String.format("%s: ₹ %s", context.getString(R.string.total_amount_due), due-payment));
        payment_paid_tv.setText(String.format("%s: ₹ %s", context.getString(R.string.total_amount_paid), payment));
        month_chip.setText(month.substring(0, 3).toUpperCase());
    }
    void setDistributorMetaData(Map<String, Object> metaData){
        this.md = metaData;
        String allotted_delivery = (md.get("allotted_delivery").toString());
        String litre_delivered = (md.get("litre_delivered").toString());
        String order_delivered = (md.get("order_delivered").toString());

        String pendingDel;
        try{
            int ad = Integer.parseInt(allotted_delivery);
            int od = Integer.parseInt(order_delivered);
            int pd = ad-od;
            pendingDel = String.format("%s", pd);
        }catch (Exception e){
            pendingDel = context.getString(R.string.server_error);
        }

        total_order_tv.setText(String.format("%s: %s", context.getString(R.string.allotted_delivery), allotted_delivery));
        consumption_tv.setText(String.format("%s: %s %s", context.getString(R.string.litre_delivered), litre_delivered, context.getString(R.string.litre_abbreviation)));
        payment_due_tv.setText(String.format("%s: %s", context.getString(R.string.order_delivered), order_delivered));
        payment_paid_tv.setText(String.format("%s: %s", context.getString(R.string.pending_delivery), pendingDel));
        month_chip.setText(month.substring(0, 3).toUpperCase());
    }

    void emptyMetaData(){
        total_order_tv.setText(String.format("%s: %s", context.getString(R.string.total_order), ""));
        consumption_tv.setText(String.format("%s: %s", context.getString(R.string.total_consumption), ""));
        payment_due_tv.setText(String.format("%s: %s", context.getString(R.string.total_amount_due), ""));
        payment_paid_tv.setText(String.format("%s: %s", context.getString(R.string.total_amount_paid), ""));
    }

}

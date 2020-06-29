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
import com.durstep.durstep.adapter.PaymentAdapter;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.StatsLoadingTask;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.model.Order;
import com.durstep.durstep.model.Payment;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Admin_StatsFragment extends Fragment {

    TextView  consumption_tv, payment_due_tv, payment_paid_tv, payment_total_tv;
    Chip month_chip;
    ProgressBar progressBar;

    RecyclerView payment_rv;
    PaymentAdapter adapter;
    Map<String, Object> md;

    FloatingActionButton add_payment_fab;

    public Admin_StatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin__stats, container, false);
        consumption_tv = v.findViewById(R.id.admin_stats_consumed_tv);
        payment_due_tv = v.findViewById(R.id.admin_stats_amountDue_tv);
        payment_paid_tv = v.findViewById(R.id.admin_stats_amountPaid_tv);
        payment_total_tv = v.findViewById(R.id.admin_stats_totalPayment_tv);
        month_chip = v.findViewById(R.id.admin_stats_month_chip);
        payment_rv = v.findViewById(R.id.admin_stats_PaymentList_rv);
        progressBar = v.findViewById(R.id.admin_stats_progress_pb);
        add_payment_fab = v.findViewById(R.id.admin_stats_addPayment_fab);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        payment_rv.setHasFixedSize(false);
        payment_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PaymentAdapter(getContext());
        payment_rv.setAdapter(adapter);

        month_chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthMenu();
            }
        });
        add_payment_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        loadPaymentsOfMonth(null);
    }
    void showMonthMenu(){
        PopupMenu menu = new PopupMenu(getContext(), month_chip);
        menu.inflate(R.menu.month_menu);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String title = item.getTitle().toString();
                month_chip.setText(title.substring(0, 3).toUpperCase());
                loadPaymentsOfMonth(title);
                adapter.allClear();
                return false;
            }
        });
        menu.show();
    }

    void loadPaymentsOfMonth(String month){
        add_payment_fab.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        String uid = DbManager.getUid();
        DbManager.loadMonthPayments(month, new StatsLoadingTask<Payment>() {
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
            public void onListLoaded(List<Payment> payments) {
                for(Payment p:payments){
                    adapter.add(p);
                    p.loadUser(adapter);
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }


    void setMetaData(Map<String, Object> metaData){
        this.md = metaData;
        double total_pay = Double.parseDouble(md.get("total_amount").toString());
        double amount_paid = Double.parseDouble(md.get("amount_paid").toString());
        double amount_due = total_pay-amount_paid;
        double consumption = Double.parseDouble(md.get("consumption").toString());

        consumption_tv.setText(String.format("%s: %s %s", getContext().getString(R.string.total_consumption), consumption, getContext().getString(R.string.litre_abbreviation)));
        payment_total_tv.setText(String.format("%s: ₹ %s", getContext().getString(R.string.total_payment), total_pay));
        payment_paid_tv.setText(String.format("%s: ₹ %s", getContext().getString(R.string.total_amount_paid), amount_due));
        payment_due_tv.setText(String.format("%s: ₹ %s", getString(R.string.total_amount_due), amount_paid));
        month_chip.setText(Utils.getDateTimeInFormat(new Timestamp(new Date()), "MMM").toUpperCase());
        add_payment_fab.setEnabled(true);
    }

    void emptyMetaData(){
        consumption_tv.setText(String.format("%s: %s", getString(R.string.total_consumption), ""));
        payment_due_tv.setText(String.format("%s: ₹ %s", getString(R.string.total_amount_due), ""));
        payment_paid_tv.setText(String.format("%s: ₹ %s", getString(R.string.total_amount_paid), ""));
        payment_total_tv.setText(String.format("%s: ₹ %s", getString(R.string.total_payment), ""));
        add_payment_fab.setEnabled(false);
    }
}
package com.durstep.durstep.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.durstep.durstep.R;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.model.Payment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
    Context context;
    List<Payment> allPayments;
    List<Payment> filteredPayments;

    public PaymentAdapter(Context context) {
        this.context = context;
        this.allPayments = new ArrayList<>();
        this.filteredPayments = new ArrayList<>();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_list_item, parent, false);
        return new PaymentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = filteredPayments.get(position);
        holder.user.setText(payment.getUserName());
        holder.month.setText(payment.getMonth());
        holder.dateTime.setText(Utils.getDateTime(payment.getTime()));
        holder.amount.setText(String.format("₹ %s", payment.getAmount()));
    }


    public void add(Payment payment){
        allPayments.add(payment);
        filteredPayments.add(payment);
        notifyDataSetChanged();
    }
    public void addAll(List<Payment> payments){
        for(Payment p:payments){
            add(p);
        }
        notifyDataSetChanged();
    }
    public void allClear(){
        allPayments.clear();
        filteredPayments.clear();
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return filteredPayments.size();
    }

    public class PaymentViewHolder extends RecyclerView.ViewHolder{

        TextView amount;
        TextView dateTime;
        TextView month;
        TextView user;
        //FloatingActionButton menu_fab;
        View v;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
            amount = v.findViewById(R.id.payment_list_item_amount_tv);
            dateTime = v.findViewById(R.id.payment_list_item_dateTime_tv);
            month = v.findViewById(R.id.payment_list_item_month_tv);
            user = v.findViewById(R.id.payment_list_item_user_tv);
            //menu_fab = v.findViewById(R.id.payment_list_item_menu_fab);
        }
    }
}

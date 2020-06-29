package com.durstep.durstep.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.durstep.durstep.R;
import com.durstep.durstep.model.Subscription;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder> {

    List<Subscription> subscriptions;
    Context context;

    public DeliveryAdapter(Context context) {
        this.context = context;
        subscriptions = new ArrayList<>();
    }

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeliveryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.subscription_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        Subscription subs = subscriptions.get(position);
        holder.litre_tv.setText(""+subs.getAmount()+" "+context.getString(R.string.litre_abbreviation));
        holder.deliveryTime_tv.setText(context.getString(R.string.subs_delivery_time)+subs.getDeliveryTime());
        holder.user_tv.setText(subs.getUserName());
        switch (subs.deliveryMarker){
            case ON_WAY:
                holder.indicator_fb.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.on_way)));
                break;
            case DELIVERED:
                holder.indicator_fb.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.delivered)));
                break;
            case SCHEDULED:
                holder.indicator_fb.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.delivered)));
                break;
        }
    }

    public void add(Subscription subscription){
        subscriptions.add(subscription);
        notifyDataSetChanged();
    }
    public void clear(){
        subscriptions.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return subscriptions.size();
    }

    public class DeliveryViewHolder extends RecyclerView.ViewHolder{

        TextView litre_tv;
        TextView user_tv;
        TextView deliveryTime_tv;
        FloatingActionButton indicator_fb;
        View root;

        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView;
            litre_tv = itemView.findViewById(R.id.subs_list_item_number_tv);
            user_tv = itemView.findViewById(R.id.subs_item_monthlyRate_tv);
            deliveryTime_tv = itemView.findViewById(R.id.subs_item_deliveryTime_tv);
            indicator_fb = itemView.findViewById(R.id.subs_item_menu_fab);

        }
    }
}

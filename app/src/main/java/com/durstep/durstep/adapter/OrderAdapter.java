package com.durstep.durstep.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.durstep.durstep.R;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.model.AppMode;
import com.durstep.durstep.model.Order;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder>{

    Context context;

    List<Order> allOrders;
    List<Order> filteredOrders;


    public OrderAdapter(List<Order> allOrders, Context context) {
        this.context = context;
        this.allOrders = allOrders;
        filteredOrders = new ArrayList<>(allOrders);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int viewId = R.layout.order_list_item;
        return new OrderViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = filteredOrders.get(position);
        holder.time.setText(context.getString(R.string.order_time)+Utils.getTime(order.getTime()));
        holder.litreAmount.setText(String.format("%s %s", order.getAmount(), context.getString(R.string.litre_abbreviation)));
        holder.date.setText(context.getString(R.string.order_date)+Utils.getDate(order.getTime()));
        holder.id.setText(order.getId());
    }

    @Override
    public int getItemCount() {
        return filteredOrders.size();
    }

    public void applyFilter(String searchText){
       // if(searchText==null){return;}
        //notifyDataSetChanged();
    }

    public void add(Order order){
        allOrders.add(order);
        filteredOrders.add(order);
        notifyDataSetChanged();
    }

    public void addAll(List<Order> orders){
        for(Order order: orders){
            add((order));
        }
    }

    public void allClear(){
        // clears all items
        filteredOrders.clear();
        allOrders.clear();
        notifyDataSetChanged();
    }

    public List<Order> getAllOrders() {
        return allOrders;
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder{

        TextView litreAmount;
        TextView id;
        TextView date;
        TextView time;
        FloatingActionButton action_fab;
        View v;


        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            v=itemView;
            time = itemView.findViewById(R.id.order_list_item_orderTime_tv);
            litreAmount = itemView.findViewById(R.id.order_list_item_number_tv);
            id = itemView.findViewById(R.id.order_list_item_orderId_tv);
            date = itemView.findViewById(R.id.order_list_item_orderDate_tv);
            action_fab = itemView.findViewById(R.id.order_list_item_menu_fab);
        }

    }
}
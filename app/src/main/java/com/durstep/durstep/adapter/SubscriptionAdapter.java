package com.durstep.durstep.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.durstep.durstep.R;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.ListItemClickListener;
import com.durstep.durstep.interfaces.MenuClickListener;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.manager.UserManager;
import com.durstep.durstep.model.AppMode;
import com.durstep.durstep.model.Subscription;
import com.durstep.durstep.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubsViewHolder> {

    List<Subscription> subscriptionList;
    Context context;
    Activity activity;

    MenuClickListener<Subscription> menuClickListener;

    ListItemClickListener<Subscription, User> subscriptionListItemClickListener;

    public SubscriptionAdapter(Context context, ListItemClickListener<Subscription, User> subscriptionListItemClickListener ) {
        this.subscriptionList = new ArrayList<>();
        //this.activity = activity;
        this.context = context;
        this.subscriptionListItemClickListener = subscriptionListItemClickListener;
    }


    @NonNull
    @Override
    public SubsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.subscription_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SubsViewHolder holder, int position) {
        Subscription subscription = subscriptionList.get(position);
        holder.litre_tv.setText(""+subscription.getAmount()+" "+context.getString(R.string.litre_abbreviation));
        holder.deliveryTime_tv.setText(context.getString(R.string.subs_delivery_time)+subscription.getDeliveryTime());
        holder.monthly_tv.setText(context.getString(R.string.subs_monthly_rate)+" ");
        DbManager.getmRef().document("admin/meta-data").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        double ratePerLitre = documentSnapshot.getDouble("rate");
                        holder.monthly_tv.setText(holder.monthly_tv.getText().toString()+Utils.getMonthlyRate(ratePerLitre, subscription.getAmount()));
                    }
                });

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscriptionListItemClickListener.onItemClicked(subscription, null);
            }
        });

        holder.menu_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuClickListener!=null&&AppMode.getAppMode(context)==AppMode.CLIENT){
                    PopupMenu menu = new PopupMenu(context, holder.menu_fab);
                    menu.inflate(R.menu.subs_item_menu);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            menuClickListener.onMenuItemClick(item.getItemId(), subscription);
                            return false;
                        }
                    });
                    menu.show();
                }
                subscriptionListItemClickListener.onItemClicked(subscription, null);
            }
        });
    }

    public void add(Subscription subscription){
        subscriptionList.add(subscription);
        notifyDataSetChanged();
    }

    public void addAll(List<Subscription> subscriptions){
        subscriptionList.clear();
        for(Subscription subscription: subscriptions){
            add(subscription);
        }
    }

    @Override
    public int getItemCount() {
        return subscriptionList.size();
    }

    public void setMenuClickListener(MenuClickListener<Subscription> menuClickListener) {
        this.menuClickListener = menuClickListener;
    }

    public class SubsViewHolder extends RecyclerView.ViewHolder{

        TextView litre_tv;
        TextView monthly_tv;
        TextView deliveryTime_tv;
        FloatingActionButton menu_fab;
        View root;

        public SubsViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView;
            litre_tv = itemView.findViewById(R.id.subs_list_item_number_tv);
            monthly_tv = itemView.findViewById(R.id.subs_item_monthlyRate_tv);
            deliveryTime_tv = itemView.findViewById(R.id.subs_item_deliveryTime_tv);
            menu_fab = itemView.findViewById(R.id.subs_item_menu_fab);
        }
    }

}

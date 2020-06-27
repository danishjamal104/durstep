package com.durstep.durstep.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.durstep.durstep.R;
import com.durstep.durstep.interfaces.ListItemClickListener;
import com.durstep.durstep.interfaces.MenuClickListener;
import com.durstep.durstep.model.Subscription;
import com.durstep.durstep.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    List<User> allUsers;
    List<User> filteredUsers;
    Context context;

    UserViewHolder expanded_vh;

    ListItemClickListener<Subscription, User> subscriptionListItemClickListener;

    MenuClickListener<User> userMenuClickListener;
    MenuClickListener<Subscription> subscriptionMenuClickListener;

    public UserAdapter(Context context, ListItemClickListener<Subscription, User> subscriptionListItemClickListener) {
        this.allUsers = new ArrayList<>();
        this.filteredUsers = new ArrayList<>();
        this.context = context;
        this.subscriptionListItemClickListener = subscriptionListItemClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.expandable_user_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = filteredUsers.get(position);
        holder.totalOrder_tv.setText("" + user.getTotalOrder());
        holder.nameNumber_tv.setText(user.getName() + "\n" + user.getNumber());
        user.setUpSubscription(context, holder.subscriptionList_rv);

        ListItemClickListener<Subscription, User> __this__ = new ListItemClickListener<Subscription, User>() {
            @Override
            public void onItemClicked(Subscription object1, User objects2) {
                subscriptionListItemClickListener.onItemClicked(object1, user);
            }
        };

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscriptionListItemClickListener.onItemClicked(null, user);
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userMenuClickListener!=null){
                    PopupMenu menu = new PopupMenu(context, v.findViewById(R.id.user_list_item_topCard_cv));
                    if(user.getType() == User.CLIENT){
                        menu.inflate(R.menu.user_client_item_menu);
                    }else{
                        menu.inflate(R.menu.user_distributor_item_menu);
                    }
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            userMenuClickListener.onMenuItemClick(item.getItemId(), user);
                            return false;
                        }
                    });
                    menu.show();
                }
            }
        });

        holder.expand_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expanded_vh != null) {
                    if (expanded_vh.getItemId() == holder.getItemId()) {
                        // close current
                        expanded_vh.expand_iv.setRotation(180f);
                        expanded_vh.subscriptionList_rv.setVisibility(View.GONE);
                        expanded_vh = null;
                    } else {
                        // close current and open new
                        expanded_vh.expand_iv.setRotation(180f);
                        expanded_vh.subscriptionList_rv.setVisibility(View.GONE);
                        holder.expand_iv.setRotation(0f);
                        holder.subscriptionList_rv.setVisibility(View.VISIBLE);
                        expanded_vh = holder;
                        user.loadSubscription(__this__, subscriptionMenuClickListener);
                    }
                } else {
                    // open current
                    expanded_vh = holder;
                    expanded_vh.expand_iv.setRotation(0f);
                    expanded_vh.subscriptionList_rv.setVisibility(View.VISIBLE);
                    user.loadSubscription(__this__, subscriptionMenuClickListener);
                }
            }
        });

    }

    public void add(User user) {
        allUsers.add(user);
        filteredUsers.add(user);
        notifyDataSetChanged();
    }

    public void addAll(List<User> users) {
        for (User user : users) {
            add(user);
        }
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }


    public void applyFilter(int filter){
        filteredUsers.clear();

        for(User u: allUsers){
            if(u.getType()==filter){
                filteredUsers.add(u);
            }
        }
        notifyDataSetChanged();
    }
    public void applyFilter(String nameNumber) {
        nameNumber = nameNumber.toLowerCase();
        filteredUsers.clear();
        if (nameNumber.equals("")) {
            filteredUsers.addAll(allUsers);
        } else {
            for (User user : allUsers) {
                if (user.getName().toLowerCase().contains(nameNumber) || user.getNumber().toLowerCase().contains(nameNumber)) {
                    filteredUsers.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setUserMenuClickListener(MenuClickListener<User> userMenuClickListener) {
        this.userMenuClickListener = userMenuClickListener;
    }

    public void setSubscriptionMenuClickListener(MenuClickListener<Subscription> subscriptionMenuClickListener) {
        this.subscriptionMenuClickListener = subscriptionMenuClickListener;
    }

    public void allClear(){
        filteredUsers.clear();
        allUsers.clear();
        notifyDataSetChanged();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        TextView totalOrder_tv, nameNumber_tv;
        ImageView expand_iv;
        RecyclerView subscriptionList_rv;
        View view;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            view =itemView;
            totalOrder_tv = itemView.findViewById(R.id.user_list_item_totalOrderNumber_tv);
            nameNumber_tv = itemView.findViewById(R.id.user_list_item_nameNumber_tv);
            expand_iv = itemView.findViewById(R.id.user_list_item_typeIllustration_iv);
            subscriptionList_rv = itemView.findViewById(R.id.user_list_item_subscriptionList_rv);
        }
    }
}

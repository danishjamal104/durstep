package com.durstep.durstep.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.durstep.durstep.R;
import com.durstep.durstep.interfaces.ListItemClickListener;
import com.durstep.durstep.model.User;

import java.util.ArrayList;
import java.util.List;

public class SimpleUserAdapter extends RecyclerView.Adapter<SimpleUserAdapter.UserViewHolder> {

    List<String> selected = new ArrayList<>();
    List<User> userList;
    Context context;

    ListItemClickListener<User, Integer> listItemClickListener;

    public SimpleUserAdapter(Context context) {
        this.userList = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_user_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name_tv.setText(user.getName());
        holder.number_tv.setText(user.getNumber());

        holder.select_rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selected.contains(user.getUid())){
                    selected.remove(user.getUid());
                   holder.select_rb.setChecked(false);
                }else{
                    selected.add(user.getUid());
                }
            }
        });
    }

    public void add(User user){
        userList.add(user);
        notifyDataSetChanged();
    }

    public void addAll(List<User> users){
        for(User u:users){
            add(u);
        }
    }

    public List<String> getSelected() {
        return selected;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder{

        TextView name_tv;
        TextView number_tv;
        CheckBox select_rb;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name_tv = itemView.findViewById(R.id.new_payment_list_item_name_tv);
            number_tv = itemView.findViewById(R.id.new_payment_list_item_number_tv);
            select_rb = itemView.findViewById(R.id.new_payment_select_rb);
        }
    }
}

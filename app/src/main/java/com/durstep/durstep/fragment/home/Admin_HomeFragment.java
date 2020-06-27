package com.durstep.durstep.fragment.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.durstep.durstep.R;
import com.durstep.durstep.adapter.UserAdapter;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.interfaces.ListItemClickListener;
import com.durstep.durstep.interfaces.MenuClickListener;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.model.Subscription;
import com.durstep.durstep.model.User;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class Admin_HomeFragment extends Fragment {

    TextInputLayout filter_til;
    ImageButton filter_ibt;

    RecyclerView recyclerView;
    UserAdapter adapter;

    public Admin_HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin__home, container, false);
        filter_til = v.findViewById(R.id.admin_home_search_til);
        filter_ibt = v.findViewById(R.id.admin_home_filter_ib);
        recyclerView = v.findViewById(R.id.admin_home_userList_rv);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        filter_ibt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterMenu();
            }
        });

        adapter = new UserAdapter(getContext(), new ListItemClickListener<Subscription, User>() {
            @Override
            public void onItemClicked(Subscription object1, User objects2) {
                if(object1==null && objects2!=null){

                }
            }
        });
        adapter.setUserMenuClickListener(new MenuClickListener<User>() {
            @Override
            public void onMenuItemClick(int id, User object) {
                switch (id){
                    case R.id.client_make_distributor:
                        break;
                    case R.id.client_show_stats:
                        break;
                    case R.id.distributor_make_client:
                        break;
                    case R.id.distributor_show_stats:
                        break;
                }
            }
        });

        filter_til.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.applyFilter(s.toString());
            }
        });

        recyclerView.setAdapter(adapter);
        loadUser();
    }

    void loadUser(){
        DbManager.getAllUser(new FirebaseTask<User>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if(isSuccess){
                    Utils.toast(getContext(), "No User");
                }else{
                    Utils.longToast(getContext(), error);
                }
            }
            @Override
            public void onSingleDataLoaded(User object) {}
            @Override
            public void onMultipleDataLoaded(List<User> objects) {
                adapter.addAll(objects);
            }
        });
    }

    void showFilterMenu(){
        PopupMenu menu = new PopupMenu(getContext(), filter_ibt);
        menu.inflate(R.menu.user_filter_menu);

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.filter_all:
                        adapter.applyFilter("");
                        break;
                    case R.id.filter_client:
                        adapter.applyFilter(User.CLIENT);
                        break;
                    case R.id.filter_distributor:
                        adapter.applyFilter(User.DISTRIBUTOR);
                        break;
                }
                return false;
            }
        });
        menu.show();
    }
}
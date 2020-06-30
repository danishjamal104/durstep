package com.durstep.durstep.fragment.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.durstep.durstep.NewDeliveryActivity;
import com.durstep.durstep.R;
import com.durstep.durstep.adapter.UserAdapter;
import com.durstep.durstep.admin.StatsDialog;
import com.durstep.durstep.helper.TrackDialog;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.interfaces.ListItemClickListener;
import com.durstep.durstep.interfaces.MenuClickListener;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.model.Subscription;
import com.durstep.durstep.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Objects;

public class Admin_HomeFragment extends Fragment {

    TextInputLayout filter_til;
    ImageButton filter_ibt;

    RecyclerView recyclerView;
    UserAdapter adapter;

    SwipeRefreshLayout refreshLayout;

    public Admin_HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin__home, container, false);
        filter_til = v.findViewById(R.id.admin_home_search_til);
        filter_ibt = v.findViewById(R.id.admin_home_filter_ib);
        recyclerView = v.findViewById(R.id.admin_home_userList_rv);
        refreshLayout = v.findViewById(R.id.admin_home_refreshLayout_srl);
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
                Utils.log(object1.toString());
                Utils.log(objects2.toString());
            }
        });
        adapter.setUserMenuClickListener(new MenuClickListener<User>() {
            @Override
            public void onMenuItemClick(int id, User user) {
                switch (id){
                    case R.id.client_make_distributor:
                        changeUserType(user, User.DISTRIBUTOR);
                        break;
                    case R.id.distributor_make_client:
                        changeUserType(user, User.CLIENT);
                        break;
                    case R.id.distributor_schedule_delivery:
                        Intent intent = new Intent(getActivity(), NewDeliveryActivity.class);
                        intent.putExtra("dId", user.getUid());
                        startActivity(intent);
                        break;
                    default:
                        StatsDialog dialog = new StatsDialog(getContext(), user);
                        break;
                }
            }
        });
        adapter.setSubscriptionMenuClickListener(new MenuClickListener<Subscription>() {
            @Override
            public void onMenuItemClick(int id, Subscription object) {
                switch (id){
                    case R.id.subs_menu_track:
                        TrackDialog trackDialog = new TrackDialog(getContext(), getActivity(), object);
                        break;
                    default:
                        break;
                }
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.allClear();
                loadUser();
            }
        });

        Objects.requireNonNull(filter_til.getEditText()).addTextChangedListener(new TextWatcher() {
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
        refresh();
    }

    void changeUserType(User user, int type){
        refreshLayout.setRefreshing(true);
        DbManager.getmRef().document(String.format("user/%s", user.getUid()))
        .update("type", type).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Utils.toast(getContext(), getString(R.string.success));
                }else{
                    Utils.longToast(getContext(), task.getException().getLocalizedMessage());
                }
                refresh();
            }
        });
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
                refreshLayout.setRefreshing(false);
            }
            @Override
            public void onSingleDataLoaded(User object) {}
            @Override
            public void onMultipleDataLoaded(List<User> objects) {
                adapter.addAll(objects);
                refreshLayout.setRefreshing(false);
            }
        });
    }

    void refresh(){
        refreshLayout.setRefreshing(true);
        adapter.allClear();
        loadUser();
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
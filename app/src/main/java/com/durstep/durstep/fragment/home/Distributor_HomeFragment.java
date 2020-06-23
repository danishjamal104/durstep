package com.durstep.durstep.fragment.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.durstep.durstep.NewDeliveryActivity;
import com.durstep.durstep.R;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.model.ActiveDelivery;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class Distributor_HomeFragment extends Fragment {

    LinearLayout detailContainer_ll;
    TextView total_tv, pending_tv, delivered_tv;
    MaterialButton update_bt;
    TextView inactive_tv, location_tv;

    ActiveDelivery activeDelivery;

    public Distributor_HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_distributor__home, container, false);
        inactive_tv = v.findViewById(R.id.dist_home_inactiveMessage_tv);
        detailContainer_ll = v.findViewById(R.id.dist_home_deliveryDetailContainer_ll);
        location_tv = v.findViewById(R.id.dist_home_delivery_location_tv);
        total_tv = v.findViewById(R.id.dist_home_total_tv);
        pending_tv = v.findViewById(R.id.dist_home_pending_tv);
        delivered_tv = v.findViewById(R.id.dist_home_delivered_tv);
        update_bt = v.findViewById(R.id.dist_home_deliveryUpdate_mbt);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inactive_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NewDeliveryActivity.class));
            }
        });
        getCurrentActiveDelivery();
    }

    void getCurrentActiveDelivery(){
        DbManager.getScheduledDelivery(new FirebaseTask<ActiveDelivery>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if(isSuccess){
                    // no active delivery case
                    getScheduledDelivery();
                }else{
                    // error case
                    hideDetail();
                    Utils.longToast(getContext(), error);
                }
            }
            @Override
            public void onSingleDataLoaded(ActiveDelivery object) {
                showDetail();
                setBtUpdateMode();
                activeDelivery = object;
                total_tv.setText(getString(R.string.total)+object.getTotal());
                pending_tv.setText(getString(R.string.pending)+object.getPending());
                delivered_tv.setText(getString(R.string.delivered)+object.getDelivered());
            }
            @Override
            public void onMultipleDataLoaded(List<ActiveDelivery> objects) {

            }
        });
    }
    void getScheduledDelivery(){
        DbManager.getScheduledDelivery(new FirebaseTask<ActiveDelivery>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if(isSuccess){
                    // no scheduled delivery case
                }else{
                    Utils.longToast(getContext(), error);
                }
                hideDetail();
            }
            @Override
            public void onSingleDataLoaded(ActiveDelivery object) {
                activeDelivery = object;
                showDetail();
                setBtStartMode();
                total_tv.setText(getString(R.string.total)+object.getTotal());
                pending_tv.setText(getString(R.string.pending)+object.getPending());
                delivered_tv.setText(getString(R.string.delivered)+object.getDelivered());
            }
            @Override
            public void onMultipleDataLoaded(List<ActiveDelivery> objects) {

            }
        });
    }

    void setBtStartMode(){
        update_bt.setText(getString(R.string.start));
    }
    void setBtUpdateMode(){
        update_bt.setText(getString(R.string.update));
    }
    void showDetail(){
        inactive_tv.setVisibility(View.GONE);
        detailContainer_ll.setVisibility(View.VISIBLE);
        update_bt.setVisibility(View.VISIBLE);
        location_tv.setVisibility(View.VISIBLE);
    }
    void hideDetail(){
        inactive_tv.setVisibility(View.VISIBLE);
        detailContainer_ll.setVisibility(View.GONE);
        update_bt.setVisibility(View.GONE);
        location_tv.setVisibility(View.GONE);
    }
}
package com.durstep.durstep.fragment.home;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.durstep.durstep.NewDeliveryActivity;
import com.durstep.durstep.R;
import com.durstep.durstep.adapter.DeliveryAdapter;
import com.durstep.durstep.adapter.SubscriptionAdapter;
import com.durstep.durstep.interfaces.DeliveryTask;
import com.durstep.durstep.interfaces.ListItemClickListener;
import com.durstep.durstep.manager.NotifyManager;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.interfaces.LocationUpdateListener;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.model.ActiveDelivery;
import com.durstep.durstep.model.Subscription;
import com.durstep.durstep.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class Distributor_HomeFragment extends Fragment {

    private final int LOCATION_PERMISSION_CODE = 372;

    LinearLayout detailContainer_ll;
    TextView total_tv, pending_tv, delivered_tv;
    MaterialButton update_bt, cancel_bt;
    TextView inactive_tv, location_tv;

    RecyclerView subscription_rv;
    DeliveryAdapter adapter;

    ProgressBar progressBar;

    ActiveDelivery activeDelivery;

    LocationUpdateListener bufferListener;

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
        progressBar = v.findViewById(R.id.dist_home_delivery_progress_pb);
        update_bt = v.findViewById(R.id.dist_home_deliveryUpdate_mbt);
        cancel_bt = v.findViewById(R.id.dist_home_deliveryCancel_mbt);
        subscription_rv = v.findViewById(R.id.dist_home_SubsList_rv);
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
        subscription_rv.setHasFixedSize(false);
        subscription_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DeliveryAdapter(getContext());
        subscription_rv.setAdapter(adapter);
        getCurrentActiveDelivery();
    }

    void getCurrentActiveDelivery() {
        enableLoading();
        DbManager.getCurrentActiveDelivery(new DeliveryTask<ActiveDelivery, Subscription>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if (isSuccess) {
                    // no active delivery case
                    getScheduledDelivery();
                } else {
                    // error case
                    hideDetail();
                    Utils.longToast(getContext(), error);
                    disableLoading();
                }
            }

            @Override
            public void onSingleDataLoaded(ActiveDelivery object) {
                showDetail();
                setBtUpdateMode();
                activeDelivery = object;
                total_tv.setText(getString(R.string.total) + " " + object.getTotal());
                pending_tv.setText(getString(R.string.pending) + " " + object.getPending());
                delivered_tv.setText(getString(R.string.delivered) + " " + object.getDelivered());
                GeoPoint loc = object.getLocation();
                location_tv.setText(""+loc.getLatitude()+"Lat "+loc.getLongitude()+"Lon");
                disableLoading();
            }

            @Override
            public void onExtraDataLoaded(List<Subscription> subscriptions, int mode) {
                if(mode==-1){
                    Utils.longToast(getContext(), getString(R.string.server_error));
                    return;
                }
                for(Subscription subs: subscriptions){
                    switch (mode){
                        case 0:
                            subs.deliveryMarker = Subscription.DeliveryMarker.ON_WAY;
                            break;
                        case 1:
                            subs.deliveryMarker = Subscription.DeliveryMarker.DELIVERED;
                            break;
                    }
                    adapter.add(subs);
                    subs.loadUser(adapter);
                }
            }
        });
    }
    void getScheduledDelivery() {
        DbManager.getScheduledDelivery(new DeliveryTask<ActiveDelivery, Subscription>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if (isSuccess) {
                    // no scheduled delivery case
                    hideDetail();
                } else {
                    Utils.longToast(getContext(), error);
                }
                disableLoading();
            }

            @Override
            public void onSingleDataLoaded(ActiveDelivery object) {
                activeDelivery = object;
                showDetail();
                setBtStartMode();
                total_tv.setText(getString(R.string.total) + " " + object.getTotal());
                pending_tv.setText(getString(R.string.pending) + " " + object.getPending());
                delivered_tv.setText(getString(R.string.delivered) + " " + object.getDelivered());
                disableLoading();
            }
            @Override
            public void onExtraDataLoaded(List<Subscription> subscriptions, int mode) {
                if(mode==-1){
                    Utils.longToast(getContext(), getString(R.string.server_error));
                    return;
                }
                for(Subscription subs: subscriptions){
                    subs.deliveryMarker = Subscription.DeliveryMarker.SCHEDULED;
                    adapter.add(subs);
                    subs.loadUser(adapter);
                }
            }

        });
    }

    void startDelivery(){
        enableLoading();
        updateLocation(new LocationUpdateListener() {
            @Override
            public void onLocationUpdate(double lat, double lon) {
                activeDelivery.setLocation(new GeoPoint(lat, lon));
                DbManager.startDelivery(activeDelivery, new FirebaseTask<Void>() {
                    @Override
                    public void onComplete(boolean isSuccess, String error) {
                        if(isSuccess){
                            setBtUpdateMode();
                            notifyOnStartDelivery();
                            getCurrentActiveDelivery();
                        }else{
                            Utils.longToast(getContext(), error);
                        }
                        disableLoading();
                    }
                    @Override
                    public void onSingleDataLoaded(Void object) {
                    }
                    @Override
                    public void onMultipleDataLoaded(List<Void> objects) {

                    }
                });
            }
        });
    }
    void updateDelivery(){
        enableLoading();
        updateLocation(new LocationUpdateListener() {
            @Override
            public void onLocationUpdate(double lat, double lon) {
                DbManager.updateLocation(new GeoPoint(lat, lon), new FirebaseTask<Void>() {
                    @Override
                    public void onComplete(boolean isSuccess, String error) {
                        if(isSuccess){
                            Utils.toast(getContext(), getString(R.string.success));
                            NotifyManager.sendLocationUpdate(getActivity(), activeDelivery);
                            adapter.clear();
                            getCurrentActiveDelivery();
                        }else{
                            Utils.longToast(getContext(), error);
                        }
                        disableLoading();
                    }
                    @Override
                    public void onSingleDataLoaded(Void object) {

                    }
                    @Override
                    public void onMultipleDataLoaded(List<Void> objects) {

                    }
                });
            }
        });
    }

    void notifyOnStartDelivery(){
        List<String> to = new ArrayList<>();
        for(DocumentReference reference: activeDelivery.getSubscription_list()){
            to.add(Utils.getUserIdFromSubscriptionRef(reference));
        }
        NotifyManager.sendDeliveryStartNotification(getActivity(), to);
    }


    void updateLocation(LocationUpdateListener listener) {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getContext());

        LocationRequest lr = new LocationRequest();
        lr.setInterval(10000);
        lr.setFastestInterval(3000);
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (client != null) {
                    client.removeLocationUpdates(this);
                }
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int latestIdx = locationResult.getLocations().size() - 1;
                    Location location = locationResult.getLocations().get(latestIdx);
                    Utils.log("Lattitude: " + location.getLatitude());
                    Utils.log("Longitude: " + location.getLongitude());
                    listener.onLocationUpdate(location.getLatitude(), location.getLongitude());
                }
            }
        };

        if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            bufferListener = listener;
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            return;
        }
        client.requestLocationUpdates(lr, callback, Looper.getMainLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_PERMISSION_CODE && grantResults.length>0){
            if(grantResults[0] ==  PackageManager.PERMISSION_GRANTED){
                updateLocation(bufferListener);
                bufferListener=null;
            }else {
                Utils.toast(getContext(), "Permission Denied!");
            }
        }
    }

    void setBtStartMode() {
        update_bt.setText(getString(R.string.start));
        cancel_bt.setText(getString(R.string.modify));
        update_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDelivery();
            }
        });
        cancel_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewDeliveryActivity.class);
                intent.putExtra("isModify", 1);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }
    void setBtUpdateMode() {
        update_bt.setText(getString(R.string.update));
        cancel_bt.setText(getString(R.string.cancel));
        update_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDelivery();
            }
        });
        cancel_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableLoading();
                DocumentReference adRef = DbManager.getmRef().document("active_delivery/"+DbManager.getUid());
                adRef.update("delivered", FieldValue.increment(-1))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            adRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    adapter.clear();
                                    getCurrentActiveDelivery();
                                    if(task.isSuccessful()){
                                        Utils.toast(getContext(), getString(R.string.success));
                                    }else{
                                        Utils.longToast(getContext(), ""+task.getException().getLocalizedMessage());
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    void showDetail(){
        inactive_tv.setVisibility(View.GONE);
        detailContainer_ll.setVisibility(View.VISIBLE);
        update_bt.setVisibility(View.VISIBLE);
        cancel_bt.setVisibility(View.VISIBLE);
        location_tv.setVisibility(View.VISIBLE);
    }
    void hideDetail(){
        inactive_tv.setVisibility(View.VISIBLE);
        detailContainer_ll.setVisibility(View.GONE);
        update_bt.setVisibility(View.GONE);
        cancel_bt.setVisibility(View.GONE);
        location_tv.setVisibility(View.GONE);
    }
    void enableLoading(){
        progressBar.setVisibility(View.VISIBLE);
    }
    void disableLoading(){
        progressBar.setVisibility(View.GONE);
    }
}
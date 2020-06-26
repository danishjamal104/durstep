package com.durstep.durstep.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import androidx.core.app.ActivityCompat;

import com.durstep.durstep.MainActivity;
import com.durstep.durstep.R;
import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.interfaces.LocationUpdateListener;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.manager.LocationManager;
import com.durstep.durstep.model.Subscription;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class TrackDialog {

    Activity activity;
    Context context;

    CustomDialog dialog;

    Subscription subscription;

    ProgressBar progressBar;

    public TrackDialog(Context context, Activity activity, Subscription subscription) {
        this.context = context;
        this.subscription = subscription;
        this.activity = activity;
    }
    public void start(ProgressBar progressBar){
        this.progressBar = progressBar;
        setUp();
    }

    private void setUp(){
        dialog = new CustomDialog(context);
        dialog.setTitle(context.getString(R.string.track_info));
        dialog.setPositive(context.getString(R.string.ok), null);
        dialog.setNegative(context.getString(R.string.cancel), null);
        if(subscription.getActive()==null){
            dialog.setMessage(context.getString(R.string.track_inactive_message));
            dialog.show();
        }else{
            enableLoading();
            track();
        }

    }

    private void track(){
        DbManager.getDeliveryLocation(subscription.getActive(), new FirebaseTask<GeoPoint>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
            }
            @Override
            public void onSingleDataLoaded(GeoPoint object) {
                accessLocation(new LocationUpdateListener() {
                    @Override
                    public void onLocationUpdate(double lat, double lon) {
                        double d = Utils.meterDistanceBetweenPoints((float)lat, (float)lon,
                                (float)object.getLatitude(), (float)object.getLongitude());
                        dialog.setMessage(String.format("Your milk is %s meter away", ""+d));
                        disableLoading();
                        dialog.show();
                    }
                });
            }

            @Override
            public void onMultipleDataLoaded(List<GeoPoint> objects) {

            }
        });
    }


    @SuppressLint("MissingPermission")
    void accessLocation(LocationUpdateListener listener){
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);

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
                    try{
                        int latestIdx = locationResult.getLocations().size() - 1;
                        Location location = locationResult.getLocations().get(latestIdx);
                        Utils.log("Lattitude: " + location.getLatitude());
                        Utils.log("Longitude: " + location.getLongitude());
                        listener.onLocationUpdate(location.getLatitude(), location.getLongitude());
                    }catch (Exception e){
                        Utils.longToast(context, context.getString(R.string.client_location_error));
                    }
                }
                disableLoading();
            }
        };
        if(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            client.requestLocationUpdates(lr, callback, Looper.getMainLooper());
        }else{
            disableLoading();
            Utils.toast(context, context.getString(R.string.turn_on_location));
        }
    }

    private void enableLoading(){
        progressBar.setVisibility(View.VISIBLE);
    }
    private void disableLoading(){
        progressBar.setVisibility(View.GONE);
    }

}

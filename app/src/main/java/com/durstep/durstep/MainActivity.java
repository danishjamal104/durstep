package com.durstep.durstep;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.durstep.durstep.authentication.SignUpActivity;
import com.durstep.durstep.fragment.home.Admin_HomeFragment;
import com.durstep.durstep.fragment.home.Distributor_HomeFragment;
import com.durstep.durstep.fragment.home.HomeFragment;
import com.durstep.durstep.fragment.ProfileFragment;
import com.durstep.durstep.fragment.qr.Dist_QrFragment;
import com.durstep.durstep.fragment.qr.QrFragment;
import com.durstep.durstep.fragment.stats.Admin_StatsFragment;
import com.durstep.durstep.fragment.stats.Distributor_StatsFragment;
import com.durstep.durstep.fragment.stats.StatsFragment;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.LocationUpdateListener;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.manager.LocationManager;
import com.durstep.durstep.manager.NotifyManager;
import com.durstep.durstep.manager.TokenManager;
import com.durstep.durstep.manager.UserManager;
import com.durstep.durstep.model.AppMode;
import com.durstep.durstep.model.Menu;
import com.durstep.durstep.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;

public class MainActivity extends BaseActivity {

    private final int LOCATION_PERMISSION_CODE = 372;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    TextView home_tv, stats_tv, profile_tv, info_tv;
    TextView currentSelected;
    FloatingActionButton scan_fab;
    int appMode;
    LocationUpdateListener bufferListener;

    int animationDuration = 250;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(DbManager.getUid()!=null){
            Bundle bundle = getIntent().getExtras();
            if(bundle!=null){
                boolean init = bundle.getBoolean("__init__", false);
                if(init){
                    boolean login = bundle.getBoolean("login", false);
                    int type = bundle.getInt("type", AppMode.CLIENT);
                    String name = bundle.getString("name");
                    String number = bundle.getString("number");
                    User user = new User();
                    user.setName(name);
                    user.setNumber(number);

                    TokenManager.handleOnLoginSignUp(MainActivity.this);
                    AppMode.updateAppMode(MainActivity.this, type);
                    UserManager.setUser(MainActivity.this, user);

                    if(!login){
                        NotifyManager.notifyAdmin_newUser(MainActivity.this, user.getName(), user.getNumber());
                    }
                }
            }
            init();
            getLocation(null);
        }else {
            startActivity(new Intent(this, SplashActivity.class));
        }

    }

    void getLocation(LocationUpdateListener listener) {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

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
                    LocationManager.updateLocation(MainActivity.this,
                            new GeoPoint(location.getLatitude(), location.getLongitude()));
                    if(listener!=null){
                        listener.onLocationUpdate(location.getLatitude(), location.getLongitude());
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            bufferListener = listener;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            return;
        }
        client.requestLocationUpdates(lr, callback, Looper.getMainLooper());
    }

    void init(){
        TokenManager.handle(this);
        appMode = AppMode.getAppMode(this);
        home_tv = findViewById(R.id.main_menu_home_tv);
        stats_tv = findViewById(R.id.main_menu_stats_tv);
        profile_tv = findViewById(R.id.main_menu_profile_tv);
        info_tv = findViewById(R.id.main_menu_info_tv);
        scan_fab = findViewById(R.id.main_scan_fab);
        setUp();
    }
    void setUp(){
        scan_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(appMode==AppMode.CLIENT){
                    openFragment(new QrFragment());
                }else{
                    openFragment(new Dist_QrFragment());
                }
                selectMenu(Menu.QR);
            }
        });
        home_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(appMode==AppMode.CLIENT){
                    openFragment(new HomeFragment());
                }else if(appMode==AppMode.DISTRIBUTOR){
                    openFragment(new Distributor_HomeFragment());
                }else {
                    openFragment(new Admin_HomeFragment());
                }
                selectMenu(Menu.HOME);
            }
        });
        stats_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(appMode==AppMode.CLIENT){
                    openFragment(new StatsFragment());
                }else if(appMode==AppMode.DISTRIBUTOR){
                    openFragment(new Distributor_StatsFragment());
                }else {
                    openFragment(new Admin_StatsFragment());
                }
                selectMenu(Menu.STATS);
            }
        });
        profile_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openFragment(new ProfileFragment());
                selectMenu(Menu.PROFILE);
            }
        });
        info_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectMenu(Menu.INFO);
            }
        });
        scan_fab.callOnClick();
    }
    void openFragment(Fragment fragment){
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragmentContainer_fl, fragment);
        fragmentTransaction.commit();
    }
    void selectMenu(Menu menu){
        View select, unSelect=null;
        if(currentSelected!=null){
            currentSelected.getCompoundDrawables()[1].setTint(getResources().getColor(R.color.menu_item_unSelected));
            currentSelected.setTextColor(getResources().getColor(R.color.menu_item_unSelected));
            unSelect = currentSelected;
        }else if(this.menu!=null){
            unSelect = scan_fab;
        }
        boolean animate = false;
        if(this.menu!=menu){
            animate=true;
        }
        this.menu = menu;
        if(menu==Menu.HOME){
            currentSelected = home_tv;
        }else if(menu==Menu.STATS){
            currentSelected = stats_tv;
        }else if(menu==Menu.PROFILE){
            currentSelected = profile_tv;
        }else if(menu==Menu.INFO){
            currentSelected = info_tv;
        }else{
            currentSelected = null;
        }
        if(currentSelected!=null){
            currentSelected.getCompoundDrawables()[1].setTint(getResources().getColor(R.color.menu_item_selected));
            currentSelected.setTextColor(getColor(R.color.menu_item_selected));
            select = currentSelected;
            if(animate){select.animate().scaleY(0.8f).scaleX(0.8f).setDuration(animationDuration).start();}
        }else{
            select = scan_fab;
            if(animate){select.animate().scaleY(1f).scaleX(1f).setDuration(animationDuration).start();}
        }
        if(unSelect!=null){
            if(animate){unSelect.animate().scaleY(0.5f).scaleX(0.5f).setDuration(animationDuration).start();}
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_PERMISSION_CODE && grantResults.length>0){
            if(grantResults[0] ==  PackageManager.PERMISSION_GRANTED){
                getLocation(bufferListener);
                bufferListener=null;
            }else {
                Utils.toast(this, "Permission Denied!");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //getLocation(null);
    }
}
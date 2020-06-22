package com.durstep.durstep;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.durstep.durstep.fragment.HomeFragment;
import com.durstep.durstep.fragment.ProfileFragment;
import com.durstep.durstep.fragment.QrFragment;
import com.durstep.durstep.fragment.StatsFragment;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.manager.TokenManager;
import com.durstep.durstep.model.Menu;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends BaseActivity {

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    TextView home_tv, stats_tv, profile_tv, info_tv;

    TextView currentSelected;

    FloatingActionButton scan_fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(DbManager.getUid()!=null){
            init();
        }else {
            startActivity(new Intent(this, SplashActivity.class));
        }
    }
    void init(){
        TokenManager.handle(this);
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
                openFragment(new QrFragment());
            }
        });
        home_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new HomeFragment());
                selectMenu(Menu.HOME);
            }
        });
        stats_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new StatsFragment());
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
        if(currentSelected!=null){
            currentSelected.getCompoundDrawables()[1].setTint(getResources().getColor(R.color.menu_item_unSelected));
            currentSelected.setTextColor(getResources().getColor(R.color.menu_item_unSelected));
        }
        if(menu==Menu.HOME){
            currentSelected = home_tv;
        }else if(menu==Menu.STATS){
            currentSelected = stats_tv;
        }else if(menu==Menu.PROFILE){
            currentSelected = profile_tv;
        }else{
            currentSelected = info_tv;
        }
        currentSelected.getCompoundDrawables()[1].setTint(getResources().getColor(R.color.menu_item_selected));
        currentSelected.setTextColor(getResources().getColor(R.color.menu_item_selected));
    }
}
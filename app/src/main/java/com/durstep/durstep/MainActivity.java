package com.durstep.durstep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.durstep.durstep.authentication.LoginActivity;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.manager.TokenManager;

public class MainActivity extends BaseActivity {

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
        setUp();
    }
    void setUp(){}
}
package com.durstep.durstep;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.durstep.durstep.authentication.SignUpActivity;

public class SplashActivity extends BaseActivity {

    TextView welcome_tv, tagline_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        welcome_tv = findViewById(R.id.intro_header_welcome_tv);
        tagline_tv = findViewById(R.id.intro_header_welcomeMessage_tv);

        welcome_tv.animate().scaleY(1).scaleX(1).setDuration(1000).start();
        tagline_tv.animate().alpha(1).setDuration(1000).start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, SignUpActivity.class));
                finishAffinity();
            }
        }, 1010);
    }
}
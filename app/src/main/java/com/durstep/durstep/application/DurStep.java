package com.durstep.durstep.application;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.durstep.durstep.manager.LocaleManager;

public class DurStep extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }
}
package com.durstep.durstep;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.durstep.durstep.manager.LocaleManager;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class BaseActivity extends AppCompatActivity {

    protected Button languageChangeButton;

    protected void changeLanguage(){
        String curr = LocaleManager.getLanguagePref(this);
        if(curr.equals(LocaleManager.ENGLISH)){
            setNewLocale(BaseActivity.this, LocaleManager.HINDI);
        }else{
            setNewLocale(BaseActivity.this, LocaleManager.ENGLISH);
        }
    }
    protected void hide(View v){
        v.setVisibility(View.GONE);
    }

    protected void show(View v){
        v.setVisibility(View.VISIBLE);
    }
    private void setNewLocale(AppCompatActivity mContext, String language) {
        LocaleManager.setNewLocale(this, language);
        Intent intent = mContext.getIntent();
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }
    protected void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    protected void log(String msg){
        Log.i("test", msg);
    }

    public boolean hasActiveInternetConnection() {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.i("test", "Error checking internet connection", e);
            }
        } else {
            Log.i("test", "No network available!");
        }
        return false;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    protected String getText(TextInputLayout til){
        return til.getEditText().getText().toString().trim();
    }

}


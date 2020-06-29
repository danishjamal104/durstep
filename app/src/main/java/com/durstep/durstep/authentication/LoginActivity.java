package com.durstep.durstep.authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.durstep.durstep.BaseActivity;
import com.durstep.durstep.MainActivity;
import com.durstep.durstep.R;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.manager.TokenManager;
import com.durstep.durstep.manager.UserManager;
import com.durstep.durstep.model.AppMode;
import com.durstep.durstep.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class LoginActivity extends BaseActivity {

    TextInputLayout number_til, pwd_til;

    MaterialButton login_btn;

    ProgressBar progressBar;

    boolean isLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }
    void init(){
        number_til = findViewById(R.id.login_number_til);
        pwd_til = findViewById(R.id.login_password_til);
        login_btn = findViewById(R.id.login_login_btn);
        progressBar = findViewById(R.id.login_progress_pb);
        setUp();
    }
    void setUp(){
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
            }
        });
    }
    private boolean validateInput(){
        String number = getText(number_til);
        String pwd =  getText(pwd_til);

        if(Utils.isValidNumber(number)){
            number_til.setErrorEnabled(false);
            if(Utils.isValidPassword(pwd)){
                pwd_til.setErrorEnabled(false);
                return true;
            }else{
                pwd_til.setError(getResources().getString(R.string.password_short_error));
                return false;
            }
        }else{
            number_til.setError(getString(R.string.username_error));
            return false;
        }
    }
    void logIn(){
        if(!validateInput()){
            return;
        }
        show(progressBar);
        String number = getText(number_til);
        String pwd = getText(pwd_til);

        DbManager.logIn(number + Utils.getDomain(), pwd, new FirebaseTask<User>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if(isSuccess){
                    isLoggedIn = true;
                }else{
                    Utils.longToast(LoginActivity.this, error);
                    hide(progressBar);
                }
            }

            @Override
            public void onSingleDataLoaded(User object) {
                if(object==null && isLoggedIn){
                    logOut();
                }else{
                    TokenManager.handleOnLoginSignUp(LoginActivity.this);
                    AppMode.updateAppMode(LoginActivity.this, object.getType());
                    UserManager.setUser(LoginActivity.this, object);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finishAffinity();
                }
                hide(progressBar);
            }

            @Override
            public void onMultipleDataLoaded(List<User> objects) {

            }
        });
    }
    void logOut(){
        DbManager.deletePushToken(this, new FirebaseTask<Void>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if(isSuccess){
                    DbManager.getmAuth().signOut();
                }else{
                    toast(error);
                }
            }

            @Override
            public void onSingleDataLoaded(Void object) {

            }

            @Override
            public void onMultipleDataLoaded(List<Void> objects) {

            }
        });
    }
}
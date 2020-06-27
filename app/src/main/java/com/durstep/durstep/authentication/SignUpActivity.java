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
import com.durstep.durstep.manager.NotifyManager;
import com.durstep.durstep.manager.TokenManager;
import com.durstep.durstep.manager.UserManager;
import com.durstep.durstep.model.AppMode;
import com.durstep.durstep.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class SignUpActivity extends BaseActivity {

    TextInputLayout name_til, number_til, pwd_til, confirmPwd_til;

    MaterialButton signUp_btn;

    LinearLayout login_ll;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();
    }
    void init(){
        name_til = findViewById(R.id.signUp_name_til);
        number_til = findViewById(R.id.signUp_number_til);
        pwd_til = findViewById(R.id.signUp_password_til);
        confirmPwd_til = findViewById(R.id.signUp_confirmPassword_til);
        signUp_btn = findViewById(R.id.signUp_signUp_btn);
        login_ll = findViewById(R.id.signUp_loginView_ll);
        progressBar = findViewById(R.id.signUp_progress_pb);
        setUp();
    }
    void setUp(){
        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
        login_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
    }
    void signUp(){
        if(!validateInput()){
            return;
        }
        show(progressBar);
        String name = getText(name_til);
        String number = getText(number_til);
        String pwd = getText(pwd_til);

        User user = new User(name, number, User.CLIENT);
        DbManager.signUp(user, number + Utils.getDomain(), pwd, new FirebaseTask<Void>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if(isSuccess){
                    TokenManager.handleOnLoginSignUp(SignUpActivity.this);
                    AppMode.updateAppMode(SignUpActivity.this, AppMode.CLIENT);
                    UserManager.setUser(SignUpActivity.this, user);
                    NotifyManager.notifyAdmin_newUser(SignUpActivity.this, user.getName(), user.getNumber());
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finishAffinity();
                }else{
                    toast(error);
                }
                hide(progressBar);
            }

            @Override
            public void onSingleDataLoaded(Void object) {

            }

            @Override
            public void onMultipleDataLoaded(List<Void> objects) {

            }
        });
    }
    boolean validateInput(){
        String name = getText(name_til);
        String number = getText(number_til);
        String pwd = getText(pwd_til);
        String confirm_pwd = getText(confirmPwd_til);

        if(name.equals("")){
            name_til.setError(getString(R.string.profile_error_new_name));
            return false;
        }else{
            name_til.setErrorEnabled(false);
        }
        if(!Utils.isValidNumber(number)){
            number_til.setError(getString(R.string.username_error));
            return false;
        }else{
            number_til.setErrorEnabled(false);
        }
        if(!Utils.isValidPassword(pwd)){
            pwd_til.setError(getString(R.string.password_short_error));
            return false;
        }else{
            pwd_til.setErrorEnabled(false);
        }
        if(confirm_pwd.equals("")){
            confirmPwd_til.setError(getString(R.string.profile_error_new_name));
            return false;
        }else{
            confirmPwd_til.setErrorEnabled(false);
        }
        if(!pwd.equals(confirm_pwd)){
            pwd_til.setError(getString(R.string.password_match_error));
            confirmPwd_til.setError(getString(R.string.password_match_error));
            return false;
        }else{
            pwd_til.setErrorEnabled(false);
            confirmPwd_til.setErrorEnabled(false);
        }

        return true;
    }


}
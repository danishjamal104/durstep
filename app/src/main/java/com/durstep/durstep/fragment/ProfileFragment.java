package com.durstep.durstep.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.durstep.durstep.R;
import com.durstep.durstep.helper.CustomDialog;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.ProfileUpdateTask;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.manager.UserManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import io.grpc.okhttp.internal.Util;

public class ProfileFragment extends Fragment {

    TextInputLayout name_til, number_til, pwd_til, confirmPwd_til;
    MaterialButton update_bt;
    TextView edit_tv;

    ProgressBar progressBar;

    String name, number;
    String new_name, new_number, new_pwd, new_confirmPwd;

    boolean isNumberUpdated = false;
    boolean isNameUpdated = false;
    boolean isPasswordUpdated = false;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        name_til = v.findViewById(R.id.profile_name_til);
        number_til = v.findViewById(R.id.profile_number_til);
        pwd_til = v.findViewById(R.id.profile_password_til);
        confirmPwd_til = v.findViewById(R.id.profile_confirmPassword_til);
        update_bt = v.findViewById(R.id.profile_update_btn);
        edit_tv = v.findViewById(R.id.profile_edit_tv);
        progressBar = v.findViewById(R.id.profile_progress_pb);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        name = UserManager.getName(getContext());
        number = UserManager.getNumber(getContext());
        setText(name_til, name);
        setText(number_til, number);

        update_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_name = getText(name_til);
                new_number = getText(number_til);
                new_pwd = getText(pwd_til);
                new_confirmPwd = getText(confirmPwd_til);
                if(validate() && (isNameUpdated || isNumberUpdated || isPasswordUpdated)){
                    reAuthenticate();
                }
            }
        });

        makeNonEditable();
    }
    boolean validate(){
        if(new_name.equals("")){
            name_til.setError(getString(R.string.profile_error_new_name));
            return false;
        }else{
            name_til.setErrorEnabled(false);
            if(!new_name.equals(name)){
                isNameUpdated = true;
            }
        }
        if(!Utils.isValidNumber(new_number)){
            number_til.setError(getString(R.string.username_error));
            return false;
        }else{
            number_til.setErrorEnabled(false);
            if(!new_number.equals(number)){
                isNumberUpdated = true;
            }
        }
        if(!new_pwd.equals("") && !Utils.isValidPassword(new_pwd)){
            pwd_til.setError(getString(R.string.password_short_error));
            return false;
        }else if(!new_pwd.equals("") && !new_pwd.equals(new_confirmPwd)){
            pwd_til.setError(getString(R.string.password_match_error));
            confirmPwd_til.setError(getString(R.string.password_match_error));
            return false;
        }else{
            pwd_til.setErrorEnabled(false);
            confirmPwd_til.setErrorEnabled(false);
            isPasswordUpdated = !new_pwd.equals("");
        }
        return true;
    }
    void reAuthenticate(){
        CustomDialog dialog = new CustomDialog(getContext());
        dialog.setInputEnabled(true);
        dialog.setTitle(getString(R.string.profile_authenticate));
        dialog.setHint(getString(R.string.profile_reAuth_hint));
        dialog.setPositive(getString(R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reAuthServerAction(dialog);
                v.setEnabled(false);
            }
        });
        dialog.setNegative(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
            }
        });

        dialog.show();
    }
    void reAuthServerAction(CustomDialog dialog){
        String oldPwd = dialog.getPassword();
        if(!Utils.isValidPassword(oldPwd)){
            dialog.setError(getString(R.string.password_short_error));
            return;
        }
        DbManager.getUser().reauthenticate(DbManager.getCred(number, oldPwd)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    dialog.setError(getString(R.string.wrong_credential));
                    return;
                }
                dialog.hide();
                enableLoading();
                dialog.setError(null);
                if(isNameUpdated){
                     DbManager.updateName(new_name, new ProfileUpdateTask() {
                         @Override
                         public void onComplete(boolean result, String data) {
                             if(result){
                                 UserManager.setName(getContext(), data);
                                 setText(name_til, data);
                                 if(!isPasswordUpdated&&!isNumberUpdated){
                                     //Utils.toast(getContext(), getString(R.string.profile_updated));
                                 }else{
                                     complexServerAction(oldPwd);
                                 }
                             }else{
                                 Utils.toast(getContext(), data);
                                 makeNonEditable();
                                 disableLoading();
                             }
                         }
                     });
                }else{
                    complexServerAction(oldPwd);
                }
            }
        });
    }
    void complexServerAction(String oldPwd){
        if(isNumberUpdated && !isPasswordUpdated){
            DbManager.updateNumber(new_number, new ProfileUpdateTask() {
                @Override
                public void onComplete(boolean result, String data) {
                    if(result){
                        UserManager.setNumber(getContext(), data);
                        setText(number_til, data);
                        Utils.toast(getContext(), getString(R.string.profile_updated));
                    }else{
                        Utils.toast(getContext(), data);
                    }
                    disableLoading();
                    makeNonEditable();
                }
            });
        }else if(!isNumberUpdated && isPasswordUpdated){
            DbManager.updatePassword(new_pwd, new ProfileUpdateTask() {
                @Override
                public void onComplete(boolean result, String data) {
                    if(!result){
                        Utils.toast(getContext(), data);
                    }else{
                        Utils.toast(getContext(), getString(R.string.profile_updated));
                    }
                    makeNonEditable();
                    disableLoading();
                }
            });
        }else{
            DbManager.updateNumber(new_number, new ProfileUpdateTask() {
                @Override
                public void onComplete(boolean result, String data) {
                    if(result){
                        UserManager.setNumber(getContext(), data);
                        setText(number_til, data);
                        DbManager.getUser().reauthenticate(DbManager.getCred(new_number, oldPwd))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            DbManager.updatePassword(new_pwd, new ProfileUpdateTask() {
                                                @Override
                                                public void onComplete(boolean result, String data) {
                                                    if(!result){
                                                        Utils.toast(getContext(), data);
                                                    }else{
                                                        Utils.toast(getContext(), getString(R.string.profile_updated));
                                                    }
                                                    makeNonEditable();
                                                    disableLoading();
                                                }
                                            });
                                        }else {
                                            Utils.toast(getContext(), task.getException().getLocalizedMessage());
                                            disableLoading();
                                        }
                                    }
                                });
                    }else{
                        Utils.toast(getContext(), data);
                        makeNonEditable();
                        disableLoading();
                    }
                }
            });
        }
    }

    void enableLoading(){
        progressBar.setVisibility(View.VISIBLE);
    }
    void disableLoading(){
        progressBar.setVisibility(View.GONE);
    }
    void makeEditable(){
        name_til.setEnabled(true);
        number_til.setEnabled(true);
        name_til.setEndIconActivated(true);
        number_til.setEndIconActivated(true);
        pwd_til.setVisibility(View.VISIBLE);
        confirmPwd_til.setVisibility(View.VISIBLE);
        update_bt.setVisibility(View.VISIBLE);
        edit_tv.setText(getString(R.string.cancel));
        edit_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeNonEditable();
            }
        });
    }
    void makeNonEditable(){
        name_til.setEnabled(false);
        number_til.setEnabled(false);
        name_til.setEndIconActivated(false);
        number_til.setEndIconActivated(false);
        pwd_til.setVisibility(View.GONE);
        confirmPwd_til.setVisibility(View.GONE);
        update_bt.setVisibility(View.GONE);
        edit_tv.setText(getString(R.string.edit));
        edit_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEditable();
            }
        });
    }
    String getText(TextInputLayout til){
        return til.getEditText().getText().toString().trim();
    }
    void setText(TextInputLayout til, String text){
        til.getEditText().setText(text);
    }
}
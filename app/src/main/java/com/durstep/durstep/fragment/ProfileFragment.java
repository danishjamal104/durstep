package com.durstep.durstep.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.durstep.durstep.R;
import com.durstep.durstep.helper.CustomDialog;
import com.durstep.durstep.manager.UserManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileFragment extends Fragment {

    TextInputLayout name_til, number_til, pwd_til, confirmPwd_til;
    MaterialButton update_bt;
    TextView edit_tv;

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
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        name_til.getEditText().setText(UserManager.getName(getContext()));
        number_til.getEditText().setText(UserManager.getNumber(getContext()));

        update_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialog dialog = new CustomDialog(getContext());
                dialog.setInputEnabled(true);
                dialog.setTitle("Re Authentication");
                dialog.setHint("Enter Old Password");
                dialog.setPositive("Ye lo", null);
                dialog.setNegative("Nai", null);

                dialog.show();
            }
        });

        makeNonEditable();
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
}
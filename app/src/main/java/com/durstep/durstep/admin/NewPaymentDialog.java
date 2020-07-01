package com.durstep.durstep.admin;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.durstep.durstep.R;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.model.Payment;
import com.durstep.durstep.model.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewPaymentDialog {

    Context context;
    Dialog dialog;
    String month;
    TextInputLayout amount_til;
    Button cancel_bt, submit_bt;
    RecyclerView user_rv;
    SimpleUserAdapter adapter;

    ProgressBar progressBar;

    FirebaseTask<Void> fbTask;


    public NewPaymentDialog(Context context, String month) {
        this.context = context;
        this.month = month;
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.new_payment_layout);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        amount_til = dialog.findViewById(R.id.new_pay_amount_til);
        cancel_bt = dialog.findViewById(R.id.new_pay_cancel_bt);
        submit_bt = dialog.findViewById(R.id.new_pay_submit_bt);
        user_rv = dialog.findViewById(R.id.new_pay_user_rv);
        user_rv.setHasFixedSize(false);
        user_rv.setLayoutManager(new LinearLayoutManager(context));
        adapter = new SimpleUserAdapter(context);
        user_rv.setAdapter(adapter);
        cancel_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        submit_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> a = adapter.getSelected();
                if(a.size()>0){
                    submit(a);
                }
            }
        });
    }
    public void start(ProgressBar progressBar, FirebaseTask<Void> fbTask){
        this.progressBar = progressBar;
        this.fbTask = fbTask;
        this.progressBar.setVisibility(View.VISIBLE);
        loadUser();
    }
    private void loadUser(){
        DbManager.getAllUserOfType(User.CLIENT, new FirebaseTask<User>() {
            @Override
            public void onComplete(boolean isSuccess, String error) {
                if(!isSuccess){
                    Utils.log(error);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onSingleDataLoaded(User object) {

            }

            @Override
            public void onMultipleDataLoaded(List<User> objects) {
                adapter.addAll(objects);
                progressBar.setVisibility(View.GONE);
                dialog.show();
            }
        });
    }
    private void submit(List<String> userIds){
        String amount = amount_til.getEditText().getText().toString().trim();
        if(!Utils.isDouble(amount)){
            amount_til.setError(context.getString(R.string.invalid_amount));
            return;
        }
        double rupee = Double.parseDouble(amount);
        amount_til.setErrorEnabled(false);



        String doc_name;
        if(month!=null){
            doc_name = String.format("%s_%s", month.substring(0, 3).toUpperCase(), Utils.getDateTimeInFormat(new Timestamp(new Date()), "yyyy"));
        }else{
            doc_name = Utils.getDateTimeInFormat(new Timestamp(new Date()), "MMM_yyyy").toUpperCase();
        }

        List<Payment> payments = new ArrayList<>();
        for(String id: userIds){
            Payment p = new Payment(){{
                setId(id);
                setAmount(rupee);
                setMonth(doc_name);
                setUser(DbManager.getmRef().document("user/"+id));
                setTime(new Timestamp(new Date()));
            }};
            payments.add(p);
        }
        DbManager.addPayments(payments, fbTask);
        progressBar.setVisibility(View.VISIBLE);
        dialog.dismiss();
    }
}

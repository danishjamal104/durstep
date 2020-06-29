package com.durstep.durstep.model;

import android.widget.Adapter;

import androidx.annotation.NonNull;

import com.durstep.durstep.adapter.PaymentAdapter;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.manager.DbManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;

public class Payment {
    String id;
    double amount;
    Timestamp time;
    String month; // JAN_2020
    DocumentReference user;
    public Payment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public DocumentReference getUser() {
        return user;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    @Exclude
    String name, number;
    @Exclude
    User offline_user;

    @Exclude
    public String getUserName(){
        if(name==null&&number==null){
            return "___, ___";
        }else{
            return String.format("%s, %s", name, number);
        }
    }

    @Exclude
    public void loadUser(PaymentAdapter adapter){
        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    User u = task.getResult().toObject(User.class);
                    offline_user = u;
                    name = u.getName();
                    number = u.getNumber();
                }else{
                    name=null;
                    number=null;
                    Utils.log(task.getException().getLocalizedMessage());
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

}

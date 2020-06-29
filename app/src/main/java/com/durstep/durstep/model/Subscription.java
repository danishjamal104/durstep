package com.durstep.durstep.model;

import androidx.annotation.NonNull;

import com.durstep.durstep.adapter.DeliveryAdapter;
import com.durstep.durstep.adapter.SubscriptionAdapter;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.manager.DbManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;

public class Subscription {

    String uId;
    String sId;
    Timestamp sDate;
    String deliveryTime;
    Double amount;
    DocumentReference active;

    public Subscription() {
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getsId() {
        return sId;
    }

    public void setsId(String sId) {
        this.sId = sId;
    }

    public Timestamp getsDate() {
        return sDate;
    }

    public void setsDate(Timestamp sDate) {
        this.sDate = sDate;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public DocumentReference getActive() {
        return active;
    }

    public void setActive(DocumentReference active) {
        this.active = active;
    }

    @Exclude
    String name, number;
    @Exclude
    User offline_user;
    @Exclude
    public DeliveryMarker deliveryMarker;

    @Exclude
    public String getUserName(){
        if(name==null&&number==null){
            return "___, ___";
        }else{
            return String.format("%s, %s", name, number);
        }
    }

    public void loadUser(DeliveryAdapter adapter){
        DbManager.getmRef().document(String.format("user/%s", uId)).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            offline_user = task.getResult().toObject(User.class);
                            name = offline_user.getName();
                            number = offline_user.getNumber();
                            adapter.notifyDataSetChanged();
                        }else{
                            Utils.log(task.getException().getLocalizedMessage());
                        }
                    }
                });
    }
    public static enum DeliveryMarker{
        SCHEDULED, ON_WAY, DELIVERED
    }
}

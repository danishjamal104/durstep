package com.durstep.durstep.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

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
}

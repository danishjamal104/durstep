package com.durstep.durstep.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class ActiveDelivery {

    int total;
    int pending;
    int delivered;
    List<DocumentReference> delivered_list;
    List<DocumentReference> subscription_list;
    GeoPoint location;

    public ActiveDelivery() {
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPending() {
        return pending;
    }

    public void setPending(int pending) {
        this.pending = pending;
    }

    public int getDelivered() {
        return delivered;
    }

    public void setDelivered(int delivered) {
        this.delivered = delivered;
    }

    public List<DocumentReference> getDelivered_list() {
        return delivered_list;
    }

    public void setDelivered_list(List<DocumentReference> delivered_list) {
        this.delivered_list = delivered_list;
    }

    public List<DocumentReference> getSubscription_list() {
        return subscription_list;
    }

    public void setSubscription_list(List<DocumentReference> subscription_list) {
        this.subscription_list = subscription_list;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    @Exclude
    public boolean isDeliveryCompleted(){
        return pending==0;
    }
}

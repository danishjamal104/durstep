package com.durstep.durstep.model;

import com.google.firebase.firestore.Exclude;

public class User {
    public final static int CLIENT = 0;
    public final static int ADMIN = 1;
    public final static int DISTRIBUTOR = -1;

    String name;
    String number;
    int type = 0;
    int totalOrder = 0;
    String uid;

    public User() {
    }

    public User(String name, String number, int type, String uid) {
        this.name = name;
        this.number = number;
        this.type = type;
        this.uid = uid;
    }

    public User(String name, String number, int type) {
        this.name = name;
        this.number = number;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public int getTotalOrder() {
        return totalOrder;
    }

    public void setTotalOrder(int totalOrder) {
        this.totalOrder = totalOrder;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Exclude
    public boolean isAdmin(){
        return type==User.ADMIN;
    }
}

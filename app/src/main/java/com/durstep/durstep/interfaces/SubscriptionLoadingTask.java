package com.durstep.durstep.interfaces;

import android.util.Pair;

import com.durstep.durstep.model.Subscription;
import com.durstep.durstep.model.User;

import java.util.List;

public interface SubscriptionLoadingTask {
    void onSubscriptionLoaded(List<Pair<Subscription, User>> pairList);
    void onError(String error);
}

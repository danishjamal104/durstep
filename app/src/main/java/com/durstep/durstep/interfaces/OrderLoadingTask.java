package com.durstep.durstep.interfaces;

import com.durstep.durstep.model.Order;

import java.util.List;
import java.util.Map;

public interface OrderLoadingTask {
    void onComplete(boolean isSuccess, String error);
    void onMetaDataLoaded(Map<String, Object> md);
    void onOrdersLoaded(List<Order> orders);
}

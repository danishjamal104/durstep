package com.durstep.durstep.interfaces;

import com.durstep.durstep.model.Order;

import java.util.List;
import java.util.Map;

public interface StatsLoadingTask<T> {
    void onComplete(boolean isSuccess, String error);
    void onMetaDataLoaded(Map<String, Object> md);
    void onListLoaded(List<T> orders);
}

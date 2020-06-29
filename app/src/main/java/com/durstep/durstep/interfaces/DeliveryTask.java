package com.durstep.durstep.interfaces;

import java.util.List;

public interface DeliveryTask<T, E> {
    public void onComplete(boolean isSuccess, String error);
    public void onSingleDataLoaded(T object);
    public void onExtraDataLoaded(List<E> eList, int mode);
}

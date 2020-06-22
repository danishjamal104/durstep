package com.durstep.durstep.interfaces;

import java.util.List;

public interface FirebaseTask<T> {
    public void onComplete(boolean isSuccess, String error);
    public void onSingleDataLoaded(T object);
    public void onMultipleDataLoaded(List<T> objects);
}

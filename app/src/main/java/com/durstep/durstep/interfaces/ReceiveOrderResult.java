package com.durstep.durstep.interfaces;

public interface ReceiveOrderResult {
    void onResult(int responseCode, String msg, String data);
    void onError(String errorMessage);
}

package com.durstep.durstep.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.durstep.durstep.R;
import com.durstep.durstep.helper.Utils;
import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.manager.NotifyManager;
import com.google.gson.Gson;
import com.google.zxing.Result;

import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrFragment extends Fragment {

    private final static int CAMERA_PERMISSION_CODE = 6392;

    TextView info_tv;
    ViewGroup frameLayout;

    ZXingScannerView mScannerView;

    public QrFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_qr, container, false);
        info_tv = view.findViewById(R.id.qr_scan_info_tv);
        frameLayout = view.findViewById(R.id.qr_scan_content_fl);
        mScannerView = new ZXingScannerView(getContext());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
            @Override
            public void handleResult(Result rawResult) {
                qrResult(rawResult.getText());
                stopCamera();
            }
        });
        startCamera();
    }

    com.durstep.durstep.model.Response unpack(String json){
        return new Gson().fromJson(json, com.durstep.durstep.model.Response.class);
    }

    void qrResult(String qrRes){
        qrRes = qrRes.trim();
        if(!Utils.isValidLitre(qrRes)){
            return;
        }
        double litre = Double.parseDouble(qrRes);

        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = String.format(Utils.ORDER_RECEIVE_URL, DbManager.getUid(), litre);

        Utils.log(url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        com.durstep.durstep.model.Response result = unpack(response);
                        if(result.getStatus()==204){
                            NotifyManager.sendDeliveryConfirmation(getContext(), result.getData(), litre);
                        }
                        Utils.log(""+result.getStatus());
                        Utils.log(result.getMsg());
                        Utils.log(""+result.getData());
                        showOrderReceiveDialog(result);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getLocalizedMessage() != null) {
                    Utils.log(error.getLocalizedMessage());
                }
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }
    void showOrderReceiveDialog(com.durstep.durstep.model.Response result){
        String title;
        if(result.getStatus()==200 || result.getStatus()==202 || result.getStatus()==204){
            title = getString(R.string.success);
        }else{
            title = getString(R.string.server_error);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(result.getMsg());
        builder.setPositiveButton(getString(R.string.ok), null);

        builder.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCamera();
    }

    void startCamera(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
        }else{
            frameLayout.addView(mScannerView);
            mScannerView.startCamera(0);
        }
    }
    void stopCamera(){
        mScannerView.stopCamera();
    }

    private void requestPermission(String permission, int code){
        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)){
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, code);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    public void onStart() {
        super.onStart();
    }


}
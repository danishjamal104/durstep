package com.durstep.durstep.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

import com.durstep.durstep.R;
import com.google.zxing.Result;

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

            }
        });
        startCamera();
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
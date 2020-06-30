package com.durstep.durstep.fragment.qr;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.durstep.durstep.R;
import com.durstep.durstep.helper.Utils;

public class Dist_QrFragment extends Fragment {

    Spinner litre_sp;
    ImageView qrCode_iv;

    public Dist_QrFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dist__qr, container, false);
        litre_sp = v.findViewById(R.id.dist_qr_litre_spinner);
        qrCode_iv = v.findViewById(R.id.dist_qr_code_iv);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.litres, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        litre_sp.setAdapter(adapter);
        litre_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String amount = litre_sp.getSelectedItem().toString().split(" ")[0];
                Bitmap bt = Utils.getLitreBitmap(amount);
                qrCode_iv.setImageBitmap(bt);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
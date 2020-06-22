package com.durstep.durstep.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public final static String ADMIN_ID = "ZXCfMorVsyXZUS4oLLHy08T1Hwn2";

    public static boolean isValidNumber(int number){
        return isValidNumber(""+number);
    }
    public static boolean isValidNumber(String number){
        if(number.equals("")){
            return false;
        }
        number = number.replaceAll("\\s", "");
        if(number.length()==10){
            return true;
        }
        return false;
    }
    public static boolean isValidPassword(String pwd){
        if(pwd.length()<8){
            return false;
        }
        return true;
    }
    public static String formatNumber(int number, int returnType){
        return formatNumber(""+number, returnType);
    }
    public static String formatNumber(String number, int returnType){

        // info: return type 0 for with country code, and -1 for without country code

        /*
         * possible cases
         * 1. 9264966639
         * 2. 09450546077
         * 3. +918196853905
         */
        number = number.replaceAll("\\s", "");

        String reverse = "";
        for(int i=number.length()-1; i>=0; i--){
            if(reverse.length()==10){
                break;
            }
            reverse += number.charAt(i);
        }

        number = "";

        for(int i=reverse.length()-1; i>=0; i--){
            number += reverse.charAt(i);
        }

        if(returnType==0){
            return "+91"+number;
        }
        return number;
    }
    public final static String getDomain(){
        return "@durstep.com";
    }
    public static String getDate(com.google.firebase.Timestamp timestamp){
        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy");
        return sfd.format(timestamp.toDate());
    }

    public static String formatTime(int hoursOfDay, int minute){
        return ""+hoursOfDay+":"+minute;
    }
    public static boolean isValidTime(String time){
        // time = "hh:mm"
        String[] hh_mm = time.split(":");
        if(hh_mm.length!=2){
            return false;
        }

        try{
            int hh = Integer.parseInt(hh_mm[0]);
            int mm = Integer.parseInt(hh_mm[1]);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public static String getDateTime(com.google.firebase.Timestamp timestamp){
        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sfd.format(timestamp.toDate());
    }

    public static String extractPhoneFromEmail(String email){
        // 9919114819@xyz.com
        return email.split("@")[0];
    }

    public static Bitmap getQrBitmap(String litre){
        String uid = FirebaseAuth.getInstance().getUid();
        String[] a = {uid, litre};
        String qrText = new Gson().toJson(a);

        Bitmap result = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(qrText, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            result = barcodeEncoder.createBitmap(bitMatrix);
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isDouble(String text){
        try{
            double a = Double.parseDouble(text);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public static void toast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
    public static void log(String msg){
        Log.i("test", msg);
    }
}
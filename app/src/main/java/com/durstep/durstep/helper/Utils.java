package com.durstep.durstep.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.durstep.durstep.manager.DbManager;
import com.durstep.durstep.model.Subscription;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.GeoPoint;
import com.google.firestore.v1.DocumentTransform;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Utils {

    public final static String ADMIN_ID = "X1XXmSzO1PVgRcDU69j9F8PLi1V2";

    public final static int LOCATION_PERMISSION_CODE = 372;
    public final static double[] available_pack = {0.5,1,1.5,2,2.5,3,3.5,4,4.5,5};

    public final static String ORDER_RECEIVE_URL = "https://us-central1-durstep-7e7a8.cloudfunctions.net/scan?to=%s&l=%s";
    public final static String NOTIFICATION_URL = "https://us-central1-durstep-7e7a8.cloudfunctions.net/notify?to=%s&title=%s&msg=%s&type=%s";

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
    public static String getTime(com.google.firebase.Timestamp timestamp){
        SimpleDateFormat sfd = new SimpleDateFormat("hh:mm aa");
        return sfd.format(timestamp.toDate());
    }
    public static String getDateTimeInFormat(Timestamp timestamp, String frm){
        SimpleDateFormat sfd = new SimpleDateFormat(frm);
        return sfd.format(timestamp.toDate());
    }

    public static boolean isValidLitre(String s){
        boolean res = false;
        double l;
        try{
            l = Double.parseDouble(s);
        }catch (Exception e){
            return res;
        }
        for(int i=0; i<available_pack.length; i++){
            if(l == available_pack[i]){
                res = true;
                break;
            }
        }
        return res;
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
    public static String getEmailFromNUmber(Context context, String number){
        return String.format("%s%s", number, getDomain());
    }

    public static String getUserIdFromSubscriptionRef(DocumentReference subsRef){
        // subsRef = user/{id}/subscription/{sId}
        String[] paths = subsRef.getPath().split("/");
        return (paths[1]);
    }

    public static float calculateDistance(GeoPoint p1, GeoPoint p2){
        Location locationA = new Location("point A");
        locationA.setLatitude(p1.getLatitude());
        locationA.setLongitude(p1.getLongitude());

        Location locationB = new Location("point B");
        locationB.setLatitude(p2.getLatitude());
        locationB.setLongitude(p2.getLongitude());

        return locationA.distanceTo(locationB);
    }

    public static double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f/Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    public static void customTimestamp(){
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date);


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

    public static Bitmap getLitreBitmap(String litre){
        Bitmap result = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(litre, BarcodeFormat.QR_CODE, 400, 400);
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

    public static List<DocumentReference> getSubsRefFromObject(List<Subscription> subscriptions){
        if(subscriptions==null||subscriptions.size()==0){
            return null;
        }
        List<DocumentReference> result = new ArrayList<>();

        for(Subscription s: subscriptions){
            result.add(DbManager.getmRef().document("user/"+s.getuId()+"/subscriptions/"+s.getsId()));
        }

        return result;
    }

    public static int getMonthlyRate(double ratePerLitre, double litre){
        double perDayCost = ratePerLitre*litre;
        double month_30 = perDayCost*30;
        double month_31 = perDayCost*31;

        return (int)((month_30+month_31)/2);
    }

    public static void toast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
    public static void longToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
    public static void log(String msg){
        Log.i("test", msg);
    }
}
package com.durstep.durstep.manager;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.interfaces.SubscriptionLoadingTask;
import com.durstep.durstep.model.ActiveDelivery;
import com.durstep.durstep.model.Subscription;
import com.durstep.durstep.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;

public class DbManager {

    private static FirebaseAuth mAuth;
    private static FirebaseFirestore mRef;
    private static DocumentReference userRef;

    public static void updatePushToken(Context context){
        // this method is design to be called only by TokenManager
        String token = TokenManager.getPushToken(context);
        if(token==null){
            return;
        }
        userRef.update("push_token", token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i("test", "onComplete: done");
                }else{
                    Log.i("test", "onComplete: " +task.getException().getLocalizedMessage());
                }
            }
        });
    }
    public static void deletePushToken(Context context, FirebaseTask<Void> firebaseTask){
        // this method is design to be called only by AuthManager
        userRef.update("push_token", null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    firebaseTask.onComplete(true, null);
                }else{
                    firebaseTask.onComplete(false, task.getException().getLocalizedMessage());
                }
            }
        });
    }
    public static void logIn(String id, String pwd, FirebaseTask<User> firebaseTask){
        getmAuth().signInWithEmailAndPassword(id, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    getCurrentUser(firebaseTask);
                }else {
                   firebaseTask.onComplete(false, task.getException().getLocalizedMessage());
                }
            }
        });
    }
    public  static void signUp(User user, String id, String pwd, FirebaseTask<Void> firebaseTask){
        getmAuth().createUserWithEmailAndPassword(id, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName("Guest")
                            .build();
                    getmAuth().getCurrentUser().updateProfile(profileUpdates);
                    createUser(user, firebaseTask);
                }else{
                    firebaseTask.onComplete(false, task.getException().getLocalizedMessage());
                }
            }
        });
    }
    public static void getCurrentUser(FirebaseTask<User> userFirebaseTask){
        getUserRef().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    userFirebaseTask.onSingleDataLoaded(task.getResult().toObject(User.class));
                }else {
                    userFirebaseTask.onSingleDataLoaded(null);
                }
            }
        });
    }
    public static void getAllUser(FirebaseTask<User> firebaseTask){
        getmRef().collection("user").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().getDocuments().size()==0){
                        firebaseTask.onComplete(true, null);
                    }else{
                        List<User> users = new ArrayList<>();
                        for(DocumentSnapshot snapshot: task.getResult()){
                            users.add(snapshot.toObject(User.class));
                        }
                        firebaseTask.onMultipleDataLoaded(users);
                    }
                }else {
                    firebaseTask.onComplete(false, task.getException().getLocalizedMessage());
                }
            }
        });
    }
    public static void getAllUserOfType(int type, FirebaseTask<User> firebaseTask){
        getmRef().collection("user")
                .whereEqualTo("type", type)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().getDocuments().size()==0){
                        firebaseTask.onComplete(true, null);
                    }else{
                        List<User> users = new ArrayList<>();
                        for(DocumentSnapshot snapshot: task.getResult()){
                            users.add(snapshot.toObject(User.class));
                        }
                        firebaseTask.onMultipleDataLoaded(users);
                    }
                }else {
                    firebaseTask.onComplete(false, task.getException().getLocalizedMessage());
                }
            }
        });
    }
    public static void createUser(User user, FirebaseTask<Void> userFirebaseTask){
        user.setUid(getUid());
        getUserRef().set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    userFirebaseTask.onComplete(true, null);
                }else {
                    userFirebaseTask.onComplete(false, task.getException().getLocalizedMessage());
                }
            }
        });
    }
    public static void createNewSubscription(Subscription subscription, FirebaseTask<Void> firebaseTask){
        getUserRef().collection("subscriptions").document(subscription.getsId()).set(subscription)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    firebaseTask.onComplete(true, null);
                }
                else{
                    firebaseTask.onComplete(false, task.getException().getLocalizedMessage());
                }
            }
        });
    }
    public static void getAllSubscriptionOfCurrentUser(FirebaseTask<Subscription> subscriptionFirebaseTask){
        getUserRef().collection("subscriptions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().getDocuments().size()==0){
                                subscriptionFirebaseTask.onComplete(true, null);
                            }else{
                                List<Subscription> subscriptionList = new ArrayList<>();
                                for(DocumentSnapshot snapshot: task.getResult()){
                                    subscriptionList.add(snapshot.toObject(Subscription.class));
                                }
                                subscriptionFirebaseTask.onMultipleDataLoaded(subscriptionList);
                            }
                        }else{
                            subscriptionFirebaseTask.onComplete(false, task.getException().getLocalizedMessage());
                        }
                    }
                });
    }
    public static void getSubscriptionOfCurrentUser(String sId, FirebaseTask<Subscription> fbTask){
        getUserRef().collection("subscriptions").document(sId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    fbTask.onSingleDataLoaded(task.getResult().toObject(Subscription.class));
                }else{
                    fbTask.onComplete(false, task.getException().getLocalizedMessage());
                }
            }
        });

    }
    public static void getSubscription(String uid, FirebaseTask<Subscription> subscriptionFirebaseTask){
        getmRef().collection("user/"+uid+"/subscriptions").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().getDocuments().size()==0){
                                subscriptionFirebaseTask.onComplete(true, null);
                            }else{
                                List<Subscription> subscriptionList = new ArrayList<>();
                                for(DocumentSnapshot snapshot: task.getResult()){
                                    subscriptionList.add(snapshot.toObject(Subscription.class));
                                }
                                subscriptionFirebaseTask.onMultipleDataLoaded(subscriptionList);
                            }
                        }else{
                            subscriptionFirebaseTask.onComplete(false, task.getException().getLocalizedMessage());
                        }
                    }
                });
    }
    public static void getPreviousDeliveryOfDistributor(String id, SubscriptionLoadingTask loadingTask){
        getmRef().document("user/"+id+"/pd/pd").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            ActiveDelivery delivery = task.getResult().toObject(ActiveDelivery.class);
                            getmRef().runTransaction(new Transaction.Function<List<Pair<Subscription, User>>>() {
                                @Nullable
                                @Override
                                public List<Pair<Subscription, User>> apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                    List<Pair<Subscription, User>> result = new ArrayList<>();

                                    String path = "user/";
                                    for(DocumentReference reference: delivery.getSubscription_list()){
                                        Subscription subscription = transaction.get(reference).toObject(Subscription.class);
                                        User user = transaction.get(DbManager.getmRef().document(path+subscription.getuId())).toObject(User.class);
                                        result.add(new Pair<>(subscription, user));
                                    }
                                    return result;
                                }
                            }).addOnCompleteListener(new OnCompleteListener<List<Pair<Subscription, User>>>() {
                                @Override
                                public void onComplete(@NonNull Task<List<Pair<Subscription, User>>> task) {
                                    if(task.isSuccessful()){
                                        loadingTask.onSubscriptionLoaded(task.getResult());
                                    }else{
                                        loadingTask.onError(task.getException().getLocalizedMessage());
                                    }
                                }
                            });
                        }else{
                            loadingTask.onError(task.getException().getLocalizedMessage());
                        }
                    }
                });
    }
    public static void createDelivery(String uId,ActiveDelivery delivery, FirebaseTask<Void> fbTask){
        getmRef().document("user/"+uId+"/pd/pd").set(delivery)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            fbTask.onComplete(true, null);
                        }else{
                            fbTask.onComplete(false, task.getException().getLocalizedMessage());
                        }
                    }
                });
    }
    public static void getScheduledDelivery(FirebaseTask<ActiveDelivery> fbTask){
        getUserRef().collection("pd").document("pd").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().getData()==null){
                                fbTask.onComplete(true, null);
                            }else{fbTask.onSingleDataLoaded(task.getResult().toObject(ActiveDelivery.class));}
                        }else{
                            fbTask.onComplete(false, task.getException().getLocalizedMessage());
                        }
                    }
                });
    }
    public static void getCurrentActiveDelivery(FirebaseTask<ActiveDelivery> fbTask){
        getmRef().document("active_delivery/"+getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().getData()==null){
                                fbTask.onComplete(true, null);
                            }else{fbTask.onSingleDataLoaded(task.getResult().toObject(ActiveDelivery.class));}
                        }else{
                            fbTask.onComplete(false, task.getException().getLocalizedMessage());
                        }
                    }
                });
    }
    public static void startDelivery(ActiveDelivery delivery, FirebaseTask<Void> fbTask){
        getmRef().document("active_delivery/"+getUid()).set(delivery)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            fbTask.onComplete(true, null);
                        }else {
                            fbTask.onComplete(false, null);
                        }
                    }
                });
    }
    public static void updateLocation(GeoPoint location, FirebaseTask<Void> fbTask){
        getmRef().document("active_delivery/"+getUid())
                .update("location", location).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    fbTask.onComplete(true, null);
                }else {
                    fbTask.onComplete(false, null);
                }
            }
        });
    }

    public static void getDeliveryLocation(DocumentReference reference, FirebaseTask<GeoPoint> fbTask){
        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    GeoPoint geoPoint = task.getResult().getGeoPoint("location");
                    fbTask.onSingleDataLoaded(geoPoint);
                }else{
                    fbTask.onComplete(false, task.getException().getLocalizedMessage());
                }
            }
        });
    }

    public static FirebaseAuth getmAuth(){
        if(mAuth==null){
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }
    public static String getUid(){
        return getmAuth().getUid();
    }
    public static FirebaseFirestore getmRef(){
        if(mRef==null){
            mRef = FirebaseFirestore.getInstance();
        }
        return mRef;
    }
    public static DocumentReference getUserRef(){
        if(userRef==null){
            userRef = getmRef().document("user/"+getUid());
        }
        return userRef;
    }

    public static DocumentReference getNewUid(){
        return getmRef().collection("user").document();
    }

    public static String getNewSubsId(){
        return getmRef().collection("subscriptions").document().getId();
    }

}

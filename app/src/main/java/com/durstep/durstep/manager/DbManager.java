package com.durstep.durstep.manager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.durstep.durstep.interfaces.FirebaseTask;
import com.durstep.durstep.model.AppMode;
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
import com.google.firebase.firestore.QuerySnapshot;

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
                    getUser(firebaseTask);
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
    public static void getUser(FirebaseTask<User> userFirebaseTask){
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
        getmRef().document("subscriptions/"+subscription.getsId()).set(subscription)
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
    public static void getSubscriptionOfCurrentUser(FirebaseTask<Subscription> subscriptionFirebaseTask){
        getmRef().collection("subscriptions")
                .whereEqualTo("uId", getUid())
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

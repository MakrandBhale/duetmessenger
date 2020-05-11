package com.makarand.duetmessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.Helper.LocalStorage;
import com.makarand.duetmessenger.Model.Couple;
import com.makarand.duetmessenger.Model.User;

public class SplashActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    LocalStorage localStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();
        localStorage = new LocalStorage(this);
        if(localStorage.getBoolean(Constants.FREE_PASS)) {
            moveToMainActivity();
            return;
            //startActivity(new Intent(this, MainActivity.class)); return;
        }
        if(mAuth.getCurrentUser() != null){
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE + "/" + mAuth.getCurrentUser().getUid());
            dbRef.keepSynced(true);
            checkIfIdExists(dbRef);
        } else {
            // user isn't signed in
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }


    }


    private void checkIfIdExists(DatabaseReference dbRef) {
        User myObj = localStorage.getUserObject(Constants.MY_OBJECT_LOCAL_STORAGE);
        Couple coupleObj = localStorage.getCoupleObject(Constants.COUPLE_OBJECT_LOCAL_STORAGE);
        if(myObj != null && coupleObj != null){
            if(coupleObj.getP1() != null && coupleObj.getP2() != null) {
                moveToMainActivity();
                return;
            }
            moveToWaitActivity(myObj);
            return;
        }

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user == null) return;
                localStorage.setUserObject(Constants.MY_OBJECT_LOCAL_STORAGE, user);
                if(dataSnapshot.hasChild("chatroomId")){
                    //user registered chatroom exists.
                    DatabaseReference coupleRef = FirebaseDatabase.getInstance().getReference(Constants.CHATROOMS_TREE).child(user.getChatroomId()).child("couple");
                    coupleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnadapshot) {
                            Couple c = dataSnapshot.getValue(Couple.class);
                            if(c != null){
                                localStorage.setCoupleObject(Constants.COUPLE_OBJECT_LOCAL_STORAGE, c);
                            }
                            if(dataSnapshot.hasChild("p1") && dataSnapshot.hasChild("p2")){
                                moveToMainActivity();
                            } else {
                                moveToWaitActivity(user);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    // user is registered chatroom doesn't exists//
                    startActivity(new Intent(getApplicationContext(), CreateChatroom.class));
                }
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void moveToWaitActivity(User user) {
        Intent i = new Intent(getApplicationContext(), WaitActivity.class);
        i.putExtra("chatroomId", user.getChatroomId());
        startActivity(i);
        finish();
    }

    private void moveToMainActivity() {
        startActivity(new Intent(getApplicationContext(), ChatsActivity.class));
        //startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}

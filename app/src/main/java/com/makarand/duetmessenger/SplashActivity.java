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
import com.makarand.duetmessenger.Model.Couple;
import com.makarand.duetmessenger.Model.User;

public class SplashActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();

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
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(dataSnapshot.hasChild("chatroomId")){
                    //user registered chatroom exists.
                    DatabaseReference chatroomRef = FirebaseDatabase.getInstance().getReference(Constants.CHATROOMS_TREE).child(user.getChatroomId()).child("couple");
                    chatroomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("p1") && dataSnapshot.hasChild("p2")){
                                Couple c = dataSnapshot.getValue(Couple.class);
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                Intent i = new Intent(getApplicationContext(), WaitActivity.class);
                                i.putExtra("chatroomId", user.getChatroomId());
                                startActivity(i);
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
}

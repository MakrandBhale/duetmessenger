package com.makarand.duetmessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.Helper.LocalStorage;
import com.makarand.duetmessenger.Model.Couple;

public class WaitActivity extends AppCompatActivity {
    DatabaseReference coupleRef;
    String chatroomId;
    LocalStorage localStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Intent i = getIntent();
        localStorage = new LocalStorage(this);
        try{
            chatroomId = i.getExtras().getString("chatroomId");
        } catch (NullPointerException e){
            e.printStackTrace();
            Toast.makeText(this, "chatroom doesn't exists.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CreateChatroom.class));
            finish();
        }
        coupleRef = FirebaseDatabase.getInstance().getReference(Constants.CHATROOMS_TREE).child(chatroomId).child("couple");
        coupleRef.keepSynced(true);
        coupleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Couple couple = dataSnapshot.getValue(Couple.class);

                if(dataSnapshot.hasChild("p1") && dataSnapshot.hasChild("p2")){
                    // both partner exist, proceed.
                    localStorage.setCoupleObject(Constants.COUPLE_OBJECT_LOCAL_STORAGE, couple);
                    startActivity(new Intent(getApplicationContext(), ChatsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

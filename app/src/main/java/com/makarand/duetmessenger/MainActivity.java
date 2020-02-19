package com.makarand.duetmessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.Model.Couple;
import com.makarand.duetmessenger.Model.Message;
import com.makarand.duetmessenger.Model.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    DatabaseReference myRef, partnerRef, chatroomRef, coupleRef, chatsRef;
    FirebaseAuth mAuth;
    String myUid, partnerUid;
    User me, partner;
    Animation fadeIn, fadeOut;

    @BindView(R.id.partner_name) TextView partnerName;
    @BindView(R.id.status_text) TextView statusText;
    @BindView(R.id.send_button) ImageButton sendButton;
    @BindView(R.id.message_box) EditText messageBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        myUid = mAuth.getCurrentUser().getUid();
        myRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE).child(myUid);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                me = dataSnapshot.getValue(User.class);
                getChatroomRef(me);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage(){
        String messageText = messageBox.getText().toString().trim();

        if(messageText.length() <= 0)
            return;

        Message message = new Message(myUid, partnerUid,String.valueOf(ServerValue.TIMESTAMP), messageText);
    }
    private void pullUp(){
        partnerName.animate()
                .translationY(-24)
                .setDuration(200)
                .start();
        statusText.startAnimation(fadeIn);
        statusText.setVisibility(View.VISIBLE);
    }

    private void pullDown(){
        partnerName.animate()
                .translationY(0)
                .setDuration(200)
                .start();
        statusText.startAnimation(fadeOut);
        statusText.setVisibility(View.GONE);
    }

    private void init() {
        addPartnerStatusListener();
        handleMessageBox();
    }

    private void handleMessageBox(){
        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                myRef.child("online").setValue(Constants.TYPING);
            }

            @Override
            public void afterTextChanged(Editable s) {
                myRef.child("online").setValue(Constants.ONLINE);
            }
        });
    }

    private void addPartnerStatusListener() {
        partnerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                switch (user.getOnline()){
                    case Constants.OFFLINE:
                        pullDown();
                        break;
                    case Constants.ONLINE:
                        pullUp();
                        statusText.setText("Online");
                        break;
                    case Constants.TYPING:
                        pullUp();
                        statusText.setText("Typing...");
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            myRef.child("online").setValue(Constants.ONLINE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        myRef.child("online").setValue(Constants.OFFLINE);
    }

    private void getChatroomRef(User user) {
        chatroomRef= FirebaseDatabase.getInstance().getReference(Constants.CHATROOMS_TREE).child(user.getChatroomId());
        coupleRef = chatroomRef.child("couple");
        chatsRef = chatroomRef.child("chats");
        coupleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("p1") && dataSnapshot.hasChild("p2")){
                    Couple c = dataSnapshot.getValue(Couple.class);

                    if(c.getP1().equals(myUid)){
                        // p1 is me so p2 is partner
                        partnerUid = c.getP2();
                    } else {
                        // p1 is
                        partnerUid = c.getP1();
                    }

                    partnerRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE).child(partnerUid);
                    partnerRef.keepSynced(true);
                    getPartnerInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPartnerInfo() {
        partnerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                partner = dataSnapshot.getValue(User.class);
                partnerName.setText(partner.getName());
                init();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}

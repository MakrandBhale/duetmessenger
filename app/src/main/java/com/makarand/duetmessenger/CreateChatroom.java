package com.makarand.duetmessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionBarOverlayLayout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makarand.duetmessenger.Helper.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateChatroom extends AppCompatActivity {
    @BindView(R.id.subtitle) TextView subtitle;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.create_chatroom_button) Button actionButton;
    @BindView(R.id.loader) ProgressBar progressBar;
    @BindView(R.id.chatroom_id) EditText chatroomIdEditText;
    @BindView(R.id.enter_chatroom_id) TextView enterChatroomId;
    DatabaseReference chatroomRef = FirebaseDatabase.getInstance().getReference(Constants.CHATROOMS_TREE);
    @BindView(R.id.new_chatroom_layout) LinearLayout newChatroomLayout;
    @BindView(R.id.existing_chatroom_layout) LinearLayout existingChatroomLayout;
    @BindView(R.id.proceed) Button existingIdButton;
    @BindView(R.id.chatroom_id1) EditText existingChatroomEdittext;
    boolean toggle = true;
    String myUid;

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chatroom);
        ButterKnife.bind(this);
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        enterChatroomId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //chatroomIdEditText.requestFocus();
                if(toggle){
                    newChatroomLayout.setVisibility(View.GONE);
                    existingChatroomLayout.setVisibility(View.VISIBLE);
                    enterChatroomId.setText("Create new chatroom instead.");
                } else {
                    newChatroomLayout.setVisibility(View.VISIBLE);
                    existingChatroomLayout.setVisibility(View.GONE);
                    enterChatroomId.setText("Already have chatroom? Click here to enter.");
                }
                toggle = !toggle;
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createChatroom();
            }
        });

        existingIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = existingChatroomEdittext.getText().toString().trim();
                if(id.length() < 3){
                    Toast.makeText(CreateChatroom.this, "Invalid id", Toast.LENGTH_SHORT).show();
                    return;
                }
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE).child(myUid);
                myRef.child("chatroomId").setValue(id)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                proceed(id, "p2");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CreateChatroom.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }

    private void createChatroom(){
        progressBar.setVisibility(View.VISIBLE);
        final String chatroomId = chatroomRef.push().getKey();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE + "/" + myUid);
        userRef.child("chatroomId").setValue(chatroomId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        chatroomIdEditText.setVisibility(View.VISIBLE);
                        chatroomIdEditText.setText(chatroomId);
                        title.setText("Success!");
                        subtitle.setText("Chatroom created. ID copied to clipboard, share it with your partner.\n");
                        actionButton.setText("Copy to clipboard");
                        setClipboard(getApplicationContext(), chatroomId);
                        actionButton.setText("Proceed");
                        actionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                proceed(chatroomId, "p1");                           }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void proceed(String id, String partnerNumber) {
        DatabaseReference chatroomRef = FirebaseDatabase.getInstance().getReference(Constants.CHATROOMS_TREE).child(id).child("couple").child(partnerNumber);
        chatroomRef.setValue(myUid)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent i = new Intent(getApplicationContext(), WaitActivity.class);
                        i.putExtra("chatroomId", id);
                        startActivity(i);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateChatroom.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        try {
            clipboard.setPrimaryClip(clip);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }
}

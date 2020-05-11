package com.makarand.duetmessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.Helper.LocalStorage;
import com.makarand.duetmessenger.Model.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.email) EditText email;
    @BindView(R.id.password) EditText password;
    @BindView(R.id.signin_button) Button signinButton;
    @BindView(R.id.register_textview)
    TextView goToRegister;
    FirebaseAuth auth ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        auth = FirebaseAuth.getInstance();
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        goToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                finish();
            }
        });

    }


    private void login() {
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        if(emailText.length() < 3 || passwordText.length() < 3){
            Toast.makeText(this, "Please enter correct information.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE + "/" + auth.getCurrentUser().getUid());
                        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                User user = dataSnapshot.getValue(User.class);
                                new LocalStorage(getApplicationContext()).setUserObject(Constants.MY_OBJECT_LOCAL_STORAGE, user);
                                dbRef.keepSynced(true);
                                Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(getApplicationContext(), WaitActivity.class);
                                try{
                                    i.putExtra("chatroomId", user.getChatroomId());
                                } catch (NullPointerException e){
                                    e.printStackTrace();
                                }
                                startActivity(i);
                                finish();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

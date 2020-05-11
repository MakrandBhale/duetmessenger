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
import com.google.firebase.auth.FirebaseUser;
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

public class SignupActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    @BindView(R.id.email) EditText email;
    @BindView(R.id.password) EditText password;
    @BindView(R.id.name) EditText name;
    @BindView(R.id.signup_button) Button signupButton;
    @BindView(R.id.login_textview) TextView loginTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
    }


    private void signUp() {
        final String nameText = name.getText().toString().trim();
        final String emailText = email.getText().toString().trim();
        final String passwordText = password.getText().toString().trim();

        if(nameText.length() < 3 || emailText.length() < 3 || passwordText.length() < 3){
            Toast.makeText(this, "Information entered can not be below 3 letters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(SignupActivity.this, "New user created.", Toast.LENGTH_SHORT).show();
                        final User user = new User(nameText, emailText, mAuth.getCurrentUser().getUid(), null, Constants.OFFLINE);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE +"/"+user.getUid());
                        ref.setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if(user.getChatroomId() == null){
                                            new LocalStorage(getApplicationContext()).setUserObject(Constants.MY_OBJECT_LOCAL_STORAGE, user);
                                            startActivity(new Intent(getApplicationContext(), CreateChatroom.class));
                                            finish();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignupActivity.this, "Failed to write user info to database.:"+e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}

package com.makarand.duetmessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.animation.Animator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ServerValue;
import com.makarand.duetmessenger.Adapter.MessageListAdapter;
import com.makarand.duetmessenger.Model.Message;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.Helper.LocalStorage;
import com.makarand.duetmessenger.Model.Couple;
import com.makarand.duetmessenger.Model.TypingMessage;
import com.makarand.duetmessenger.Model.User;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.util.ArrayList;
import java.util.Objects;

import java.util.concurrent.TimeUnit;


import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatsActivity extends AppCompatActivity {
    @BindView(R.id.partner_name)
    TextView partnerName;

    @BindView(R.id.send_button)
    ImageButton sendButton;
    @BindView(R.id.emojiButton)
    ImageButton emojiButton;
    @BindView(R.id.message_box)
    EmojiEditText messageBox;
    @BindView(R.id.partner_avtar)
    ImageView avtarImageView;
    @BindView(R.id.message_list)
    RecyclerView messageListRecyclerView;
    @BindView(R.id.rootView)
    LinearLayout rootView;
    @BindView(R.id.fragment_container_layout)
    FrameLayout fragmentContainer;
    @BindView(R.id.go_down_button)
    FloatingActionButton goDownButton;
    @BindView(R.id.go_down_button_container)
    LinearLayout goDownLayout;
    @BindView(R.id.new_messages_counter)
    MaterialTextView newMessagesCounter;
    @BindView(R.id.green_online_dot) ImageView greenOnlineDot;
    @BindView(R.id.status_text) MaterialTextView onlineStatusTextView;

    LocalStorage localStorage;
    User me, partner;
    String myUid, partnerUid;
    LinearLayoutManager linearLayoutManager;
    //MessageListAdapter adapter;
    ArrayList<Message> messageArrayList = new ArrayList<>();
    DatabaseReference chatsRef, myRef, partnerRef;
    int unreadMessages = 0;
    long lastChangedTime = System.currentTimeMillis();
    long currentTime = System.currentTimeMillis();
    MessageListAdapter adapter;
    FirebaseAuth mAuth;
    int typingIndicatorIndex;
    private int typingMessageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EmojiManager.install(new IosEmojiProvider());
        setContentView(R.layout.activity_chats);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        localStorage = new LocalStorage(this);
        me = localStorage.getUserObject(Constants.MY_OBJECT_LOCAL_STORAGE);
        partner = localStorage.getUserObject(Constants.PARTNER_OBJECT_LOCAL_STORAGE);
        if (me == null) localStorage.setBoolean(Constants.FREE_PASS, false);
        myUid = me.getUid();
        myRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE + "/" + myUid);
        getPartnerId();
        partnerRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE + "/" + partnerUid);
        chatsRef = FirebaseDatabase.getInstance().getReference(Constants.CHATROOMS_TREE + "/" + me.getChatroomId() + "/chats");
        if (partner == null) {
            localStorage.setBoolean(Constants.FREE_PASS, false);
        } else {
            setPartnerProfileInfo(partner);
        }
        setPartnerListener();
        setupRecyclerView();


        localStorage.setBoolean(Constants.FREE_PASS, true);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse)
                        .duration(250)
                        .playOn(sendButton);
                sendMessage();
            }
        });
        goDownButton.setOnClickListener(view -> {
            try {
                newMessagesCounter.setVisibility(View.GONE);
                messageListRecyclerView.scrollToPosition(Objects.requireNonNull(messageListRecyclerView.getAdapter()).getItemCount() - 1);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });
        fetchMessages();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //addSeenListener();
            }
        });
        setupMyTypingListener();
        //setupPartnerListener();
    }


    private void fetchMessages() {
        chatsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                message.setShowMessageStatus(true);
                //messageArrayList.add(message);
                adapter.addNewMessage(message);
                //adapter.add(message);
                //adapter.setList(messageArrayList);
                //messageListRecyclerView.smoothScrollToPosition(index);
                scrollToBottom();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message messageToBeUpdated = dataSnapshot.getValue(Message.class);
                adapter.updateMessage(messageToBeUpdated);
                //adapter.update(messageToBeUpdated);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public static long getDateDiff(long timeUpdate, long timeNow, TimeUnit timeUnit) {
        long diffInMillies = Math.abs(timeNow - timeUpdate);
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    private void setupRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        };
        messageListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ((SimpleItemAnimator) messageListRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        linearLayoutManager.setStackFromEnd(true);
        messageListRecyclerView.setLayoutManager(linearLayoutManager);


        adapter = new MessageListAdapter(myUid, getApplicationContext());
        messageListRecyclerView.setAdapter(adapter);
    }

    private void scrollToBottom() {
        messageListRecyclerView.scrollToPosition(Objects.requireNonNull(messageListRecyclerView.getAdapter()).getItemCount() - 1);
    }

    private void notifyMessagesAtBottom() {
        unreadMessages++;
        newMessagesCounter.setText(unreadMessages + " new messages");
        if (newMessagesCounter.getVisibility() != View.VISIBLE) {
            YoYo.with(Techniques.SlideInRight)
                    .onStart(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            newMessagesCounter.setVisibility(View.VISIBLE);
                        }
                    })
                    .duration(300)
                    .playOn(newMessagesCounter);
        }
    }

    private Message getLastMessage() {
        if (messageArrayList.size() > 0) {
            return messageArrayList.get(messageArrayList.size() - 1);
        }
        return null;
    }


    private void sendMessage() {
        if (messageBox.getText() == null) return;
        myRef.child("online").setValue(Constants.ONLINE);
        String messageText = messageBox.getText().toString().trim();

        if (messageText.length() <= 0)
            return;
        messageBox.setText("");
        String messageId = chatsRef.push().getKey();
        Message message = new Message(messageId, myUid, partnerUid, messageText, Constants.MESSAGE_STATUS_SENDING);
        messageListRecyclerView.scrollToPosition(messageListRecyclerView.getAdapter().getItemCount() - 1);


        chatsRef.child(messageId).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                chatsRef.child(messageId).child("messageStatus").setValue(Constants.MESSAGE_STATUS_SENT);
                //messageListRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                chatsRef.push().setValue(message);
            }
        });
    }


    private void setPartnerProfileInfo(User partner) {
        partnerName.setText(partner.getName());
        Glide
                .with(getApplicationContext())
                .load(partner.getAvtar())
                .centerCrop()
                .placeholder(R.drawable.ic_user)
                .into(avtarImageView);
    }

    private void getPartnerId() {
        Couple c = localStorage.getCoupleObject(Constants.COUPLE_OBJECT_LOCAL_STORAGE);
        if (c.getP1() != null && c.getP2() != null) {
            if (c.getP1().equals(myUid)) {
                // p1 is me so p2 is partner
                partnerUid = c.getP2();
            } else {
                // p1 is
                partnerUid = c.getP1();
            }
            //setPartnerListener();
        }
    }

    private void showOnlineIndicator(){
        if(onlineStatusTextView.getVisibility() != View.VISIBLE)
        YoYo.with(Techniques.SlideInUp)
                .duration(300)
                .onStart(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        onlineStatusTextView.setVisibility(View.VISIBLE);
                    }
                })
                .playOn(onlineStatusTextView);
        if(greenOnlineDot.getVisibility() != View.VISIBLE)
        YoYo.with(Techniques.ZoomIn)
                .duration(500)
                .onStart(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        greenOnlineDot.setVisibility(View.VISIBLE);
                    }
                })
                .playOn(greenOnlineDot);

    }

    private void hideOnlineIndicator(){
        YoYo.with(Techniques.ZoomOut)
                .duration(300)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        greenOnlineDot.setVisibility(View.GONE);
                    }
                })
                .playOn(greenOnlineDot);
        YoYo.with(Techniques.SlideOutDown)
                .duration(300)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        onlineStatusTextView.setVisibility(View.GONE);
                    }
                })
                .playOn(onlineStatusTextView);
    }

    private void setPartnerListener() {

        DatabaseReference partnerRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE).child(partnerUid);
        partnerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User temp = dataSnapshot.getValue(User.class);
                if (temp == null) return;
                localStorage.setUserObject(Constants.PARTNER_OBJECT_LOCAL_STORAGE, partner);
                partner = temp;
                setPartnerProfileInfo(partner);
                switch (partner.getOnline()) {
                    case Constants.TYPING:
                        showOnlineIndicator();

                        TypingMessage typingMessage = new TypingMessage(partner.getAvtar());
                        adapter.showTypingIndicator(typingMessage);
                        if (!messageListRecyclerView.canScrollVertically(1)) {
                            scrollToBottom();
                        }
                        break;
                    case Constants.ONLINE:
                        showOnlineIndicator();
                        adapter.hideTypingIndicator();
                        break;
                    case Constants.OFFLINE:
                        hideOnlineIndicator();
                        //Toast.makeText(ChatsActivity.this, "offline", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addSeenListener() {
        messageListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleMessage = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                int lastVisibleMessage = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                ArrayList<Message> visibleMessagesArrayList = new ArrayList<>();
                for (int i = firstVisibleMessage; i <= lastVisibleMessage; i++) {
                    if (messageArrayList.size() > lastVisibleMessage && messageArrayList.get(i).getSeenTime() == null) {
                        visibleMessagesArrayList.add(messageArrayList.get(i));
                    }
                }
                for (Message m : visibleMessagesArrayList) {
                    if (m.getSender().equals(partnerUid) && me != null) {
                        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference(Constants.CHATROOMS_TREE).child(me.getChatroomId()).child("chats").child(m.getMessageId()).child("seenTime");
                        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Long value = dataSnapshot.getValue(Long.class);
                                if (value == null) {
                                    messageRef.setValue(ServerValue.TIMESTAMP);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        //messageRef.child("seenTime").setValue(ServerValue.TIMESTAMP);
                        //m.setSeenTime(ServerValue.TIMESTAMP);
                    }
                }

                /*GO DOWN BUTTON*/
                if (dy < 0) {
                    if (goDownButton.getVisibility() != View.VISIBLE) {
                        YoYo.with(Techniques.ZoomIn)
                                .duration(300)
                                .onStart(new YoYo.AnimatorCallback() {
                                    @Override
                                    public void call(Animator animator) {
                                        goDownButton.setVisibility(View.VISIBLE);
                                    }
                                })
                                .playOn(goDownButton);
                    }
                }
                if (!messageListRecyclerView.canScrollVertically(1)) {
                    unreadMessages = 0;
                    YoYo.with(Techniques.ZoomOut)
                            .duration(300)
                            .onEnd(new YoYo.AnimatorCallback() {
                                @Override
                                public void call(Animator animator) {
                                    goDownButton.setVisibility(View.GONE);
                                }
                            })
                            .playOn(goDownButton);

                    YoYo.with(Techniques.SlideOutRight)
                            .onStart(new YoYo.AnimatorCallback() {
                                @Override
                                public void call(Animator animator) {
                                    newMessagesCounter.setVisibility(View.GONE);
                                }
                            })
                            .duration(300)
                            .playOn(newMessagesCounter);

                }
            }
        });
    }


    private void setupMyTypingListener() {

        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) return;
                myRef.child("online").setValue(Constants.TYPING);
                //partnerName.setText("Online");
                lastChangedTime = System.currentTimeMillis();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        currentTime = System.currentTimeMillis();
                        if (getDateDiff(lastChangedTime, currentTime, TimeUnit.SECONDS) >= Constants.TYPING_TIMEOUT) {
                            myRef.child("online").setValue(Constants.ONLINE);
                        }
                    }
                }, Constants.TYPING_TIMEOUT * 1000);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
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

}

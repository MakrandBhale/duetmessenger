package com.makarand.duetmessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.camerakit.CameraKitView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makarand.duetmessenger.Adapter.MessageListAdapter;
import com.makarand.duetmessenger.Fragments.LoadingFragment;
import com.makarand.duetmessenger.Fragments.MessageBubbleEffectPreviewFragment;
import com.makarand.duetmessenger.Fragments.ProfileFragment;
import com.makarand.duetmessenger.Helper.EndToEnd;
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
import com.makarand.duetmessenger.Model.User;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import java.util.concurrent.TimeUnit;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.zelory.compressor.Compressor;

public class ChatsActivity extends AppCompatActivity implements ProfileFragment.OnFragmentInteractionListener, MessageBubbleEffectPreviewFragment.OnFragmentInteractionListener{
    private static final int CAMERA_ACTIVITY_CODE = 1011;
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
    @BindView(R.id.loadingCardViewLayout)
    MaterialCardView loadingCardViewLayout;
    @BindView(R.id.media_buttons_container)
            LinearLayout mediaButtonsContainer;
    @BindView(R.id.typing_container) LinearLayout includeTypingIndicatorContainer;
    @BindView(R.id.typing_partner_name) TextView typing_partner_name;
    @BindView(R.id.show_media_control_buttons)
    AppCompatImageButton show_media_control_buttons;
    @BindView(R.id.image_to_send_imageview) ImageView imageToSendImageView;
    @BindView(R.id.image_to_send_container) MaterialCardView imageToSendContainer;
    Uri imageToSendUri = null;
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
    String lastMessageKey = null;
    boolean fetchingOldMessage = false;
    EmojiPopup emojiPopup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EmojiManager.install(new IosEmojiProvider());
        setContentView(R.layout.activity_chats);
        ButterKnife.bind(this);
        MaterialToolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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
                //fetchOldMessage();
                sendMessage(-1);
                if(emojiPopup.isShowing()){
                    emojiPopup.toggle();
                }
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
        fetchNewMessages();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                addSeenListener();
            }
        });
        setupMyTypingListener();

        show_media_control_buttons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_media_control_buttons.setVisibility(View.GONE);
                mediaButtonsContainer.setVisibility(View.VISIBLE);
            }
        });

        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setOnEmojiPopupShownListener(() -> emojiButton.setImageResource(R.drawable.ic_keyboard_24px))
                .setOnEmojiPopupDismissListener(() -> emojiButton.setImageResource(R.drawable.ic_emoji_24px))
                .build(messageBox);

        //typingIndicatorContainer = findViewById(R.id.typing_container);
        //setupPartnerListener();
        sendButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(Objects.requireNonNull(messageBox.getText()).length() > 0){

                }
                    //startBubbleEffectFragment();
                else return false;
                return true;
            }
        });

    }
    @OnClick(R.id.emojiButton)
    public void openEmojiPanel(View v){
        emojiPopup.toggle();
    }


    private void showLoading(){
        fetchingOldMessage = true;
        YoYo.with(Techniques.SlideInDown)
                .duration(300)
                .onStart(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        loadingCardViewLayout.setVisibility(View.VISIBLE);
                    }
                })
                .playOn(loadingCardViewLayout);

    }

    private void hideLoading(){
        fetchingOldMessage = false;
        YoYo.with(Techniques.SlideOutUp)
                .duration(300)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        loadingCardViewLayout.setVisibility(View.GONE);
                    }
                })
                .playOn(loadingCardViewLayout);

    }
    private void fetchOldMessage(){
        if(lastMessageKey == null){
            //fetchNewMessages();
            return;
        }

        if(fetchingOldMessage) return;
        showLoading();
        Query query = chatsRef.orderByKey()
                .endAt(lastMessageKey)
                .limitToLast(Constants.MESSAGE_LIMIT);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Message> temp = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Message message = snapshot.getValue(Message.class);
                    try {
                        EndToEnd endToEnd = new EndToEnd(me.getChatroomId());
                        message.setMessage(endToEnd.decrypt(message.getMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    temp.add(message);
                }
                lastMessageKey = temp.get(0).getMessageId();
                //temp.remove(temp.size() - 1);
                Collections.reverse(temp);
                for(int i = 0; i < temp.size() -1;i++){
                    if(temp.get(i).getMessageStatus() != temp.get(i+1).getMessageStatus()){
                        temp.get(i).setShowMessageStatus(true);
                    }
                }

                for(int i = 1;i < temp.size(); i++){
                    adapter.addOldMessage(temp.get(i));
                }

                hideLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoading();
            }
        });

    }
    private void fetchNewMessages() {
        Query query = chatsRef.orderByKey().limitToLast(Constants.MESSAGE_LIMIT);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                try {
                    EndToEnd endToEnd = new EndToEnd(me.getChatroomId());
                    message.setMessage(endToEnd.decrypt(message.getMessage()));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }


                if(lastMessageKey == null)
                    lastMessageKey = dataSnapshot.getKey();
                if(message.getSender().equals(partnerUid) && !dataSnapshot.hasChild("arrivalTime")) {
                    DatabaseReference messageRef = chatsRef.child(dataSnapshot.getKey());
                    HashMap<String, Object> messageStatusUpdate = new HashMap<>();
                    //messageStatusUpdate.put("arrivalTime", ServerValue.TIMESTAMP);
                    //messageStatusUpdate.put("messageStatus", Constants.MESSAGE_STATUS_DELIVERED);
                    messageRef.child("arrivalTime").setValue(ServerValue.TIMESTAMP);
                }
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


        adapter = new MessageListAdapter(myUid, getApplicationContext(), me.getChatroomId());
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


    private void sendMessage(int technique) {
        if (messageBox.getText() == null) return;
        myRef.child("online").setValue(Constants.ONLINE);
        String messageText = messageBox.getText().toString().trim();
        try {
            EndToEnd endToEnd = new EndToEnd(me.getChatroomId());
            messageText = endToEnd.encrypt(messageText);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (messageText.isEmpty() && imageToSendUri == null)
            return;
        messageBox.setText("");
        String messageId = chatsRef.push().getKey();
        Message message;
        if(technique == -1)
            message = new Message(messageId, myUid, partnerUid, messageText, Constants.MESSAGE_STATUS_SENDING);
        else {
            message = new Message(messageId, myUid, partnerUid, messageText, Constants.MESSAGE_STATUS_SENDING, technique);
        }
        messageListRecyclerView.scrollToPosition(messageListRecyclerView.getAdapter().getItemCount() - 1);

        if(imageToSendUri != null){
            /*user selected an image*/
            imageToSendContainer.setVisibility(View.GONE);
            uploadImage(messageId);
            message.setImage(Constants.IMAGE_UPLOAD_IN_PROGRESS);

        }

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
        imageToSendUri = null;

    }


    private void setPartnerProfileInfo(User partner) {
        partnerName.setText(partner.getName());
        typing_partner_name.setText(partner.getName());
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
                        showTypingIndicator();
                        break;
                    case Constants.ONLINE:
                        showOnlineIndicator();
                        hideTypingIndicator();
                        break;
                    case Constants.OFFLINE:
                        hideOnlineIndicator();
                        hideTypingIndicator();
                        //Toast.makeText(ChatsActivity.this, "offline", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showTypingIndicator() {
        adapter.showTypingIndicator();
/*        YoYo.with(Techniques.SlideInUp)
                .duration(600)
                .onStart(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        includeTypingIndicatorContainer.setVisibility(View.VISIBLE);
                    }
                })
                .playOn(includeTypingIndicatorContainer);*/
        scrollToBottom();
    }

    private void hideTypingIndicator() {
        adapter.hideTypingIndicator();
        /*      YoYo.with(Techniques.SlideOutDown)
                .duration(300)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        includeTypingIndicatorContainer.setVisibility(View.GONE);
                    }
                })
                .playOn(includeTypingIndicatorContainer);
*/
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

                if(firstVisibleMessage <= Constants.REQUEST_OLD_MESSAGES_OFFSET){
                    fetchOldMessage();
                }
                ArrayList<Message> visibleMessagesArrayList = new ArrayList<>();
                for (int i = firstVisibleMessage; i <= lastVisibleMessage; i++) {
                    if (firstVisibleMessage >= 0 && messageArrayList.size() > lastVisibleMessage && messageArrayList.get(i).getSeenTime() == null) {
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

                if (charSequence.length() == 0) {
                    sendButton.setVisibility(View.GONE);
                    show_media_control_buttons.setVisibility(View.GONE);

                    mediaButtonsContainer.setVisibility(View.VISIBLE);
                    return;
                }
                if(charSequence.length() > 0){
                    sendButton.setVisibility(View.VISIBLE);
                }
                if(charSequence.length() > 15){
                    mediaButtonsContainer.setVisibility(View.GONE);
                    show_media_control_buttons.setVisibility(View.VISIBLE);
                } else {
                    mediaButtonsContainer.setVisibility(View.VISIBLE);
                    show_media_control_buttons.setVisibility(View.GONE);
                }


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);

        MenuItem item= menu.findItem(R.id.settings);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings:
                startSettingsFragment();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startSettingsFragment() {
        Fragment settingsFragment = new ProfileFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.setCustomAnimations(R.anim.fragments_enter_animation, R.anim.fragments_exit_animation);

        fragmentTransaction.add(R.id.fragment_container_layout, settingsFragment, "SettingsFragment");
        fragmentContainer.setVisibility(View.VISIBLE);
        fragmentTransaction.addToBackStack("SettingsFragment");
        fragmentTransaction.commit();
    }

    private void startBubbleEffectFragment(){
        Bundle bundle = new Bundle();
        String myMessage = Objects.requireNonNull(messageBox.getText()).toString();
        bundle.putString("myMessage", myMessage );
        Fragment MessageBubbleEffectFragement = new MessageBubbleEffectPreviewFragment();
        MessageBubbleEffectFragement.setArguments(bundle);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container_layout, MessageBubbleEffectFragement, "BubbleEffectFragmgent");
        fragmentContainer.setVisibility(View.VISIBLE);
        fragmentTransaction.addToBackStack("BubbleEffectFragmgent");
        fragmentTransaction.commit();
    }


    private void startLoadingFragment(){
        Fragment loadingFragment = new LoadingFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.setCustomAnimations(R.anim.fragments_enter_animation, R.anim.fragments_exit_animation);

        fragmentTransaction.add(R.id.fragment_container_layout, loadingFragment, "LoadingFragment");
        fragmentContainer.setVisibility(View.VISIBLE);
        fragmentTransaction.addToBackStack("LoadingFragment");
        fragmentTransaction.commit();
    }

    private void removeLoadingFragment(){
        if(getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
            fragmentContainer.setVisibility(View.GONE);
/*            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                }
            }, 300);*/
        }
    }
    private Uri compressImage(Uri photoUri) {
        try{
            File compressedImageFile =
                    new Compressor(getApplicationContext())
                            .setQuality(50)
                            .compressToFile(new File(photoUri.getPath()));
            return Uri.fromFile(compressedImageFile);
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private String getDateFingerprint(){
        Date dNow = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
        return ft.format(dNow);
    }

    private void uploadImage(String chatId){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageReference = storage.getReference(Constants.MEDIA_IMAGE_STORAGE_PATH + myUid + "/images/duet_messenger_" + getDateFingerprint() + ".jpg");
        final Uri compressedImage = compressImage(imageToSendUri);
        if(compressedImage == null){
            Toast.makeText(getApplicationContext(), "Failed to upload image.", Toast.LENGTH_LONG).show();
            return;
        }
        imageToSendUri = null;
        storageReference.putFile(compressedImage)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                DatabaseReference tempRef = chatsRef.child(chatId).child("image");
                                tempRef.setValue(String.valueOf(downloadUri));
                                /*loader.setVisibility(View.GONE);
                                //LocalStorage localStorage = new LocalStorage(getActivity());
                                //localStorage.setString(Constants.AVTAR_LOCAL_KEY, String.valueOf(downloadUri));
                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE + "/"+myUid+"/");
                                myRef.child("avtar").setValue(String.valueOf(downloadUri));
                                fetchAvtarFromLocalStorage();*/
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(getActivity(), "Error uploading file:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        //loader.setVisibility(View.GONE);
                    }
                });
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @OnClick (R.id.open_gallery_button)
    public void openGallery(View view){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(ChatsActivity.this);
    }

    @OnClick(R.id.close_image_button)
    public void closeImageButton(View view){
        imageToSendUri = null;
        imageToSendContainer.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentByTag("SettingsFragment");
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageToSendUri = result.getUri();
                showImagePreview();
                //uploadImage(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        if (requestCode == CAMERA_ACTIVITY_CODE) {
            if(resultCode == Activity.RESULT_OK){
                imageToSendUri = Uri.parse(data.getStringExtra("cameraImage"));
                showImagePreview();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void showImagePreview() {
        sendButton.setVisibility(View.VISIBLE);
        Glide.with(getApplicationContext())
                .load(imageToSendUri)

                .into(imageToSendImageView);
        YoYo.with(Techniques.SlideInUp)
                .delay(50)
                .duration(300)
                .onStart(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        imageToSendContainer.setVisibility(View.VISIBLE);
                    }
                })
                .playOn(imageToSendContainer);
        sendButton.setVisibility(View.VISIBLE);
    }

    @OnClick (R.id.open_camera_button)
    public void openCameraActivity(View view){
        Intent i = new Intent(this, CameraActivity.class);
        startActivityForResult(i, CAMERA_ACTIVITY_CODE);
    }

    @Override
    public void onBackPressed(){
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if(count == 0){
            super.onBackPressed();

        } else {
            getSupportFragmentManager().popBackStack();
            fragmentContainer.setVisibility(View.GONE);
/*            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                }
            }, 300);*/
        }
    }

    @Override
    public void onFragmentInteraction(int AnimationType) {

    }

    @Override
    public void onBubbleAnimationAdded(int technique) {
        sendMessage(technique);
    }
}

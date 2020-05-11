package com.makarand.duetmessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.makarand.duetmessenger.Adapter.MessageListAdapter;
import com.makarand.duetmessenger.Fragments.LoadingFragment;
import com.makarand.duetmessenger.Fragments.ProfileFragment;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.Helper.LocalStorage;
import com.makarand.duetmessenger.Model.Couple;
import com.makarand.duetmessenger.Model.Message;
import com.makarand.duetmessenger.Model.User;
import com.makarand.duetmessenger.Threads.TypingThread;

import com.theartofdev.edmodo.cropper.CropImage;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ProfileFragment.OnFragmentInteractionListener{
    DatabaseReference myRef, partnerRef, chatroomRef, coupleRef, chatsRef;
    FirebaseAuth mAuth;
    String myUid, partnerUid;
    User me, partner;
    Animation fadeIn, fadeOut;
    MessageListAdapter adapter;
    final ArrayList<Message> messageArrayList = new ArrayList<>();
    final ArrayList<Message> partnerMessageArrayList = new ArrayList<>();
    EmojiPopup emojiPopup;
    LinearLayoutManager linearLayoutManager;
    LocalStorage localStorage;
    int unReadMessages = 0;
    @BindView(R.id.partner_name) TextView partnerName;
    @BindView(R.id.status_text) TextView statusText;
    @BindView(R.id.send_button) ImageButton sendButton;
    @BindView(R.id.emojiButton) ImageButton emojiButton;
    @BindView(R.id.message_box) EmojiEditText messageBox;
    @BindView(R.id.partner_avtar) ImageView avtarImageView;
    @BindView(R.id.message_list)
    RecyclerView messageListRecyclerView;
    @BindView(R.id.rootView) LinearLayout rootView;
    @BindView(R.id.fragment_container_layout)FrameLayout fragmentContainer;
    @BindView(R.id.go_down_button) FloatingActionButton goDownButton;
    @BindView(R.id.go_down_button_container) LinearLayout goDownLayout;
    @BindView(R.id.new_messages_counter) MaterialTextView newMessagesCounter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EmojiManager.install(new IosEmojiProvider());
        setContentView(R.layout.activity_main);
        MaterialToolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ButterKnife.bind(this);
/*
        startLoadingFragment();
        setRecyclerViewListener();
*/

        mAuth = FirebaseAuth.getInstance();

        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setOnEmojiPopupShownListener(() -> emojiButton.setImageResource(R.drawable.ic_keyboard_24px))
                .setOnEmojiPopupDismissListener(() -> emojiButton.setImageResource(R.drawable.ic_emoji_24px))
                .build(messageBox);

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
                YoYo.with(Techniques.Pulse)
                        .duration(250)
                        .playOn(sendButton);
                sendMessage();
            }
        });



        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageListRecyclerView.setLayoutManager(linearLayoutManager);
        addSeenListener();

        goDownButton.setOnClickListener(view -> {
            try{
                newMessagesCounter.setVisibility(View.GONE);
                messageListRecyclerView.scrollToPosition(Objects.requireNonNull(messageListRecyclerView.getAdapter()).getItemCount() - 1);
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        });
        /*Just for some time*/
        //startSettingsFragment();
        //fetchAvtar();
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
                for(int i = firstVisibleMessage; i <= lastVisibleMessage;i++){
                    if(messageArrayList.size() > lastVisibleMessage && messageArrayList.get(i).getSeenTime() == null){
                        visibleMessagesArrayList.add(messageArrayList.get(i));
                    }
                }
                for(Message m : visibleMessagesArrayList){
                    if(m.getSender().equals(partnerUid) && me != null){
                        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference(Constants.CHATROOMS_TREE).child(me.getChatroomId()).child("chats").child(m.getMessageId()).child("seenTime");
                        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Long value = dataSnapshot.getValue(Long.class);
                                if(value == null){
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
                if(dy < 0){
                    if(goDownButton.getVisibility() != View.VISIBLE){
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
                if(!messageListRecyclerView.canScrollVertically(1)){
                    unReadMessages = 0;
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

    @OnClick(R.id.emojiButton)
    public void openEmojiPanel(View v){
        emojiPopup.toggle();
    }

    private void sendMessage(){
        if(messageBox.getText() == null) return;
        String messageText = messageBox.getText().toString().trim();

        if(messageText.length() <= 0)
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

    private void trySendingMessageAgain(Message message){
        /*Just in case the message failed to be sent, a helper method.
        * Don't be surprised if timestamp is of past because message object is created at previous time.
        * */
        chatsRef.push().setValue(message);
    }

    private void pullUp(String msg){
        statusText.setText(msg);
        partnerName.animate()
                .translationY(-16)
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
        fetchMessages();
        handleMessageBox();
        addPartnerStatusListener();
    }

    private Message getLastMessage(){
        if(messageArrayList.size() > 0){
            return messageArrayList.get(messageArrayList.size() - 1);
        }
        return null;
    }

    private void fetchMessages() {
        adapter = new MessageListAdapter(myUid,  getApplicationContext());
        messageListRecyclerView.setAdapter(adapter);
        chatsRef.keepSynced(true);
//        TODO : get messages in set of 10, scroll to get more
        /*https://stackoverflow.com/questions/44777989/firebase-infinite-scroll-list-view-load-10-items-on-scrolling/44796538#44796538*/
        chatsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                if(message == null) return;
                try{
                    if(message.getSender().equals(partnerUid) && !dataSnapshot.hasChild("arrivalTime")){
                        String messageKey = dataSnapshot.getKey();
                        assert messageKey != null;
                        DatabaseReference messageRef = chatsRef.child(messageKey);
                        HashMap<String, Object> messageStatusUpdate = new HashMap<>();
                        messageStatusUpdate.put("arrivalTime", ServerValue.TIMESTAMP);
                        messageStatusUpdate.put("messageStatus", Constants.MESSAGE_STATUS_DELIVERED);
                        messageRef.updateChildren(messageStatusUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Toast.makeText(MainActivity.this, "updated", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //messageRef.child("arrivalTime").setValue(ServerValue.TIMESTAMP);
                        //messageRef.child("messageStatus").setValue(Constants.MESSAGE_STATUS_DELIVERED);
                    }
                    /*Message top = getLastMessage();
                    if(top != null){
                        if(top.getMessageStatus() == message.getMessageStatus()){
                            top.setShowMessageStatus(false);
                        } else {
                            top.setShowMessageStatus(true);
                        }
                        adapter.notifyItemChanged(messageArrayList.size() - 1);
                    }
                    message.setShowMessageStatus(true);*/
                    messageArrayList.add(message);
                    //updateLastMessageStatus(messageArrayList);

                    adapter.notifyItemInserted(messageArrayList.indexOf(message));

                    boolean atBottom = messageListRecyclerView.canScrollVertically(1);
                    if(!atBottom){
                        /*at the bottom*/
                        messageListRecyclerView.scrollToPosition(Objects.requireNonNull(messageListRecyclerView.getAdapter()).getItemCount() - 1);
                    } else {
//                        not at bottom.
                        if(message.getSender().equals(myUid)){
                            messageListRecyclerView.scrollToPosition(Objects.requireNonNull(messageListRecyclerView.getAdapter()).getItemCount() - 1);
                        } else {
                            unReadMessages++;
                            newMessagesCounter.setText(unReadMessages + " new messages");
                            if(newMessagesCounter.getVisibility() != View.VISIBLE){
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
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
                /*TODO: Go to new Message if you are already at end of list.*/
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message messageToBeUpdated = dataSnapshot.getValue(Message.class);
                int indexOf = messageArrayList.indexOf(messageToBeUpdated);
                //updateMessageStatus(messageToBeUpdated);
                /*messageToBeUpdated.setShowMessageStatus(true);
                if(messageArrayList.size() > 1){
                    *//*its not last item*//*
                    Message prev = messageArrayList.get(indexOf - 1);
                    if(messageToBeUpdated.getMessageStatus() == prev.getMessageStatus()){
                        prev.setShowMessageStatus(false);
                        adapter.notifyItemChanged(indexOf - 1);
                    }
                }*/
                if(indexOf >= 0){
                    messageArrayList.set(indexOf, messageToBeUpdated);
                }


                //updateLastMessageStatus(messageArrayList);

                adapter.notifyItemChanged(indexOf);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //Boolean exists0 = messageArrayList.contains(dataSnapshot.getValue(Message.class));
                Message message = dataSnapshot.getValue(Message.class);
                messageArrayList.remove(message);
                //Boolean exists = messageArrayList.contains(dataSnapshot.getValue(Message.class));
                adapter.notifyItemRemoved(messageArrayList.indexOf(message));
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void updateLastMessageStatus(ArrayList<Message> messageArrayList) {
        Message lastMessage = null;
        for(Message m :messageArrayList){
            if(m.getSender().equals(myUid)){
                /*My Message*/
                m.setShowMessageStatus(false);
                lastMessage = m;
            }
        }
        if(lastMessage != null)  {
            messageArrayList.get(messageArrayList.indexOf(lastMessage)).setShowMessageStatus(true);
        }
    }

    private void handleMessageBox(){
        TypingThread typingThread = new TypingThread(myRef);

        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count > 0)
                    typingThread.typingStarted();
            }

            @Override
            public void afterTextChanged(Editable s) {
                typingThread.typingStopped();
                //myRef.child("online").setValue(Constants.ONLINE);
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
                        pullUp("Online");
                        break;
                    case Constants.TYPING:
                        pullUp("Typing...");
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
        chatsRef.keepSynced(true);
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
                try{

                    partner = dataSnapshot.getValue(User.class);
                    partnerName.setText(partner.getName());
                    init();
                    setupPartnerAvtarListener(partnerRef);
                } catch (Exception e){
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupPartnerAvtarListener(DatabaseReference partnerRef) {
        LocalStorage localStorage = new LocalStorage(this);

        partnerRef.child("avtar").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String avtarUri = dataSnapshot.getValue(String.class);
                Glide
                        .with(getApplicationContext())
                        .load(avtarUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user)
                        .into(avtarImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void storePartnerAvtarLocally(String avtarUri) {
        /*make sure to store uri in local storage*/


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

    @Override
    public void onFragmentInteraction(Uri uri) {

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
                Uri resultUri = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

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
}
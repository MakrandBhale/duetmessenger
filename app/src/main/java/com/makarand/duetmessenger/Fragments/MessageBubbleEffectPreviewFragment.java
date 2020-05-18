package com.makarand.duetmessenger.Fragments;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.R;
import com.vanniktech.emoji.EmojiTextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MessageBubbleEffectPreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageBubbleEffectPreviewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private AppCompatImageButton close,heartBeat,soft, angry, excited;
    private AppCompatImageButton sendHeartBeat,sendSoft, sendAngry, sendExcited;
    private AppCompatImageButton previousVisibleButton = null;
    private EmojiTextView emojiTextView;
    private RelativeLayout messageBubble;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    OnFragmentInteractionListener onFragmentInteractionListener = null;

    public MessageBubbleEffectPreviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MessageBubbleEffectPreviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessageBubbleEffectPreviewFragment newInstance(String param1, String param2) {
        MessageBubbleEffectPreviewFragment fragment = new MessageBubbleEffectPreviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_message_bubble_effect_preview, container, false);
        Bundle bundle = this.getArguments();
        String myValue = bundle.getString("myMessage");

        emojiTextView = view.findViewById(R.id.message_text);
        emojiTextView.setText(myValue);

        messageBubble = view.findViewById(R.id.message_preview);
        close = view.findViewById(R.id.fragment_close_button);

        heartBeat = view.findViewById(R.id.heart_beat_button);
        sendHeartBeat = view.findViewById(R.id.heart_beat_send_button);

        soft = view.findViewById(R.id.soft_button);
        sendSoft = view.findViewById(R.id.soft_send_button);

        angry = view.findViewById(R.id.angry_button);
        sendAngry = view.findViewById(R.id.angry_send_button);

        excited = view.findViewById(R.id.excited_button);
        sendExcited = view.findViewById(R.id.excited_send_button);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        setListeners();
        return view;
    }

    private void setListeners() {

        heartBeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSendButton(sendHeartBeat, Constants.HEART_BEAT);
            }
        });

        soft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSendButton(sendSoft, Constants.SOFT);
            }
        });

        angry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSendButton(sendAngry, Constants.ANGRY);
            }
        });

        excited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSendButton(sendExcited, Constants.EXCITED);
            }
        });
    }


    private void showSendButton(AppCompatImageButton sendButton, int tech){

        if(previousVisibleButton != null){
            previousVisibleButton.setVisibility(View.GONE);
        }
        Techniques techniques;
        switch (tech){
            case Constants.HEART_BEAT:
                techniques = Techniques.Pulse;
                break;
            case Constants.SOFT:
                techniques = Techniques.FadeIn;
                break;
            case Constants.ANGRY:
                techniques = Techniques.Shake;
                break;
            case Constants.EXCITED:
                techniques = Techniques.Tada;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + tech);
        }

        YoYo.with(techniques)
                .delay(50)
                .playOn(messageBubble);

        YoYo.with(Techniques.ZoomIn)
                .duration(150)
                .onStart(animator -> {
                    sendButton.setVisibility(View.VISIBLE);
                    sendButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onFragmentInteractionListener.onBubbleAnimationAdded(tech);
                            getActivity().onBackPressed();
                        }
                    });
                    //Toast.makeText(getActivity().getApplicationContext(), tech, Toast.LENGTH_SHORT).show();
                })
                .playOn(sendButton);
        previousVisibleButton = sendButton;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int AnimationType);
        void onBubbleAnimationAdded(int technique);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }
}

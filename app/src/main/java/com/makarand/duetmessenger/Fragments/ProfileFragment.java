package com.makarand.duetmessenger.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makarand.duetmessenger.Helper.Constants;
import com.makarand.duetmessenger.Helper.LocalStorage;
import com.makarand.duetmessenger.Model.User;
import com.makarand.duetmessenger.R;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import id.zelory.compressor.Compressor;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ProgressBar loader;
    private ImageView avtarImageView;
    private OnFragmentInteractionListener mListener;
    private LinearLayout nameContainer, chatroomIdContainer;
    private MaterialTextView nameTextView, chatroomIdTextView;
    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        MaterialButton changeAvtarButton = view.findViewById(R.id.change_avtar_button);
        ImageButton closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });
        avtarImageView = view.findViewById(R.id.avtar_imageview);
        loader = view.findViewById(R.id.loader);
        chatroomIdContainer = view.findViewById(R.id.chatroom_id_container);
        nameContainer = view.findViewById(R.id.name_container);
        nameTextView = view.findViewById(R.id.name_text_view);
        chatroomIdTextView = view.findViewById(R.id.chatroom_id_textview);

        fetchProfileInfo();
        fetchAvtarFromLocalStorage();
        changeAvtarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        nameContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNameChangeDialog();
            }
        });

        chatroomIdContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String chatroomId = chatroomIdTextView.getText().toString().trim();
                if(chatroomId.length() > 0) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, chatroomId);
                    sendIntent.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    startActivity(shareIntent);

                    /*ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("chatroomId", chatroomId);
                    assert clipboard != null;
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getActivity(), "Chatroom Id copied to clipboard", Toast.LENGTH_LONG);*/
                } else {
                    Toast.makeText(getActivity(), "Failed to get chatroom id from server, please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        return view;
    }

    private void openNameChangeDialog() {
        MaterialAlertDialogBuilder dialog =  new MaterialAlertDialogBuilder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.name_bottom_sheet_layout, null);

        TextInputEditText editText = promptsView.findViewById(R.id.new_name_edittext);

        dialog.setView(promptsView);
        dialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = editText.getText().toString().trim();
                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE + "/" + myUid + "/");
                myRef.child("name").setValue(name)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getActivity(), "Name updated.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Toast.makeText(getActivity(), "Failed to update name. Try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


        dialog.show();

    }

    private void fetchProfileInfo() {
        String myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE + "/" + myUid);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User me = dataSnapshot.getValue(User.class);
                if(me != null){
                    if(me.getName() != null && me.getChatroomId() != null){
                        nameTextView.setText(me.getName());
                        chatroomIdTextView.setText(me.getChatroomId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchAvtarFromLocalStorage() {
        loader.setVisibility(View.VISIBLE);
        LocalStorage localStorage = new LocalStorage(Objects.requireNonNull(getActivity()));
        String photoURI = localStorage.getString(Constants.AVTAR_LOCAL_KEY);
        if(photoURI == null){
            /*No record of avtar url locally fetch from db*/
            getAvtarUriFromDb();
        }
        Glide
                .with(getActivity())
                .load(photoURI)
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        loader.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        loader.setVisibility(View.GONE);
                        return false;
                    }
                })
                .centerCrop()
                .placeholder(R.drawable.ic_user)
                .into(avtarImageView);

        /*String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE + "/"+myUid+"/");

/*        myRef.child("avtar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loader.setVisibility(View.GONE);
            }
        });*/
    }

    private void getAvtarUriFromDb() {
        String myuid = FirebaseAuth.getInstance().getUid();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE + "/" + myuid);
        myRef.child("avtar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String avtarUri = dataSnapshot.getValue(String.class);
                if(avtarUri != null){
                    LocalStorage localStorage = new LocalStorage(Objects.requireNonNull(getActivity()));
                    localStorage.setString(Constants.AVTAR_LOCAL_KEY, String.valueOf(avtarUri));
                    fetchAvtarFromLocalStorage();
                } else {
                    /*user does not has a photo yet*/
                    loader.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loader.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Some error occurred: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private void openImagePicker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setFixAspectRatio(true)
                .setAspectRatio(1,1)
                .start(getActivity());

/*
        MaterialAlertDialogBuilder dialog =  new MaterialAlertDialogBuilder(getActivity());
        String[] colors = new String[]{
                "Open Camera",
                "Choose from gallery"
        };

        dialog.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        //camera
                        break;
                    case 1:
                        //gallery

                        break;
                }
            }
        });


        dialog.show();
*/

    }

    private void uploadImage(Uri photoUri){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageReference = storage.getReference("avtars/" + myUid + "/avtar/avtar.jpg");
        final Uri compressedImage = compressImage(photoUri);

        if(compressedImage == null){
            Toast.makeText(getActivity(), "Failed to upload image.", Toast.LENGTH_LONG).show();
            return;
        }

        storageReference.putFile(compressedImage)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                loader.setVisibility(View.GONE);
                                LocalStorage localStorage = new LocalStorage(getActivity());
                                localStorage.setString(Constants.AVTAR_LOCAL_KEY, String.valueOf(downloadUri));
                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constants.USERS_TREE + "/"+myUid+"/");
                                myRef.child("avtar").setValue(String.valueOf(downloadUri));
                                fetchAvtarFromLocalStorage();
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error uploading file:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        loader.setVisibility(View.GONE);
                    }
                });
    }

    private Uri compressImage(Uri photoUri) {
        try{
            File compressedImageFile =
                    new Compressor(getActivity())
                            .setQuality(50)
                    .compressToFile(new File(photoUri.getPath()));
            return Uri.fromFile(compressedImageFile);
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == getActivity().RESULT_OK) {
                loader.setVisibility(View.VISIBLE);
                Uri resultUri = result.getUri();
                //avtarImageView.setImageURI(resultUri);

                uploadImage(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}

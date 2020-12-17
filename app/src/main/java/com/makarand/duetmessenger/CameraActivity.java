package com.makarand.duetmessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.camerakit.CameraKitView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.makarand.duetmessenger.Helper.Constants;
import com.vanniktech.emoji.EmojiButton;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity {
    private CameraKitView cameraKitView;
    private static final int CAMERA_ACTIVITY_CODE = 1011;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @BindView(R.id.image_preview_imageview)
    AppCompatImageView imagePreviewImageview;
    @BindView(R.id.image_preview_container)
    RelativeLayout imagePreviewContainer;
    @BindView(R.id.send_button)
    AppCompatImageButton nextButton;
    @BindView(R.id.capture_bottom_bar) LinearLayout captureBottomBar;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        cameraKitView = findViewById(R.id.camera_view);
        verifyStoragePermissions(this);
    }

    @OnClick(R.id.capture_image_button)
    public void captureImage(View v){
        YoYo.with(Techniques.Pulse)
                .duration(500)
                .delay(50)
                .playOn(v);

        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {
                File dir = new File(Environment.getExternalStorageDirectory() + "/Duet Messenger/media/sent/");
                File savedPhoto = new File(dir, "DuetMessenger" + getDateFingerprint() + ".jpg");
                try {
                    if(!dir.exists()){
                        dir.mkdirs();
                    }
                    FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                    outputStream.write(capturedImage);
                    outputStream.close();
                    showPreviewImage(savedPhoto);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }

            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImageBack();
            }
        });
    }

    private void sendImageBack(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("cameraImage", imageUri.toString());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void showPreviewImage(File savedPhoto) {
        imageUri = Uri.fromFile(savedPhoto);
        if(imageUri == null){
            return;
        }
        Glide
                .with(getApplicationContext())
                .load(savedPhoto)
                .centerCrop()
                .into(imagePreviewImageview);
        toggleImagePreview();
    }

    private void toggleImagePreview() {
        if(imagePreviewContainer.getVisibility() == View.VISIBLE){
            captureBottomBar.setVisibility(View.VISIBLE);
            imagePreviewContainer.setVisibility(View.GONE);
        } else {
            captureBottomBar.setVisibility(View.GONE);
            imagePreviewContainer.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        cameraKitView.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraKitView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private String getDateFingerprint(){
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
        return ft.format(dNow);
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onBackPressed() {
        if(imagePreviewContainer.getVisibility() == View.VISIBLE){
            toggleImagePreview();
        } else {
            super.onBackPressed();
        }
    }
}

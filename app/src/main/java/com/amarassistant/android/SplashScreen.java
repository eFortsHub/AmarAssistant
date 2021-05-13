package com.amarassistant.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

public class SplashScreen extends AppCompatActivity {

    private final int SPLASH_DELAY = 1500;
    private ImageView mImageViewLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();

        getWindow().setBackgroundDrawable(null);



        initializeViews();
        animateLogo();
        goToMainPage();
    }


    private void initializeViews() {
        mImageViewLogo = findViewById(R.id.logos);
    }

    private void goToMainPage() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreen.this, Website.class));
            finish();
        }, SPLASH_DELAY);
    }

    private void animateLogo() {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_without_duration);
        fadeInAnimation.setDuration(SPLASH_DELAY);

        mImageViewLogo.startAnimation(fadeInAnimation);
    }
}

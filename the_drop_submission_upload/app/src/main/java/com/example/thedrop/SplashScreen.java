package com.example.thedrop;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.transition.Fade;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getWindow().setExitTransition(new Fade());
        getWindow().setEnterTransition(new Fade());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent loginScreen = new Intent(SplashScreen.this, LoginScreen.class);
                startActivity(loginScreen);
                finish();
            }
        }, 1500);
    }
}
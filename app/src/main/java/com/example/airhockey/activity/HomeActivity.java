package com.example.airhockey.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.airhockey.R;

public class HomeActivity extends AppCompatActivity {

    ImageView playBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        playBtn = findViewById(R.id.home_play_btn);
        playBtn.setOnClickListener((v -> {
//            TODO: CHANGE IT TO ConnectionActivity
            Intent intent = new Intent(getApplicationContext(), ConnectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }));
        Animation animation = new ScaleAnimation(1f, 1.05f, 1f, 1.05f);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        playBtn.startAnimation(animation);
    }



}
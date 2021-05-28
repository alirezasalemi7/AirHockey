package com.example.airhockey.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.example.airhockey.R;

public class ConnectionActivity extends AppCompatActivity {

    ImageView clientBtn;
    ImageView serverBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        clientBtn = findViewById(R.id.connection_client_btn);
        serverBtn = findViewById(R.id.connection_server_btn);
        Animation animation = new ScaleAnimation(1f, 1.05f, 1f, 1.05f);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        serverBtn.startAnimation(animation);
        clientBtn.startAnimation(animation);

        clientBtn.setOnClickListener(v -> {

        });
        serverBtn.setOnClickListener(v -> {

        });
    }
}
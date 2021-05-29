package com.example.airhockey.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.os.Bundle;
import android.view.View;

import com.example.airhockey.R;
import com.example.airhockey.view.StrikerView;


public class GameActivity extends AppCompatActivity {


    StrikerView strikerView;
    ConstraintLayout gameLayout;

    void setNewPositionForStriker(int width, int height, boolean player){
        if (strikerView != null){
            gameLayout.removeView(strikerView);
        }
        float startLocationFactor = 0.8f;
        strikerView = new StrikerView(this,width,height,player);
        strikerView.setOnTouchListener(strikerView);
        ConstraintSet set = new ConstraintSet();
        strikerView.setId(View.generateViewId());
        gameLayout.addView(strikerView, -1);
        set.clone(gameLayout);
        if (player) {
            set.connect(strikerView.getId(), ConstraintSet.TOP, gameLayout.getId(), ConstraintSet.TOP, (int) (startLocationFactor * height));
            set.connect(strikerView.getId(),ConstraintSet.BOTTOM,gameLayout.getId(),ConstraintSet.BOTTOM);
        }
        else {
            set.connect(strikerView.getId(), ConstraintSet.TOP, gameLayout.getId(), ConstraintSet.TOP);
            set.connect(strikerView.getId(),ConstraintSet.BOTTOM,gameLayout.getId(),ConstraintSet.BOTTOM, (int) (startLocationFactor * height));

        }
        set.connect(strikerView.getId(),ConstraintSet.LEFT,gameLayout.getId(),ConstraintSet.LEFT);
        set.connect(strikerView.getId(),ConstraintSet.RIGHT,gameLayout.getId(),ConstraintSet.RIGHT);
        set.applyTo(gameLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int height = getWindowManager().getDefaultDisplay().getHeight();
        gameLayout = findViewById(R.id.board_layout);
        setNewPositionForStriker(width,height,false);
    }
}
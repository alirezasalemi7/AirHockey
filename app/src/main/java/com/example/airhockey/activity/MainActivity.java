package com.example.airhockey.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.airhockey.R;
import com.example.airhockey.view.StrikerView;

public class MainActivity extends AppCompatActivity {


    StrikerView strikerView;
    ConstraintLayout gameLayout;

    void setNewPositionForStriker(int width, int height){
        if (strikerView != null){
            gameLayout.removeView(strikerView);
        }
        strikerView = new StrikerView(this,width,height);
        strikerView.setOnTouchListener(strikerView);
        ConstraintSet set = new ConstraintSet();
        strikerView.setId(View.generateViewId());
        gameLayout.addView(strikerView, -1);
        set.clone(gameLayout);
        set.connect(strikerView.getId(),ConstraintSet.TOP,gameLayout.getId(),ConstraintSet.TOP,(int)(0.8*height));
        set.connect(strikerView.getId(),ConstraintSet.BOTTOM,gameLayout.getId(),ConstraintSet.BOTTOM);
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
        setNewPositionForStriker(width,height);
    }
}
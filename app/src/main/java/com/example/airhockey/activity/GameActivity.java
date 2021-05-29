package com.example.airhockey.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.os.Bundle;
import android.view.View;

import com.example.airhockey.R;
import com.example.airhockey.view.StrikerView;


public class GameActivity extends AppCompatActivity {


    StrikerView playerStrikerView;
    StrikerView opponentStrikerView;
    ConstraintLayout gameLayout;

    void setNewPositionForPlayerStriker(int width, int height){
        if (playerStrikerView != null){
            gameLayout.removeView(playerStrikerView);
        }
        playerStrikerView = new StrikerView(this,width,height,true);
        playerStrikerView.setOnTouchListener(playerStrikerView);
        setPositionForStriker(playerStrikerView, width, height, true);
    }

    void setNewPositionForOpponentStriker(int width, int height){
        if (opponentStrikerView != null){
            gameLayout.removeView(opponentStrikerView);
        }
        opponentStrikerView = new StrikerView(this,width,height,false);
        opponentStrikerView.setOnTouchListener(opponentStrikerView);
        setPositionForStriker(opponentStrikerView, width, height, false);
    }

    void setPositionForStriker(StrikerView view,int width, int height, boolean player){
        float startLocationFactor = 0.8f;
        ConstraintSet set = new ConstraintSet();
        view.setId(View.generateViewId());
        gameLayout.addView(view, -1);
        set.clone(gameLayout);
        if (player) {
            set.connect(view.getId(), ConstraintSet.TOP, gameLayout.getId(), ConstraintSet.TOP, (int) (startLocationFactor * height));
            set.connect(view.getId(),ConstraintSet.BOTTOM,gameLayout.getId(),ConstraintSet.BOTTOM);
        }
        else {
            set.connect(view.getId(), ConstraintSet.TOP, gameLayout.getId(), ConstraintSet.TOP);
            set.connect(view.getId(),ConstraintSet.BOTTOM,gameLayout.getId(),ConstraintSet.BOTTOM, (int) (startLocationFactor * height));

        }
        set.connect(view.getId(),ConstraintSet.LEFT,gameLayout.getId(),ConstraintSet.LEFT);
        set.connect(view.getId(),ConstraintSet.RIGHT,gameLayout.getId(),ConstraintSet.RIGHT);
        set.applyTo(gameLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int height = getWindowManager().getDefaultDisplay().getHeight();
        gameLayout = findViewById(R.id.board_layout);
        setNewPositionForPlayerStriker(width,height);
        setNewPositionForOpponentStriker(width,height);
    }
}
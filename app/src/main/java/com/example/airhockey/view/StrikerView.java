package com.example.airhockey.view;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.airhockey.R;
import com.example.airhockey.utils.SerializablePair;

public class StrikerView extends androidx.appcompat.widget.AppCompatImageView implements View.OnTouchListener {

    private float dX = 0,dY = 0;
    private int width,height;
    private float radiusFactor = 0.1f;
    private int radius;
    private float posX,posY;
    private boolean player;
    private boolean isPositionChanged = false;

    public StrikerView(@NonNull Context context, int width, int height, boolean player) {
        super(context);
        if (player){
            this.setImageResource(R.drawable.img_player);
        }
        else {
            this.setImageResource(R.drawable.img_com);
        }
        this.width = width;
        this.height = height;
        radius = (int) (radiusFactor * width);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(2*radius,2*radius);
        this.setLayoutParams(params);
        this.player = player;
        Log.i("sizeX", ""+width);
        Log.i("sizeY", ""+height);

    }

    private float calculatePosX(float x){
        if (x < 0){
            return 0;
        }
        if (x + 2 * radius > width){
            return width - 2 * radius;
        }
        return x;
    }

    private float calculatePosY(float y){
        if (y < 0){
            return 0;
        }
        if (y + 2 * radius > height){
            return height - 2 * radius;
        }
        if (y < height/2f && player){
            return height/2f;
        }
        if (y + 2 * radius > height/2f && !player){
            return height/2f - 2 * radius;
        }
        return y;
    }

    public void setPosition(float x, float y){
        if (!player){
            x = x - 2 * radius;
            y = y - 2 * radius;
        }
        this.animate()
                .x(calculatePosX(x))
                .y(calculatePosY(y))
                .setDuration(0)
                .start();
        posX = x;
        posY = y;
        isPositionChanged = true;
    }

    public boolean isPositionChanged() {
        boolean temp = isPositionChanged;
        isPositionChanged = false;
        return temp;
    }

    public SerializablePair<Integer, Integer> getPosition() {
        return new SerializablePair<>((int) posX, (int) posY);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (!player){
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            {
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();

            } break;
                case MotionEvent.ACTION_MOVE:
            {
                setPosition(event.getRawX() + dX, event.getRawY() + dY);
            } break;
            default:
                return false;
        }
        return true;
    }
}

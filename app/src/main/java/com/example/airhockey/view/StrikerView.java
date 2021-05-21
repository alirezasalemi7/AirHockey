package com.example.airhockey.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.airhockey.R;

public class StrikerView extends androidx.appcompat.widget.AppCompatImageView implements View.OnTouchListener {

    float dX = 0,dY = 0;
    int width,height;
    float radiusFactor = 0.1f;
    int radius;
    float posX,posY;

    public StrikerView(@NonNull Context context, int width, int height) {
        super(context);
        this.setImageResource(R.drawable.img_player);
        this.width = width;
        this.height = height;
        radius = (int) (radiusFactor * width);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(2*radius,2*radius);
        this.setLayoutParams(params);
        Log.i("sizeX", ""+width);
        Log.i("sizeY", ""+height);
    }

    public StrikerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setImageResource(R.drawable.img_player);
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
        if (y < height/2){
            return height/2;
        }
        return y;
    }

    public void setPosition(float x, float y){
        this.animate()
                .x(calculatePosX(x))
                .y(calculatePosY(y))
                .setDuration(0)
                .start();
        posX = x;
        posY = y;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
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

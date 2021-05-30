package com.example.airhockey.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.view.View;

import com.example.airhockey.R;
import com.example.airhockey.models.MessageConstants;
import com.example.airhockey.services.BluetoothService;
import com.example.airhockey.utils.LocationConverter;
import com.example.airhockey.view.StrikerView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class GameActivity extends AppCompatActivity {


    StrikerView playerStrikerView;
    StrikerView opponentStrikerView;
    ConstraintLayout gameLayout;

    private BluetoothService bluetoothService = BluetoothService.getInstance();
    private boolean isPositionChanged = false;
    private LocationConverter converter;

    private class BluetoothHandler extends Handler {
        public BluetoothHandler() {
            super();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MessageConstants.MESSAGE_READ:
                    Pair<Integer, Integer> position = converter.reflect(converter.convertToRealPoint((Pair<Double, Double>) deserialize((byte[]) msg.obj)));
                    opponentStrikerView.setPosition(position.first.floatValue(), position.second.floatValue());
                    break;
            }
        }
    }

    private final Handler bluetoothHandler = new BluetoothHandler();

    void setNewPositionForPlayerStriker(int width, int height) {
        if (playerStrikerView != null) {
            gameLayout.removeView(playerStrikerView);
        }
        playerStrikerView = new StrikerView(this, width, height, true);
        playerStrikerView.setOnTouchListener(playerStrikerView);
        setPositionForStriker(playerStrikerView, width, height, true);
        isPositionChanged = true;
    }

    void setNewPositionForOpponentStriker(int width, int height) {
        if (opponentStrikerView != null) {
            gameLayout.removeView(opponentStrikerView);
        }
        opponentStrikerView = new StrikerView(this, width, height, false);
        opponentStrikerView.setOnTouchListener(opponentStrikerView);
        setPositionForStriker(opponentStrikerView, width, height, false);
    }

    void setPositionForStriker(StrikerView view, int width, int height, boolean player) {
        float startLocationFactor = 0.8f;
        ConstraintSet set = new ConstraintSet();
        view.setId(View.generateViewId());
        gameLayout.addView(view, -1);
        set.clone(gameLayout);
        if (player) {
            set.connect(view.getId(), ConstraintSet.TOP, gameLayout.getId(), ConstraintSet.TOP, (int) (startLocationFactor * height));
            set.connect(view.getId(), ConstraintSet.BOTTOM, gameLayout.getId(), ConstraintSet.BOTTOM);
        } else {
            set.connect(view.getId(), ConstraintSet.TOP, gameLayout.getId(), ConstraintSet.TOP);
            set.connect(view.getId(), ConstraintSet.BOTTOM, gameLayout.getId(), ConstraintSet.BOTTOM, (int) (startLocationFactor * height));

        }
        set.connect(view.getId(), ConstraintSet.LEFT, gameLayout.getId(), ConstraintSet.LEFT);
        set.connect(view.getId(), ConstraintSet.RIGHT, gameLayout.getId(), ConstraintSet.RIGHT);
        set.applyTo(gameLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int height = getWindowManager().getDefaultDisplay().getHeight();
        gameLayout = findViewById(R.id.board_layout);
        bluetoothService.setHandler(bluetoothHandler);
        converter = new LocationConverter(height, width);
        setNewPositionForPlayerStriker(width, height);
        setNewPositionForOpponentStriker(width, height);
        Thread gameThread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                gameLoop();
            }
        });
    }


    public void gameLoop() {
//        TODO: change condition to win or lose
        while (true) {
            if (isPositionChanged) {
                bluetoothService.write(serialize(converter.convertToFractionalPoint(playerStrikerView.getPosition())));
                isPositionChanged = false;
            }
        }
    }

    public byte[] serialize(Object object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object deserialize(byte[] bytes) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
package com.example.airhockey.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.example.airhockey.R;
import com.example.airhockey.models.MessageConstants;
import com.example.airhockey.models.State;
import com.example.airhockey.services.BluetoothService;
import com.example.airhockey.utils.LocationConverter;
import com.example.airhockey.utils.PhysicalEventCalculator;
import com.example.airhockey.utils.ProtocolUtils;
import com.example.airhockey.view.BallView;
import com.example.airhockey.models.Pair;
import com.example.airhockey.view.StrikerView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


public class GameActivity extends AppCompatActivity {


    StrikerView playerStrikerView;
    StrikerView opponentStrikerView;
    BallView ballView;
    ConstraintLayout gameLayout;
    PhysicalEventCalculator physicalEventCalculator;

    private BluetoothService bluetoothService = BluetoothService.getInstance();
    private boolean isPositionChanged = false;
    private LocationConverter converter;

    private Timer goalAckTimer;
    private Timer collisionTimer;
    private long TIMEOUT = 100;

    private AtomicBoolean waitForSync;

    private int scorePlayer = 0;
    private int scoreOpponent = 0;
    private final int MAX_SCORE_TO_WIN = 7;
    int width;
    int height;

    private class BluetoothHandler extends Handler {
        public BluetoothHandler() {
            super();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MessageConstants.MESSAGE_READ:
                    byte[] msgBytes = (byte[]) msg.obj;
                    InputStream inputStream = new ByteArrayInputStream(msgBytes);
                    ProtocolUtils.MessageTypes type = ProtocolUtils.getTypeOfMessage(inputStream);
                    if (type == ProtocolUtils.MessageTypes.POSITION_REPORT){
                        Pair<Double,Double> rPosition = null;
                        try {
                            rPosition = ProtocolUtils.receivePositionMessage(inputStream);
                            Pair<Integer, Integer> position = converter.reflectPosition(converter.convertToRealPoint(rPosition));
                            opponentStrikerView.setPosition(position.first.floatValue(), position.second.floatValue());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (type == ProtocolUtils.MessageTypes.BALL_COLLISION_REPORT){
                        Pair<Pair<Double,Double>,Pair<Double,Double>> ballInfo = null;
                        Pair<Double,Double> collisionPosition = null;
                        Pair<Double,Double> collisionSpeed = null;
                        try {
                            ballInfo = ProtocolUtils.receiveBallCollisionMessage(inputStream);
                            collisionPosition = ballInfo.first;
                            collisionSpeed = ballInfo.second;
                            Pair<Double, Double> position = converter.reflectPositionBall(converter.convertToRealPoint(collisionPosition));
                            Pair<Double, Double> speed = converter.reflectSpeed(converter.convertToRealPoint(collisionSpeed));
                            physicalEventCalculator.setBallNewState(position, speed);
                            bluetoothService.write(ProtocolUtils.sendBallCollisionAck());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (type == ProtocolUtils.MessageTypes.BALL_COLLISION_ACK){
                        stopCollisionTimer();
                    }
                    if (type == ProtocolUtils.MessageTypes.GOAL_SCORED_REPORT){
                        scorePlayer += 1;
                        Toast.makeText(getApplicationContext(), "player " + scorePlayer + " - " + scoreOpponent + " opponent",Toast.LENGTH_LONG).show();
                        setNewPositionForPlayerStriker(width, height);
                        setNewPositionForOpponentStriker(width, height);
                        bluetoothService.write(ProtocolUtils.sendGoalSCoredAck());
                        if (scorePlayer == MAX_SCORE_TO_WIN || scoreOpponent == MAX_SCORE_TO_WIN){
                            bluetoothService.stopConnection();
                            goToEndGame();
                        }
                        else {
                            setAfterGoalPositionForBall(true);
                        }
                    }
                    if (type == ProtocolUtils.MessageTypes.GOAL_SCORED_ACK){
                        stopGoalAckTimer();
                        waitForSync.set(false);
                        Toast.makeText(getApplicationContext(), "player " + scorePlayer + " - " + scoreOpponent + " opponent",Toast.LENGTH_LONG).show();
                        setNewPositionForPlayerStriker(width, height);
                        setNewPositionForOpponentStriker(width, height);
                        if (scorePlayer == MAX_SCORE_TO_WIN || scoreOpponent == MAX_SCORE_TO_WIN){
                            bluetoothService.stopConnection();
                            goToEndGame();
                        }
                        else {
                            setAfterGoalPositionForBall(false);
                        }
                    }
                    break;
            }
        }
    }

    void goToEndGame() {
        Intent intent = new Intent(getApplicationContext(), EndGameActivity.class);
        intent.putExtra("player_score", scorePlayer);
        intent.putExtra("opponent_score", scoreOpponent);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    void startGoalAckTimer(){
        goalAckTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                bluetoothService.write(ProtocolUtils.sendGoalSCored());
                goalAckTimer.schedule(this, TIMEOUT);
            }
        }, TIMEOUT);
    }

    void stopGoalAckTimer(){
        goalAckTimer.cancel();
    }

    void startCollisionTimer(){
        collisionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                State ballState = physicalEventCalculator.getBallState();
                bluetoothService.write(ProtocolUtils.sendBallCollision(ballState.getPosition(), ballState.getVelocity()));
                collisionTimer.schedule(this, TIMEOUT);
            }
        }, TIMEOUT);
    }

    void stopCollisionTimer(){
        collisionTimer.cancel();
    }

    private final Handler bluetoothHandler = new BluetoothHandler();

    void setNewPositionForPlayerStriker(int width, int height) {
        if (playerStrikerView != null) {
            gameLayout.removeView(playerStrikerView);
        }
        playerStrikerView = new StrikerView(this, width, height, true);
        playerStrikerView.setOnTouchListener(playerStrikerView);
        setPositionForStriker(playerStrikerView, width, height, true);
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

    void setStartPositionForBall() {
        if (ballView != null){
            gameLayout.removeView(ballView);
        }
        ballView = new BallView(getApplicationContext(), width, height);
        ConstraintSet set = new ConstraintSet();
        ballView.setId(View.generateViewId());
        gameLayout.addView(ballView, -1);
        set.clone(gameLayout);
        set.connect(ballView.getId(), ConstraintSet.TOP, gameLayout.getId(), ConstraintSet.TOP);
        set.connect(ballView.getId(), ConstraintSet.BOTTOM, gameLayout.getId(), ConstraintSet.BOTTOM);
        set.connect(ballView.getId(), ConstraintSet.LEFT, gameLayout.getId(), ConstraintSet.LEFT);
        set.connect(ballView.getId(), ConstraintSet.RIGHT, gameLayout.getId(), ConstraintSet.RIGHT);
        set.applyTo(gameLayout);
    }

    void setAfterGoalPositionForBall(boolean playerScored) {
        if (ballView != null){
            gameLayout.removeView(ballView);
        }
        ballView = new BallView(getApplicationContext(), width, height);
        float startLocationFactor = 0.6f;
        ConstraintSet set = new ConstraintSet();
        ballView.setId(View.generateViewId());
        gameLayout.addView(ballView, -1);
        set.clone(gameLayout);
        if (playerScored) {
            set.connect(ballView.getId(), ConstraintSet.TOP, gameLayout.getId(), ConstraintSet.BOTTOM, (int) (startLocationFactor * height));
            set.connect(ballView.getId(), ConstraintSet.BOTTOM, gameLayout.getId(), ConstraintSet.TOP);
        } else {
            set.connect(ballView.getId(), ConstraintSet.TOP, gameLayout.getId(), ConstraintSet.BOTTOM);
            set.connect(ballView.getId(), ConstraintSet.BOTTOM, gameLayout.getId(), ConstraintSet.TOP, (int) (startLocationFactor * height));

        }
        set.connect(ballView.getId(), ConstraintSet.LEFT, gameLayout.getId(), ConstraintSet.LEFT);
        set.connect(ballView.getId(), ConstraintSet.RIGHT, gameLayout.getId(), ConstraintSet.RIGHT);
        set.applyTo(gameLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameLayout = findViewById(R.id.board_layout);
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        physicalEventCalculator = new PhysicalEventCalculator(width,height);
        bluetoothService.setHandler(bluetoothHandler);
        converter = new LocationConverter(height, width);
        setNewPositionForPlayerStriker(width, height);
        setNewPositionForOpponentStriker(width, height);
        setStartPositionForBall();
        Thread gameThread = new Thread(() -> {
                gameLoop();
        });
        gameThread.start();
    }


    public void gameLoop() {
        waitForSync.set(false);
        physicalEventCalculator.setRadius(ballView.getRadius(), playerStrikerView.getRadius());
        while (true) {
            while (waitForSync.get());
            if (scorePlayer == MAX_SCORE_TO_WIN || scoreOpponent == MAX_SCORE_TO_WIN){
                break;
            }
            if (playerStrikerView.isPositionChanged()) {
                Pair<Double,Double> currentPoint = converter.convertToFractionalPoint(playerStrikerView.getPosition());
                byte[] array = ProtocolUtils.sendStrikerPosition(currentPoint);
                bluetoothService.write(array);
            }
            if (physicalEventCalculator.isGoalScored()){
                bluetoothService.write(ProtocolUtils.sendGoalSCored());
                waitForSync.set(true);
                startGoalAckTimer();
                continue;
            }
            if (physicalEventCalculator.isHitToStriker(playerStrikerView.getPosition(), ballView.getPosition(), playerStrikerView.getRadius(), ballView.getRadius())){
                Pair<Double,Double> position = physicalEventCalculator.getCollisionPositionForBall();
                Pair<Double,Double> velocity = physicalEventCalculator.getSpeedOfBallAfterCollision();
                bluetoothService.write(ProtocolUtils.sendBallCollision(position,velocity));
                startCollisionTimer();
            }
            physicalEventCalculator.move(0.017);
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {}
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
            objectInputStream.read();
            return objectInputStream.readObject();
        } catch (StreamCorruptedException e){
            return null;
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
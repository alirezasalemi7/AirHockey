package com.example.airhockey.utils;

import android.util.Log;

import com.example.airhockey.models.Pair;
import com.example.airhockey.models.State;


public class PhysicalEventCalculator {
    private final int AXIS_X = 0;
    private final int AXIS_Y = 1;
    private final int CORNER = 2;
    private final double GOAL_LENGTH_FACTOR = 0.5;
    private final double GOAL_WITH_FACTOR = 0.05;
    private final int xLength;
    private final int yLength;
    private int axis;
    private int ballRadius;
    private int strikerRadius;
    private State prevBallState;
    private State currentBallState;
    private State prevPlayerStrikerState;
    private State currentPlayerStrikerState;

    public PhysicalEventCalculator(int xLength, int yLength, State initBall, State initStriker) {
        this.xLength = xLength;
        this.yLength = yLength;
        this.prevBallState = initBall;
        this.currentBallState = new State(initBall.getVelocity(), initBall.getPosition());
        this.prevPlayerStrikerState = initStriker;
        this.currentPlayerStrikerState = new State(initStriker.getVelocity(),initStriker.getPosition());
        Log.e("loc", initBall.getPosition().toString());
    }

    public void setRadius(int ballRadius, int strikerRadius) {
        this.ballRadius = ballRadius;
        this.strikerRadius = strikerRadius;
    }

    public State reflectHittingToSurface() {
        switch (axis) {
            case AXIS_X:
                return new State(new Pair<>(-currentBallState.getVelocity().first*0.8, currentBallState.getVelocity().second*0.8)
                        , new Pair<>(currentBallState.getPosition().first, currentBallState.getPosition().second));
            case AXIS_Y:
                return new State(new Pair<>(currentBallState.getVelocity().first*0.8, -currentBallState.getVelocity().second*0.8)
                        , new Pair<>(currentBallState.getPosition().first, currentBallState.getPosition().second));
            case CORNER:
                return new State(new Pair<>(-currentBallState.getVelocity().first*0.8, -currentBallState.getVelocity().second*0.8)
                        , new Pair<>(currentBallState.getPosition().first, currentBallState.getPosition().second));
//            default: TODO -> Throw error
        }
        return null;
    }

    private Pair<Double, Double> moveWithSteadyVelocity(double dt, Pair<Double, Double> velocity, Pair<Double, Double> curBallPos) {
        return new Pair<>(curBallPos.first + velocity.first * dt, curBallPos.second + velocity.second * dt);
    }

    public void setBallNewState(Pair<Double,Double> position, Pair<Double,Double> velocity){
        currentBallState = new State(velocity, position);
    }

    public void setPlayerStrikerPosition(Pair<Double,Double> position){
        double prevX = prevPlayerStrikerState.getPosition().first;
        double prevY = prevPlayerStrikerState.getPosition().second;
        prevPlayerStrikerState = currentPlayerStrikerState;
        currentPlayerStrikerState = new State(new Pair<>(position.first-prevX,position.second-prevY), position);
    }

    public void move(double dt) {
        setPlayerStrikerPosition(currentPlayerStrikerState.getPosition());
        Pair<Double, Double> curBallPos = currentBallState.getPosition();
        State newState;
        if (isHitToStriker()) {
            Log.e("collision", "here");
            Pair<Double, Double> velocity = calculateVelocityAfterHit();
            newState = new State(velocity, moveWithSteadyVelocity(dt, velocity, curBallPos));
        } else if (checkHittingToWalls()) {
            newState = reflectHittingToSurface();
        } else {
            Pair<Double, Double> velocity = currentBallState.getVelocity();
            newState = new State(velocity, moveWithSteadyVelocity(dt, velocity, curBallPos));
        }
        prevBallState = currentBallState;
        currentBallState = newState;
    }

    public boolean isHitToStriker() {
        Pair<Double, Double> strikerPos = currentPlayerStrikerState.getPosition();
        Pair<Double, Double> ballPos = currentBallState.getPosition();
//        Log.e("loc", strikerPos.toString());
//        Log.e("loc", ballPos.toString());
        return (strikerRadius + ballRadius) >= (Math.sqrt(Math.pow((ballPos.first - strikerPos.first), 2) + Math.pow((ballPos.second - strikerPos.second), 2)));
    }

    public Pair<Double, Double> calculateVelocityAfterHit() {
        Pair<Double, Double> vb = currentBallState.getVelocity();
        Pair<Double, Double> vs = currentPlayerStrikerState.getVelocity();
        Log.e("velocity", vs.toString());
        return new Pair<>(2 * vs.first - vb.first, 2 * vs.second - vb.second);
    }

    public boolean checkHittingToWalls() {
        Pair<Double, Double> currentPosition = currentBallState.getPosition();
        Pair<Double, Double> previousPosition = prevBallState.getPosition();
        boolean isHit = false;
        if ((currentPosition.first <= ballRadius && previousPosition.first > ballRadius) || (currentPosition.first >= (xLength - ballRadius) && previousPosition.first < (xLength - ballRadius))) {
            axis = AXIS_X;
            isHit = true;
        }
        if ((currentPosition.second <= ballRadius && previousPosition.second > ballRadius) || (currentPosition.second >= (yLength - ballRadius) && previousPosition.second < (yLength - ballRadius))) {
            axis = isHit ? CORNER : AXIS_Y;
            isHit = true;
        }
        return isHit;
    }

    public Pair<Double,Double> getCollisionPositionForBall() {
        return prevBallState.getPosition();
    }

    public Pair<Double,Double> getSpeedOfBallAfterCollision() {
        return calculateVelocityAfterHit();
    }

    public boolean isGoalScored() {
        double x = currentBallState.getPosition().first;
        double y = currentBallState.getPosition().second;
        if (x > xLength * (1 - GOAL_LENGTH_FACTOR)/2 && x < xLength * ((1 - GOAL_LENGTH_FACTOR)/2 + GOAL_LENGTH_FACTOR)){
            return y > (1 - GOAL_WITH_FACTOR) * yLength;
        }
        return false;
    }

    public State getBallState() {
        return currentBallState;
    }
}
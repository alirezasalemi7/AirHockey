package com.example.airhockey.utils;

import com.example.airhockey.models.Pair;
import com.example.airhockey.models.State;


public class PhysicalEventCalculator {
    private final int AXIS_X = 0;
    private final int AXIS_Y = 1;
    private final int CORNER = 2;
    private final double GOAL_LENGTH_fACTOR = 0.5;
    private final double GOAL_WITH_FACTOR = 0.05;
    private int xLength;
    private int yLength;
    private int axis;
    private int ballRadius;
    private int strikerRadius;
    private State prevBallState;
    private State currentBallState;
    private State prevPlayerStrikerState;
    private State currentPlayerStrikerState;

    public PhysicalEventCalculator(int xLength, int yLength) {
        this.xLength = xLength;
        this.yLength = yLength;
    }

    public void setRadius(int ballRadius, int strikerRadius) {
        this.ballRadius = ballRadius;
        this.strikerRadius = strikerRadius;
    }

    public State reflectHittingToSurface(State current) {
        switch (axis) {
            case AXIS_X:

                return new State(new Pair<>(-current.getVelocity().first, current.getVelocity().second)
                        , new Pair<>(current.getPosition().first, current.getPosition().second));
            case AXIS_Y:
                return new State(new Pair<>(current.getVelocity().first, -current.getVelocity().second)
                        , new Pair<>(current.getPosition().first, current.getPosition().second));
            case CORNER:
                return new State(new Pair<>(-current.getVelocity().first, -current.getVelocity().second)
                        , new Pair<>(current.getPosition().first, current.getPosition().second));
//            default: TODO -> Throw error
        }
        return null;
    }

    public void setBallNewState(Pair<Double,Double> position, Pair<Double,Double> velocity){
        currentBallState = new State(velocity, position);
    }

    public void setPlayerStrikerPosition(Pair<Double,Double> position){
        double prevX = prevPlayerStrikerState.getPosition().first;
        double prevY = prevPlayerStrikerState.getPosition().second;
        currentPlayerStrikerState = new State(new Pair<>(position.first-prevX,position.second-prevY), position);
    }

    public void move(double dt) {
        setPlayerStrikerPosition(currentPlayerStrikerState.getPosition());
        // todo complete move. we have radius here
    }

    public boolean isHitToStriker(Pair<Integer, Integer> strikerPos, Pair<Integer, Integer> ballPos, int strikerRad, int ballRad) {
        return (ballRad + strikerRad) >= (Math.sqrt(Math.pow((ballPos.first - strikerPos.first), 2) + Math.pow((ballPos.first - strikerPos.first), 2)));
    }

    public Pair<Double, Double> calculateVelocityAfterHit(Pair<Double, Double> vb, Pair<Double, Double> vs) {
        return new Pair<>(2 * vs.first - vb.first, 2 * vs.second - vb.second);
    }

    public boolean checkHittingToWalls(State current, State previous, int radius) {
        Pair<Double, Double> currentPosition = current.getPosition();
        Pair<Double, Double> previousPosition = previous.getPosition();
        boolean isHit = false;
        if ((currentPosition.first <= radius && previousPosition.first > radius) || (currentPosition.first >= (xLength - radius) && previousPosition.first < (xLength - radius))) {
            axis = AXIS_X;
            isHit = true;
        }
        if ((currentPosition.second <= radius && previousPosition.second > radius) || (currentPosition.second >= (yLength - radius) && previousPosition.second < (yLength - radius))) {
            axis = isHit ? CORNER : AXIS_Y;
            isHit = true;
        }
        return isHit;
    }

    public Pair<Double,Double> getCollisionPositionForBall() {
        return prevBallState.getPosition();
    }

    public Pair<Double,Double> getSpeedOfBallAfterCollision() {
        return calculateVelocityAfterHit(prevBallState.getVelocity(), prevPlayerStrikerState.getVelocity());
    }

    public boolean isGoalScored() {
        double x = currentBallState.getPosition().first;
        double y = currentBallState.getPosition().second;
        if (x > xLength * (1 - GOAL_LENGTH_fACTOR)/2 && x < xLength * ((1 - GOAL_LENGTH_fACTOR)/2 + GOAL_LENGTH_fACTOR)){
            if (y > (1 - GOAL_WITH_FACTOR) * yLength){
                return true;
            }
        }
        return false;
    }

    public State getBallState() {
        return currentBallState;
    }
}
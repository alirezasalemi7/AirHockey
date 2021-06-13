package com.example.airhockey.utils;

import com.example.airhockey.models.Pair;
import com.example.airhockey.models.State;


public class PhysicalEventCalculator {
    private final int AXIS_X = 0;
    private final int AXIS_Y = 1;
    private final int CORNER = 2;
    private int xLength;
    private int yLength;
    private int axis;

    public PhysicalEventCalculator(int xLength, int yLength) {
        this.xLength = xLength;
        this.yLength = yLength;
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

    public State move(State current, int dt) {
        return new State(new Pair<>(current.getVelocity().first, current.getVelocity().second)
                , new Pair<>(current.getPosition().first + current.getVelocity().first * dt
                    , current.getPosition().second + current.getVelocity().second * dt));
    }

    public boolean isHittedToStriker(Pair<Double, Double> strikerPos, Pair<Double, Double> ballPos, int strikerRad, int ballRad) {
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

}
package com.example.airhockey.utils;

import android.util.Log;

import com.example.airhockey.models.Pair;
import com.example.airhockey.models.State;
import com.example.airhockey.models.Vector;


public class PhysicalEventCalculator {
    private final int AXIS_X = 0;
    private final int AXIS_Y = 1;
    private final int CORNER = 2;
    private final double GOAL_LENGTH_FACTOR = 0.5;
    private final double GOAL_WITH_FACTOR = 0.05;
    private final int xLength;
    private final int yLength;
    private final double remainedForce = 0.8;
    private int axis;
    private int ballRadius;
    private int strikerRadius;
    private State prevBallState;
    private State currentBallState;
    private State prevPlayerStrikerState;
    private State currentPlayerStrikerState;
    private final double dt;
    private boolean touched = false;

    public PhysicalEventCalculator(int xLength, int yLength, State initBall, State initStriker, double dt) {
        this.dt = dt;
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
                return new State(new Pair<>(-currentBallState.getVelocity().first * remainedForce, currentBallState.getVelocity().second)
                        , new Pair<>(currentBallState.getPosition().first, currentBallState.getPosition().second));
            case AXIS_Y:
                return new State(new Pair<>(currentBallState.getVelocity().first, -currentBallState.getVelocity().second * remainedForce)
                        , new Pair<>(currentBallState.getPosition().first, currentBallState.getPosition().second));
            case CORNER:
                return new State(new Pair<>(-currentBallState.getVelocity().first * remainedForce, -currentBallState.getVelocity().second * remainedForce)
                        , new Pair<>(currentBallState.getPosition().first, currentBallState.getPosition().second));
//            default: TODO -> Throw error
        }
        return null;
    }

    private Pair<Double, Double> moveWithSteadyVelocity(double dt, Pair<Double, Double> velocity) {
        Pair<Double, Double> curBallPos = currentBallState.getPosition();
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

    private Double findDistance(Pair<Double, Double> a, Pair<Double, Double> b) {
        return (Math.sqrt(Math.pow((a.first - b.first), 2) + Math.pow((a.second - b.second), 2)));
    }

    public void move() {
        Pair<Double, Double> curStrikerPos = currentPlayerStrikerState.getPosition();
        setPlayerStrikerPosition(curStrikerPos);
        Pair<Double, Double> curBallPos = currentBallState.getPosition();
        State newState;
        if (isHitToStriker(curStrikerPos, curBallPos)) {
            Log.e("collision", "here");
            Pair<Double, Double> velocity = calculateVelocityAfterHit();
            double distanceFactor = (ballRadius + strikerRadius) / findDistance(curBallPos, curStrikerPos);
            newState = new State(velocity
                        , new Pair<>((1 - distanceFactor) * curStrikerPos.first + distanceFactor * curBallPos.first
                            , (1 - distanceFactor) * curStrikerPos.second + distanceFactor * curBallPos.second));
//            newState = checkBallCollision();
        } else if (checkHittingToWalls()) {
            this.touched = false;
            newState = reflectHittingToSurface();
        } else {
            this.touched = false;
            Pair<Double, Double> velocity = currentBallState.getVelocity();
            newState = new State(velocity, moveWithSteadyVelocity(dt, velocity));
        }
        prevBallState = currentBallState;
        currentBallState = newState;
    }

    public void updateByHittingToStriker() {
        State newState;
        Pair<Double, Double> curBallPos = currentBallState.getPosition();
        Pair<Double, Double> curStrikerPos = currentPlayerStrikerState.getPosition();
        if (isHitToStriker(curStrikerPos, curBallPos)) {
            Log.e("collision", "here");
            Pair<Double, Double> velocity = calculateVelocityAfterHit();
            double distanceFactor = (ballRadius + strikerRadius) / findDistance(curBallPos, curStrikerPos);
            newState = new State(velocity
                    , new Pair<>((1 - distanceFactor) * curStrikerPos.first + distanceFactor * curBallPos.first
                        , (1 - distanceFactor) * curStrikerPos.second + distanceFactor * curBallPos.second));
            prevBallState = currentBallState;
            currentBallState = newState;
//            prevBallState = currentBallState;
//            currentBallState = checkBallCollision();
        }
    }

    public boolean isHitToStriker(Pair<Double, Double> strikerPos, Pair<Double, Double> ballPos) {
//        Log.e("loc", strikerPos.toString());
//        Log.e("loc", ballPos.toString());
        return (strikerRadius + ballRadius) > findDistance(strikerPos, ballPos);
    }
    
    public State checkBallCollision() {
        State newState;
        Vector distanceVector = new Vector(currentBallState.getPosition());
        distanceVector.subtract(new Vector(currentPlayerStrikerState.getPosition()));
        Double distance = distanceVector.value() - ((ballRadius + strikerRadius));
        if (this.touched) {
            Pair<Double, Double> velocity = distanceVector.getUnit().getScalarMultiply(distance).data;
            setPlayerStrikerPosition(moveWithSteadyVelocity(dt, velocity));
            newState = new State(distanceVector.getUnit().getScalarMultiply((-distance) * 0.5f).getAdd(new Vector(currentBallState.getVelocity())).data, moveWithSteadyVelocity(dt, velocity));
        } else {
            this.touched = true;
            Vector vn1 = new Vector(distanceVector);
            vn1.setUnit();
            Vector vt1 = vn1.getNormal();
            Vector strikerVelocity = new Vector(currentPlayerStrikerState.getVelocity());
            Vector ballVelocity = new Vector(currentBallState.getVelocity());
            Double v1n = strikerVelocity.dotProduct(vn1);
            Double v1t = strikerVelocity.dotProduct(vn1);
            Double v2n = ballVelocity.dotProduct(vn1);
            Double v2t = ballVelocity.dotProduct(vt1);
            Double v1n_a = (((strikerRadius - ballRadius) * v1n) + (ballRadius) * v2n) / (strikerRadius + ballRadius);
            Vector vn2 = vn1.getScalarMultiply((((ballRadius - strikerRadius) * v2n) + (strikerRadius) * v1n) / (strikerRadius + ballRadius));
            Vector vt2 = vt1.getScalarMultiply(v2t);
            vn1.scalarMultiply(v1n_a);
            vt1.scalarMultiply(v1t);
            Vector ballNewVelocity = vn2.getAdd(vt2);//.getScalarMultiply(1.2d);
            if (ballNewVelocity.value() > 2)
                ballNewVelocity.getUnit().getScalarMultiply(2d);
            newState = new State(ballNewVelocity.data, moveWithSteadyVelocity(dt, ballNewVelocity.data));
        }
        return newState;
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
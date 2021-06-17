package com.example.airhockey.utils;

import android.util.Log;

import com.example.airhockey.models.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.airhockey.models.ProtocolConstants.COLLISION_ACK;
import static com.example.airhockey.models.ProtocolConstants.COLLISION_MSG;
import static com.example.airhockey.models.ProtocolConstants.DOUBLE_PATTERN;
import static com.example.airhockey.models.ProtocolConstants.END_MSG;
import static com.example.airhockey.models.ProtocolConstants.GOAL_ACK;
import static com.example.airhockey.models.ProtocolConstants.GOAL_SCORED;
import static com.example.airhockey.models.ProtocolConstants.POSITION_MSG;
import static com.example.airhockey.models.ProtocolConstants.SEP;

public class ProtocolUtils {

    public enum MessageTypes {
        POSITION_REPORT,
        BALL_COLLISION_REPORT,
        BALL_COLLISION_ACK,
        GOAL_SCORED_REPORT,
        GOAL_SCORED_ACK,
        UNKNOWN
    }

    public static byte[] sendStrikerPosition(Pair<Double,Double> position){
        return String.format(Locale.US, POSITION_MSG + "%f" + SEP + "%f" + END_MSG, position.first, position.second).getBytes();
    }

    public static byte[] sendBallCollision(Pair<Double,Double> position, Pair<Double,Double> velocity){
        return String.format(Locale.US, COLLISION_MSG + "%f" + SEP + "%f" + SEP + "%f" + SEP + "%f" + END_MSG, position.first, position.second, velocity.first, velocity.second).getBytes();
    }

    public static byte[] sendBallCollisionAck(){
        return (COLLISION_ACK + "" + END_MSG).getBytes();
    }

    public static byte[] sendGoalScoredAck(float goal){
        return (GOAL_ACK + "" + goal + END_MSG).getBytes();
    }

    public static byte[] sendGoalScored(float goal){
        return (GOAL_SCORED + "" + goal + END_MSG).getBytes();
    }

    public static MessageTypes getTypeOfMessage(InputStream stream){
        try {
            switch ((char) stream.read()) {
                case POSITION_MSG:
                    return MessageTypes.POSITION_REPORT;
                case COLLISION_MSG:
                    return MessageTypes.BALL_COLLISION_REPORT;
                case COLLISION_ACK:
                    return MessageTypes.BALL_COLLISION_ACK;
                case GOAL_SCORED:
                    return MessageTypes.GOAL_SCORED_REPORT;
                case GOAL_ACK:
                    return MessageTypes.GOAL_SCORED_ACK;
                default:
                    return MessageTypes.UNKNOWN;
            }
        }
        catch (Exception e){
            return MessageTypes.UNKNOWN;
        }
    }

    static private String getString(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuffer sb = new StringBuffer();
        String str;
        while((str = reader.readLine()) != null)
            sb.append(str);
        return sb.toString().trim();
    }

    static public Pair<Double,Double> receivePositionMessage(InputStream stream) throws Exception {
        String message = getString(stream);
        Matcher matcher = Pattern.compile(DOUBLE_PATTERN).matcher(message);
        Log.e("msg", message);
        Double[] inputs = new Double[2];
        for (int i = 0; i < 2; i++) {
            if (!matcher.find()) throw new Exception("corrupted message");
            inputs[i] = Double.parseDouble(matcher.group());
        }
        return new Pair<>(inputs[0], inputs[1]);
    }

    static public Pair<Pair<Double,Double>,Pair<Double,Double>> receiveBallCollisionMessage(InputStream stream) throws Exception {
        String message = getString(stream);
        Matcher matcher = Pattern.compile(DOUBLE_PATTERN).matcher(message);
        Log.e("msg", message);
        Double[] inputs = new Double[4];
        for (int i = 0; i < 4; i++) {
            if (!matcher.find()) throw new Exception("corrupted message");
            inputs[i] = Double.parseDouble(matcher.group());
        }
        return new Pair<>(new Pair<>(inputs[0], inputs[1]),new Pair<>(inputs[2], inputs[3]));
    }

    static public int receiveGoalScoredMessage(InputStream stream) throws Exception {
        String message = getString(stream);
        Matcher matcher = Pattern.compile(DOUBLE_PATTERN).matcher(message);
        Log.e("msg", message);
        Double[] inputs = new Double[1];
        for (int i = 0; i < 1; i++) {
            if (!matcher.find()) throw new Exception("corrupted message");
            inputs[i] = Double.parseDouble(matcher.group());
        }
        return inputs[0].intValue();
    }

    static public int receiveGoalScoredAckMessage(InputStream stream) throws Exception {
        String message = getString(stream);
        Matcher matcher = Pattern.compile(DOUBLE_PATTERN).matcher(message);
        Log.e("msg", message);
        Double[] inputs = new Double[1];
        for (int i = 0; i < 1; i++) {
            if (!matcher.find()) throw new Exception("corrupted message");
            inputs[i] = Double.parseDouble(matcher.group());
        }
        return inputs[0].intValue();
    }

}

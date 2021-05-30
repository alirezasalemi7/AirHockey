package com.example.airhockey.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.io.StringBufferInputStream;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProtocolUtils {

    static char END_MSG = '@';
    static char SEP = '#';
    static char POSITION_REPORT = 'P';

    public enum MessageTypes {
        POSITION_REPORT,
        UNKNOWN
    }

    public static byte[] sendStrikerPosition(SerializablePair<Double,Double> position){
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(POSITION_REPORT);
        messageBuilder.append(position.first);
        messageBuilder.append(SEP);
        messageBuilder.append(position.second);
        messageBuilder.append(END_MSG);
        return messageBuilder.toString().getBytes();
    }

    public static MessageTypes getTypeOfMessage(InputStream stream){
        try {
            char type = (char) stream.read();
            if (type == POSITION_REPORT){
                return MessageTypes.POSITION_REPORT;
            }
        }
        catch (Exception e){
            return MessageTypes.UNKNOWN;
        }
        return MessageTypes.UNKNOWN;
    }

    static private String getString(InputStream stream) throws IOException {
        InputStreamReader isReader = new InputStreamReader(stream);
        //Creating a BufferedReader object
        BufferedReader reader = new BufferedReader(isReader);
        StringBuffer sb = new StringBuffer();
        String str;
        while((str = reader.readLine())!= null){
            sb.append(str);
        }
        return sb.toString().trim();
    }

    static public SerializablePair<Double,Double> receivePositionMessage(InputStream stream) throws IOException,Exception{
        String message = getString(stream);
        String regex ="[-+]?[0-9]*\\.[0-9]+";
        Matcher matcher = Pattern.compile(regex).matcher(message);
        SerializablePair<Double,Double> pair = new SerializablePair<Double, Double>(0d, 0d);
        Log.e("msg", message);
        if (!matcher.find()){
            throw new Exception("corrupted message");
        }
        pair.first = Double.parseDouble(matcher.group());
        if (!matcher.find()){
            throw new Exception("corrupted message");
        }
        pair.second = Double.parseDouble(matcher.group());
        return pair;
    }

}

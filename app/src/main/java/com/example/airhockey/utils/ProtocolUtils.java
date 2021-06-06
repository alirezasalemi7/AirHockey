package com.example.airhockey.utils;

import android.util.Log;

import com.example.airhockey.models.SerializablePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.airhockey.models.ProtocolConstants.DOUBLE_PATTERN;
import static com.example.airhockey.models.ProtocolConstants.END_MSG;
import static com.example.airhockey.models.ProtocolConstants.POSITION_MSG;
import static com.example.airhockey.models.ProtocolConstants.SEP;

public class ProtocolUtils {

    public enum MessageTypes {
        POSITION_REPORT,
        UNKNOWN
    }

    public static byte[] sendStrikerPosition(SerializablePair<Double,Double> position){
        return String.format(Locale.US, POSITION_MSG + "%f" + SEP + "%f" + END_MSG, position.first, position.second).getBytes();
    }

    public static MessageTypes getTypeOfMessage(InputStream stream){
        try {
            switch ((char) stream.read()) {
                case POSITION_MSG:
                    return MessageTypes.POSITION_REPORT;
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

    static public SerializablePair<Double,Double> receivePositionMessage(InputStream stream) throws Exception {
        String message = getString(stream);
        Matcher matcher = Pattern.compile(DOUBLE_PATTERN).matcher(message);
        Log.e("msg", message);
        Double[] inputs = new Double[2];
        for (int i = 0; i < 2; i++) {
            if (!matcher.find()) throw new Exception("corrupted message");
            inputs[i] = Double.parseDouble(matcher.group());
        }
        return new SerializablePair<>(inputs[0], inputs[1]);
    }

}

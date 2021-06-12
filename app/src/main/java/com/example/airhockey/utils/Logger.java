package com.example.airhockey.utils;

import android.os.Environment;

import com.example.airhockey.models.SerializablePair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    private static Logger instance;
    private final static String FILE_NAME = "logs.log";
    private final static String ADDRESS = "AirHockey";
    private File logFile;
    private FileWriter writer;

    private Logger() throws IOException {
        File root = new File(Environment.getExternalStorageDirectory(), ADDRESS);
        if (!root.exists()) {
            root.mkdirs();
        }
        logFile = new File(root, FILE_NAME);
        writer = new FileWriter(logFile);
    }

    public static Logger getInstance() throws IOException {
        if (instance == null){
            instance = new Logger();
        }
        return instance;
    }

    public void log(String type, String message) throws IOException{
        writer.write(type + " : " + message + "\n");
    }

    public void logBallPosition(int frame, SerializablePair<Integer,Integer> location) throws IOException {
        String message = "frame = " + frame + " posX = " + location.first + " posY = " + location.second;
        log("BallPosReport", message);
    }
}

package com.example.airhockey.utils;


import android.util.Pair;

public class LocationConverter {
    private int height;
    private int width;
//  (Width, Height) -> (1, height / width)
    public LocationConverter(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public Pair<Integer, Integer> convertToRealPoint(Pair<Double, Double> fractionalPoint) {
        return new Pair<>(fractionalPoint.first.intValue() * height, fractionalPoint.second.intValue() * width);
    }

    public Pair<Double, Double> convertToFractionalPoint(Pair<Integer, Integer> realPoint) {
        return new Pair<>(realPoint.first.doubleValue() / height, realPoint.second.doubleValue() / width);
    }

    public Pair<Integer, Integer> reflect(Pair<Integer, Integer> inputPoint) {
        return new Pair<>(height - inputPoint.first, inputPoint.second);
    }


}

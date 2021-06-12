package com.example.airhockey.utils;


import com.example.airhockey.models.SerializablePair;

public class LocationConverter {
    private int height;
    private int width;
//  (Width, Height) -> (1, height / width)
    public LocationConverter(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public SerializablePair<Integer, Integer> convertToRealPoint(SerializablePair<Double, Double> fractionalPoint) {
        return new SerializablePair<>((int) (fractionalPoint.first * width), (int) (fractionalPoint.second * height));
    }

    public SerializablePair<Double, Double> convertToFractionalPoint(SerializablePair<Integer, Integer> realPoint) {
        return new SerializablePair<>(realPoint.first.doubleValue() / width, realPoint.second.doubleValue() / height);
    }

    public SerializablePair<Integer, Integer> reflectPosition(SerializablePair<Integer, Integer> inputPoint) {
        return new SerializablePair<>(width - inputPoint.first, height - inputPoint.second);
    }

    public SerializablePair<Integer, Integer> reflectSpeed(SerializablePair<Integer, Integer> inputPoint) {
        return new SerializablePair<>(-inputPoint.first, -inputPoint.second);
    }

}

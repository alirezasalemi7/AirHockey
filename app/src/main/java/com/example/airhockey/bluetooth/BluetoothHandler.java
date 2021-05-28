package com.example.airhockey.bluetooth;

public class BluetoothHandler {
    private static BluetoothHandler _instance;

    private BluetoothHandler() {}

    public BluetoothHandler getInstance() {
        if (_instance == null)
            _instance = new BluetoothHandler();
        return _instance;
    }
}

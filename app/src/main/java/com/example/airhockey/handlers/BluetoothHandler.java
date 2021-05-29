package com.example.airhockey.handlers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

public class BluetoothHandler {
    private static BluetoothHandler _instance;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket transferSocket;

    private final String APP_NAME = "AirHockey";
    private static final UUID SERVICE_UUID = UUID.fromString("0e628292-c018-11eb-8529-0242ac130003");

    private BluetoothHandler() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothHandler getInstance() {
        if (_instance == null)
            _instance = new BluetoothHandler();
        return _instance;
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket temp = null;
            try {
                temp = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
            } catch (IOException e) {
//                TODO: send message to user
            }
            bluetoothSocket = temp;
        }

        @Override
        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                bluetoothSocket.connect();
            } catch (IOException e) {
                cancel();
                return;
            }
            transferSocket = bluetoothSocket;
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
//                TODO: send proper message to user
            }
        }
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, SERVICE_UUID);
            } catch (IOException e) {
//                TODO: snd message
            }
        }

        @Override
        public void run() {
            transferSocket = null;
            while (true) {
                try {
                    transferSocket = serverSocket.accept();
                } catch (IOException e) {
//                    TODO: Send error message
                    break;
                }
                if (transferSocket != null) {
                    cancel();
                    break;
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
//                        TODO: send error message
            }
        }
    }
}

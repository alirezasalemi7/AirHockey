package com.example.airhockey.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.airhockey.R;
import com.example.airhockey.handlers.BluetoothHandler;

import java.util.ArrayList;
import java.util.Set;

public class ConnectionActivity extends AppCompatActivity {

    private final int REQUEST_ENABLE_BT = 0;
    private final int REQUEST_SET_DISCOVERABLE = 1;

    ImageView clientBtn;
    ImageView serverBtn;
    private BluetoothAdapter bluetoothAdapter;
//    TODO: the next two sets need to be showed in a list of items (to be selected by user)
//      After setting that, you need to call cancelDiscovery() in onClickListener and save data
    private Set<BluetoothDevice> foundedDevices;
    private Set<BluetoothDevice> pairedDevices;
//    private BluetoothHandler bluetoothHandler;
    ListView lv;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND: {
                    foundedDevices.add(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                    break;
                }
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
//                    TODO: finish checking and send message if needed
                    break;
                }
            }
        }
    };

    public void startScan() {
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.startDiscovery();
    }


    public void setView() {
        setContentView(R.layout.activity_connection);
        clientBtn = findViewById(R.id.connection_client_btn);
        serverBtn = findViewById(R.id.connection_server_btn);
        Animation animation = new ScaleAnimation(1f, 1.05f, 1f, 1.05f);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        serverBtn.startAnimation(animation);
        clientBtn.startAnimation(animation);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        clientBtn.setOnClickListener(v -> {

        });
        serverBtn.setOnClickListener(v -> {

        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView();
//        bluetoothHandler = BluetoothHandler.getInstance();
        if (!bluetoothAdapter.isEnabled())
            turnOnBluetooth();
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        pairedDevices = bluetoothAdapter.getBondedDevices();
    }

    private void turnOnBluetooth(){
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, REQUEST_ENABLE_BT);  
    }

    private void makeDeviceDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(discoverableIntent, REQUEST_SET_DISCOVERABLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (resultCode) {
//            case REQUEST_ENABLE_BT:
//                if (resultCode == Activity.RESULT_OK) {
//                      TODO: check values and setup the game
//                } else {
//                    TODO: Send a message and tell that bluetooth must be enabled and rerun the action
//                }
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }
}
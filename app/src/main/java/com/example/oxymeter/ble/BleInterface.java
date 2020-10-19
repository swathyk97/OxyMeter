package com.example.oxymeter.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;


import com.example.oxymeter.dialog.DeviceListAdapter;
import com.example.oxymeter.dialog.SearchDevicesDialog;

import org.w3c.dom.Text;

import java.util.ArrayList;

public interface BleInterface {

    interface StateListener {
        void onConnected();

        void onDisconnected();

        void onFoundDevice(BluetoothDevice device);

        void onReceiveData(byte[] dat);

        void onServicesDiscovered();

        void onScanStop();


    }

    interface Connection {
        void statusCheck(LocationManager locationManager, BleController mBleControl, DeviceListAdapter mBtDevicesAdapter, SearchDevicesDialog mSearchDialog, ArrayList<BluetoothDevice> mBtDevices);

        void dexter();
    }
    interface Message{

        void message(String message);

        void searchButton(String message);

        void setText(String message, long time, int spo2, int pulse);

        void add(int amp);

        void intent();

        void disconnect();

        void connect();

    }

}

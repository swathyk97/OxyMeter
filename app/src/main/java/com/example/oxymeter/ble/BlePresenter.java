package com.example.oxymeter.ble;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;

import com.example.oxymeter.MainActivity;
import com.example.oxymeter.dialog.DeviceListAdapter;
import com.example.oxymeter.dialog.SearchDevicesDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

public class BlePresenter implements BleInterface.Connection {
    private final Context mContext;
    BleInterface.Message message;


    public BlePresenter( Context mContext,BleInterface.Message message) {
        this.mContext = mContext;
        this.message=message;

    }



    @Override
    public void dexter() {
        Dexter.withContext(mContext)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }



    @Override
    public void statusCheck(LocationManager locationManager, BleController mBleControl, DeviceListAdapter mBtDevicesAdapter, SearchDevicesDialog mSearchDialog, ArrayList<BluetoothDevice> mBtDevices) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                                message.intent();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();

        } else {
            if (!mBleControl.isConnected()) {
                mBleControl.scanLeDevice(true);
                mSearchDialog.show();
                mBtDevices.clear();
                mBtDevicesAdapter.notifyDataSetChanged();
            } else {
                mBleControl.disconnect();
            }
        }
    }


}



package com.example.oxymeter;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.location.LocationManager;
import android.widget.Toast;


import com.example.oxymeter.ble.BleController;
import com.example.oxymeter.ble.BleInterface;
import com.example.oxymeter.data.DataParser;
import com.example.oxymeter.data.onPackageReceivedListener;
import com.example.oxymeter.dialog.DeviceListAdapter;
import com.example.oxymeter.dialog.SearchDevicesDialog;



import java.util.ArrayList;


public class Common implements BleInterface.StateListener {
    private final Context mContext;
    public DataParser mDataParser;
    public BleController mBleControl;
    public SearchDevicesDialog mSearchDialog;
    public DeviceListAdapter mBtDevicesAdapter;
    public ArrayList<BluetoothDevice> mBtDevices = new ArrayList<>();
    public LocationManager manager;
    BleInterface.Message alert;


    public Common(Context mContext, BleInterface.Message alert) {
        this.mContext = mContext;
        this.alert=alert;
    }

    public void ble() {
        manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mBtDevicesAdapter = new DeviceListAdapter(mContext, mBtDevices);
        mBleControl = BleController.getDefaultBleController(this);
        mBleControl.enableBtAdapter();
    }

    public void dialog() {
        mDataParser = new DataParser(new onPackageReceivedListener() {
            @Override
            public void onOxiParamsChanged(final DataParser.OxiParams params) {
                alert.setText("Interval : %d ms  SpO2 : %d   Pulse Rate : %d", System.currentTimeMillis(), params.getSpo2(), params.getPulseRate());
            }

            @Override
            public void onPlethWaveReceived(final int amp) {
                alert.add(amp);
            }
        });
        mDataParser.start();
    }
    public void search(Context mContext) {
        mSearchDialog = new SearchDevicesDialog(mContext, mBtDevicesAdapter) {
            @Override
            public void onSearchButtonClicked() {
                mBtDevices.clear();
                mBtDevicesAdapter.notifyDataSetChanged();
                mBleControl.scanLeDevice(true);
            }

            @Override
            public void onClickDeviceItem(int pos) {
                BluetoothDevice device = mBtDevices.get(pos);
                alert.message("Name : " + device.getName() + "     " + "Mac : " + device.getAddress());
                mBleControl.connect(device);
                dismiss();
            }
        };


    }


    @Override
    public void onConnected() {
       alert.searchButton("Disconnect");
       alert.connect();
       Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected() {
        alert.disconnect();
        Toast.makeText(mContext, "Disconnected", Toast.LENGTH_SHORT).show();
        alert.searchButton("Search");
    }

    @Override
    public void onFoundDevice(BluetoothDevice device) {
        if (!mBtDevices.contains(device)) {
            mBtDevices.add(device);
            mBtDevicesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onReceiveData(byte[] dat) {

        mDataParser.add(dat);
    }

    @Override
    public void onServicesDiscovered() {

    }

    @Override
    public void onScanStop() {
        mSearchDialog.stopSearch();
    }


}



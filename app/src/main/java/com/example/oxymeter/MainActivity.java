package com.example.oxymeter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.oxymeter.ble.BleController;
import com.example.oxymeter.data.DataParser;
import com.example.oxymeter.dialog.DeviceListAdapter;
import com.example.oxymeter.dialog.SearchDevicesDialog;
import com.example.oxymeter.views.WaveformView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.String.format;

public class MainActivity extends Activity implements BleController.StateListener {

    private long time = 0;
    private final static String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.btnSearch)
    Button btnSearch;
    @BindView(R.id.tvStatus)
    TextView tvStatus;
    @BindView(R.id.tvParams)
    TextView tvResult;
    @BindView(R.id.wfvPleth)
    WaveformView wfvPleth;
//    @BindView(R.id.etNewBtName)
//    EditText etNewBtName;
//    @BindView(R.id.llChangeName)
//    LinearLayout llChangeName;

    private DataParser mDataParser;
    private BleController mBleControl;

    private SearchDevicesDialog mSearchDialog;
    private DeviceListAdapter mBtDevicesAdapter;
    private ArrayList<BluetoothDevice> mBtDevices = new ArrayList<>();
    private PermissionToken permissionToken = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Dexter.withContext(this)
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
        mDataParser = new DataParser(new DataParser.onPackageReceivedListener() {
            @Override
            public void onOxiParamsChanged(final DataParser.OxiParams params) {

                runOnUiThread(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        tvResult.setText(format("Interval : %d ms  SpO2 : %d   Pulse Rate : %d", System.currentTimeMillis() - time, params.getSpo2(), params.getPulseRate()));
                    }
                });
                time = System.currentTimeMillis();
            }

            @Override
            public void onPlethWaveReceived(final int amp) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wfvPleth.addAmp(amp);
                    }
                });
            }
        });
        mDataParser.start();

        mBleControl = BleController.getDefaultBleController(this);
        mBleControl.enableBtAdapter();
        mBleControl.bindService(this);

        mBtDevicesAdapter = new DeviceListAdapter(this, mBtDevices);
        mSearchDialog = new SearchDevicesDialog(this, mBtDevicesAdapter) {
            @Override
            public void onSearchButtonClicked() {
                mBtDevices.clear();
                mBtDevicesAdapter.notifyDataSetChanged();
                mBleControl.scanLeDevice(true);
            }

            @Override
            public void onClickDeviceItem(int pos) {
                BluetoothDevice device = mBtDevices.get(pos);
                tvStatus.setText("Name : " + device.getName() + "     " + "Mac : " + device.getAddress());
                mBleControl.connect(device);
                dismiss();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBleControl.registerBtReceiver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBleControl.unregisterBtReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDataParser.stop();
        mBleControl.unbindService(this);
        System.exit(0);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSearch:
                if (!mBleControl.isConnected()) {
                    mBleControl.scanLeDevice(true);
                    mSearchDialog.show();
                    mBtDevices.clear();
                    mBtDevicesAdapter.notifyDataSetChanged();
                } else {
                    mBleControl.disconnect();
                }
                break;
//            case R.id.tvGetSource:
//                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(Const.GITHUB_SITE)));
//                break;
//            case R.id.btnChangeName:
//                mBleControl.changeBtName(etNewBtName.getText().toString());
//                break;
        }
    }

    @Override
    public void onFoundDevice(final BluetoothDevice device) {
        if (!mBtDevices.contains(device)) {
            mBtDevices.add(device);
            mBtDevicesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onConnected() {
        btnSearch.setText("Disconnect");
        tvStatus.setVisibility(View.VISIBLE);
        tvResult.setVisibility(View.VISIBLE);
        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected() {
        tvStatus.setVisibility(View.GONE);
        tvResult.setVisibility(View.GONE);
        wfvPleth.reset();
        Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
        btnSearch.setText("Search");
        //llChangeName.setVisibility(View.GONE);
    }

    @Override
    public void onReceiveData(byte[] dat) {
        mDataParser.add(dat);
    }

    @Override
    public void onServicesDiscovered() {
        //llChangeName.setVisibility(mBleControl.isChangeNameAvailable() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onScanStop() {
        mSearchDialog.stopSearch();
    }
}

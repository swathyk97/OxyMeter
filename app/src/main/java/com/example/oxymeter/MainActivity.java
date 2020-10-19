package com.example.oxymeter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.example.oxymeter.ble.BleInterface;
import com.example.oxymeter.ble.BlePresenter;
import com.example.oxymeter.views.WaveformView;



import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.String.format;

public class MainActivity extends Activity implements BleInterface.Message {

    private long tTime = 0;
    private final static String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.btnSearch)
    Button btnSearch;
    @BindView(R.id.tvStatus)
    TextView tvStatus;
    @BindView(R.id.tvParams)
    TextView tvResult;
    @BindView(R.id.wfvPleth)
    WaveformView wfvPleth;

    BleInterface.Connection connection;
    private Common common;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        connection = new BlePresenter( this,this);
        connection.dexter();
        common = new Common(getApplicationContext(),this);
        common.ble();
        common.mBleControl.bindService(this);
        common.dialog();
        common.search(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        common.mBleControl.registerBtReceiver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        common.mBleControl.unregisterBtReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        common.mDataParser.stop();
        common.mBleControl.unbindService(this);
        System.exit(0);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSearch:
                connection.statusCheck(common.manager, common.mBleControl, common.mBtDevicesAdapter, common.mSearchDialog, common.mBtDevices);
        }
    }


    @Override
    public void message(String message) {
        tvStatus.setText(message);
    }

    @Override
    public void searchButton(String message) {
        btnSearch.setText(message);
    }

    @Override
    public void setText(final String message, final long time, final int spo2, final int pulse) {
        runOnUiThread(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                tvResult.setText(format(message, time - tTime, spo2, pulse));
            }

        });
        tTime = System.currentTimeMillis();
    }

    @Override
    public void add(final int amp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                wfvPleth.addAmp(amp);
            }
        });

    }

    @Override
    public void intent() {
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    @Override
    public void disconnect() {
          tvStatus.setVisibility(View.GONE);
          tvResult.setVisibility(View.GONE);
          wfvPleth.reset();
    }

    @Override
    public void connect() {
        tvStatus.setVisibility(View.VISIBLE);
        tvResult.setVisibility(View.VISIBLE);
    }

}

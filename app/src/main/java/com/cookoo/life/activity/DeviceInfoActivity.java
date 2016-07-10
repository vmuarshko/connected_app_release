package com.cookoo.life.activity;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.cookoo.life.R;
import com.cookoo.life.domain.DeviceInfo;
import com.cookoo.life.service.BluetoothService;
import com.cookoo.life.utilities.Conversion;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created By: travis.roberson
 * Date: 2/4/14
 * Time: 9:39 AM
 */
public class DeviceInfoActivity extends Activity {

    public final static String TAG = DeviceInfoActivity.class.getSimpleName();
    private TextView deviceName;
    private TextView deviceAddress;
    private TextView manufacturer;
    private TextView softwareRevision;
    private TextView hardwareRevision;
    private TextView firmwareRevision;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_info_activity);

        deviceName = (TextView) findViewById(R.id.deviceName);
        deviceName.setText("");
        deviceAddress = (TextView) findViewById(R.id.deviceAddress);
        deviceAddress.setText("");
        manufacturer = (TextView) findViewById(R.id.manufacturerValue);
        manufacturer.setText("");
        softwareRevision = (TextView) findViewById(R.id.softwareRevisionValue);
        softwareRevision.setText("");
        hardwareRevision = (TextView) findViewById(R.id.hardwareRevisionValue);
        hardwareRevision.setText("");
        firmwareRevision = (TextView) findViewById(R.id.firmwareRevisionValue);
        firmwareRevision.setText("");
        populateDeviceInfo();

    }

    private void populateDeviceInfo() {
        Log.d(TAG, "populateDeviceInfo");
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        deviceName.setText(deviceInfo.getDeviceName());
        deviceAddress.setText(deviceInfo.getDeviceAddress());
        manufacturer.setText(deviceInfo.getDeviceManufacturer());
        softwareRevision.setText(deviceInfo.getSoftwareRevision());
        hardwareRevision.setText(deviceInfo.getHardwareRevision());
        firmwareRevision.setText(deviceInfo.getFirmwareRevision());
    }

}

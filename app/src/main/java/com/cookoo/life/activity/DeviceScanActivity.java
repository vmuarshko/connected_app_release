package com.cookoo.life.activity;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cookoo.life.service.BluetoothService;
import com.cookoo.life.R;
import com.cookoo.life.btdevice.BleDeviceInfo;


public class DeviceScanActivity extends ListActivity {
    private static final String TAG = DeviceScanActivity.class.getSimpleName();
    private DeviceListAdapter deviceListAdapter = null;
    private boolean mScanning;
    private Handler mHandler;
    private final String COOKOO = "cookoo";
    private final String COGITO = "cogito";
    public static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private BluetoothService bluetoothService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_devices);
        }
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        PackageManager packageManager = getPackageManager();
        if (packageManager == null || !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        bluetoothService = BluetoothService.getInstance();
        registerReceiver(mGattUpdateReceiver, BluetoothService.getGattUpdateIntentFilter());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG,"onDestroy()");
        unregisterReceiver(mGattUpdateReceiver);
    }


    final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, String.format("received intent: %s", intent.getAction()));
            deviceListAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        MainActivity.showSplash = false;
        super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                deviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        MainActivity.atmpConn = false;
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (bluetoothService == null) {
            Log.e(TAG,"Cannot continue without BluetoothService");
            finish();
        }
        if (!bluetoothService.isBluetoothEnabled()) {
            Log.d(TAG, "preparing to enable bluetooth adapter");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Initializes list view adapter.
        List<BleDeviceInfo> deviceList = new ArrayList<BleDeviceInfo>();
        if (deviceListAdapter == null) {
            deviceListAdapter = new DeviceListAdapter(deviceList);
        }
        setListAdapter(deviceListAdapter);
        for(BluetoothDevice bl : bluetoothService.getBondedDevices()){
            if (bl == null) {
                Log.e(TAG,"null device info");
                continue;
            }
            BleDeviceInfo bi = new BleDeviceInfo(bl, 0);
            setDeviceName(bi);
            if(deviceListAdapter.isValidDevice(bi.getName())) {
                deviceList.add(bi);
            }
        }
        deviceListAdapter.notifyDataSetChanged();
        scanLeDevice(true);
    }
    private void setDeviceName(BleDeviceInfo deviceInfo){
        try {
            BluetoothDevice device = deviceInfo.getBluetoothDevice();
            if (device == null) {
                deviceInfo.setName("");
            } else {
                String name = device.getName();
                deviceInfo.setName(name != null ? name : "");
            }
        } catch (Exception ex) {
            Log.e(TAG,"Unable to get device name; is Bluetooth in an error state?",ex);
            deviceInfo.setName("");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
        scanLeDevice(false);
//        bluetoothService.cancelDeviceDiscovery();
        deviceListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG,"onListItemClick");
        final BleDeviceInfo device = deviceListAdapter.getDevice(position);
        if (device == null) {return; }
        if (mScanning){
            bluetoothService.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        bluetoothService.cancelDeviceDiscovery();
        if(isConnected(device.getBluetoothDevice())) {
            showAlreadyConnDialog(position);
        }
        else if(device.getRssi() == 0) {
            showOORDialog(position);
        }
        else {
            showPinInfoDialog(device.getBluetoothDevice(), position);
        }
    }

    private void showOORDialog(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Dialog))
                .setTitle(R.string.old_firmware_warn)
                .setCancelable(false)
                .setMessage(R.string.not_in_range)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        this.addForgetButton(alertDialogBuilder,position);
        alertDialogBuilder.create().show();
    }

    private void addForgetButton(AlertDialog.Builder alertDialogBuilder, final int position){
        alertDialogBuilder.setNegativeButton(
                R.string.forget, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deviceListAdapter.removeDevice(position);
                dialog.cancel();
            }
        });
    }

    private void showAlreadyConnDialog(final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Dialog))
                .setTitle(R.string.old_firmware_warn)
                .setCancelable(false)
                .setMessage(R.string.already_connected)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        this.addForgetButton(alertDialogBuilder,position);
        alertDialogBuilder.create().show();
    }


    private void showPinInfoDialog(final BluetoothDevice device, final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Dialog))
                .setTitle(R.string.old_firmware_warn)
                .setCancelable(true)
                .setMessage(R.string.old_firmware_message)
                .setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.atmpConn = true;
                        MainActivity.mConnected = false;
                        Intent intent = new Intent(BluetoothService.ACTION_PERFORM_CONNECT);
                        intent.putExtra(BluetoothService.EXTRA_DEVICE_ADDRESS, device.getAddress());
                        intent.putExtra(BluetoothService.EXTRA_CONNECTION_TYPE, BluetoothService.DIRECT_CONNECT);
                        sendBroadcast(intent);
                        onBackPressed();
                    }
                });

        if (bluetoothService.getBondedDevices().contains(device)) {
            this.addForgetButton(alertDialogBuilder,position);
        }
        alertDialogBuilder.create().show();
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothService.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothService.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            bluetoothService.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    private boolean isConnected(BluetoothDevice device){
        int connectionState = bluetoothService.getConnectionState(device, BluetoothProfile.GATT);
        return (connectionState == BluetoothProfile.STATE_CONNECTED);
    }

    private void foundDevice(BluetoothDevice device, int rssi) {
        Log.d(TAG, String.format("adding device %s", device.getAddress()));

        if (!deviceListAdapter.deviceInfoExists(device.getAddress())) {
            // New device
            BleDeviceInfo bl = new BleDeviceInfo(device, rssi);
            deviceListAdapter.addDevice(bl);
        } else {
            // Already in list, update RSSI info
            BleDeviceInfo deviceInfo = deviceListAdapter.findDeviceInfo(device);
            Log.d(TAG, "device rssi value " + rssi);
            deviceInfo.updateRssi(rssi);
        }
        for(BluetoothDevice alreadyExists : bluetoothService.getBondedDevices()){
            BleDeviceInfo bi = new BleDeviceInfo(alreadyExists, 0);
            deviceListAdapter.addDevice(bi);
        }
        deviceListAdapter.notifyDataSetChanged();
    }


    // Adapter for holding devices found through scanning.
    private class DeviceListAdapter extends BaseAdapter {
        private List<BleDeviceInfo> mDevices;
        private LayoutInflater mInflator;


        public DeviceListAdapter(List<BleDeviceInfo> devices) {
            super();
            mDevices = devices;
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }


        public void addDevice(final BleDeviceInfo device) {
            boolean add = true;
            for(BleDeviceInfo bl : mDevices) {
                if (bl.getBluetoothDevice().getAddress().equals(device.getBluetoothDevice().getAddress())) {
                    add = false;
                }
            }
            if(add) {
                setDeviceName(device);
                if (isValidDevice(device.getName())){mDevices.add(device);}
            }
        }

        public BleDeviceInfo getDevice(int position) {
            return mDevices.get(position);
        }

        public void clear() {
            mDevices.clear();
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.pairingStatus = (TextView) view.findViewById(R.id.pairing_status);
                viewHolder.connectionStatus =  (TextView) view.findViewById(R.id.connection_state);
                viewHolder.rssiValue = (TextView) view.findViewById(R.id.rssi_value);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BleDeviceInfo device = mDevices.get(i);
            viewHolder.deviceName.setText(device.getName());
            viewHolder.deviceAddress.setText(device.getBluetoothDevice().getAddress());

            if(isBonded(device.getBluetoothDevice())) {
                viewHolder.pairingStatus.setText(R.string.paired);
            }
            else {
                viewHolder.pairingStatus.setText(R.string.not_paired);
            }

            if(isConnected(device.getBluetoothDevice())) {
                viewHolder.connectionStatus.setText(R.string.connected);
                String inRangeText = String.format(" %s %s",getString(R.string.device_in_range),getString(R.string.yes));
                viewHolder.rssiValue.setText(inRangeText);
            } else {
                viewHolder.connectionStatus.setText(R.string.not_connected);
                String inRangeText = String.format(" %s %s",getString(R.string.device_in_range),(device.getRssi() < 0? getString(R.string.yes): getString(R.string.no)));
                viewHolder.rssiValue.setText(inRangeText);
            }
            return view;
        }


        private boolean deviceInfoExists(String address) {
            for (BleDeviceInfo mDevice : mDevices) {
                if (mDevice.getBluetoothDevice().getAddress().equals(address)) {
                    return true;
                }
            }
            return false;
        }

        private BleDeviceInfo findDeviceInfo(BluetoothDevice device) {
            for (BleDeviceInfo mDevice : mDevices) {
                if (mDevice.getBluetoothDevice().getAddress().equals(device.getAddress())) {
                    return mDevice;
                }
            }
            return null;
        }

        private boolean isBonded(BluetoothDevice device){
            return device.getBondState() == BluetoothDevice.BOND_BONDED;
        }

        private boolean isValidDevice(String name){
            return name != null && (name.toLowerCase().contains(COOKOO) || name.toLowerCase().contains(COGITO));
        }

        public void removeDevice(int position) {
            // remove bond
            final BleDeviceInfo bleDeviceInfo = this.getDevice(position);
            BluetoothDevice btDevice = bleDeviceInfo.getBluetoothDevice();
            try {
                if (isConnected(btDevice)) {
                    bluetoothService.disconnect(true);
                    bluetoothService.clearLastConnectedDevice();
                }
                boolean removed = this.removeBond(btDevice);
                if (removed) {
                    Log.d(TAG,String.format("Forgot device %s",btDevice.toString()));
                    this.mDevices.remove(position);
                    this.notifyDataSetChanged();
                } else {
                    throw new Exception("unable to forget device");
                }

            } catch (Exception e){
                Log.e(TAG,"failure to forget BleDevice",e);
            }
        }
        private boolean removeBond(BluetoothDevice btDevice)
                throws Exception
        {
            Class btClass = BluetoothDevice.class; // Class.forName("android.bluetooth.BluetoothDevice");
            Method removeBondMethod = btClass.getMethod("removeBond");
            Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
            return returnValue.booleanValue();
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    Log.d(TAG, "onLEScan callback");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            foundDevice(device,rssi);
                        }
                    });
                }
            };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView pairingStatus;
        TextView connectionStatus;
        TextView rssiValue;
    }



}
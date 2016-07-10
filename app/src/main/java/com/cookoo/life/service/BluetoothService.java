package com.cookoo.life.service;

import android.text.format.Time;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.*;

import com.cookoo.life.R;
import com.cookoo.life.activity.DevicePreferenceActivity;
import com.cookoo.life.activity.MainActivity;
import com.cookoo.life.btdevice.BTserviceAttributes;
import com.cookoo.life.btdevice.BleAction;
import com.cookoo.life.btdevice.BluetoothQueue;
import com.cookoo.life.btdevice.GattErrorCodes;
import com.cookoo.life.domain.AlertCategories;
import com.cookoo.life.domain.DeviceInfo;
import com.cookoo.life.utilities.DevInfo;
import android.bluetooth.*;
import android.content.*;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.*;
import android.os.Process;
import android.app.Service;
import android.util.Log;


public class BluetoothService extends Service {
    public static final String PREFERENCES_KEY = "com.cookoo.life";
    public static BluetoothGattCharacteristic ImmediateAlertService;
    private final static String TAG = BluetoothService.class.getSimpleName();
    private BluetoothManager mBluetoothManager;
    private static BluetoothService mThis = null;
    public static BluetoothGattService deviceInfoService;
    public static BluetoothGattCharacteristic CDPcharacteristic;
    public static BluetoothGattCharacteristic OADcharacteristic;
    public static BluetoothGattCharacteristic CDP_heartbeat_characteristic;
    private BluetoothAdapter mBluetoothAdapter;
    private volatile String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    public static volatile boolean canToggleBTOn = false;
    private volatile boolean canTriggerReconnection = false;

    public final static String ACTION_GATT_CONNECTED = "com.connected.btdevice.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.connected.btdevice.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.connected.btdevice.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.connected.btdevice.ACTION_DATA_AVAILABLE";
    public final static String ACTION_ALERT_ACKNOWLEDGED = "com.connected.watch.action.ALERT_ACKNOWLEDGED";
    public final static String ACTION_DEVICE_OOR = "com.connected.btdevice.ACTION_DEVICE_OOR";
    public final static String ACTION_UPDATE_DEVICE = "com.connected.btdevice.ACTION_UPDATE_DEVICE";
    public final static String ACTION_GATT_DEVICE_NOT_FOUND = "com.connected.btdevice.DEVICE_NOT_FOUND";
    public final static String ACTION_GATT_BAD_CONNECTION = "com.connected.btdevice.ACTION_GATT_BAD_CONNECTION";
    public final static String ACTION_GATT_CONNECTING = "com.connected.btdevice.ACTION_GATT_CONNECTING";
    public final static String ACTION_PERFORM_CONNECT = "com.connected.life.PERFORM_CONNECT";
    public final static String ACTION_CYCLE_BLUETOOTH = "com.connected.life.CYCLE_BLUETOOTH";
    public final static String ACTION_CANCEL_CONNECTION = "com.connected.life.CANCEL_CONNECTION";
    public final static String ACTION_GATT_BAD_LINK_KEY = "com.connected.life.BAD_LINK";

    public final static String EXTRA_DATA = "com.connected.btdevice.EXTRA_DATA";
    public final static String EXTRA_DEVICE_ADDRESS = "extra_device_address";
    public final static String EXTRA_CONNECTION_TYPE = "extra_connection_type";

    public final static String DIRECT_CONNECT = "direct_connect";

    private BluetoothQueue bluetoothQueue = BluetoothQueue.getInstance();

    private final static String NON_VALID_CHARS = "[^\\x20-\\x7E]+";
    SharedPreferences prefs = null;
    private DumpConnectionTimer dumpConn = null;
    private final int MAX_QUEUE_THRESH = 25;
    private volatile boolean mBusy = false; // Write/read pending response
    public static volatile boolean inServiceMode = false;

    private boolean handlesBonding = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, String.format("Received start id %d: %s", startId, intent));
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        prefs = getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PERFORM_CONNECT);
        filter.addAction(ACTION_CANCEL_CONNECTION);
        filter.addAction(ACTION_UPDATE_DEVICE);

        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        filter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);

        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        registerReceiver(btServiceReceiver, filter);

        IntentFilter timeChangeFilter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(timeChangedReceiver,timeChangeFilter);


        initialize();

        handlesBonding = !(DevInfo.isHTC());

    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(btServiceReceiver);
        unregisterReceiver(timeChangedReceiver);
    }


    private final BroadcastReceiver timeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"received time broadcast");
            final String action = intent.getAction();

            if (action.equals(Intent.ACTION_TIME_CHANGED) ||
                    action.equals(Intent.ACTION_TIMEZONE_CHANGED))
            {
                Log.d(TAG, "time was changed on phone");
                setCurrentTime();
            }
        }
    };

    private final BroadcastReceiver btServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, String.format("action was %s", action));
            Log.d(TAG,"Last device connected address was " + mBluetoothDeviceAddress);
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                Log.d(TAG,"pairing request");
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.i(TAG, "acl disconnected - clearing queue"); // TODO what cleanup is necessary to allow smooth reconnect?
                bluetoothQueue.setBluetoothGatt(null);

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Log.i(TAG,"acl disconnect requested");
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.i(TAG,"captured acl connected");
            } else if(action.contains(BluetoothAdapter.ACTION_STATE_CHANGED)){
                Log.d(TAG, String.format("BluetoothAdapter state was changed"));
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,0);
                int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE,0);
                Log.d(TAG, String.format("previous state was %d and new state is %d", previousState, state));
                if(BluetoothAdapter.STATE_ON == state) {
                    Log.d(TAG, "bluetooth switched on");
                    if (mBluetoothDeviceAddress != null) {
                        Log.d(TAG, String.format("trying to reconnect to %s", mBluetoothDeviceAddress));
                        try { //wait before attempting a reconnect
                            Thread.sleep(5000);
                        }  catch (Exception ex){
                            ex.printStackTrace();
                        }
                        connect(mBluetoothDeviceAddress);
                    } else {
                        Log.d(TAG,"no previous bluetooth address known to connect to");
                    }
                }
                else if(BluetoothAdapter.STATE_OFF == state){
                    Log.d(TAG, String.format("bluetooth switched off.  can toggle bt on: %s", canToggleBTOn));
                    close();
                    broadcastUpdate(ACTION_GATT_DISCONNECTED);
                    if(canToggleBTOn){
                        Log.d(TAG, "switching bt back on; listen for action state to change to on to try reconnecting");
                        mBluetoothAdapter.enable();
                        canToggleBTOn = false;
                    }
                }
            } else if(action.contains(ACTION_PERFORM_CONNECT)) {
                Log.d(TAG, "received a connection request");
                String address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
                connect(address);
            }  else if (action.contains(ACTION_CANCEL_CONNECTION)){
                cancelConnectionAttempt();
            } else if(action.contains(ACTION_UPDATE_DEVICE)){
                Log.d(TAG, "received update device request");
                writeCharacteristic(CDPcharacteristic, AlertCategories.OAD_INIT,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                inServiceMode = true;
            } else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                if (handlesBonding) {
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,0);
                    Log.d(TAG, String.format("bond state changed to %d",bondState));
                    if (BluetoothDevice.BOND_BONDED == bondState) {
                        Log.d(TAG, "time to discover services");
                        if (mBluetoothGatt == null) {
                            Log.e(TAG,"bond state is bonded but gatt is null!");
                            disconnect();
                        } else {
                            mBluetoothGatt.discoverServices();
                        }
                    } else if (BluetoothDevice.BOND_NONE == bondState) {
                        broadcastUpdate(ACTION_GATT_DISCONNECTED); // TODO do we want to broadcast this right now?
                    }
                } else {
                    Log.i(TAG,"App defers to phone to handle bonding");
                }
            }
        };
    };


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){

        @Override
        /**
         * Callback indicating when GATT client has connected/disconnected to/from a remote
         * GATT server.
         *
         * @param gatt GATT client
         * @param status Status of the connect or disconnect operation.
         *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         * @param newState Returns the new connection state. Can be one of
         *                  {@link BluetoothProfile#STATE_DISCONNECTED} or
         *                  {@link BluetoothProfile#STATE_CONNECTED}
         */
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, String.format("on connection state change hit newState??: %d   Status: %d", newState, status));
            cancelDumpConnTimer();
            mBluetoothGatt = gatt;
            if (status != BluetoothGatt.GATT_SUCCESS) {
                String message = GattErrorCodes.GATT_CODE_MESSAGES.get(status);
                Log.e(TAG, String.format("Operation did not succeed: status is %d message %s", status,message));
                disconnect(true);
//                if (canTriggerReconnection && mBluetoothDeviceAddress != null) {
//                    Log.d(TAG,"going to try to reconnect"); // TODO decide if we need a counter
//                    Intent intent = new Intent(BluetoothService.ACTION_PERFORM_CONNECT);
//                    intent.putExtra(BluetoothService.EXTRA_DEVICE_ADDRESS, mBluetoothDeviceAddress);
//                    intent.putExtra(BluetoothService.EXTRA_CONNECTION_TYPE, BluetoothService.DIRECT_CONNECT);
//                    sendBroadcast(intent);
//                }
            } else {
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    canTriggerReconnection = true;
                    bluetoothQueue.setBluetoothGatt(gatt);
                    broadcastUpdate(BluetoothService.ACTION_GATT_CONNECTING);
                    if (!handlesBonding) {
                        Log.d(TAG,"don't handle bonding, we're connected so discover services");
                        mBluetoothGatt.discoverServices();
                    } else {
                        Log.d(TAG, String.format("Connected to GATT server. Connected device was: %s", gatt.getDevice().getAddress()));
                        int bondState = mBluetoothGatt.getDevice().getBondState();
                        Log.d(TAG, String.format("checking bond state: %d", bondState));
                        if(bondState == BluetoothDevice.BOND_NONE) {
                            Log.d(TAG,"no bond so creating one and callback should handle the service discovery");
                            BluetoothDevice device = mBluetoothGatt.getDevice();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                device.createBond();
                            } else {
                                try {
                                    Method m = device.getClass()
                                            .getMethod("createBond", (Class[]) null);
                                    m.invoke(device, (Object[]) null);
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }
                        } else if (bondState == BluetoothDevice.BOND_BONDED) {
                            Log.d(TAG, "discover services for connected and bonded device");
                            mBluetoothGatt.discoverServices();
                        } else if (bondState == BluetoothDevice.BOND_BONDING){
                            Log.d(TAG,"still bonding, services will be discovered when state changes to bonded");
                        }
                    }
                } else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                    bluetoothQueue.setBluetoothGatt(null);

                    Log.d(TAG, "Disconnected from GATT server");
                    disconnect();
                    if(inServiceMode){
                        unpairDevice(mBluetoothGatt.getDevice());
                        connect(mBluetoothDeviceAddress);

                    } else if (
                            mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON
                                    &&
                                    mBluetoothGatt != null && mBluetoothGatt.getDevice() != null
                                    &&
                                    canTriggerReconnection
                            ) {
                        Log.d(TAG, "attempting a reconnection");
                        delayRequest();
                        boolean connectResult = connect(mBluetoothDeviceAddress);
                        Log.d(TAG, String.format("connection attempt result %s", connectResult));
                        if (connectResult) {
                            bluetoothQueue.setBluetoothGatt(mBluetoothGatt);
                            setCurrentTime();
                        }
                    } else {
                        Log.d(TAG, "closing connection; not attempting a reconnection");
                        close();
                        //canToggleBTOn = false;
                    }
                }
                else {
                    Log.w(TAG, String.format("Unknown state found was: %d", newState));
                }
            }
        }

        /**
         * Callback indicating the result of a characteristic write operation.
         * if there are more items in the queue keep them rolling
         *
         * <p>If this callback is invoked while a reliable write transaction is
         * in progress, the value of the characteristic represents the value
         * reported by the remote device. An application should compare this
         * value to the desired value to be written. If the values don't match,
         * the application must abort the reliable write transaction.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#writeCharacteristic}
         * @param characteristic Characteristic that was written to the associated
         *                       remote device.
         * @param status The result of the write operation
         *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         */
        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic, int status){
            Log.d(TAG, "onCharacteristicWrite()");
            if (status != BluetoothGatt.GATT_SUCCESS) {
                this.logStatusError("onCharacteristicWrite", status);
            }
            bluetoothQueue.sentBleAction(status == BluetoothGatt.GATT_SUCCESS);
        }

        private void unpairDevice(BluetoothDevice device) {
            try {
                Method m = device.getClass()
                        .getMethod("removeBond", (Class[]) null);
                m.invoke(device, (Object[]) null);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(),e);
            }
        }


        /**
         * Callback indicating the result of a descriptor write operation.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#writeDescriptor}
         * @param descriptor Descriptor that was written to the associated
         *                   remote device.
         * @param status The result of the write operation
         *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         */
        @Override
        public void onDescriptorWrite (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
            if (BluetoothGatt.GATT_SUCCESS == status) {
                Log.d(TAG,"onDescriptorWrite()");
                broadcastUpdate(ACTION_GATT_CONNECTED);
                if (CDPcharacteristic.getDescriptor(
                        BTserviceAttributes.CDP_DESCRIPTOR_CONFIG).equals(descriptor)) {
                    configureDeviceForUse();
                }
            } else {
                this.logStatusError("onDescriptorWrite",status);
                // TBD if this fails, what should we do?
                if (CDPcharacteristic.getDescriptor(
                        BTserviceAttributes.CDP_DESCRIPTOR_CONFIG).equals(descriptor)){
                    Log.e(TAG,"failed to enable notifications!");
                }
            }

        }

        /**
         * Callback reporting the result of a descriptor read operation.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#readDescriptor}
         * @param descriptor Descriptor that was read from the associated
         *                   remote device.
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the read operation
         *               was completed successfully
         */
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
            if (BluetoothGatt.GATT_SUCCESS == status) {
                Log.d(TAG,"onDescriptorRead");
            }
            else {
                this.logStatusError("onDescriptorRead",status);
            }
        }

        /**
         * Callback reporting the RSSI for a remote device connection.
         *
         * This callback is triggered in response to the
         * {@link BluetoothGatt#readRemoteRssi} function.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#readRemoteRssi}
         * @param rssi The RSSI value for the remote device
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the RSSI was read successfully
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status){
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, String.format("onReadRemoteRssi gatt:  %s   rssi:  %d", gatt, rssi));
            } else {
                this.logStatusError("onReadRemoteRssi",status);
            }
        }


        @Override
        /**
         * Callback invoked when the list of remote services, characteristics and descriptors
         * for the remote device have been updated, ie new services have been discovered.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#discoverServices}
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the remote device
         *               has been explored successfully.
         */
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered");
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                if(inServiceMode)  {
                    Log.d(TAG, "device is in service mode");
                    try {
                        BluetoothGattService oadService = gatt.getService(BTserviceAttributes.CDP_OAD_SERVICE_UUID);
                        OADcharacteristic = oadService.getCharacteristic(BTserviceAttributes.CDP_OAD_CHAR_UUID2);
                        Log.d(TAG, "got oad characteristic");
                        gatt.setCharacteristicNotification(OADcharacteristic, true);
                        delayRequest();
                        //TODO: this is where we load in the update firmware file
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.w(TAG, "null oad char");
                    }
                } else if(status == 0){
                    Log.d(TAG, "device is not in service mode");
                    DeviceInfo.getInstance().setDeviceAddress(gatt.getDevice().getAddress());

                    BluetoothGattService gattService = gatt.getService(BTserviceAttributes.CDP_SERVICE_UUID);
                    BluetoothGattService immAlertService =
                            gatt.getService(BTserviceAttributes.CDP_SERVICE_IMMEDIATE_ALERT_UUID);
                    deviceInfoService = gatt.getService(BTserviceAttributes.CDP_SERVICE_DEVICE_INFO_UUID);
                    if (deviceInfoService == null) {
                        Log.w(TAG,"error retrieving Device Info Service");
                    } else {
                        Log.d(TAG,"retrieved device info service");
                    }

                    CDPcharacteristic =
                            gattService.getCharacteristic(BTserviceAttributes.CDP_DATA_CHANNEL_CHARACTERISTIC_UUID);

                    CDP_heartbeat_characteristic =
                            gattService.getCharacteristic(BTserviceAttributes.CDP_DATA_CHANNEL_CHARACTERISTIC_UUID);

                    ImmediateAlertService =
                            immAlertService.getCharacteristic(BTserviceAttributes.ALERT_LEVEL_CHARACTERISTIC_UUID);

                    boolean cdpNotifs = gatt.setCharacteristicNotification(CDPcharacteristic, true);

                    Log.d(TAG, String.format("set cdp notifs? %s", cdpNotifs));

                    boolean heartbeatNotifs = gatt.setCharacteristicNotification(CDP_heartbeat_characteristic, true);
                    Log.d(TAG, String.format("set heartbeat notifs? %s", heartbeatNotifs)
                    );

                    enableNotifications();

                }
            }
            else {
                this.logStatusError("onServicesDiscovered",status);
            }

        }

        private void delayRequest() {
            try {
                Thread.sleep(2000);
            }  catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        @Override
        /**
         * Callback reporting the result of a characteristic read operation.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#readCharacteristic}
         * @param characteristic Characteristic that was read from the associated
         *                       remote device.
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the read operation
         *               was completed successfully.
         */
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG,"onCharacteristicRead");
                readDeviceInfoCharacteristic(characteristic);
            } else {
                logStatusError("onCharacteristicRead", status);
            }
            bluetoothQueue.sentBleAction(BluetoothGatt.GATT_SUCCESS == status);
        }

        private void readDeviceInfoCharacteristic(BluetoothGattCharacteristic characteristic){
            if (!BTserviceAttributes.DEVICE_INFO_CHARACTERISTICS.contains(characteristic.getUuid())) {return;}
            try {
                DeviceInfo deviceInfo = DeviceInfo.getInstance();
                UUID uuid = characteristic.getUuid();

                if (BTserviceAttributes.DEVICE_MODEL_NUMBER.equals(uuid)){
                    deviceInfo.setDeviceName(getCleanedString(characteristic));
                }
                else if (BTserviceAttributes.DEVICE_SOFTWARE_REVISION.equals(uuid)){
                    deviceInfo.setSoftwareRevision(getCleanedString(characteristic));
                }
                else if (BTserviceAttributes.DEVICE_HARDWARE_REVISION.equals(uuid)){
                    deviceInfo.setHardwareRevision(getCleanedString(characteristic));
                }
                else if (BTserviceAttributes.DEVICE_FIRMWARE_REVISION.equals(uuid)){
                    deviceInfo.setFirmwareRevision(getCleanedString(characteristic));
                }
                else if (BTserviceAttributes.DEVICE_MANUFACTURER_NAME.equals(uuid)){
                    deviceInfo.setDeviceManufacturer(getCleanedString(characteristic));
                }
            } catch (Exception e){
                Log.w(TAG,"Exception reading device info",e);
            }

        }

        private String getCleanedString(BluetoothGattCharacteristic characteristic) {
            String value = characteristic.getStringValue(0).replaceAll(NON_VALID_CHARS, "");
            Log.d(TAG, String.format("value for characteristic %s is %s", characteristic.getUuid().toString(), value));
            return value;
        }

        @Override
        /**
         * Callback triggered as a result of a remote characteristic notification.
         *
         * @param gatt GATT client the characteristic is associated with
         * @param characteristic Characteristic that has been updated as a result
         *                       of a remote notification event.
         */
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, String.format("onCharacteristicChanged() triggered by remote notification for characteristic [%s]", characteristic.getUuid().toString()));
            bluetoothQueue.sentBleAction(true);
            broadcastUpdate(characteristic);
        }

        @Override
        /**
         * Callback invoked when a reliable write transaction has been completed.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#executeReliableWrite}
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the reliable write
         *               transaction was executed successfully
         */
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG,"onReliableWriteCompleted");
            } else {
                logStatusError("onReliableWriteCompleted",status);
            }
        }

        private void logStatusError(String method, int status) {
            String statusName = GattErrorCodes.GATT_CODE_MESSAGES.get(status);
            Log.e(TAG, String.format("%s status %04X desc %s",method, status, statusName));
        }

    };

    private void configureDeviceForUse() {
        writeAlertConfiguration();
        setCurrentTime();
        writeLinkLossConfigurationTime(false);
        readDeviceInfo();
    }

    /**
     * Enable notifications - this must happen first after discovery;
     * success or failure is reported to {@link android.bluetooth.BluetoothGattCallback#onDescriptorWrite},
     * at which time we can write the configurations needed
     */
    private void enableNotifications() {
        BluetoothGattDescriptor descriptor = CDPcharacteristic.getDescriptor(
                BTserviceAttributes.CDP_DESCRIPTOR_CONFIG);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    public void writeLinkLossConfigurationTime(boolean enabled) {
        Log.d(TAG, String.format("link loss configuration time -- enabled? %s", enabled));
        byte defaultLength = (enabled ? AlertCategories.LINK_LOSS_ENABLED : AlertCategories.LINK_LOSS_DISABLED);
        byte[] bytes = new byte[5];
        bytes[0] = AlertCategories.WRITE_MESSAGE;
        bytes[1] = AlertCategories.CONFIGURABLE_ITEM;
        bytes[2] = AlertCategories.LINK_LOSS_ALERT_CONFIGURATION_TIME;
        bytes[3] = defaultLength;
        bytes[4] = defaultLength;
        CDPcharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        addCharacteristicToWriteQueue(CDPcharacteristic,bytes);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {
        Log.d(TAG,String.format("broadcastUpdate for gatt characteristic %s ", characteristic.getUuid().toString()));
        final Intent intent = new Intent(BluetoothService.ACTION_DATA_AVAILABLE);

        if (BTserviceAttributes.CDP_DATA_HEARTBEAT_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "CDP DATA format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "CDP DATA format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                Log.d(TAG, String.format("Extra string in BluetoothService: %s", stringBuilder.toString()));
                intent.putExtra(EXTRA_DATA, stringBuilder.toString());
                byte messageType = (byte) (data[0] & 0xFF);
                Log.d(TAG,String.format("byte at 0 is %d",messageType));
                if (messageType == AlertCategories.ALERT_ACKNOWLEDGED) {
                    handleAcknowledgement(data);
                    Intent ackIntent = new Intent(ACTION_ALERT_ACKNOWLEDGED);
                    sendBroadcast(ackIntent);
                    return;
                } else if (AlertCategories.TRIGGER_ACKNOWLEDGED == messageType){
                    this.cancelImmediateAlert();
                } else if (AlertCategories.TRIGGER_UPDATED == messageType) {
                    Log.d(TAG, "Trigger updated");
                    // this is where to add in the other commands
                    if (data.length > 1) {
                        byte command = data[1];
                        if (0x03 == command) {
                            findMyPhone();
                        } else {
                            Log.i(TAG,String.format("not configured for command %s", command));
                        }
                    }
                } else if (AlertCategories.CONFIRM_MESSAGE == messageType){
                    Log.d(TAG,"Confirmation message");
                }
                else if (AlertCategories.ERROR_MESSAGE == messageType )
                {
                    byte error = data[1];
                    Log.e(TAG,"Error received for message type: " + error);
                    if (error ==  0x04) {
                        Log.d(TAG,"BD Address Response");
                    }
                } else if (AlertCategories.OAD_INIT_ACKNOWLEDGE == messageType) {
                    Log.d(TAG, "OAD Init acknowledged; device entered service mode");
                    inServiceMode = true;
                }
                else {
                    Log.w(TAG, String.format("not configured to handle this request: %s", stringBuilder.toString()));
                }
            }
        }
        sendBroadcast(intent);
    }

    private void handleAcknowledgement(final byte[] data){
        Log.d(TAG,"handleAcknowledgement");
        AlertCategories alertCategories = AlertCategories.getInstance();
        alertCategories.setAlerts(new byte[]{data[1],data[2]});
        Log.d(TAG,alertCategories.toString());
        if (alertCategories.isCallAlert()) {
            silenceMyPhone();
        }
    }

    private void silenceMyPhone() {
        Log.d(TAG, "silence my phone");
        if(NotifListenerService.acceptTelephony) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            NotifListenerService.cancelAlert();
        }
    }

    private void findMyPhone() {
        Log.d(TAG, "Find my phone");

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int originalVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.cookoo);
        mp.start();
        try {
            Thread.sleep(3000);
            //sleep for 3 seconds prior to setting volume back
        }  catch(InterruptedException ex){
            ex.printStackTrace();
        }
        am.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume,0);
    }


    /**
     * Set current time on the device
     */
    public void setCurrentTime() {
        int[] timeData = getTimeData();
        Log.d(TAG,"time data in org.bluetooth.service.current_time format");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        if (CDPcharacteristic == null) {
            Log.e(TAG, "CDP Characteristic is null!");
            return;
        }
        CDPcharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        List<Integer> uint16indexes = Arrays.asList(new Integer[]{1}); // index(es) of uint16 values (currently only year but in case we want to reuse logic)
        CDPcharacteristic.setValue(new byte[timeData.length + uint16indexes.size()]); // we need to clear any values set otherwise
        for (int i = 0, offset = 0; i < timeData.length; i++) {
            boolean isUInt16 = uint16indexes.contains(i);
            int type = isUInt16 ? BluetoothGattCharacteristic.FORMAT_UINT16 : BluetoothGattCharacteristic.FORMAT_UINT8;
            CDPcharacteristic.setValue(timeData[i],type,offset++);
            if (isUInt16) { offset++;} // additional offset for uint16
        }
        Log.d(TAG, String.format("Characteristic values we are writing?: %s", this.integersToString(timeData)));

        addCharacteristicToWriteQueue(CDPcharacteristic,CDPcharacteristic.getValue());
    }

    /**
     * update a single alert source mask
     * @param alertCategories
     * @param isOn
     */
    public void updateAlertSourceMask(Byte[] alertCategories, boolean isOn){
        Log.d(TAG, String.format("updating alert source mask for category %s to %b", alertCategories[0], isOn));
        if (CDPcharacteristic == null) {return;}
        byte statusByte = (byte) (isOn ? 0x01 : 0x00);
        int length = 3 + (alertCategories.length * 2);
        byte[] bytes = new byte[length];
        bytes[0] = AlertCategories.WRITE_MESSAGE;
        bytes[1] = AlertCategories.CONFIGURABLE_ITEM;
        bytes[2] = AlertCategories.ALERT_CONFIGURATION;
        int index = 3;
        for (byte category : alertCategories) {
            bytes[index++] = category;
            bytes[index++] = statusByte;
        }
        writeCharacteristic(CDPcharacteristic,bytes,BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void sendOutOfRangeAlert(boolean on){
        // The link loss alert time sets the time between a link loss event and the alert.
        // If the time is set to 0xFFFF, then the link loss alert is disabled.
        // If set to 0x0000 then the alert is immediate.
        Log.d(TAG, String.format("set out of range alert on? %s", on));
        byte[] bytes = new byte[5];
        bytes[0] = AlertCategories.WRITE_MESSAGE;
        bytes[1] = AlertCategories.CONFIGURABLE_ITEM;
        bytes[2] = AlertCategories.SCHEDULE;
        byte config = (byte) (on ? 0x00 : 0xFF);
        bytes[3] = config;
        bytes[4] = config;
        writeCharacteristic(BluetoothService.CDPcharacteristic, bytes, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

    }

    /**
     * update entire alert source mask
     */
    public void writeAlertConfiguration() {
        Log.d(TAG, "updating alert source mask");
        if (CDPcharacteristic == null) {return;}
        // NB if more than 8 categories are added to the AlertCategories#CATEGORY_MASK_TO_PREFERENCE map,
        // will need to break up the writes
        int length = 3 + (AlertCategories.CATEGORY_MASK_TO_PREFERENCE.size() * 2);
        byte[] bytes = new byte[length];
        bytes[0] = AlertCategories.WRITE_MESSAGE;
        bytes[1] = AlertCategories.CONFIGURABLE_ITEM;
        bytes[2] = AlertCategories.ALERT_CONFIGURATION;
        int index = 3;

        for (Byte category : AlertCategories.CATEGORY_MASK_TO_PREFERENCE.keySet()) {
            String key = AlertCategories.CATEGORY_MASK_TO_PREFERENCE.get(category);
            boolean isOn = prefs.getBoolean(key, true);
            byte statusByte = (byte) (isOn ? 0x01 : 0x00);
            bytes[index++] = category;
            bytes[index++] = statusByte;
        }
        writeCharacteristic(CDPcharacteristic,bytes,BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

    }


    /**
     * Get current time data in Exact Time 256 format
     * @return
     */
    private int[] getTimeData() {
        Log.d(TAG,"getting current time");
        /*
         * org.bluetooth.characteristic.current_time
         * - Exact Time 256: org.bluetooth.characteristic.exact_time_256
         *   - Day Date Time: org.bluetooth.characteristic.day_date_time
         *     - Date Time: org.bluetooth.characteristic.date_time
         *       - Year: uint16
         *       - Month: uint8
         *       - Day: uint8
         *       - Hours: uint8
         *       - Minutes: uint8
         *       - Seconds: uint8
         *     - Day of Week: org.bluetooth.characteristic.day_of_week
         *       - uint8
         *   - Fractions256: uint8
         * - Adjust Reason: 8bit
         */

        Time currentTime = new Time();
        currentTime.setToNow();
        Log.d(TAG,String.format("current time is %s",currentTime.toString(),currentTime.year));
        int year = currentTime.year;

        int[] timeData = new int[10];
        timeData[0] = AlertCategories.CURRENT_TIME; // message type UINT8
        timeData[1] = year; // year as UINT16
        timeData[2] = (currentTime.month + 1); // month
        timeData[3] = (currentTime.monthDay); // day
        timeData[4] = (currentTime.hour); // hours
        timeData[5] = (currentTime.minute); // minutes
        timeData[6] = (currentTime.second); // seconds
        timeData[7] = (currentTime.weekDay + 1); // day of week
        timeData[8] = 0x00; // 1/256th of a second; we're using second precision so always zero
        timeData[9] = 0x01; // reason
        return timeData;
    }

    public void readDeviceInfo() {
        Log.d(TAG, "readDeviceInfo");
        List<UUID> characteristicsToRead = new ArrayList<UUID>(Arrays.asList(
                BTserviceAttributes.DEVICE_MODEL_NUMBER,
                BTserviceAttributes.DEVICE_SOFTWARE_REVISION,
                BTserviceAttributes.DEVICE_HARDWARE_REVISION,
                BTserviceAttributes.DEVICE_FIRMWARE_REVISION,
                BTserviceAttributes.DEVICE_MANUFACTURER_NAME
        ));
        if (deviceInfoService != null) {
            for (UUID uuid : characteristicsToRead) {
                bluetoothQueue.addBleAction(new BleAction(AlertCategories.READ_MESSAGE, deviceInfoService.getCharacteristic(uuid),new byte[0]));
            }
        }
    }

    public boolean startLeScan(BluetoothAdapter.LeScanCallback leScanCallback) {
        Log.d(TAG,"startLeScan");
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.startLeScan(leScanCallback);
        } else {
            Log.w(TAG,"call to bluetooth adapter start LE Scan when adapter is null");
        }
        return false;
    }


    private class DumpConnectionTimer {
        Timer timer;
        public DumpConnectionTimer(int seconds) {
            timer = new Timer();
            timer.schedule(new RestartTimer(), seconds*1000);
        }
        class RestartTimer extends TimerTask {
            public void run() {
                Log.w(TAG, "dumping connection");
                disconnect();
                close();
                initialize();
                mBusy = false;
                connect(mBluetoothDeviceAddress);
                timer.cancel(); //Terminate the timer thread
                timer.purge();
            }
        }
    }

    private void cancelDumpConnTimer() {
        try {
            if(dumpConn != null) {
                dumpConn.timer.cancel();
                dumpConn.timer.purge();
            }
        }  catch(Exception ex) {
            ex.printStackTrace();
            Log.w(TAG, "there was no restart timer");
        }
    }

    private void restartDumpConnTimer() {
        if (DevInfo.isSamsung()) {
            dumpConn = new DumpConnectionTimer(10);
        }
    }

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,"onUnbind");
        disconnect();
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     * @return Return true if the initialization is successful.
     */
    private void initialize() {
        Log.d(TAG,"initialize");
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        mThis = this;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
            } else {
                mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter == null) {
                    Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
                }
            }
        }
    }


    /**
     * Close a connection currently in progress
     * and prevent an attempted reconnection
     */
    public void cancelConnectionAttempt() {

        SharedPreferences.Editor editor =  prefs.edit();
        if(prefs.contains(MainActivity.LAST_CONNECTED_DEVICE))
            editor.remove(MainActivity.LAST_CONNECTED_DEVICE);
        canTriggerReconnection = false;
        disconnect();
        close();
    }

    public void clearLastConnectedDevice() {
        SharedPreferences.Editor editor =  prefs.edit();
        if(prefs.contains(MainActivity.LAST_CONNECTED_DEVICE))
            editor.remove(MainActivity.LAST_CONNECTED_DEVICE);
        mBluetoothDeviceAddress = null;
    }

    public void clearExistingConnection() {
        Log.d(TAG,"clearing existing connection");
        canTriggerReconnection = false;
        disconnect();
        close();
        mBluetoothDeviceAddress = null;
        try {
            Thread.sleep(1000);
        }  catch (Exception ex) {
            Log.w(TAG, "thread was interrupted");
        }
    }



    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (address == null) {
            Log.e(TAG,"attempting to reconnect with no device address!");
            return false;
        }
        Log.d(TAG, String.format("trying to connect with address: %s", address));
        if (!this.isBluetoothEnabled()) {
            Log.e(TAG, "request to connect when bluetooth is not enabled");
            broadcastUpdate(ACTION_GATT_DEVICE_NOT_FOUND);
            broadcastUpdate(ACTION_GATT_DISCONNECTED);
            return false;
        }
        cancelDumpConnTimer();
        SharedPreferences.Editor editor =  prefs.edit();
        if(prefs.contains(MainActivity.LAST_CONNECTED_DEVICE)) {
            editor.remove(MainActivity.LAST_CONNECTED_DEVICE);
        }
        canTriggerReconnection = true;

        int numDevices = numConnectedDevices();
        Log.d(TAG, String.format("number of connected devices %d", numDevices));
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        int connectionState = getConnectionState(device, BluetoothProfile.GATT);
        if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
            editor.putString(MainActivity.LAST_CONNECTED_DEVICE, device.getAddress());
            editor.commit();

            if(numDevices > 0){
                Log.w(TAG, "disconnecting from previously connected devices");
                clearExistingConnection();
            }

            if(DevInfo.isSamsung()) {
                Log.i(TAG,"Samsung phone, scheduling dump connection timer");
                dumpConn = new DumpConnectionTimer(10);
            }

            // Previously connected device. Try to reconnect.
            if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
                Log.d(TAG, "Re-use GATT connection");
                if (mBluetoothGatt.connect()) {
                    return true;
                } else {
                    Log.d(TAG, "GATT re-connect failed; will try new connection");
                    mBluetoothGatt = null;
                }
            }

            Log.d(TAG, "Create a new GATT connection.");
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            if (mBluetoothGatt == null) {
                Log.e(TAG,"gatt returned is null!");
            } else {
                Log.d(TAG,"Connection attempt started; results reported asynchronously");
            }
            mBluetoothDeviceAddress = address;
            return true;
        } else {
            //We have lost a way to programatically disconnect from device.. restart BT.
            mBluetoothDeviceAddress = address;
            mBluetoothAdapter.disable();
            canToggleBTOn = true;
            Log.w(TAG, String.format("device was in connection state %d. restarting bt. and will try to reconnect to %s", connectionState,mBluetoothDeviceAddress));
            return false;
        }

    }

    /**
     * disconnect and optionally close the GATT. If closed, it will be set to null as well
     * @param andClose
     */
    public void disconnect(boolean andClose) {
        disconnect();
        if (andClose && mBluetoothGatt != null) {
            Log.d(TAG,"closing gatt to prevent reuse in an error state");
            try {
                mBluetoothGatt.close();
            } catch (Exception e) {
                Log.e(TAG,"error closing gatt",e);
            } finally {
                mBluetoothGatt = null;
            }
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection.
     * The disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)} callback.
     */
    public void disconnect() {
        Log.d(TAG,"disconnect()");
        if (mBluetoothAdapter == null || mBluetoothDeviceAddress == null || mBluetoothGatt == null || mBluetoothManager == null) {
            Log.w(TAG, "disconnect: Bluetooth not initialized");
        }
        else {
            Log.i(TAG, String.format("disconnecting %s", mBluetoothDeviceAddress));
            try {
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                }
            } catch (Exception e) {
                Log.e(TAG,"error disconnecting",e);
            }
        }
        broadcastUpdate(ACTION_GATT_DISCONNECTED);
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are released properly.
     */
    public void close() {
        Log.i(TAG, "close");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }




    //
    // Utility functions
    //
    public static BluetoothGatt getBtGatt() {
        if(mThis != null)
            return mThis.mBluetoothGatt;
        else
            return null;
    }

    public static BluetoothService getInstance() {
        if (mThis == null) {
            Log.w(TAG,"Call to get BluetoothService instance when instance is null");
        }
        return mThis;
    }

    public int getConnectionState(BluetoothDevice device, int profile) {
        if (this.mBluetoothManager != null) {
            return this.mBluetoothManager.getConnectionState(device,profile);
        } else {
            Log.w(TAG,"request for connection state for device when bluetooth manager was null! Returning disconnected");
        }
        return BluetoothProfile.STATE_DISCONNECTED;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public boolean cancelDeviceDiscovery() {
        Log.d(TAG,"cancelDeviceDiscovery");
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.cancelDiscovery();
        } else {
            Log.w(TAG,"request to cancel device discovery when adapter is null!");
        }
        return false;
    }

    public void stopLeScan(BluetoothAdapter.LeScanCallback leScanCallback) {
        Log.d(TAG,"stopLeScan");
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.stopLeScan(leScanCallback);
        } else {
            Log.w(TAG,"stop LE Scan called when bluetooth adapter is null");
        }
    }

    public boolean isBluetoothEnabled(){
        Log.d(TAG,"isBluetoothEnabled");
        return (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled());
    }

    public Set<BluetoothDevice> getBondedDevices(){
        Log.d(TAG,"getBondedDevices");
        if (mBluetoothAdapter != null){
            return mBluetoothAdapter.getBondedDevices();
        } else {
            Log.w(TAG,"request for bonded devices when bluetooth adapter was null!");
        }
        return null;
    }


    public int numConnectedDevices() {
        int n = 0;

        if (mBluetoothGatt != null) {
            List<BluetoothDevice> devList;
            devList = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
            n = devList.size();
        }
        return n;
    }


    byte[] stringToByte(String text) {
        final String[] split = text.split("\\s+");
        final byte[] result = new byte[split.length];
        int i = 0;
        for (String b : split){
            result[i] = (byte)Integer.parseInt(b, 16);
            Log.d(TAG, String.format("TO BYTES ARRAY: %s", result[i]));
            i++;
        }
        return result;
    }


    String integersToString(int[] integers) {
        if (integers == null || integers.length < 1) { return "<none passed in>";}
        StringBuilder sb = new StringBuilder();
        for (int anInt : integers){
            sb.append(String.format("%04X ", anInt));
        }
        return sb.toString().trim();

    }


    /**
     * write a char
     * @param characteristic
     * @param val
     * @param writeType
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] val, Integer writeType) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "writeCharacteristic - bytes - BluetoothAdapter not initialized or device not connected");
            return;
        }
        if (characteristic == null) {
            Log.e(TAG, "received request to write to a null characteristic!");
            return;
        }
        characteristic.setWriteType(writeType);

        addCharacteristicToWriteQueue(characteristic,val);

    }

    private void addCharacteristicToWriteQueue(BluetoothGattCharacteristic characteristic,byte[] bytes) {
        bluetoothQueue.addBleAction(new BleAction(AlertCategories.WRITE_MESSAGE,characteristic,bytes));
    }


    public void triggerImmediateAlertFindMe(){
        Log.d(TAG,"Find Device");
        writeCharacteristic(
                BluetoothService.ImmediateAlertService,
                AlertCategories.IMMEDIATE_ALERT_HIGH,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void triggerImmediateAlertOther(){
        writeCharacteristic(BluetoothService.ImmediateAlertService, AlertCategories.IMMEDIATE_ALERT_LOW,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void cancelImmediateAlert(){
        Log.d(TAG,"Find device canceled");
        writeCharacteristic(
                BluetoothService.ImmediateAlertService,
                AlertCategories.IMMEDIATE_ALERT_OFF,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void sendBatteryAlert(byte batteryPercentage) {
        Log.d(TAG, String.format("sending battery alert with percentage %s", batteryPercentage));
        writeCharacteristic(BluetoothService.CDPcharacteristic,
                new byte[]{AlertCategories.BATTERY, batteryPercentage}, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void cancelBatteryAlert(){
        sendBatteryAlert((byte) 99);
    }

    public void sendLowBatteryAlert(){
        sendBatteryAlert((byte) 20);
    }

    public void sendUnreadAlert(byte alertCategory, int count) {
        Log.d(TAG, String.format("sending unread alert for category %s with count %s", alertCategory, count));
        byte[] bytes = new byte[]{AlertCategories.UNREAD_ALERT_STATUS,alertCategory, (byte) count};
        BluetoothService.getInstance().writeCharacteristic(BluetoothService.CDPcharacteristic,
                bytes, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void sendNewAlert(byte alertCategory, int count, String message) {
        Log.d(TAG, String.format("sending new alert for category %s with count %s and message %s", alertCategory, count,message));
        byte[] messageBytes;
        byte[] bytes = new byte[10];
        bytes[0] = AlertCategories.NEW_ALERT;
        bytes[1] = alertCategory;
        bytes[2] = (byte)count;
        try {
            if (message != null) {
                messageBytes = message.getBytes("UTF-8");
                if (messageBytes.length > 0) {
                    if (messageBytes.length > 7) {Log.i(TAG,"Only first 7 bytes of message are accepted");}
                    for (int i = 3, j = 0; i < bytes.length && j < messageBytes.length; i++,j++) {
                        bytes[i] = messageBytes[j];
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            Log.e(TAG,"UTF-8 is unsupported! Cannot send message with alert");
        }
        BluetoothService.getInstance().writeCharacteristic(BluetoothService.CDPcharacteristic,
                bytes, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

    }

    public void sendCriticalBatteryAlert(){
        sendBatteryAlert((byte) 10);
    }


    public boolean isConnected(){
        return mBluetoothGatt != null && mBluetoothGatt.getDevice() != null;
    }

    /**
     * broadcast current connection state for use by listening filters
     */
    public void sendConnectionStateBroadcast() {
        if (isConnected()) {
            broadcastUpdate(ACTION_GATT_CONNECTED);
        } else {
            broadcastUpdate(ACTION_GATT_DISCONNECTED);
        }
    }

    /**
     * convenience method to get an intent filter for intents broadcast by this service
     * @return
     */
    public static IntentFilter getGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothService.ACTION_GATT_BAD_CONNECTION);
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BluetoothService.ACTION_CYCLE_BLUETOOTH);
        intentFilter.addAction(BluetoothService.ACTION_DEVICE_OOR);
        intentFilter.addAction(BluetoothService.ACTION_GATT_BAD_LINK_KEY);
        return intentFilter;
    }




}
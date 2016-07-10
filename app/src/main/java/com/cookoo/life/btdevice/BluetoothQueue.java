package com.cookoo.life.btdevice;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;
import com.cookoo.life.domain.AlertCategories;
import com.cookoo.life.utilities.DevInfo;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The ConnecteDevice expects sequential communication but the Android Bluetooth implementation
 * uses callbacks to inform us when things are done; this queue bridges the gap
 * <p/>
 * Created by stevi.deter on 3/13/14.
 */
public class BluetoothQueue {
    private final static String TAG = BluetoothQueue.class.getSimpleName();
    private static final List<Byte> knownActions = Arrays.asList(new Byte[]{AlertCategories.READ_MESSAGE, AlertCategories.WRITE_MESSAGE});
    private static BluetoothQueue bluetoothQueue;
    private Queue<BleAction> bleActions = new ConcurrentLinkedQueue<BleAction>();
    private volatile boolean isBusy = false;
    private BluetoothGatt bluetoothGatt;
    private volatile UUID currentBusyUuid;

    private BluetoothQueue() {
    }

    public static BluetoothQueue getInstance() {
        if (bluetoothQueue == null) {
            bluetoothQueue = new BluetoothQueue();
        }
        return bluetoothQueue;
    }

    /**
     * schedule an action to be submitted to the ConnecteDevice
     *
     * @param bleAction
     * @return
     */
    public boolean addBleAction(BleAction bleAction) {
        Log.d(TAG, String.format("adding new BLE Action to the queue with UUID %s", bleAction.getUuid().toString()));
        if (bluetoothGatt == null) {
            Log.e(TAG, "attempt to add an action when I have no GATT to use!");
            return false;
        }
        if (!knownActions.contains(bleAction.getActionCode())) {
            Log.e(TAG, String.format("attempt to add an action with an unknown action code %s", bleAction.getActionCode()));
            return false;
        }
        boolean accepted = bleActions.offer(bleAction);
        if (!accepted) {
            Log.e(TAG, "unable to queue the action at this time!");
        } else {
            Log.d(TAG, String.format("action queue is currently %d long", bleActions.size()));
            sendBleAction();
        }
        return accepted;
    }

    /**
     * see if we can send a new action.
     */
    private void sendBleAction() {
        try {
            Log.d(TAG, String.format("asked to send a BLE Action to the device while queue is %d long", bleActions.size()));
            if (bleActions.isEmpty()) {
                Log.i(TAG, "no actions to send at this time");
            } else if (isBusy) {
                Log.d(TAG, "busy with previous action");
            } else if (bluetoothGatt == null) {
                Log.e(TAG, "no active GATT to send to");
            } else {
                BleAction action = bleActions.poll();
                if (action != null) {
                    isBusy = true;
                    currentBusyUuid = action.getUuid();
                    Log.d(TAG, String.format("sending action type %s with UUID %s", action.getActionCode(), action.getUuid().toString()));
                    BluetoothGattCharacteristic characteristic = action.getCharacteristic();
                    characteristic.setValue(action.getValue());
                    if (AlertCategories.WRITE_MESSAGE == action.getActionCode()) {
                        Log.d(TAG, String.format("writing characteristic with values: %s", this.bytesToString(characteristic.getValue())));
                        bluetoothGatt.writeCharacteristic(characteristic);
                    } else if (AlertCategories.READ_MESSAGE == action.getActionCode()) {
                        bluetoothGatt.readCharacteristic(characteristic);
                    } else {
                        Log.e(TAG, "unknown action " + action.getActionCode());
                        currentBusyUuid = null;
                        isBusy = false;
                        throw new IllegalStateException("incorrectly queued request");
                    }
                    checkForStuckRead(action.getUuid().toString());
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception using queue!", ex);
            // TODO probably want to kick the bluetooth connection at this point
            bleActions.clear();
        }
    }

    // still investigating why queue gets stuck, (likely not catching a callback)
    // until then, kick the queue if it's not moving
    // trying to avoid a separate thread to minimize risk
    // of setting off competing writes - the whole point
    // of this class is to force sequential communication with the device
    public void checkForStuckRead(final String busyUuid) {
        // HTC seems to still want to be busy
        if (DevInfo.isHTC()) {
            long htcSleep = 100;
            try {
                Thread.sleep(htcSleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Timer timer = new Timer();
        timer.schedule(new QueueTimer(busyUuid), 1000);
    }

    /**
     * callback informs us that it's finished with an activity so it's ready for the next
     *
     * @param wasSuccessful
     */
    public void sentBleAction(boolean wasSuccessful) {
        Log.d(TAG, String.format("BLE Action was sent to the device. Was it successful? %s", wasSuccessful));
        isBusy = false;
        currentBusyUuid = null;

        sendBleAction();
    }

    /**
     * gatt to communicate with. Currently setting from service to avoid circular dependency with the service
     * and to keep track of when it's actually available
     *
     * @param bluetoothGatt
     */
    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        Log.d(TAG, "setting the bluetooth gatt to use for sending characteristics; clearing existing queue");
        bleActions.clear();
        isBusy = false;
        currentBusyUuid = null;
        this.bluetoothGatt = bluetoothGatt;
    }

    /**
     * convenience method for human readable input on what we're sending on writes
     *
     * @param bytes
     * @return
     */
    String bytesToString(byte[] bytes) {
        if (bytes == null || bytes.length < 1) {
            return "<none passed in>";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    private class QueueTimer extends TimerTask {
        private String busyUuid;

        private QueueTimer(String busyUuid) {
            this.busyUuid = busyUuid;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (isBusy && currentBusyUuid != null && busyUuid.equals(currentBusyUuid.toString())) {
                Log.w(TAG, "Queue got stuck on UUID " + currentBusyUuid + ", look into why! Kicking it to move it forward");
                isBusy = false;
                currentBusyUuid = null;
                sendBleAction();
            }
        }
    }

}

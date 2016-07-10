package com.cookoo.life.btdevice;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

/**
 * Created by stevi.deter on 3/13/14.
 */
public class BleAction {

    private UUID uuid;
    private byte actionCode;
    private BluetoothGattCharacteristic characteristic;
    private byte[] value;

    public BleAction(byte actionCode, BluetoothGattCharacteristic characteristic, byte[] value) {
        this.actionCode = actionCode;
        this.characteristic = characteristic;
        this.uuid = UUID.randomUUID();
        this.value = value;
    }

    public byte getActionCode() {
        return actionCode;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    /**
     * handle to a specific characteristic action request
     * @return
     */
    public UUID getUuid() {
        return uuid;
    }

    public byte[] getValue() {
        return value;
    }
}

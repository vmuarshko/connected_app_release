package com.cookoo.life.btdevice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;




public class BTserviceAttributes {

    public static UUID CDP_SERVICE_UUID = UUID.fromString("4B455254-7472-11e1-a575-0002a5d58001");
    public static UUID CDP_DATA_CHANNEL_CHARACTERISTIC_UUID = UUID.fromString("4B455254-7472-11e1-a575-0002a5d54001");
    public static UUID CDP_DATA_HEARTBEAT_CHARACTERISTIC_UUID = UUID.fromString("4B455254-7472-11e1-a575-0002a5d54002");

    public static UUID CDP_SERVICE_GAP_UUID				= UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public static UUID CDP_SERVICE_GATT_UUID			= UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    public static UUID CDP_SERVICE_IMMEDIATE_ALERT_UUID	= UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public static UUID ALERT_LEVEL_CHARACTERISTIC_UUID  = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

    public static UUID CDP_SERVICE_DEVICE_INFO_UUID		= UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static UUID DEVICE_SOFTWARE_REVISION         = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");
    public static UUID DEVICE_MODEL_NUMBER              = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
    public static UUID DEVICE_SERIAL_NUMBER             = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");
    public static UUID DEVICE_FIRMWARE_REVISION         = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static UUID DEVICE_HARDWARE_REVISION         = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");
    public static UUID DEVICE_MANUFACTURER_NAME         = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static UUID DEVICE_INFO_PNP_ID               = UUID.fromString("00002a50-0000-1000-8000-00805f9b34fb");

    public static UUID CDP_DESCRIPTOR_CONFIG            = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static UUID CDP_DESCRIPTOR_CONFIG2           = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");

    public static UUID CDP_OAD_SERVICE_UUID             = UUID.fromString("F000FFC0-0451-4000-B000-000000000000");
    public static UUID CDP_OAD_CHAR_UUID                = UUID.fromString("F000FFC1-0451-4000-B000-000000000000");
    public static UUID CDP_OAD_CHAR_UUID2               = UUID.fromString("F000FFC2-0451-4000-B000-000000000000");

    public static String OAD_Ack = "F1";


    public static List<UUID> DEVICE_INFO_CHARACTERISTICS = new ArrayList<UUID>(Arrays.asList(DEVICE_FIRMWARE_REVISION, DEVICE_HARDWARE_REVISION,DEVICE_INFO_PNP_ID, DEVICE_SERIAL_NUMBER, DEVICE_MANUFACTURER_NAME, DEVICE_MODEL_NUMBER, DEVICE_SOFTWARE_REVISION));

}
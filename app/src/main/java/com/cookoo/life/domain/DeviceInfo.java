package com.cookoo.life.domain;

/**
 * Created by stevi.deter on 3/3/14.
 */
public class DeviceInfo {

    private String deviceName;
    private String deviceAddress;
    private String deviceManufacturer;
    private String softwareRevision;
    private String hardwareRevision;
    private String firmwareRevision;

    private static DeviceInfo deviceInfo;
    private DeviceInfo(){}

    public static DeviceInfo getInstance(){
        if (deviceInfo == null) {deviceInfo = new DeviceInfo();}
        return deviceInfo;
    }

    public static void reset() {
        deviceInfo = new DeviceInfo();
    }


    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public void setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
    }

    public String getSoftwareRevision() {
        return softwareRevision;
    }

    public void setSoftwareRevision(String softwareRevision) {
        this.softwareRevision = softwareRevision;
    }

    public String getHardwareRevision() {
        return hardwareRevision;
    }

    public void setHardwareRevision(String hardwareRevision) {
        this.hardwareRevision = hardwareRevision;
    }

    public String getFirmwareRevision() {
        return firmwareRevision;
    }

    public void setFirmwareRevision(String firmwareRevision) {
        this.firmwareRevision = firmwareRevision;
    }

    public int getDeviceVersion(){
//        if (softwareRevision)
        return 0;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "deviceName='" + deviceName + '\'' +
                ", deviceAddress='" + deviceAddress + '\'' +
                ", deviceManufacturer='" + deviceManufacturer + '\'' +
                ", softwareRevision='" + softwareRevision + '\'' +
                ", hardwareRevision='" + hardwareRevision + '\'' +
                ", firmwareRevision='" + firmwareRevision + '\'' +
                '}';
    }
}

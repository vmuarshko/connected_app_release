package com.cookoo.life.utilities;

import android.util.Log;

public class DevInfo {
    public static final String  ANDROID         =   android.os.Build.VERSION.RELEASE;       //The current development codename, or the string "REL" if this is a release build.
    public static final String  BOARD           =   android.os.Build.BOARD;                 //The name of the underlying board, like "goldfish".    
    public static final String  BOOTLOADER      =   android.os.Build.BOOTLOADER;            //  The system bootloader version number.
    public static final String  BRAND           =   android.os.Build.BRAND;                 //The brand (e.g., carrier) the software is customized for, if any.
    public static final String  CPU_ABI         =   android.os.Build.CPU_ABI;               //The name of the instruction set (CPU type + ABI convention) of native code.
    public static final String  CPU_ABI2        =   android.os.Build.CPU_ABI2;              //  The name of the second instruction set (CPU type + ABI convention) of native code.
    public static final String  DEVICE          =   android.os.Build.DEVICE;                //  The name of the industrial design.
    public static final String  DISPLAY         =   android.os.Build.DISPLAY;               //A build ID string meant for displaying to the user
    public static final String  FINGERPRINT     =   android.os.Build.FINGERPRINT;           //A string that uniquely identifies this build.
    public static final String  HARDWARE        =   android.os.Build.HARDWARE;              //The name of the hardware (from the kernel command line or /proc).
    public static final String  HOST            =   android.os.Build.HOST;  
    public static final String  ID              =   android.os.Build.ID;                    //Either a changelist number, or a label like "M4-rc20".
    public static final String  MANUFACTURER    =   android.os.Build.MANUFACTURER;          //The manufacturer of the product/hardware.
    public static final String  MODEL           =   android.os.Build.MODEL;                 //The end-user-visible name for the end product.
    public static final String  PRODUCT         =   android.os.Build.PRODUCT;               //The name of the overall product.
  //  public static final String  RADIO           =   android.os.Build..RADIO;                 //The radio firmware version number.
    public static final String  TAGS            =   android.os.Build.TAGS;                  //Comma-separated tags describing the build, like "unsigned,debug".
    public static final String  TYPE            =   android.os.Build.TYPE;                  //The type of build, like "user" or "eng".
    public static final String  USER            =   android.os.Build.USER;                  //
    public static boolean DidRun = false;
    
    public static void WriteAllDeviceInformation(){
    	DidRun = true;
    	Log.d("DEVICE_INFO","ANDROID: "+DevInfo.ANDROID);
    	Log.d("DEVICE_INFO","BOARD: "+DevInfo.BOARD);
    	Log.d("DEVICE_INFO","BOOTLOADER: "+DevInfo.BOOTLOADER);
    	Log.d("DEVICE_INFO","BRAND: "+DevInfo.BRAND);
    	Log.d("DEVICE_INFO","CPU_ABI: "+DevInfo.CPU_ABI);
    	Log.d("DEVICE_INFO","CPU_ABI2: "+DevInfo.CPU_ABI2);
    	Log.d("DEVICE_INFO","DEVICE: "+DevInfo.DEVICE);
    	Log.d("DEVICE_INFO","DISPLAY: "+DevInfo.DISPLAY);
    	Log.d("DEVICE_INFO","FINGERPRINT: "+DevInfo.FINGERPRINT);
    	Log.d("DEVICE_INFO","HARDWARE: "+DevInfo.HARDWARE);
    	Log.d("DEVICE_INFO","HOST: "+DevInfo.HOST);
    	Log.d("DEVICE_INFO","ID: "+DevInfo.ID);
    	Log.d("DEVICE_INFO","MANUFACTURER: "+DevInfo.MANUFACTURER);
    	Log.d("DEVICE_INFO","MODEL: "+DevInfo.MODEL);
    	Log.d("DEVICE_INFO","PRODUCT: "+DevInfo.PRODUCT);
    	Log.d("DEVICE_INFO","TAGS: "+DevInfo.TAGS);
    	Log.d("DEVICE_INFO","TYPE: "+DevInfo.TYPE);
    	Log.d("DEVICE_INFO","USER: "+DevInfo.USER);
    }




    public static boolean isHTC() {
      if(MANUFACTURER.equalsIgnoreCase("htc"))
          return true;
      else
          return false;
    }


    public static boolean isSamsung() {
        if(MANUFACTURER.equalsIgnoreCase("samsung"))
            return true;
        else
            return false;
    }


    public static boolean isNote3() {
        if(MODEL.contains("SM-N900"))
            return true;
        else
            return false;
    }
}


package com.cookoo.life.service;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import com.cookoo.life.activity.MainActivity;
import com.cookoo.life.utilities.DevInfo;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import re.notifica.Notificare;

/**
 * Created By: travis.roberson
 * Date: 11/26/13
 * Time: 11:44 AM
 */
public class RegistrationService extends IntentService {


    private final String NAMESPACE = "urn:accountwsdl";
    private final String APP_BASE_URL = "http://members.connectedevice.com/product/cookoolife/application.php";
    private final String CONN_BASE_URL = "http://members.connectedevice.com/product/cookoolife/connected_device.php";
    private final String USER_BASE_URL = "http://members.connectedevice.com/product/cookoolife/user_profile.php";
    private final String MOBILE_BASE_URL = "http://members.connectedevice.com/product/cookoolife/mobile_device.php";
    private final String SOAP_ACTION = "urn:accountwsdl#account";
    private final String METHOD_NAME = "connect";
    private final String TOKEN = "token";
    private final String FIRST = "first";
    private final String LAST = "last";
    private final String EMAIL = "email";
    private final String APP_NAME = "name";
    private final String VERSION = "version";
    private final String FIRST_RUN_DATE = "first_run_date";
    private final String DOWNLOAD_DATE = "download_date";
    private final String IDENTIFIER = "identifier";
    private final String MODEL = "model";
    private final String SOFTWARE = "software";
    private final String CAPACITY = "capacity";
    private final String TIMESTAMP = "timestamp";
    private final String BDADDR = "bdaddr";
    private final String FRIENDLY_NAME = "friendly_name";
    private final String CHAR_NAME = "characteristic_name";
    private final String MANU_NAME = "manufacturer_name";
    private final String MODEL_STRING = "model_string";
    private final String SERIAL_STRING = "serial_number_string";
    private final String HARDWARE_REV_STRING = "hardware_revision_string";
    private final String SOFTWARE_REV_STRING = "software_revision_string";
    private final String FIRMWARE_REV_STRING = "firmware_revision_string";
    private final String PNP_ID = "pnp_id";

    private final String TAG = "RegistrationService";
    public final static String INTENT_REGISTER = "intent_register";
    public final static String INTENT_REGISTER_CONN_DEVICE = "intent_register_conn_device";

    public RegistrationService() {
        super("RegistrationService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent.hasExtra(INTENT_REGISTER)){
            sendUserProfile();
            sendAppData();
            sendMobileData();
        } else if(intent.hasExtra(INTENT_REGISTER_CONN_DEVICE)){
           // sendConnectedData();
        }
    }

    /**
     * Do a soap call with custom request and provided url
     * @param request
     * @param url
     * @return
     * @throws Exception
     */
    private String doSoapCall(SoapObject request, String url) throws Exception {

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(url);
        androidHttpTransport.call(SOAP_ACTION, envelope);
        String response =  envelope.getResponse().toString();
        return response;
    }



    /**
     * Send user profile data
     */
    private void sendUserProfile() {
        try{
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            PropertyInfo prop = new PropertyInfo();

            prop.setName(TOKEN);
            prop.setValue(getToken());
            request.addProperty(prop);

            prop.setName(FIRST);
            prop.setValue(getFirstName());
            request.addProperty(prop);

            prop.setName(LAST);
            prop.setValue(getLastName());
            request.addProperty(prop);

            prop.setName(EMAIL);
            prop.setValue(getUserName());
            request.addProperty(prop);

            String response = doSoapCall(request, USER_BASE_URL);
            Log.d(TAG, "Response for sendUserProfile: " + response.toString());
        }

        catch(Exception e){
            //eat the exception. let's not bug the user about it
            e.printStackTrace();
            Log.d(TAG, "Caught exception in sending User Profile.  Message was: " + e.getMessage());
        }
    }


    /**
     * Send application data
     */
    private void sendAppData(){
        try{
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            PropertyInfo prop = new PropertyInfo();
            prop.setName(EMAIL);
            prop.setValue(getUserName());
            request.addProperty(prop);

            prop.setName(APP_NAME);
            prop.setValue(getFirstName());
            request.addProperty(prop);

            prop.setName(VERSION);
            prop.setValue(getAppVersion());
            request.addProperty(prop);

            prop.setName(DOWNLOAD_DATE);
            prop.setValue(getAppInstallDate());
            request.addProperty(prop);

            prop.setName(FIRST_RUN_DATE);
            prop.setValue(getAppFirstRunDate());
            request.addProperty(prop);

            String response =  doSoapCall(request, APP_BASE_URL);
            Log.d(TAG, "Response for sendAppData: " + response.toString());
        }

        catch(Exception e){
            //eat the exception. let's not bug the user about it
            e.printStackTrace();
            Log.d(TAG, "Caught exception in sending App Data.  Message was: " + e.getMessage());
        }

    }

    /**
     * Send mobile device data
     */
    private void sendMobileData(){

        try{
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            PropertyInfo prop = new PropertyInfo();

            prop.setName(EMAIL);
            prop.setValue(getUserName());
            request.addProperty(prop);

            prop.setName(IDENTIFIER);
            prop.setValue(DevInfo.ID);
            request.addProperty(prop);

            prop.setName(MODEL);
            prop.setValue(DevInfo.MODEL);
            request.addProperty(prop);

            prop.setName(SOFTWARE);
            prop.setValue(DevInfo.ANDROID);
            request.addProperty(prop);

            prop.setName(CAPACITY);
            prop.setValue(getTotalMemory());
            request.addProperty(prop);

            String response =  doSoapCall(request, MOBILE_BASE_URL);
            Log.d(TAG, "Response for sendMobileData: " + response.toString());
        }

        catch(Exception e){
            //eat the exception. let's not bug the user about it
            e.printStackTrace();
            Log.d(TAG, "Caught exception in sending Mobile Data.  Message was: " + e.getMessage());
        }


    }

    /**
     * Send connected device data
     */
    private void sendConnectedData(){

        try{
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            PropertyInfo prop = new PropertyInfo();

            prop.setName(EMAIL);
            prop.setValue(getUserName());
            request.addProperty(prop);

            prop.setName(TIMESTAMP);
            prop.setValue(System.currentTimeMillis());
            request.addProperty(prop);

            prop.setName(BDADDR);
            prop.setValue("");
            request.addProperty(prop);

            prop.setName(FRIENDLY_NAME);
            prop.setValue("");
            request.addProperty(prop);

            prop.setName(CHAR_NAME);
            prop.setValue("");
            request.addProperty(prop);

            prop.setName(MANU_NAME);
            prop.setValue("");
            request.addProperty(prop);

            prop.setName(MODEL_STRING);
            prop.setValue("");
            request.addProperty(prop);

            prop.setName(SERIAL_STRING);
            prop.setValue("");
            request.addProperty(prop);

            prop.setName(HARDWARE_REV_STRING);
            prop.setValue("");
            request.addProperty(prop);

            prop.setName(SOFTWARE_REV_STRING);
            prop.setValue("");
            request.addProperty(prop);

            prop.setName(FIRMWARE_REV_STRING);
            prop.setValue("");
            request.addProperty(prop);

            prop.setName(PNP_ID);
            prop.setValue("");
            request.addProperty(prop);

            String response =  doSoapCall(request, CONN_BASE_URL);
            Log.d(TAG, "Response for sendMobileData: " + response.toString());
        }

        catch(Exception e){
            //eat the exception. let's not bug the user about it
            e.printStackTrace();
            Log.d(TAG, "Caught exception in sending Mobile Data.  Message was: " + e.getMessage());
        }

    }

    private long getTotalMemory() {
        ActivityManager actManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        long totalMemory = memInfo.totalMem;
        return totalMemory;
    }


    private long getAppFirstRunDate() {
        try {
            long first_run = MainActivity.activity.getPackageManager().
                    getPackageInfo("package.name", 0).lastUpdateTime;
            return first_run;  //time in milliseconds
        }   catch (Exception ex){
            ex.printStackTrace();
            return System.currentTimeMillis();  // couldn' get install time return current system time
        }

    }


    private long getAppInstallDate() {
        try {
            long installed = MainActivity.activity.getPackageManager().
                    getPackageInfo("package.name", 0).firstInstallTime;
            return installed;  //time in milliseconds
        }   catch (Exception ex){
            ex.printStackTrace();
            return System.currentTimeMillis();  // couldn't get install time return current system time
        }
    }


    private String getAppVersion() {
        try {
            String versionName = MainActivity.activity.getPackageManager().
                    getPackageInfo(MainActivity.activity.getPackageName(), 0).versionName;
            return versionName;
        }  catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Failed to get version name for package.  Probably was missing in Manifest");
            return "";
        }
    }

    private String getUserName() {
        Context ctx = Notificare.shared().getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences(AccountService.ACCOUNT_PREFS, Context.MODE_PRIVATE);
        String username = prefs.getString(AccountService.USER_NAME, "");
        return username;
    }

    private String getFirstName() {
        Context ctx = Notificare.shared().getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences(AccountService.ACCOUNT_PREFS, Context.MODE_PRIVATE);
        String first = prefs.getString(AccountService.FIRST_NAME, "");
        return first;
    }

    private String getToken() {
        Context ctx = Notificare.shared().getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences(AccountService.ACCOUNT_PREFS, Context.MODE_PRIVATE);
        String password = prefs.getString(AccountService.TOKEN, "");
        return password;
    }

    private String getLastName() {
        Context ctx = Notificare.shared().getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences(AccountService.ACCOUNT_PREFS, Context.MODE_PRIVATE);
        String last = prefs.getString(AccountService.LAST_NAME, "");
        return last;
    }
}

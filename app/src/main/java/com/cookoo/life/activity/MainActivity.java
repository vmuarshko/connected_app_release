package com.cookoo.life.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.*;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.CalendarContract;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.cookoo.life.R;
import com.cookoo.life.domain.DatabaseHelper;
import com.cookoo.life.fragment.CameraFragment;
import com.cookoo.life.notification.NotifCategories;
import com.cookoo.life.service.BluetoothService;
import com.cookoo.life.service.NotifListenerService;
import com.cookoo.life.service.NotificareIntentService;
import com.cookoo.life.utilities.DevInfo;
import com.cookoo.life.utilities.NotificationAdapter;
import com.cookoo.life.utilities.StatusBarCount;
import com.cookoo.life.utilities.UserUtil;
import com.stackmob.android.sdk.common.StackMobAndroid;
import re.notifica.Notificare;

import java.util.*;

public class MainActivity extends FragmentActivity implements View.OnClickListener{

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String NOTIF_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private final static String STACKMOB_PROD_TOKEN = "64eef064-417a-4c5a-9be2-db1e652ee1b7";
    public static final String ACTION_BATTERY_CHANGED = "com.connected.BATTERY_CHANGED";
    public static final String NOTIFICATION_EVENT = "notification_event";
    public static final String CLEAR_NOTIFICATION_DATABASE = "clear_database";
    public final static String NOTIFICATION_LISTENER_EVENT =
            "com.connected.watch.activity.MainActivity.NOTIFICATION_LISTENER_EVENT";
    public static Activity activity;
    private static boolean weStealAudio = true;
    private NotificationReceiver nReceiver;
    public static boolean mConnected = false;
    public static BluetoothService mBluetoothLeService;
    public static String mDeviceAddress;

    private static boolean immediateAlertLevel = false;
    SharedPreferences prefs = null;
    DatabaseHelper db;

    public static String LAST_CONNECTED_DEVICE = "last_connected_device";
    private ImageButton lockButton = null;
    private ImageButton cameraButton = null;
    private ImageButton findmeButton = null;
    private ImageButton settingsButton = null;
    private ImageButton gearButton = null;
    private ImageButton connectButton = null;
    private ImageButton clearAllButton = null;
    private ImageButton deviceConnected = null;
    private TextView noNotifText = null;
    private ListView listView1 = null;
    private ImageButton noNotifImage = null;
    private ProgressBar mProgress;
    private ImageButton demoConnectButton = null;
    private RelativeLayout demoTransparent = null;

    private TimeOut connectionTimer = null;
    private CheckNotifications checkNotifications = null;
    private AddNotificationsTimer addNotificationsTimer = null;
    private final int DEFAULT_WAIT_BEFORE_SEND = 10;

    AlertDialog alertDialog = null;
    AlertDialog oorDialog = null;
    AlertDialog checkNotifDialog = null;

    CameraFragment cameraFragment = null;

    //LEFT COLUMN BUTTON
    private ImageButton fPrivate = null;
    private ImageButton fSocial = null;
    private ImageButton fEmail = null;
    private ImageButton fSchedule = null;
    private ImageButton fCall = null;
    private ImageButton fBattery = null;

    public static boolean bPrivate = false;
    public static boolean bSocial = false;
    public static boolean bEmail = false;
    public static boolean bSchedule = false;
    public static boolean bCall = false;
    public static boolean bBattery = false;
    public static boolean bOutOfRangeAlert = false;

    public static boolean privateEnabled = true;
    public static boolean socialEnabled = true;
    public static boolean emailEnabled = true;
    public static boolean scheduleEnabled = true;
    public static boolean callEnabled = true;
    public static boolean batteryEnabled = true;

    public static boolean showSplash = true;
    public static boolean atmpConn = false;
    public static boolean oBack = false;
    ArrayList<Long> sortedKeys;
    ArrayList filteredKeys;
    AudioManager audioManager;
    MediaPlayer outOfRangeAlertPlayer;
    public static boolean isNotificareLaunched = false;
    private boolean hasVisitedNotifications = false;

    private static NotificationAdapter mAdapter;
    private ArrayList<StatusBarNotification> mValues;
    private BluetoothAdapter mBluetoothAdapter;
    private PowerConnectionReceiver nBattery;
    private int lastBatteryPercentage = 100;

    // TODO move into a proper build script
    private boolean DEVELOPER_MODE = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        DevInfo.WriteAllDeviceInformation();
        prefs = getSharedPreferences(BluetoothService.PREFERENCES_KEY, MODE_PRIVATE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setContentView(R.layout.activity_main);
        activity = this;


        lockButton = (ImageButton) findViewById(R.id.lock_button);
        cameraButton = (ImageButton) findViewById(R.id.camera_button);
        findmeButton = (ImageButton) findViewById(R.id.findme_button);
        settingsButton = (ImageButton) findViewById(R.id.settings_button);
        gearButton = (ImageButton) findViewById(R.id.gear_button);
        connectButton = (ImageButton) findViewById(R.id.device_textview);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        deviceConnected = (ImageButton) findViewById(R.id.device_connected);
        demoConnectButton = (ImageButton) findViewById(R.id.connect_device_demo);
        demoTransparent = (RelativeLayout) findViewById(R.id.transparent_overlay);

        clearAllButton = (ImageButton) findViewById(R.id.clear_all);
        // Left side filters
        fPrivate = (ImageButton) findViewById(R.id.fPrivate);
        fSocial = (ImageButton) findViewById(R.id.fSocial);
        fEmail = (ImageButton) findViewById(R.id.fEmail);
        fSchedule = (ImageButton) findViewById(R.id.fSchedule);
        fCall = (ImageButton) findViewById(R.id.fCall);
        fBattery = (ImageButton) findViewById(R.id.fBattery);
        noNotifText = (TextView) findViewById(R.id.no_notif_text);
        listView1 = (ListView) findViewById(R.id.listView1);
        nReceiver = new NotificationReceiver();
        nBattery = new PowerConnectionReceiver();
        mValues = new ArrayList<StatusBarNotification>();
        filteredKeys = new ArrayList<Long>();

        connectButton.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);
        listView1.setVisibility(View.GONE);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        StackMobAndroid.init(getApplicationContext(), 1, STACKMOB_PROD_TOKEN);

        if(!isNotificareLaunched){
            Notificare.shared().launch(this.getApplication());
            Notificare.shared().setIntentReceiver(NotificareIntentService.class);
            isNotificareLaunched = true;
        }


        fPrivate.setOnClickListener(this);
        fSocial.setOnClickListener(this);
        fEmail.setOnClickListener(this);
        fSchedule.setOnClickListener(this);
        fCall.setOnClickListener(this);
        fBattery.setOnClickListener(this);
        mProgress.setOnClickListener(this);
        demoConnectButton.setOnClickListener(this);
        lockButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        findmeButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        gearButton.setOnClickListener(this);
        connectButton.setOnClickListener(this);
        deviceConnected.setOnClickListener(this);
        mProgress.setOnClickListener(this);
        clearAllButton.setOnClickListener(this);

        if(prefs.contains(LAST_CONNECTED_DEVICE)) {
            mDeviceAddress = prefs.getString(LAST_CONNECTED_DEVICE, "");
            Log.i(TAG,String.format("last connected device address from preferences is %s",mDeviceAddress));
        } else {
            Log.d(TAG,"No last connected device in preferences");
        }

        Intent gattServiceIntent = new Intent(getBaseContext(), BluetoothService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE | BIND_IMPORTANT);

        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_LISTENER_EVENT);
        registerReceiver(nReceiver, filter);


        IntentFilter filterB = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        filterB.addAction(ACTION_BATTERY_CHANGED);
        registerReceiver(nBattery, filterB);
        NotifCategories.SetAlertMasks();

        IntentFilter alertAcknowledgedFilter = new IntentFilter(BluetoothService.ACTION_ALERT_ACKNOWLEDGED);
        registerReceiver(alertAcknowledgedReceiver,alertAcknowledgedFilter);

        registerReceiver(mGattUpdateReceiver, BluetoothService.getGattUpdateIntentFilter());


        addStoredMessages();
        Log.d(TAG, "completed onCreate()");
    }

    /**
     * Add stored messages from database
     */
    public void addStoredMessages(){
        Log.d(TAG, "adding stored messages");
        List<com.cookoo.life.domain.StatusBarNotification> unSeenSbns =
                new ArrayList<com.cookoo.life.domain.StatusBarNotification>();
        List<com.cookoo.life.domain.StatusBarNotification> seenSbns =
                new ArrayList<com.cookoo.life.domain.StatusBarNotification>();
        db = new DatabaseHelper(getApplicationContext());
        try {
            unSeenSbns = db.getAllSbnsBySeen(false);
            seenSbns = db.getAllSbnsBySeen(true);
        }  catch (Exception ex){
            ex.printStackTrace();
        } finally {
            db.closeDB();
        }

        boolean dataChanged = false;
        try  {
            for(com.cookoo.life.domain.StatusBarNotification sbn: unSeenSbns){
                if(!NotifListenerService.NotifCollection.containsKey(sbn.getId())){
                    Log.d(TAG, String.format("adding stored sbn to unseen collection%s", sbn.getPackageName()));
                    Notification no = new Notification();
                    no.tickerText = sbn.getTicker();
                    StatusBarNotification sb = new StatusBarNotification(sbn.getPackageName(), sbn.getPackageName(),
                        sbn.getId().intValue(), sbn.getTag(), 0,0,0,no, android.os.Process.myUserHandle(), sbn.getId());
                    NotifListenerService.NotifCollection.put(sbn.getId(), sb);
                    dataChanged = true;
                }
            }

            for(com.cookoo.life.domain.StatusBarNotification sbn: seenSbns){
                if(!NotifListenerService.SeenCollection.containsKey(sbn.getId())){
                    Log.d(TAG, String.format("adding stored sbn to seen collection%s", sbn.getPackageName()));
                    Notification no = new Notification();
                    no.tickerText = sbn.getTicker();
                    StatusBarNotification sb = new StatusBarNotification(sbn.getPackageName(), sbn.getPackageName(),
                        sbn.getId().intValue(), sbn.getTag(), 0,0,0,no, android.os.Process.myUserHandle(), sbn.getId());
                    NotifListenerService.SeenCollection.put(sbn.getId(), sb);
                    dataChanged = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "unable to reload saved notifications.  probably due to sqlite being too slow");
        }

        if(dataChanged){
            Log.d(TAG, "data has changed");
            Intent i = new  Intent(NOTIFICATION_LISTENER_EVENT);
            i.putExtra("notification_event","");
            sendBroadcast(i);
        }

    }





    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case(R.id.lock_button):
                changeLinkLossAlert();
                break;
            case(R.id.camera_button):
                toCamera();
                break;
            case(R.id.findme_button):
                triggerFindMeAlert();
                break;
            case(R.id.settings_button):
                toDevicePrefs();
                break;
            case(R.id.gear_button):
                toGeneralSettings();
                break;
            case(R.id.device_textview):
                toScanActivity();
                break;
            case(R.id.clear_all):
                clearAll();
                break;
            case(R.id.fPrivate):
                triggerPrivate();
                break;
            case(R.id.fSocial):
                triggerSocial();
                break;
            case(R.id.fEmail):
                triggerEmail();
                break;
            case(R.id.fSchedule):
                triggerSchedule();
                break;
            case(R.id.fCall):
                triggerCall();
                break;
            case(R.id.fBattery):
                triggerBattery();
                break;
            case(R.id.progress):
                toScanActivity();
                break;
            case(R.id.device_connected):
                toScanActivity();
                break;
            case(R.id.connect_device_demo):
                toScanActivity();
                demoTransparent.setVisibility(View.GONE);
                demoConnectButton.setEnabled(false);
                break;
            default:
                Log.d(TAG, String.format("No case found for button id: %d", v.getId()));
                break;
        }
    }



    private void triggerBattery() {
        if (!bBattery){
            bBattery = true;
            fBattery.setBackgroundColor(getResources().getColor(R.color.red));
        }else{
            bBattery = false;
            fBattery.setBackgroundColor(getResources().getColor(R.color.blue));
        }
        filterOnChange();
    }

    private void triggerCall() {
        if (!bCall){
            bCall = true;
            fCall.setBackgroundColor(getResources().getColor(R.color.orange));
        }else{
            bCall = false;
            fCall.setBackgroundColor(getResources().getColor(R.color.blue));
        }
        filterOnChange();
    }


    private void triggerSchedule(){
        if (!bSchedule){
            bSchedule = true;
            fSchedule.setBackgroundColor(getResources().getColor(R.color.tangerine));
        }else{
            bSchedule = false;
            fSchedule.setBackgroundColor(getResources().getColor(R.color.blue));
        }
        filterOnChange();
    }

    private void triggerEmail(){
        if (!bEmail){
            bEmail = true;
            fEmail.setBackgroundColor(getResources().getColor(R.color.yellow));
        }else{
            bEmail = false;
            fEmail.setBackgroundColor(getResources().getColor(R.color.blue));
        }
        filterOnChange();
    }



    private void triggerSocial() {
        if (!bSocial){
            bSocial = true;
            fSocial.setBackgroundColor(getResources().getColor(R.color.green));
        }else{
            bSocial = false;
            fSocial.setBackgroundColor(getResources().getColor(R.color.blue));
        }
        filterOnChange();
    }


    private void triggerPrivate(){
        if (!bPrivate){
            bPrivate = true;
            fPrivate.setBackgroundColor(getResources().getColor(R.color.black));
        }else{
            bPrivate = false;
            fPrivate.setBackgroundColor(getResources().getColor(R.color.blue));
        }
        filterOnChange();
    }

    private void clearAll() {
        mValues.clear();
        new AlertDialog.Builder(MainActivity.activity)
                .setCancelable(false)
                .setTitle(R.string.clear_all_notif)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(NotifListenerService.NOTIFICATION_TO_MAIN);
                        i.putExtra(NotifListenerService.NOTIFICATION_COMMAND, NotifListenerService.NOTIFICATION_CLEARALL);
                        sendBroadcast(i);
                        i = new Intent(NOTIFICATION_LISTENER_EVENT);
                        i.putExtra(NOTIFICATION_EVENT, CLEAR_NOTIFICATION_DATABASE);
                        sendBroadcast(i);
                        if (mAdapter != null){
                            mAdapter.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }).create().show();
    }


    private void toScanActivity() {
        if(atmpConn){
           showAttemptConnDialog();


        } else {
            Intent intent = new Intent(this, DeviceScanActivity.class);
            startActivity(intent);
        }
    }

    private void toDevicePrefs() {
        Intent intent = new Intent(this, DevicePreferenceActivity.class);
        startActivity(intent);
    }


    private void triggerFindMeAlert() {
        if (!immediateAlertLevel) {
            immediateAlertLevel = true;
            findmeButton.setImageResource(R.drawable.findme_blue_icon);
            mBluetoothLeService.triggerImmediateAlertFindMe();
        } else {
            immediateAlertLevel = false;
            findmeButton.setImageResource(R.drawable.findme_icon);
            mBluetoothLeService.cancelImmediateAlert();
        }
    }



    private void changeLinkLossAlert(){
        Log.d(TAG, String.format("Are link loss alerts enabled? %s", bOutOfRangeAlert));
        bOutOfRangeAlert = !bOutOfRangeAlert;
        mBluetoothLeService.writeLinkLossConfigurationTime(bOutOfRangeAlert);
        lockButton.setImageResource(bOutOfRangeAlert ? R.drawable.lock_icon_blue : R.drawable.lock_icon);
        Log.d(TAG, String.format("have switched link loss/out of range to %s", bOutOfRangeAlert));
    }



    private void setLinkLossAlert(){
        Log.d(TAG, String.format("setting oor alerts on watch to %s", bOutOfRangeAlert));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        try {
            unregisterReceiver(mGattUpdateReceiver);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            Log.w(TAG, "failed to unregister mGattUpdateReceiver");
        }

        try {
            unregisterReceiver(nReceiver);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            Log.w(TAG, "failed to unregister nReceiver");
        }

        try {
            unregisterReceiver(nBattery);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            Log.w(TAG, "failed to unregister bBattery receiver");
        }
        try {
            unregisterReceiver(alertAcknowledgedReceiver);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            Log.w(TAG, "failed to unregister alertAcknowledgedReceiver receiver");
        }

        try {
            Intent notifListenerSvc = new Intent(this, NotifListenerService.class);
            stopService(notifListenerSvc);
            this.unbindService(this.mServiceConnection);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            Log.w(TAG, "failed to unregister BluetoothService");
        }
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            Log.d(TAG, "btservice was connected");
            mBluetoothLeService = ((BluetoothService.LocalBinder) service).getService();
            if (mBluetoothLeService.getBluetoothAdapter() == null) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothAdapter = mBluetoothLeService.getBluetoothAdapter();
            mConnected = mBluetoothLeService.isConnected();
            mBluetoothLeService.sendConnectionStateBroadcast();
            if(mDeviceAddress != null && !mDeviceAddress.isEmpty() && !mConnected){
                Intent intent = new  Intent(BluetoothService.ACTION_PERFORM_CONNECT);
                intent.putExtra(BluetoothService.EXTRA_DEVICE_ADDRESS, mDeviceAddress);
                intent.putExtra(BluetoothService.EXTRA_CONNECTION_TYPE,"");
                sendBroadcast(intent);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG,"service is now down ------- ------- ");
            mBluetoothLeService = null;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && oBack) {
            moveTaskToBack(true);
            return true;
        }
        oBack = true;
        return super.onKeyDown(keyCode, event);
       }


    /**
     * Add missed notifications after specified interval
     */
    public class AddNotificationsTimer {
        Timer timer;
        public AddNotificationsTimer(int seconds) {
            timer = new Timer();
            timer.schedule(new AddNotiTask(), seconds*1000);
        }
        class AddNotiTask extends TimerTask {
            public void run() {
                addMissedNotifications();
                timer.cancel(); //Terminate the timer thread
            }
        }
    }



   private boolean checkPhone(StatusBarNotification sbn) {
       boolean check = false;
       if(sbn.getPackageName().equals("com.android.phone")){
           if(sbn.getNotification().icon == NotifCategories.missed_call_icon ||
                   sbn.getNotification().icon == 2130837634 ||
                   sbn.getNotification().icon == NotifCategories.voicemail_icon){
               check = true;
           }
       } else
            check = true;

       return check;
   }




    private void addMissedNotifications() {
        Log.d(TAG, "adding missed notifications");
        HashMap<Integer, StatusBarCount> itemsToSend = new HashMap<Integer, StatusBarCount>();
        try {
            final Map<Long,StatusBarNotification> temp = NotifListenerService.NotifCollection;
            final Map<String, Object> packageMap = NotifCategories.packageAlertMap;

            for (Map.Entry notif : temp.entrySet()) {
                StatusBarNotification sbn = (StatusBarNotification) notif.getValue();
                String packageName = sbn.getPackageName();
                if(packageMap.containsKey(packageName) && checkPhone(sbn) &&
                        !NotifListenerService.SeenCollection.containsKey(sbn.getPostTime())){
                    Integer icon = (Integer) packageMap.get(packageName);
                    if(itemsToSend.containsKey(icon)){
                        Log.d(TAG, "incrementing count");
                        itemsToSend.get(icon).setCount(itemsToSend.get(icon).getCount() + 1);
                    } else {
                        Log.d(TAG, "adding new item");
                        itemsToSend.put(icon,new StatusBarCount(sbn, 1));
                    }
                }
            }
        } catch (Exception ex) {
            Log.w(TAG, "notif collection was likely null");
            ex.printStackTrace();
        }

        if(!itemsToSend.isEmpty()){
            Log.d(TAG, "preparing to add missed notifications to device");
            for(Map.Entry notif : itemsToSend.entrySet()){
                StatusBarCount sbc = (StatusBarCount) notif.getValue();
                pauseBetweenNotiSend();
                NotifListenerService.NotifHandler(sbc.getSbn(), true, sbc.getCount());
            }

        }
    }

     private void pauseBetweenNotiSend(){
         try {
             Thread.sleep(200);
         }  catch(Exception ex){
             ex.printStackTrace();
         }
     }




   private void performOnConnectedConfig() {
       Log.d(TAG,"performOnConnectedConfig");

       if(alertDialog != null){
           alertDialog.cancel();
       }
       if(outOfRangeAlertPlayer != null) {
           outOfRangeAlertPlayer.stop();//in case one happens to be running already
       }
       connectButton.setVisibility(View.GONE);
       mProgress.setVisibility(View.GONE);
       deviceConnected.setVisibility(View.VISIBLE);
       findmeButton.setEnabled(true);
       lockButton.setEnabled(true);
       mConnected = true;
       atmpConn = false;
       setLinkLossAlert();
       lastBatteryPercentage = 100;
       sendBroadcast(new Intent(ACTION_BATTERY_CHANGED));
       Toast.makeText(getApplicationContext(), R.string.success_connect, Toast.LENGTH_SHORT).show();
   }


   private void performOnDisconnectedConfig() {
       Log.d(TAG,"performOnDisconnectedConfig");
       if(bOutOfRangeAlert){
           if(alertDialog != null){
               alertDialog.cancel();
           }
           createOutOfRangeAlert();
           if(outOfRangeAlertPlayer != null) {
               outOfRangeAlertPlayer.stop();//in case one happens to be running already
           }
           outOfRangeAlertPlayer = MediaPlayer.create(activity, R.raw.cookoo);
           outOfRangeAlertPlayer.setLooping(true);
           outOfRangeAlertPlayer.start();
           new AlertIntervalTimer(30);
       }

       connectButton.setVisibility(View.VISIBLE);
       mProgress.setVisibility(View.GONE);
       deviceConnected.setVisibility(View.GONE);
       findmeButton.setEnabled(false);
       lockButton.setEnabled(false);
       atmpConn = false;
       mConnected = false;
   }


    private final BroadcastReceiver alertAcknowledgedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"alert acknowledgement received");
            immediateAlertLevel = false;
            findmeButton.setImageResource(R.drawable.findme_icon);
            NotifListenerService.clearActiveNotifications();
            db = new DatabaseHelper(getApplicationContext());
            for (StatusBarNotification sbn : NotifListenerService.NotifCollection.values()) {
                long id = sbn.getPostTime();
                db.updateSbnAsSeen(sbn);
                NotifListenerService.SeenCollection.put(id, sbn);
            }
            db.closeDB();
            populateList();

        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, String.format("mGattUpdateReceiver received action %s", action));

            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                Log.d(TAG, "found pairing request");
            } else if (BluetoothService.ACTION_GATT_BAD_CONNECTION.equals(action)) {
                Log.d(TAG, "found bad connection");
                if (!mConnected) {
                    createRecoveryDialog();
                }
            } else if (BluetoothService.ACTION_CYCLE_BLUETOOTH.equals(action)) {
                Log.d(TAG, "cycle bluetooth");
                Toast.makeText(getApplicationContext(), R.string.cycling_bluetooth, Toast.LENGTH_SHORT).show();

            } else if (BluetoothService.ACTION_GATT_CONNECTING.equals(action)) {
                Log.d(TAG, "device is connecting");
                Toast.makeText(getApplicationContext(), R.string.attempting_connection, Toast.LENGTH_SHORT).show();
                connectButton.setVisibility(View.GONE);
                mProgress.setVisibility(View.VISIBLE);
                deviceConnected.setVisibility(View.GONE);
                atmpConn = true;

            } else if (BluetoothService.ACTION_GATT_BAD_LINK_KEY.equals(action)) {
                Log.d(TAG, "watch has bad link key");
                createWatchPairingDialog();
                connectButton.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
                deviceConnected.setVisibility(View.GONE);
                mConnected = false;
                atmpConn = false;
            } else if (BluetoothService.ACTION_GATT_DEVICE_NOT_FOUND.equals(action)) {
                Log.d(TAG, "device not found");
                Toast.makeText(getApplicationContext(), R.string.device_not_found, Toast.LENGTH_SHORT).show();
                connectButton.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
                deviceConnected.setVisibility(View.GONE);
                mConnected = false;
                atmpConn = false;
            } else if (BluetoothService.ACTION_DEVICE_OOR.equals(action)) {
                Log.d(TAG, "device does not appear to be in range");
                connectButton.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
                deviceConnected.setVisibility(View.GONE);
                mConnected = false;
                atmpConn = false;
                createOORDialog();
            } else if (BluetoothService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "Service was connected");
                performOnConnectedConfig();
            } else if (BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "Service was disconnected");
                performOnDisconnectedConfig();
            } else if (BluetoothService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "Services are discovered...");
                Toast.makeText(getApplicationContext(), R.string.set_up_notifications, Toast.LENGTH_SHORT).show();
            } else if (BluetoothService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "data is available");
                // TODO refactor to use bytes instead of stringified versions
                String cdpMessage = intent.getStringExtra(BluetoothService.EXTRA_DATA);

                Log.d(TAG, String.format("STRING EXTRA?!??!!:  %s", cdpMessage));
                if ((cdpMessage.contains("C3 04"))) {
                    // performOnConnectedConfig();

                } else if (cdpMessage.contains("03 C3")) { //we now know we are fully connected
                    if (addNotificationsTimer != null) {
                        addNotificationsTimer.timer.cancel();
                        addNotificationsTimer.timer.purge();
                        addNotificationsTimer = null;

                    }
                    addNotificationsTimer = new AddNotificationsTimer(DEFAULT_WAIT_BEFORE_SEND);
                } else if (cdpMessage.contains("C0 02")) {
                    Log.d(TAG, "going to write full battery to watch");
                    mBluetoothLeService.cancelBatteryAlert();

                    immediateAlertLevel = false;
                    findmeButton.setImageResource(R.drawable.findme_icon);
                    Log.d(TAG, "going to turn off immediate alert");
                    mBluetoothLeService.cancelImmediateAlert();
                } else if (cdpMessage.contains("C0 01")) {

                    if (CameraFragment.CameraIsActive) {
                        CameraFragment.forceButtonPress();
                    } else if (audioManager.isMusicActive() || !weStealAudio) {
                        if (weStealAudio && audioManager.isMusicActive() && (NotifCategories.
                                EnabledCategoryBitMask & NotifCategories.MUSIC_CONTROL) >= 1) {
                            try {
                                weStealAudio = false;
                                audioManager.requestAudioFocus(audioListener, AudioManager.
                                        STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                            } catch (Exception e) {
                                Log.d(TAG, String.format("We had critical error when requestion 'Audio Focus' %s", e));
                            }
                        } else {
                            if (!weStealAudio) {
                                weStealAudio = true;
                                audioManager.abandonAudioFocus(audioListener);
                            }
                        }
                    }
                    Log.d(TAG, "CMD button push");
                }
                //DO NOT CHANGE THE ORDERING OF THIS BEFORE ALL ELSE, C1 stands for "alert ACK",
                // but only want to call find me if C1 00 00 meaning, a find me and nothing else is ocurring.
                // C1 00 10
                else if (cdpMessage.contains("C1")) {

                    immediateAlertLevel = false;
                    findmeButton.setImageResource(R.drawable.findme_icon);

                    NotifListenerService.clearAllNotifications();
                    db = new DatabaseHelper(getApplicationContext());
                    for (StatusBarNotification sbn : NotifListenerService.NotifCollection.values()) {
                        long id = sbn.getPostTime();
                        db.updateSbnAsSeen(sbn);
                        NotifListenerService.SeenCollection.put(id, sbn);
                    }
                    db.closeDB();
                    populateList();
                }
            }
        }
    };


    OnAudioFocusChangeListener audioListener = new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            // code to execute
        }
    };



    public static void TurnOn_Batt(int crit_low_off){

        if(!BluetoothService.inServiceMode)
            switch(crit_low_off){
                case 0:{
                    mBluetoothLeService.cancelBatteryAlert();
                    break;
                }
                case 1:{
                    mBluetoothLeService.sendLowBatteryAlert();
                    break;
                }
                case 2:{
                    mBluetoothLeService.sendCriticalBatteryAlert();
                    break;
                }

            }
    }

    public static void TurnOn_IMA(boolean on,String x){
        if (mBluetoothLeService == null) {
            Log.e(TAG,"bluetooth LE service is null");
            return;
        }
        if(on){
            immediateAlertLevel = true;
            if(x.equals("")){
                mBluetoothLeService.triggerImmediateAlertFindMe();
            }
            else{
                mBluetoothLeService.triggerImmediateAlertOther();
            }
        }
        else{
            immediateAlertLevel = false;
            mBluetoothLeService.cancelImmediateAlert();
        }

    }


    /**
     * Fire off to camera mode fragment
     */
    protected void toCamera() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        if(cameraFragment == null)
            cameraFragment = new CameraFragment();

        if(!cameraFragment.isVisible()){
            ft.replace(R.id.mainActivityLayout, cameraFragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }


    /**
     * Fire off to demo mode fragment
     */
    protected void toGeneralSettings() {
        Intent intent = new Intent(MainActivity.activity, GeneralSettingsActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onResume() {

        super.onResume();
        Log.d(TAG, "onResume");
        if(prefs.contains(LAST_CONNECTED_DEVICE)) {
            mDeviceAddress = prefs.getString(LAST_CONNECTED_DEVICE, "");
        }

        if(prefs.getBoolean("firstrun", true) && !NotifListenerService.isNotificationAccessEnabled){
            demoTransparent.setVisibility(View.VISIBLE);
            demoConnectButton.setEnabled(true);
            db = new DatabaseHelper(getApplicationContext());
            db.onUpgrade(db.getWritableDatabase(), 1, 2);
            createWelcomeDialog();

        } else {
                if(!prefs.getBoolean("firstrun", true))
                   if(checkNotifications != null){
                       checkNotifications.timer.cancel();
                       checkNotifications.timer.purge();
                   }
                   checkNotifications = new CheckNotifications(60);
        }

       if(!UserUtil.isUserSignedIn()){
           if(!hasVisitedNotifications && showSplash){
                Intent intent = new Intent(this, SplashActivity.class);
                startActivity(intent);
               }
       }

        oBack = true;
        NotifCategories.SetAlertMasks();
        markAsRead();

        if(!mConnected){
            findmeButton.setEnabled(false);
            lockButton.setEnabled(false);
        }

        if(atmpConn) {
           mProgress.setVisibility(View.VISIBLE);
           deviceConnected.setVisibility(View.GONE);
           connectButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        showSplash = true;
        super.onStop();
        Log.d(TAG, "onStop() was called");
    }



    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "mainActivity onPause()");
        super.onPause();
    }


    /**
     * Mark messages as read based on PreferenceManager configuration
     */
    public void markAsRead() {
        Log.d(TAG, "marking as read");
        try {
            sortedKeys = new ArrayList<Long>(NotifListenerService.NotifCollection.keySet());
            Collections.sort(sortedKeys);
            Collections.reverse(sortedKeys);
        }   catch(NullPointerException ex ) {
            ex.printStackTrace();
        }
        boolean dataChanged = false;
        try {
            db = new DatabaseHelper(getApplicationContext());
            for (Long sortedKey : sortedKeys) {
                boolean remove = false;
                for (int i = 0; i < NotifCategories.AndroidNotifCategories.length; i++) {

                    if (String.valueOf(NotifListenerService.NotifCollection.get(sortedKey)
                            .getPackageName()).equals(NotifCategories.AndroidNotifCategories[i])) {
                        int notifColor = NotifCategories.AndroidColors[i];

                        if (!privateEnabled && notifColor == R.color.black) {
                            remove = true;

                        } else if (!socialEnabled && notifColor == R.color.green) {
                            remove = true;

                        } else if (!emailEnabled && notifColor == R.color.yellow) {
                            remove = true;

                        } else if (!scheduleEnabled && notifColor == R.color.tangerine) {
                            remove = true;

                        } else if (!callEnabled && notifColor == R.color.orange) {
                            remove = true;
                        }  else if(!batteryEnabled && notifColor == R.color.red) {
                            remove = true;
                        }

                        Log.d(TAG, String.format("remove was %s", remove));
                        if(remove){
                            dataChanged = true;
                            Log.d(TAG, "removing a notification");
                            NotifListenerService.NotifHandler(NotifListenerService.NotifCollection.get(sortedKey), false, 0);
                            db.updateSbnAsSeen(NotifListenerService.NotifCollection.get(sortedKey));
                            NotifListenerService.SeenCollection.put(NotifListenerService.NotifCollection.
                                    get(sortedKey).getPostTime(), NotifListenerService.NotifCollection.get(sortedKey));
                            NotifListenerService.NotifCollection.remove(NotifListenerService.NotifCollection.
                                    get(sortedKey));
                        }
                    }


                remove = false;
            }

        }
        db.closeDB();
        } catch(Exception ex){
            ex.printStackTrace();
        }
        if(dataChanged){
            Log.d(TAG, "data was changed");
            Intent i = new  Intent(NOTIFICATION_LISTENER_EVENT);
            i.putExtra("notification_event","");
            sendBroadcast(i);
        }
    }




    /**
     * Filter notifications when a button is pressed
     */
    public void filterOnChange(){

        try {
            mValues.clear();
            sortedKeys = new ArrayList<Long>(NotifListenerService.NotifCollection.keySet());
            Collections.sort(sortedKeys);
            Collections.reverse(sortedKeys);
        }   catch(NullPointerException ex ) {
                ex.printStackTrace();
        }


        // Add Previously filtered items to the seen list - then wipe it.
        if( ! filteredKeys.isEmpty() ){
            try {
                db = new DatabaseHelper(getApplicationContext());
                for (Object filteredKey : filteredKeys) {
                    NotifListenerService.SeenCollection.put(NotifListenerService.NotifCollection.
                            get(filteredKey).getPostTime(), NotifListenerService.NotifCollection.get(filteredKey));
                    db.updateSbnAsSeen(NotifListenerService.NotifCollection.get(filteredKey));
                    NotifListenerService.NotifHandler(NotifListenerService.NotifCollection.get(filteredKey), false, 0);
                }
            } catch (NullPointerException ex){
                ex.printStackTrace();
                // no notification available
            } finally {
                db.closeDB();
                filteredKeys.clear();
            }

        }

		/*
		 * If there is no filter active, then the user has cleared all filters, make all notifications available for viewing.
		 */
        if ( ! ( bPrivate || bSocial || bEmail || bSchedule || bCall || bBattery ) ){
            try {
                for (Long sortedKey : sortedKeys) {
                    if (NotifListenerService.NotifCollection.get(sortedKey).getPackageName().
                            equals("com.android.phone") && NotifListenerService.NotifCollection.
                            get(sortedKey).getNotification().icon == 2130837640) {

                    }   else {
                        mValues.add(NotifListenerService.NotifCollection.get(sortedKey));
                    }
                }

            } catch(Exception ex){
                ex.printStackTrace();
                //notif collection didn't contain what we hoped it would
            }
        }

		/*
		 * Apply the filter and populate the notification list - once done add all
		 * filtered values to the collection ready to be added to the SEEN collection
		 */

        try {
            for (Long sortedKey : sortedKeys) {
                for (int i = 0; i < NotifCategories.AndroidNotifCategories.length; i++) {

                if (String.valueOf(NotifListenerService.NotifCollection.get(sortedKey)
                        .getPackageName()).equals(NotifCategories.AndroidNotifCategories[i])) {
                    int notifColor = NotifCategories.AndroidColors[i];
                    boolean add = false;

                    if (bPrivate && notifColor == R.color.black) {
                        add = true;

                    } else if (bSocial && notifColor == R.color.green) {
                        add = true;

                    } else if (bEmail && notifColor == R.color.yellow) {
                        add = true;

                    } else if (bSchedule && notifColor == R.color.tangerine) {
                        add = true;

                    } else if (bCall && notifColor == R.color.orange) {
                        add = true;
                    }  else if(bBattery && notifColor == R.color.red ) {
                        add = true;
                    }

                    if (add) {

                        if (NotifListenerService.NotifCollection.get(sortedKey).
                                getPackageName().equals("com.android.phone") && NotifListenerService.
                                NotifCollection.get(sortedKey).getNotification().icon == 2130837640) {
                        } else {
                            Log.d(TAG, String.format("package passed: %s", NotifListenerService.
                                    NotifCollection.get(sortedKey).getPackageName()));
                            mValues.add(NotifListenerService.NotifCollection.get(sortedKey));
                            filteredKeys.add((Long) sortedKey);
                        }
                      }
                   }
                 }
            }
        }  catch(Exception ex){
            ex.printStackTrace();
        }
        Log.d(TAG, String.format("mValues:  %s", mValues));
        populateList();
    }



    /**
     * Timer to set by default how long we sound the alarm
     * for if user doesn't dismiss
     */
    public class AlertIntervalTimer {
        Timer timer;
        public AlertIntervalTimer(int seconds) {
            timer = new Timer();
            timer.schedule(new AlertIntervalTask(), seconds*1000);
        }
        class AlertIntervalTask extends TimerTask {
            public void run() {
                outOfRangeAlertPlayer.stop();
                timer.cancel(); //Terminate the timer thread
            }
        }
    }


    /**
     * Start a timer task for bluetooth connectivity
     * If we still can't connect after a specified interval
     * show a recovery dialog
     */
    public class TimeOut {
        Timer timer;

        public TimeOut(int seconds) {
            timer = new Timer();
            timer.schedule(new TimeOutTask(), seconds*1000);
        }
        class TimeOutTask extends TimerTask {
            public void run() {
                createRecoveryDialog();
                timer.cancel(); //Terminate the timer thread
            }
        }

    }



    /**
     * Start a timer task for bluetooth connectivity
     * If we still can't connect after a specified interval
     * show a recovery dialog
     */
    public class CheckNotifications {
        Timer timer;

        public CheckNotifications(int seconds) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new CheckNotiTask(), seconds*1000, seconds*1000);
        }
        class CheckNotiTask extends TimerTask {
            public void run() {
                restartNotificationAccess();
            }
        }

    }


    /**
     * Notification event/bar receiver
     */
    public class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "got a new notification");
            String temp = intent.getStringExtra(("notification_event"));
            mValues.clear();

            if (temp != null) {
                if (NotifListenerService.NotifCollection.size() == 0) {
                    Log.d(TAG, "notif collection was empty");
                    listView1.setVisibility(View.GONE);
                    noNotifText.setVisibility(View.VISIBLE);
                    noNotifImage.setVisibility(View.VISIBLE);

                } else {
                    listView1.setVisibility(View.VISIBLE);
                    noNotifText.setVisibility(View.GONE);
                    noNotifImage.setVisibility(View.GONE);
                    sortedKeys = new ArrayList<Long>(NotifListenerService.NotifCollection.keySet());
                    Collections.sort(sortedKeys);
                    Collections.reverse(sortedKeys);
                    Log.d(TAG, "preparing to get a new database helper");
                    db = new DatabaseHelper(getApplicationContext());
                    for (Long sortedKey : sortedKeys) {

                        if (NotifListenerService.NotifCollection.get(sortedKey).
                                getPackageName().equals("com.android.phone") && NotifListenerService.
                                NotifCollection.get(sortedKey).getNotification().icon == 2130837640) {

                        } else {
                            if(NotifListenerService.NotifCollection.
                                    get(sortedKey).getPackageName().equals("com.android.email")){
                                String notifString = NotifListenerService.NotifCollection.get(sortedKey).toString();
                                if(notifString.indexOf("contentTitle=") != -1 &&
                                        notifString.indexOf("contentText=") != -1){
                                    StringBuilder sb = new StringBuilder();
                                    int start = notifString.indexOf("contentTitle=") + 13;
                                    int end =  notifString.indexOf("contentText=");
                                    while(start != end){
                                        sb.append(notifString.charAt(start));
                                        start++;
                                    }
                                    if(!sb.toString().trim().equals("N"))
                                        NotifListenerService.NotifCollection.get(sortedKey).
                                                getNotification().tickerText = sb.toString();
                                }
                            }
                            mValues.add(NotifListenerService.NotifCollection.get(sortedKey));
                            if(NotifListenerService.NotifCollection.containsKey(sortedKey) &&
                                    db.getSbn(NotifListenerService.NotifCollection.get(sortedKey).getPostTime()).getId() == null){
                                Log.d(TAG, "creating new db entry");
                                db.createSbn(NotifListenerService.NotifCollection.get(sortedKey));

                            }
                        }
                        db.closeDB();
                        Log.d(TAG, String.format("mValues:  %s", mValues));

                        if (  bPrivate || bSocial || bEmail || bSchedule || bCall || bBattery ){
                            filterOnChange();
                        }else{
                            populateList();
                        }

                    }
                }
            }
        }
    }

    private void populateList() {
        Log.d(TAG, String.format("size added: %d", mValues.size()));
        mAdapter = new NotificationAdapter(getBaseContext(),
                R.layout.listview_item_row, mValues);
        listView1.setAdapter(mAdapter);
        listView1.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.d(TAG, String.format("Items %d click on : %sadding to seen", arg2, mValues.get(arg2).getPackageName()));
                checkToGiveFurtherAction(mValues.get(arg2));
                NotifListenerService.NotifHandler(NotifListenerService.NotifCollection.get(mValues.get(arg2).getPostTime()), false, 0);
                db = new DatabaseHelper(getApplicationContext());
                db.updateSbnAsSeen(mValues.get(arg2));
                db.closeDB();
                NotifListenerService.SeenCollection.put(mValues.get(arg2).getPostTime(), mValues.get(arg2));
                populateList();

            }

        });

    }



    /**
     * See if we can offer any further action when a particular
     * notification is clicked.  ex. fire up a dialer
     * @param sbn
     */
    private void checkToGiveFurtherAction(StatusBarNotification sbn){
        Log.d(TAG, String.format("package name was: %s", sbn.getPackageName()));
        try {
            if(sbn.getPackageName().equals("com.android.phone") && isTelephonyEnabled()){

             //TODO: At some point wire up phone


            }  else if(sbn.getPackageName().equals("com.skype.raider")){
                try {
                    this.getApplicationContext().getPackageManager().getPackageInfo("com.skype.raider", 0);
                    Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.skype.raider");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.d(TAG, "Skype not installed");
                }

            }  else if(sbn.getPackageName().equals("com.maaii.maaii")){
                try {
                    this.getApplicationContext().getPackageManager().getPackageInfo("com.maaii.maaii", 0);
                    Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.maaii.maaii");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.d(TAG, "Maaii not installed");
                }

            }else if(sbn.getPackageName().equals("com.android.calendar") ||
                    sbn.getPackageName().equals("com.htc.calendar")){

                Calendar cal = new GregorianCalendar();
                cal.setTime(new Date());
                long time = cal.getTime().getTime();
                Uri.Builder builder =
                        CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                builder.appendPath(Long.toString(time));
                Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
                startActivity(intent);

            }  else if(sbn.getPackageName().equals("com.facebook.katana")){
                try {
                    this.getApplicationContext().getPackageManager().getPackageInfo("com.facebook.katana", 0);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://feed"));
                    startActivity(intent);
                } catch (Exception e) {
                    Log.d(TAG, "Facebook not installed");
                }
            } else if(sbn.getPackageName().equals("com.google.android.talk")){
                try {
                    this.getApplicationContext().getPackageManager().getPackageInfo("com.google.android.talk", 0);
                    Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.google.android.talk");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.d(TAG, "Hangouts not installed");
                }
            } else if(sbn.getPackageName().equals("com.google.android.gm")){
                try {
                    this.getApplicationContext().getPackageManager().getPackageInfo("com.google.android.gm", 0);
                    Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.d(TAG, "Gmail not installed");
                }
            } else if(sbn.getPackageName().equals("com.android.email")){
                try {
                    this.getApplicationContext().getPackageManager().getPackageInfo("com.android.email", 0);
                    Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.android.email");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.d(TAG, "Email client not installed");
                }
            }  else if(sbn.getPackageName().equals("com.android.mms")){
                try {
                    Log.d(TAG, "launching mms");
                    this.getApplicationContext().getPackageManager().getPackageInfo("com.android.mms", 0);
                    Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.android.mms");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.d(TAG, "mms client not installed");
                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            //eat exception so it doesn't interfere with anything
        }
    }

    private boolean isTelephonyEnabled(){
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        return tm != null && tm.getSimState()==TelephonyManager.SIM_STATE_READY;
    }


    /**
     * convert battery percentage to hex string and write
     * characteristic to the watch
     * @param batteryPercent
     */
    public static void batteryStatusToDevice(int batteryPercent){
        if (mBluetoothLeService != null) {
            mBluetoothLeService.sendBatteryAlert((byte) batteryPercent);
        } else {
            Log.d(TAG,"no BLE service so battery status not sent to device");
        }
    }

    public class PowerConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.d(TAG,"onReceive action battery changed");
                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = context.registerReceiver(null, ifilter);
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level / (float)scale;
                int watchConversionPercentage = (int) Math.round(batteryPct*100);
                Log.d(TAG, String.format("battery level is to %d%%", watchConversionPercentage));
                if (watchConversionPercentage == lastBatteryPercentage){
                    Log.d(TAG,"battery hasn't changed that much don't send an update");
                    return;
                }
                lastBatteryPercentage = watchConversionPercentage;
                Log.d(TAG, String.format("battery changed to %d%%", lastBatteryPercentage));

                if(batteryPct <= .1) {

                    Notification no = new Notification();
                    no.tickerText = "Battery is Critical";
                    StatusBarNotification sb = new StatusBarNotification("com.connected.critical.battery",
                            "com.connected.critical.battery",
                            System.identityHashCode(System.currentTimeMillis()), "" +
                            "", 0,0,0,no, android.os.Process.myUserHandle(), System.currentTimeMillis());

                    boolean add = true;
                    if(NotifListenerService.NotifCollection.size() > 0) {
                        for (StatusBarNotification statusBar : NotifListenerService.NotifCollection.values()) {
                            if(statusBar.getPackageName().contains("critical.battery")){
                                add = false;
                                break;
                            }
                        }
                    }
                    if(add)  {
                        batteryStatusToDevice(watchConversionPercentage);
                        NotifListenerService.NotifCollection.put(sb.getPostTime(), sb);
                        Intent i = new  Intent(NOTIFICATION_LISTENER_EVENT);
                        i.putExtra("notification_event","");
                        sendBroadcast(i);
                    }
                } else if ( batteryPct <= .2 ){

                    Notification no = new Notification();
                    no.tickerText = "Battery is Low";
                    StatusBarNotification sb = new StatusBarNotification("com.connected.low.battery",
                            "com.connected.low.battery",
                            System.identityHashCode(System.currentTimeMillis()), "" +
                            "", 0,0,0,no, android.os.Process.myUserHandle(), System.currentTimeMillis());
                    boolean add = true;
                    if(NotifListenerService.NotifCollection.size() > 0) {
                        for (StatusBarNotification statusBar : NotifListenerService.NotifCollection.values()) {
                            if(statusBar.getPackageName().contains("low.battery")){
                                add = false;
                                break;
                            }
                        }
                    }
                    if(add)  {
                        batteryStatusToDevice(watchConversionPercentage);
                        NotifListenerService.NotifCollection.put(sb.getPostTime(), sb);
                        Intent i = new  Intent(NOTIFICATION_LISTENER_EVENT);
                        i.putExtra("notification_event","");
                        sendBroadcast(i);
                    }
                } else {
                    Log.d(TAG, "sending normal battery status to watch");
                    batteryStatusToDevice(watchConversionPercentage);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * Create a welcome dialog on first run
     */
    private void createWelcomeDialog() {
        Log.d(TAG, "createWelcomeDialog()");
        prefs.edit().putBoolean("firstrun", false).commit();

        boolean pref = prefs.getBoolean("firstrun", true);
        Log.d(TAG, String.format("firstrun was %s", pref));

        new AlertDialog.Builder(MainActivity.activity)
                .setTitle(R.string.welcome)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(MainActivity.activity)
                                .setCancelable(false)
                                .setTitle(R.string.notifications)
                                .setMessage(R.string.notification_perm)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(
                                                NOTIF_LISTENER_SETTINGS);
                                        Log.d(TAG, String.format("intent?%s", intent));
                                        startActivity(intent);
                                        hasVisitedNotifications = true;
                                    }
                                }).create().show();
                    }
                }).create().show();
    }


    /**
     * Create a welcome dialog on first run
     */
    private void restartNotificationAccess() {
        Log.d(TAG, "restartNotificationAccess()");
        if(!NotifListenerService.isNotificationAccessEnabled){
            this.runOnUiThread(new Runnable() {
                public void run() {

                AlertDialog.Builder restartDialog = new AlertDialog.Builder(MainActivity.activity);
                restartDialog.setCancelable(false);
                restartDialog.setTitle(R.string.notifications_stopped);
                restartDialog.setMessage(R.string.notification_recheck);
                restartDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(NOTIF_LISTENER_SETTINGS);
                        Log.d(TAG, String.format("intent?%s", intent));
                        startActivity(intent);
                    }
                });
                    if(checkNotifDialog == null || !checkNotifDialog.isShowing()){
                        checkNotifDialog = restartDialog.create();
                        checkNotifDialog.show();
                    }

                }
            });
        }


   }




    /**
     * Create a recover dialog for when watch pairing times out due to
     * typically bluetooth issues.
     */
    private void createRecoveryDialog() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder recoveryDialog = new AlertDialog.Builder(activity);
                recoveryDialog.setTitle(R.string.problems_conn);
                recoveryDialog.setCancelable(false);
                recoveryDialog.setPositiveButton(R.string.restart_bluetooth,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                    mProgress.setVisibility(View.GONE);
                    connectButton.setVisibility(View.VISIBLE);
                    if(connectionTimer != null)
                        connectionTimer.timer.cancel();
               mBluetoothAdapter.disable();
               BluetoothService.canToggleBTOn = true;

                }
        });
                recoveryDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        StringBuilder sb = new StringBuilder();
        Resources res = getResources();
        sb.append(res.getString(R.string.cycle_bluetooth));
        recoveryDialog.setMessage(sb.toString());
        if(alertDialog == null || !alertDialog.isShowing()){
            alertDialog = recoveryDialog.create();
            alertDialog.show();
        }
           }
        });
    }


    private void createOutOfRangeAlert() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder rangeDialog = new AlertDialog.Builder(activity);
                rangeDialog.setTitle(R.string.out_of_range);
                rangeDialog.setCancelable(false);
                rangeDialog.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        if(outOfRangeAlertPlayer != null)
                            outOfRangeAlertPlayer.stop();
                    }
                });

               rangeDialog.setMessage(R.string.disconnected);
               if(oorDialog == null || !oorDialog.isShowing()){
                oorDialog = rangeDialog.create();
                oorDialog.show();
               }
            }
        });

    }

    private void showAttemptConnDialog() {
        Log.d(TAG, "already attempting a connection");

        new AlertDialog.Builder(MainActivity.activity)
                .setCancelable(false)
                .setTitle(R.string.old_firmware_warn)
                .setMessage(R.string.attempting_conn)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        atmpConn = false;
                        connectButton.setVisibility(View.VISIBLE);
                        mProgress.setVisibility(View.GONE);
                        deviceConnected.setVisibility(View.GONE);
                        Intent intent = new Intent(BluetoothService.ACTION_CANCEL_CONNECTION);
                        sendBroadcast(intent);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "user clicked");
                    }
                }).create().show();

    }

    private void createOORDialog() {
        Log.d(TAG, "device appears to be OOR");
        new AlertDialog.Builder(MainActivity.activity)
                .setCancelable(false)
                .setTitle(R.string.device_not_found)
                .setMessage(R.string.device_is_oor)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();

    }

    private void createWatchPairingDialog() {
        Log.d(TAG, "device appears to have bad link key");
        new AlertDialog.Builder(MainActivity.activity)
                .setCancelable(false)
                .setTitle(R.string.out_of_sync)
                .setMessage(R.string.device_is_oos)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();

    }




}



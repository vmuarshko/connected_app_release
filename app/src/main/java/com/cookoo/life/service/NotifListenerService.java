package com.cookoo.life.service;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.cookoo.life.activity.MainActivity;
import com.cookoo.life.domain.AlertCategories;
import com.cookoo.life.notification.NotifCategories;

import java.util.*;


public class NotifListenerService extends NotificationListenerService {
    private static String TAG = NotifListenerService.class.getSimpleName();
    public static Map<Long, StatusBarNotification> NotifCollection = new HashMap<Long, StatusBarNotification>();
    public static Map<Long, StatusBarNotification> SeenCollection = new HashMap<Long, StatusBarNotification>();
    public final static String NOTIFICATION_TO_MAIN =
            "com.connected.watch.service.NotifListenerService.NLServiceReceiver_Event";
    public static final String NOTIFICATION_COMMAND = "command";
    public static final String NOTIFICATION_CLEARALL = "clearall";
    private NLServiceReceiver notifRec;
    public static volatile boolean acceptTelephony = true;
    private static Long lastPhoneCallTime = new Long(0);
    private static Long lastSkypeAlertTime = new Long(0);
    private static final Long DEFAULT_TIME_DIFF = new Long(3000);
    public static boolean isNotificationAccessEnabled = false;
    public static PhoneAlert alert;
    private static AudioManager am = null;
    private static int ringerMode = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Notification service is now bound");
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ringerMode = am.getRingerMode();
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        notifRec = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_TO_MAIN);
        registerReceiver(notifRec, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent mIntent) {
        Log.d(TAG, String.format("onBind(%s)", mIntent.getAction()));
        NotifCollection = new HashMap<Long, StatusBarNotification>();
        SeenCollection = new HashMap<Long, StatusBarNotification>();
        IBinder mIBinder = super.onBind(mIntent);
        isNotificationAccessEnabled = true;
        return mIBinder;
    }

    @Override
    public boolean onUnbind(Intent mIntent) {
        Log.d(TAG, "onUnBind()");
        boolean mOnUnbind = super.onUnbind(mIntent);
        isNotificationAccessEnabled = false;
        return mOnUnbind;
    }


    /**
     * if there's a phone call, send an immediate alert to the phone
     */
    private PhoneStateListener mPhoneListener = new PhoneStateListener() {


        public void onCallStateChanged(int state, String incomingNumber) {
            Log.d("mPhoneListener", String.format("Accept telephone was %s", acceptTelephony));
            Log.d("mPhoneListener", String.format("call state changed to: %d incoming num: %d", state, incomingNumber.length()));
            acceptTelephony = false;
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d("mPhoneListener", "state ringing");
                    try {
                        ringerMode = am.getRingerMode();
                        if (MainActivity.callEnabled && incomingNumber.length() > 7) {
                            Log.d(TAG, "setting new phone alert");
                            alert = new PhoneAlert(1);
                            acceptTelephony = true;
                        } else {
                            acceptTelephony = false;
                        }
                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                        Log.w(TAG, "bailing out of phone call alert");
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d("mPhoneListener", "state offhook");
                    cancelAlert();
                    am.setRingerMode(ringerMode);
                    acceptTelephony = false;
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d("mPhoneListener", "state idle");
                    cancelAlert();
                    am.setRingerMode(ringerMode);
                    acceptTelephony = true;
                    break;
                default:
                    Log.d(TAG, "Unknown phone state=" + state);
                    acceptTelephony = false;
                    break;

            }
        }
    };


    public static void cancelAlert() {
        if (alert != null) {
            alert.timer.cancel();
            alert.timer.purge();
            alert = null;
        }

        if (BluetoothService.getInstance() != null) {
            BluetoothService.getInstance().cancelImmediateAlert();
            BluetoothService.getInstance().sendUnreadAlert(AlertCategories.CALL, 0);
        }
        addAllToSeen("com.android.phone");
    }


    public class PhoneAlert {
        Timer timer;

        public PhoneAlert(int seconds) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new PhoneAlertTask(), 1, seconds * 1000);
        }

        class PhoneAlertTask extends TimerTask {
            public void run() {
                if (MainActivity.mBluetoothLeService != null) {
                    Log.d(TAG, "sending simple alert");
                    MainActivity.mBluetoothLeService.triggerImmediateAlertOther();
                } else {
                    Log.w(TAG, "Bluetooth LE Service is null; PhoneAlertTast not sent");
                }
            }
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notifRec);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification StatusBar) {
        Log.i(TAG, String.format("onNotificationPosted: %s", StatusBar.toString()));
        HandleNotification(StatusBar, true);
        Intent i = new Intent(MainActivity.NOTIFICATION_LISTENER_EVENT);
        i.putExtra(MainActivity.NOTIFICATION_EVENT, "onNotificationPosted :" + StatusBar.getPackageName() + "n");
        sendBroadcast(i);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification StatusBar) {
        Log.i(TAG, String.format("onNotificationPosted: %s", StatusBar.toString()));
        HandleNotification(StatusBar, false);  // Add the notification to the SeenCollection -
        Intent i = new Intent(MainActivity.NOTIFICATION_LISTENER_EVENT);
        i.putExtra(MainActivity.NOTIFICATION_EVENT, "onNotificationRemoved :" + StatusBar.getPackageName() + "n");
        sendBroadcast(i);
    }

    public static void HandleNotification(StatusBarNotification sbn, boolean add_or_remove) {
        Log.d(TAG, String.format("Handling notification %s", sbn.toString()));

        Long post_time = sbn.getPostTime();
        Log.d(TAG, post_time.toString());
        Log.d(TAG, String.format("adding notification: %s", add_or_remove));
        if (!acceptTelephony) {
            Log.d(TAG, String.format("Notification REJECTED (TELEPHONY): %s icon: %d", sbn.getPackageName(), sbn.getNotification().icon));
            return;
        }

        //Have seen cases on Note 3 where multiple incoming call alerts get posted.  doing a check to prevent duplicates
        Long timeDiff = post_time - lastPhoneCallTime;
        Log.d(TAG, String.format("Time Diff: %s", timeDiff.toString()));
        if (acceptTelephony && (sbn.getNotification().icon == 2130839019 ||
                sbn.getNotification().icon == 2130839012) && (timeDiff < DEFAULT_TIME_DIFF)) {
            Log.d(TAG, String.format("Notification REJECTED Potential Duplicate (TELEPHONY): %s icon: %d", sbn.getPackageName(), sbn.getNotification().icon));
            return;
        }


        Long skypeTimeDiff = post_time - lastSkypeAlertTime;
        Log.d(TAG, "Skype Time Diff: " + timeDiff.toString());
        if (sbn.getNotification().icon == 2130837959 && (skypeTimeDiff < DEFAULT_TIME_DIFF)) {
            Log.d(TAG, String.format("Notification REJECTED Potential Skype Duplicate): %s icon: %d", sbn.getPackageName(), sbn.getNotification().icon));
            return;
        }

        if (sbn.getNotification().icon == 2130837997 || sbn.getNotification().icon == 2130837986) {
            Log.d(TAG, "Notification REJECTED skype file transfer inbound/outbound");
            return;
        }

        if (sbn.getNotification().icon == 2130840359 || (sbn.getTag() != null && sbn.getTag().contains("Upload"))) {
            Log.d(TAG, "Notification REJECTED facebook file upload");
            return;
        }

        if (sbn.getNotification().icon == 2130837959) {
            lastSkypeAlertTime = post_time;
        }

        if (sbn.getNotification().icon == 2130839019 || sbn.getNotification().icon == 2130839012) {
            lastPhoneCallTime = post_time;
        }

        Log.d("notification", String.format("sbn.id():  %d", sbn.getId()));
        Log.d("notification", String.format("time when posted?: %d notification?: %s", post_time, sbn.getNotification()));
        Boolean containsKey = NotifCollection.containsKey(post_time);
        Log.d(TAG, "size of notif collection was: " + NotifCollection.size());
        if (add_or_remove) {
            if (!containsKey) {
                if (acceptNotif(sbn)) {
                    if (sbn.getNotification().icon == 2130837560 && sbn.getNotification().tickerText == null) {
                        Log.d(TAG, "found erroneous calendar notification");
                        NotifHandler(sbn, false, 0);
                        SeenCollection.put(post_time, sbn);
                        addAllToSeen(sbn.getPackageName());
                    } else {
                        Log.d(TAG, String.format("Notification ADDED: %s icon: %d", sbn.getPackageName(), sbn.getNotification().icon));
                        final Integer notiIcon = (Integer) NotifCategories.packageAlertMap.get(sbn.getPackageName());
                        final Map<Long, StatusBarNotification> temp = NotifListenerService.NotifCollection;
                        int addCount = 1;
                        for (Map.Entry notif : temp.entrySet()) {
                            StatusBarNotification existingSbn = (StatusBarNotification) notif.getValue();
                            String packageName = existingSbn.getPackageName();
                            if ((notiIcon == NotifCategories.packageAlertMap.get(packageName) ||
                                    notiIcon == NotifCategories.packageAlertMap.get(sbn.getPackageName()))
                                    && !SeenCollection.containsKey(notif.getKey())) {
                                addCount++;
                            }
                        }
                        Log.d(TAG, String.format("addcount was %d", addCount));
                        NotifHandler(sbn, true, addCount);
                        NotifCollection.put(post_time, sbn);
                    }
                } else {
                    Log.d(TAG, String.format("Notification REJECTED: %s icon: %d", sbn.getPackageName(), sbn.getNotification().icon));
                }
            }
        } else {
            if (containsKey) {
                Log.d(TAG, "removing notification");
                NotifHandler(sbn, false, 0);
                SeenCollection.put(post_time, sbn);
                if (sbn.getPackageName().equals("com.android.phone") && sbn.getNotification().icon == 2130837640) {

                } else {
                    addAllToSeen(sbn.getPackageName());
                }

            }
        }
    }


    private static void addAllToSeen(String packagename) {
        Log.d(TAG, "adding all to seen");
        ArrayList<Long> keyList = new ArrayList<Long>(NotifListenerService.NotifCollection.keySet());
        if (!keyList.isEmpty()) {
            for (int x = 0; x < keyList.size(); x++) {
                if (NotifListenerService.NotifCollection.get(keyList.get(x)).getPackageName().equals(packagename)) {
                    NotifListenerService.SeenCollection.put(NotifListenerService.NotifCollection.get(keyList.get(x)).getPostTime(),
                            NotifListenerService.NotifCollection.get((keyList.get(x))));
                }
            }
        }
    }

    // Add to the seen collection
    // Send a broadcast to alert the UI the dataSet has changed
    // Remove from active notifications, handle the removal with BluetoothService :
    private void seenAndRemove(Long id) {
        Log.d(TAG, "seen and remove");
        if (NotifCollection.containsKey(id)) {

            NotifListenerService.SeenCollection.put(NotifListenerService.NotifCollection.get(id).getPostTime(),
                    NotifListenerService.NotifCollection.get(id));
            NotifHandler(NotifCollection.get(id), false, 0);
            Intent i = new Intent(MainActivity.NOTIFICATION_LISTENER_EVENT);
            i.putExtra(MainActivity.NOTIFICATION_EVENT, "seenAndRemove notification :" + NotifCollection.get(id).getPackageName() + "n");
            sendBroadcast(i);
        }
    }


    // Returns true if the StatusBarNotification is an accepted package type
// If there is additional logic to check on the type, check, and modify returned value accordingly
    public static boolean acceptNotif(StatusBarNotification sbn) {
        Log.d(TAG, "checking if can accept:" + sbn.getPackageName());
        int lGate = Arrays.asList(NotifCategories.AndroidNotifCategories).indexOf(sbn.getPackageName());
        Log.d(TAG, "index found was " + lGate);
        if (lGate != -1) {

            switch (lGate) {

                case 0:
                    return NotifCategories.isEnabled(NotifCategories.EMAIL_ALERT);
                case 1:
                    return NotifCategories.isEnabled(NotifCategories.CALL_ALERT);
                case 2:
                    return NotifCategories.isEnabled(NotifCategories.PRIVATE_ALERT);
                case 4:
                case 13:
                case 17:
                    return NotifCategories.isEnabled(NotifCategories.SCHEDULE_ALERT);
                case 5:
                case 6:
                case 7:
                case 8:
                case 10:
                case 15:
                case 16:
                    return NotifCategories.isEnabled(NotifCategories.SOCIAL_ALERT);
                case 9: {   // Filter Skype notifications to not add notifications without ticker text
                    Log.d("skype_notification", "checking for skype notification");
                    if (NotifCategories.isEnabled(NotifCategories.SOCIAL_ALERT)) {
                        Log.d("skype_notification", "found skype notification");
                        if (sbn.getNotification().tickerText == null) {
                            Log.d("skype_notification", "ticker text was found to be null");
                            return false;
                        } else {
                            return true;
                        }

                    } else {
                        return false;
                    }
                }
                case 11:
                case 12:
                    return NotifCategories.isEnabled(NotifCategories.BATTERY_ALERT);
                case 14:
                    return NotifCategories.isEnabled(NotifCategories.EMAIL_ALERT);
                default:
                    Log.d(TAG, String.format("no case found for %d", lGate));
                    return false;
            }
        } else {
            Log.d(TAG, String.format("not accepting: %s", sbn.getPackageName()));
            return false;
        }
    }

    /**
     * clear notifications that are currently on; minimize traffic by only clearing active notifications
     * battery alerts are handled separately to avoid turning off when battery still low
     */
    public static void clearActiveNotifications() {
        Log.d(TAG, "clear currently active notifications");
        if (BluetoothService.getBtGatt() != null && !BluetoothService.inServiceMode) {
            AlertCategories categories = AlertCategories.getInstance();
            List<Byte> activeCategories = new ArrayList<Byte>();
            if (categories.isAlarmAlert()) {
                activeCategories.add(AlertCategories.ALARM);
            }
            if (categories.isCallAlert()) {
                activeCategories.add(AlertCategories.CALL);
            }
            if (categories.isEmailAlert()) {
                activeCategories.add(AlertCategories.EMAIL);
            }
            if (categories.isSocialAlert()) {
                activeCategories.add(AlertCategories.SOCIAL);
            }
            if (categories.isPrivateAlert()) {
                activeCategories.add(AlertCategories.PRIVATE);
            }
            if (categories.isScheduleAlert()) {
                activeCategories.add(AlertCategories.SCHEDULE);
            }
            if (categories.isMissedCallAlert()) {
                activeCategories.add(AlertCategories.MISSED_CALL);
            }

            for (Byte category : activeCategories) {
                BluetoothService.getInstance().sendUnreadAlert(category, 0);
            }
        }
    }


    public static void clearAllNotifications() {
        Log.d(TAG,"clearAllNotifications");
        if (BluetoothService.getBtGatt() != null && !BluetoothService.inServiceMode) {

            for (byte category : AlertCategories.notificationsToClear) {
                BluetoothService.getInstance().sendUnreadAlert(category, 0);
            }
            BluetoothService.getInstance().cancelBatteryAlert();
            BluetoothService.getInstance().cancelImmediateAlert();

        }


    }


    public static void NotifHandler(StatusBarNotification sbn, boolean addNotification, int notif_count) {
        String packagename = sbn.getPackageName();
        Notification notification = sbn.getNotification();
        Log.d(TAG, String.format("notif handler package name: %s What are the contents of notification?: %d   icon:  %d", packagename, notification.describeContents(), notification.icon));
        if (BluetoothService.getBtGatt() != null && !BluetoothService.inServiceMode) {
            int i = 0;
            for (; i < NotifCategories.AndroidNotifCategories.length; i++) {
                Log.d(TAG, "package names: " + NotifCategories.AndroidNotifCategories[i]);
                if (packagename.equals(NotifCategories.AndroidNotifCategories[i])) {
                    switch (i) {
                        // EMAIL
                        case NotifCategories.CDP_CAT_ID_EMAIL:
                        case NotifCategories.CDP_CAT_ID_ANDROID_MAIL: {
                            if ((NotifCategories.EnabledCategoryBitMask & NotifCategories.EMAIL_ALERT) >= 1) {
                                Log.d(TAG, "email alert");
                                BluetoothService.getInstance().sendUnreadAlert(AlertCategories.EMAIL, notif_count);
                            }
                            break;
                        }
                        // PRIVATE
                        case NotifCategories.CDP_CAT_ID_PRIVATE: {
                            if ((NotifCategories.EnabledCategoryBitMask & NotifCategories.PRIVATE_ALERT) >= 1) {
                                BluetoothService.getInstance().sendUnreadAlert(AlertCategories.PRIVATE, notif_count);
                                Log.d(TAG, "private category");
                            }
                            break;
                        }
                        // CALLS (including missed)
                        case NotifCategories.CDP_CAT_ID_CALL: {
                            Log.d("notification_logic", "Icon Was: " + notification.icon);
                            if ((NotifCategories.EnabledCategoryBitMask & NotifCategories.CALL_ALERT) >= 1) {

                                if (notification.icon == NotifCategories.missed_call_icon ||
                                        notification.icon == 2130837634 ||
                                        notification.icon == NotifCategories.voicemail_icon) {
                                    BluetoothService.getInstance().sendUnreadAlert(AlertCategories.MISSED_CALL, notif_count);
                                } else {
                                    BluetoothService.getInstance().sendUnreadAlert(AlertCategories.CALL, notif_count);
                                }
                            }
                            break;

                        }
                        // Schedule
                        case NotifCategories.CDP_CAT_ID_ANDROID_CAL:
                        case NotifCategories.CDP_CAT_ID_CALENDAR:
                        {
                            Log.d(TAG, "found schedule category");
                            if ((NotifCategories.EnabledCategoryBitMask & NotifCategories.SCHEDULE_ALERT) >= 1) {
                                BluetoothService.getInstance().sendUnreadAlert(AlertCategories.SCHEDULE, notif_count);
                            }
                            break;
                        }
                        // Alarms
                        case NotifCategories.CDP_CAT_ID_ALARMS:
                        case NotifCategories.CDP_CAT_ID_CLOCKPACKAGE: {
                            Log.d(TAG, "found alarm category");
                            // TODO switch to Alarm filter when added
                            if ((NotifCategories.EnabledCategoryBitMask & NotifCategories.SCHEDULE_ALERT) >= 1) {
                                BluetoothService.getInstance().sendUnreadAlert(AlertCategories.ALARM, notif_count);
                            }
                            break;
                        }
                        // SOCIAL
                        case NotifCategories.CDP_CAT_ID_SOCIAL:
                        case NotifCategories.CDP_CAT_ID_GTALK:
                        case NotifCategories.CDP_CAT_ID_SKYPE:
                        case NotifCategories.CDP_CAT_ID_WECHAT:
                        case NotifCategories.CDP_CAT_ID_WHATSAPP:
                        case NotifCategories.CDP_CAT_ID_MAAII:
                        case NotifCategories.CDP_CAT_ID_LINE:
                        case NotifCategories.CDP_CAT_ID_VIBER: {
                            if ((NotifCategories.EnabledCategoryBitMask & NotifCategories.SOCIAL_ALERT) >= 1) {
                                BluetoothService.getInstance().sendUnreadAlert(AlertCategories.SOCIAL, notif_count);
                            }
                            break;
                        }
                        // BATTERY
                        // TODO should we send the battery notification via status bar notifications or just via battery manager?
                        case NotifCategories.CDP_CAT_ID_BATTERY: {
                            if ((NotifCategories.EnabledCategoryBitMask & NotifCategories.BATTERY_ALERT) >= 1) {
                                if (addNotification) {
                                    if (packagename.contains("low")) {
                                        BluetoothService.getInstance().sendLowBatteryAlert();
                                    } else if (packagename.contains("critical")) {
                                        BluetoothService.getInstance().sendCriticalBatteryAlert();
                                    } else {
                                        BluetoothService.getInstance().cancelBatteryAlert();
                                    }
                                } else {
                                    BluetoothService.getInstance().cancelBatteryAlert();
                                }
                            }
                            break;
                        }

                        default:
                            Log.d(TAG, String.format("no match found for %d", i));
                            break;
                    }
                }
            }
        } else {
            Log.d(TAG, "gatt was unavailable or in service mode");
        }
    }


    /***
     * Receive broadcasts to
     */
    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, String.format("onReceive intent %s", intent.getAction()));
            if (intent.getStringExtra(NOTIFICATION_COMMAND).equals(NOTIFICATION_CLEARALL)) {
                NotifListenerService.this.cancelAllNotifications();
                NotifCollection.clear();
                SeenCollection.clear();

                Intent i3 = new Intent(MainActivity.NOTIFICATION_LISTENER_EVENT);
                i3.putExtra(MainActivity.NOTIFICATION_EVENT, "===== Notification List ====");
                sendBroadcast(i3);
            }
        }
    }
}

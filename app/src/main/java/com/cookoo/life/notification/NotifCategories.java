package com.cookoo.life.notification;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.cookoo.life.R;
import com.cookoo.life.activity.MainActivity;

import java.util.HashMap;


/**
 * DEV NOTES (VERY IMPORTANT)
 * Anytime you need to add a new notification category for now it currently must be added to the following things:
 * A bit ghetto right?
 * 1.NotifCategories
 * 2.AndroidNotifIcons
 * 3.AndroidNotifCategories
 * 4.AndroidColors
 * 5.{@Code NotifCategories#acceptNotif(StatusBarNotification sbn)}
 * 6.{@Code BluetoothService#NotifHandler(StatusBarNotification sbn, boolean add_or_remove)
 * 7.{@Code MainActivity#BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()...}
 */
public class NotifCategories {
    private static final String TAG = "NotifCategories";
    /*! Proprietary alert category IDs */
    public static final int CDP_CAT_ID_EMAIL = 0x00;
    //public static final int CDP_CAT_ID_ANDROID     		 = 0x01;
    public static final int CDP_CAT_ID_CALL = 0x01;
    public static final int CDP_CAT_ID_PRIVATE = 0x02;
    public static final int CDP_CAT_ID_ALARMS = 0x03;
    public static final int CDP_CAT_ID_CALENDAR = 0x04;
    public static final int CDP_CAT_ID_SOCIAL = 0x05;
    public static final int CDP_CAT_ID_GTALK = 0x06;
    public static final int CDP_CAT_ID_WECHAT = 0x07;
    public static final int CDP_CAT_ID_WHATSAPP = 0x08;
    public static final int CDP_CAT_ID_SKYPE = 0x09;
    public static final int CDP_CAT_ID_MAAII = 0xA;
    public static final int CDP_CAT_ID_BATTERY = 0xB;
    public static final int CDP_CAT_ID_ANDROID_CAL = 0xD;
    public static final int CDP_CAT_ID_ANDROID_MAIL = 0xE;
    public static final int CDP_CAT_ID_VIBER = 0xF;
    public static final int CDP_CAT_ID_LINE = 0x10;
    public static final int CDP_CAT_ID_CLOCKPACKAGE = 0x11;
        //public static final int CDP_CAT_ID_ENTERTAINMENT        = 0x0A;
    //public static final int CDP_CAT_ID_WEATHER              = 0x0C;
    //public static final int CDP_CAT_ID_CURRENT_TIME         = 0x10;

    //public static final int CDP_CAT_ID_OTHER                = 0xF0;
    //public static final int CDP_CAT_ID_HEALTH_AND_FITNESS   = 0xF8;
    //public static final int CDP_CAT_ID_BUSINESS_AND_FINANCE = 0xF9;

    public static int EnabledCategoryBitMask = 0x00;
    public static int ActiveAlertBitMask = 0;


    /*
     * Below are the Bluetooth SIG mappings for alert categories.
     *
     */
    //public static final int SCHEDULE_ALERTS = 0x80;
    public static final int EMAIL_ALERT = 0x02;
    public static final int NEWS_ACTIVE = 0x04;
    public static final int CALL_ALERT = 0x08;
    public static final int MISSED_CALL_ALERT = 0x10;
    public static final int PRIVATE_ALERT = 0x20;
    public static final int SCHEDULE_ALERT = 0x80;
    public static final int CALL_ACTIVE = 0x80;
    public static final int SOCIAL_ALERT = 0x200;
    public static final int BATTERY_ALERT = 0x400;
    public static final int ALARM_ACTIVE = 0x800;
    public static final int MUSIC_CONTROL = 0x8000;

    public static int[] MaskedAlerts = {
            EMAIL_ALERT,
            CALL_ALERT,
            MISSED_CALL_ALERT,
            PRIVATE_ALERT,
            ALARM_ACTIVE,
            SCHEDULE_ALERT,
            SOCIAL_ALERT,
            SOCIAL_ALERT,
            SOCIAL_ALERT,
            SOCIAL_ALERT,
            SOCIAL_ALERT,
            SOCIAL_ALERT,
            BATTERY_ALERT,
            BATTERY_ALERT,
            SCHEDULE_ALERT,
            EMAIL_ALERT,
            SOCIAL_ALERT,
            SOCIAL_ALERT
    };

    public static final String[] AndroidNotifCategories = {
            "com.google.android.gm", // MAIL || 2130837691
            "com.android.phone", // PHONE || MISSED CALL : 2130837634
            "com.android.mms",  // PRIVATE || 2130837767
            "com.google.android.deskclock",
            "com.htc.calendar", // EVENT || 2130837560
            "com.facebook.katana", // SOCIAL || 2130839930 || 2130839926
            "com.google.android.talk",
            "com.tencent.mm",// PRIVATE ( wechat ?? //JESSE CONFIRM) || 2130838926x
            "com.whatsapp",
            "com.skype.raider",
            "com.maaii.maaii",
            "com.connected.low.battery",
            "com.connected.critical.battery",
            "com.android.calendar",
            "com.android.email",
            "com.viber.voip",
            "jp.naver.line.android",
            "com.sec.android.app.clockpackage" // alarm


    };


    private static final int alarm_icon = R.drawable.nc2_alarm;
    private static final int battery_crit_icon = R.drawable.nc2_battery_crit;
    private static final int battery_low = R.drawable.nc2_battery_low;
    private static final int call_missed = R.drawable.nc2_call;
    private static final int call_icon = R.drawable.nc2_call;
    private static final int email_icon = R.drawable.nc2_email;
    private static final int private_message = R.drawable.nc2_private;
    private static final int social_message = R.drawable.nc2_social;
    private static final int schedule = R.drawable.nc2_schedule;
    private static final int system_high = R.drawable.nc2_system;
    private static final int system_low = R.drawable.nc_system_high;
    private static final int seen_battery = R.drawable.seen_battery;
    private static final int seen_social = R.drawable.seen_social;
    private static final int seen_private = R.drawable.seen_private;
    private static final int seen_call = R.drawable.seen_call;
    private static final int seen_schedule = R.drawable.seen_schedule;
    private static final int seen_email = R.drawable.seen_email;
    private static final int default_icon = 0;


    public final static HashMap<String, Object> packageAlertMap;

    static {
        packageAlertMap = new HashMap<String, Object>();
        packageAlertMap.put("com.google.android.gm", email_icon);
        packageAlertMap.put("com.android.phone", call_icon);
        packageAlertMap.put("com.android.mms", private_message);
        packageAlertMap.put("com.google.android.deskclock", alarm_icon);
        packageAlertMap.put("com.htc.calendar", schedule);
        packageAlertMap.put("com.facebook.katana", social_message);
        packageAlertMap.put("com.google.android.talk", social_message);
        packageAlertMap.put("com.tencent.mm", social_message);
        packageAlertMap.put("com.whatsapp", social_message);
        packageAlertMap.put("com.skype.raider", social_message);
        packageAlertMap.put("com.maaii.maaii", social_message);
        packageAlertMap.put("com.connected.low.battery", battery_low);
        packageAlertMap.put("com.connected.critical.battery", battery_crit_icon);
        packageAlertMap.put("com.android.calendar", schedule);
        packageAlertMap.put("com.android.email", email_icon);
        packageAlertMap.put("com.viber.voip", social_message);
        packageAlertMap.put("jp.naver.line.android", social_message);
        packageAlertMap.put("com.sec.android.app.clockpackage", alarm_icon);
    }


    // TO DO : Remove unused array of int's AndroidNotifIcons[]
    public static final int[] AndroidNotifIcons = {
            email_icon, // MAIL || 2130837691
            call_icon, // PHONE || MISSED CALL : 2130837634
            private_message,  // PRIVATE || 2130837767
            default_icon,
            schedule, // EVENT || 2130837560
            social_message, // SOCIAL || 2130839930 || 2130839926 FACEBOOK
            social_message, // PRIVATE ( GTalk ) || 2130838926
            social_message, // WeChat
            social_message, // WhatsApp
            social_message, // Skype
            social_message, //Maaii
            battery_low,
            battery_crit_icon,
            schedule, //com.android.calendar
            email_icon,
            social_message,
            social_message,
            alarm_icon
    };


    // TO DO : Remove unused array of int's AndroidNotifIcons[]
    public static final int[] AndroidSeenIcons = {
            seen_email, // MAIL || 2130837691
            seen_call, // PHONE || MISSED CALL : 2130837634
            seen_private,  // PRIVATE || 2130837767
            default_icon,
            seen_schedule, // EVENT || 2130837560
            seen_social, // SOCIAL || 2130839930 || 2130839926 FACEBOOK
            seen_social, // PRIVATE ( GTalk ) || 2130838926
            seen_social, // WeChat
            seen_social, // WhatsApp
            seen_social, // Skype
            seen_social, //Maaii
            seen_battery,
            seen_battery,
            seen_schedule, //com.android.calendar
            seen_email,
            seen_social,
            seen_social,
            default_icon
    };


    //HTC ONE:
    public static final String[] AndroidNotifNames = {
            "Mail", // MAIL || 2130837691
            "Phone", // PHONE || MISSED CALL : 2130837634
            "Text Message",  // PRIVATE || 2130837767
            "com.google.android.deskclock",
            "Event", // EVENT || 2130837560
            "Facebook", // SOCIAL || 2130839930 || 2130839926 SOCIAL FACEBOOK
            "GTalk", // PRIVATE ( GTalk ) || 2130838926
            "WeChat",
            "WhatsApp",
            "Skype",
            "Maaii",
            "Low Battery",
            "Critical Battery",
            "Event",
            "Mail",
            "Viber",
            "Line",
            "Alarm"

    };


    public static final int[] AndroidColors = {
            R.color.yellow, // MAIL || 2130837691
            R.color.orange, // PHONE || MISSED CALL : 2130837634
            R.color.black,  // PRIVATE || 2130837767
            R.color.magenta,    // ?? DESKCLOCK ??
            R.color.tangerine, // EVENT || 2130837560
            R.color.green, // SOCIAL || 2130839930 || 2130839926
            R.color.green, // PRIVATE ( GTalk ) || 2130838926
            R.color.green, // WeChat
            R.color.green, // WhatsApp
            R.color.green, // Skype
            R.color.green, //Maaii
            R.color.red, //low battery
            R.color.red, //critical battery
            R.color.tangerine, //Android calendar
            R.color.yellow,
            R.color.green,
            R.color.green,
            R.color.tangerine,
    };


    public static final String[] AndroidIgnored = {
            "com.android.settings",
            "com.android.providers.media",
            "com.android.vending",
            "com.htc.android.mail",
            "android",
            "com.htc.usage",
            "com.htc.music",
            "2130837640",
            "com.android.providers.downloads",
    };

    public static boolean isEnabled(int alert) {
        boolean isEnabled = ((EnabledCategoryBitMask & alert) == alert);
        Log.d(TAG,String.format("alert %d is enabled? %b",alert,isEnabled));
        return isEnabled;
    }

    public static void SetAlertMasks() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.activity);
        Log.d(TAG, String.format("starting mask: %d", EnabledCategoryBitMask));

        if (sp.getBoolean("set_on_alarm_social", true)) {
            EnabledCategoryBitMask |= SOCIAL_ALERT;
            MainActivity.socialEnabled = true;
            Log.d(TAG,"social is true");
        } else if (!sp.getBoolean("set_on_alarm_social", true)) {
            EnabledCategoryBitMask = (EnabledCategoryBitMask ^ SOCIAL_ALERT);
            MainActivity.socialEnabled = false;
            Log.d(TAG,"social is false");
        }

        if (sp.getBoolean("set_on_alarm_private", true)) {
            EnabledCategoryBitMask |= PRIVATE_ALERT;
            MainActivity.privateEnabled = true;
            Log.d(TAG,"text is true");
        } else if (!sp.getBoolean("set_on_alarm_private", true)) {
            EnabledCategoryBitMask = (EnabledCategoryBitMask ^ PRIVATE_ALERT);
            MainActivity.privateEnabled = false;
            Log.d(TAG,"text is false");
        }

        if (sp.getBoolean("set_on_alarm_email", true)) {
            EnabledCategoryBitMask |= EMAIL_ALERT;
            MainActivity.emailEnabled = true;
            Log.d(TAG,"mail is true");
        } else if (!sp.getBoolean("set_on_alarm_email", true)) {
            EnabledCategoryBitMask = (EnabledCategoryBitMask ^ EMAIL_ALERT);
            MainActivity.emailEnabled = false;
            Log.d(TAG,"mail is false");
        }
        if (sp.getBoolean("set_on_alarm_calendar", true)) {
            // this is actually the events toggle, not calendar.
            EnabledCategoryBitMask |= SCHEDULE_ALERT;
            MainActivity.scheduleEnabled = true;
            Log.d(TAG,"event is true");
        } else if (!sp.getBoolean("set_on_alarm_calendar", true)) {
            // this is actually the events toggle, not calendar.
            EnabledCategoryBitMask = (EnabledCategoryBitMask ^ SCHEDULE_ALERT);
            MainActivity.scheduleEnabled = false;
            Log.d(TAG,"event is false");
        }

        if (sp.getBoolean("set_on_alarm_battery", true)) {
            EnabledCategoryBitMask |= BATTERY_ALERT;
            MainActivity.batteryEnabled = true;
            Log.d(TAG,"battery is true");
        } else if (!sp.getBoolean("set_on_alarm_battery", true)) {
            EnabledCategoryBitMask = (EnabledCategoryBitMask ^ BATTERY_ALERT);
            MainActivity.batteryEnabled = false;
            Log.d(TAG,"battery is false");
        }

        if (sp.getBoolean("set_on_alarm_call", true)) {
            EnabledCategoryBitMask |= CALL_ALERT;
            MainActivity.callEnabled = true;
            Log.d(TAG,"phone is true");
        } else if (!sp.getBoolean("set_on_alarm_call", true)) {
            EnabledCategoryBitMask = (EnabledCategoryBitMask ^ CALL_ALERT);
            MainActivity.callEnabled = false;
            Log.d(TAG,"phone is false");
        }

        if (sp.getBoolean("set_music_control", true)) {
            EnabledCategoryBitMask |= MUSIC_CONTROL;
        } else if (!sp.getBoolean("set_music_control", true)) {
            EnabledCategoryBitMask = (EnabledCategoryBitMask ^ MUSIC_CONTROL);
        }
        Log.d(TAG, String.format("current mask: %d", EnabledCategoryBitMask));
    }

    public static final int missed_call_icon = 17301631;
    public static final int voicemail_icon = 17301630;

}
package com.cookoo.life.domain;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stevi.deter on 3/1/14.
 */
public class AlertCategories {

    private final static String TAG = AlertCategories.class.getSimpleName();

    // bit mask 0
    private static final int simpleMask = 0x1;
    private static final int emailMask = 0x2;
    private static final int newsMask = 0x4;
    private static final int callMask = 0x8;
    private static final int missedCallMask = 0x10;
    private static final int privateMask = 0x20;
    private static final int voiceMailMask = 0x40;
    private static final int scheduleMask = 0x80;

    // bit mask 1
    private static final int highPriorityMask = 0x100;
    private static final int socialMask = 0x200;
    private static final int locationMask = 0x400;
    private static final int entertainmentMask = 0x800;
    private static final int weatherMask = 0x1000;
    private static final int healthAndFitnessMask = 0x2000;
    private static final int alarmMask = 0x8000; // NB: FW is sending 0x8000 while documentation says 0x400
//    private static final int callActiveMask = 0x8000;

    // App default alert source mask
    public static final byte SIMPLE = (byte)0x00;
    public static final byte EMAIL = (byte)0x01;
    public static final byte NEWS = (byte) 0x02;
    public static final byte CALL = (byte)0x03;
    public static final byte MISSED_CALL = (byte)0x04;
    public static final byte PRIVATE = (byte) 0x05;
    public static final byte VOICE_MAIL = (byte)0x06;
    public static final byte SCHEDULE = (byte)0x07;
    public static final byte HIGH_PRIORITY = (byte)0x08;
    public static final byte SOCIAL = (byte)0x09;
    public static final byte ENTERTAINMENT = (byte)0xB;
    public static final byte CURRENT_TIME = (byte)0x10;
    public static final byte BATTERY = (byte)0x20;
    public static final byte OTHER = (byte)0xF0;
    public static final byte HEALTH_AND_FITNESS = (byte)0xF8;
    public static final byte BUSINESS_AND_FINANCE = (byte)0xF9;
    public static final byte ALARM = (byte)0xFC;

    // alert notification service V10r00
    public static final byte NEW_ALERT = (byte)0x09;
    public static final byte UNREAD_ALERT_STATUS = (byte)0x0B;



    // core operations
    public static final byte READ_MESSAGE = 0x01;
    public static final byte WRITE_MESSAGE = 0x02;
    public static final byte CONFIRM_MESSAGE = 0x03;
    public static final byte ERROR_MESSAGE = 0x04;

    // data heartbeat messages
    public static final byte DATA_HEARTBEAT_RATE = 0x05;
    public static final byte NOTIFICATION_DATA_HB_MESSAGE = (byte) 0xCB;


    // configurable items
    public static final byte BUTTON_CONFIGURATION = 0x01; // button press duration
    public static final byte ALERT_CONFIGURATION = 0x02; // ON/OFF state and Alert Source for each alert
    public static final byte DEVICE_NAME = 0x03;
    public static final byte BD_ADDRESS = 0x04;
    public static final byte SERIAL_NUMBER = 0x05;
    public static final byte LICENSE_KEY = 0x06;
    public static final byte LINK_LOSS_ALERT_CONFIGURATION_TIME = 0x07; // time between a link loss event and the alert
    public static final byte BATTERY_CONFIGURATION = 0x08;
    public static final byte DATE_AND_TIME_FORMAT = 0x09;
    public static final byte DISPLAY_RESOLUTION = 0x0A;
    public static final byte ACTIVITY_REPORT_RATE = 0x0B;
    public static final byte ACTIVITY_LEVEL_THRESHOLD = 0x0C;
    public static final byte LANGUAGE_CONFIGURATION = 0x0D;

    // link loss defaults
    public static final byte LINK_LOSS_DISABLED = (byte) 0xFF;
    public static final byte LINK_LOSS_ENABLED = (byte) 0x00;

    public final static byte TRIGGER_UPDATED = (byte) 0xC0;
    public final static byte ALERT_ACKNOWLEDGED = (byte) 0xC1;
    public final static byte MANAGE_CONNECTION = (byte) 0xC2;
    public static final byte CONFIGURABLE_ITEM = (byte) 0xC3;
    public final static byte TRIGGER_ACKNOWLEDGED = (byte) 0xC4;
    public final static byte LOCATION = (byte) 0xC5;
    public final static byte WEATHER = (byte) 0xC6;
    public final static byte USER_ACTIVITY = (byte) 0xC7;
    public final static byte REST_AND_STRESS_STATISTICS = (byte) 0xC8;
    public final static byte REMINDERS = (byte) 0xC9;
    public final static byte ALARMS = (byte) 0xCA;
    public final static byte NOTIFICATION_STATISTICS = (byte) 0xCB;
    public final static byte TEXT = (byte) 0xCD;
    public final static byte ICON = (byte) 0xCE;
    public final static byte CONTINUATION = (byte) 0xCF;



    public static final byte[] notificationsToClear = new byte[]{EMAIL, PRIVATE, MISSED_CALL,
            CALL, ALARM, SOCIAL, SCHEDULE};


    public static final Map<Byte,String> CATEGORY_MASK_TO_PREFERENCE;
    static {
        CATEGORY_MASK_TO_PREFERENCE = new HashMap<Byte,String>();
        CATEGORY_MASK_TO_PREFERENCE.put(AlertCategories.SOCIAL,"set_on_alarm_social");
        CATEGORY_MASK_TO_PREFERENCE.put(AlertCategories.PRIVATE,"set_on_alarm_private");
        CATEGORY_MASK_TO_PREFERENCE.put( AlertCategories.EMAIL,"set_on_alarm_email");
        CATEGORY_MASK_TO_PREFERENCE.put( AlertCategories.SCHEDULE,"set_on_alarm_calendar");
        CATEGORY_MASK_TO_PREFERENCE.put(AlertCategories.BATTERY,"set_on_alarm_battery");
        CATEGORY_MASK_TO_PREFERENCE.put(AlertCategories.CALL,"set_on_alarm_call");
        CATEGORY_MASK_TO_PREFERENCE.put(AlertCategories.MISSED_CALL,"set_on_alarm_call");
        CATEGORY_MASK_TO_PREFERENCE.put(AlertCategories.VOICE_MAIL,"set_on_alarm_call");
    }

    public static final byte[] IMMEDIATE_ALERT_OFF = new byte[]{0x00};
    public static final byte[] IMMEDIATE_ALERT_LOW = new byte[]{0x01};
    public static final byte[] IMMEDIATE_ALERT_HIGH = new byte[]{0x02};

    public static final byte[] OAD_INIT = new byte[]{(byte) 0xF0};
    public static final byte OAD_INIT_ACKNOWLEDGE = (byte) 0xF1;
    public static final byte DATA_PACKET = (byte) 0xFF;

    public static final byte ERROR_READ_NOT_PERMITTED = (byte) 0x02;
    public static final byte ERROR_WRITE_NOT_PERMITTED = (byte) 0x03;
    public static final byte ERROR_MESSAGE_TYPE_NOT_SUPPORTED = (byte) 0x0A;




    private boolean simpleAlert;
    private boolean emailAlert;
    private boolean newsAlert;
    private boolean callAlert;
    private boolean missedCallAlert;
    private boolean privateAlert;
    private boolean voiceMailAlert;
    private boolean scheduleAlert;
    private boolean highPriorityAlert;
    private boolean socialAlert;
    private boolean locationAlert;
    private boolean entertainmentAlert;
    private boolean weatherAlert;
    private boolean healthAndFitnessAlert;
    private boolean alarmAlert;
    private boolean callActiveAlert;

    private static AlertCategories instance = null;

    private AlertCategories(){}

    public static AlertCategories getInstance() {
        if (instance == null) { instance = new AlertCategories();}
        return instance;
    }


    public void setAlerts(byte[] data) {
        if (data != null && data.length == 2) {
            int combined =  ((data[0] << 8) | (data[1] & 0Xff));
            this.simpleAlert = applyMask(combined, simpleMask);
            this.emailAlert = applyMask(combined, emailMask);
            this.newsAlert = applyMask(combined, newsMask);
            this.callAlert = applyMask(combined, callMask);
            this.missedCallAlert = applyMask(combined, missedCallMask);
            this.privateAlert = applyMask(combined, privateMask);
            this.voiceMailAlert = applyMask(combined, voiceMailMask);
            this.scheduleAlert = applyMask(combined, scheduleMask);
            this.highPriorityAlert = applyMask(combined, highPriorityMask);
            this.socialAlert = applyMask(combined, socialMask);
            this.locationAlert = applyMask(combined, locationMask);
            this.entertainmentAlert = applyMask(combined, entertainmentMask);
            this.weatherAlert = applyMask(combined, weatherMask);
            this.healthAndFitnessAlert = applyMask(combined, healthAndFitnessMask);
            this.alarmAlert = applyMask(combined, alarmMask);
//            this.callActiveAlert = applyMask(combined, callActiveMask);

        } else {
            Log.d(TAG,"expected data to be two elements long");
        }
    }

    private boolean applyMask(int combined, int mask) {
        return ((combined & mask) == mask);
    }


    public boolean isSimpleAlert() {
        return simpleAlert;
    }

    public void setSimpleAlert(boolean simpleAlert) {
        this.simpleAlert = simpleAlert;
    }

    public boolean isEmailAlert() {
        return emailAlert;
    }

    public boolean isNewsAlert() {
        return newsAlert;
    }

    public void setNewsAlert(boolean newsAlert) {
        this.newsAlert = newsAlert;
    }

    public boolean isCallAlert() {
        return callAlert;
    }

    public void setCallAlert(boolean callAlert) {
        this.callAlert = callAlert;
    }

    public void setEmailAlert(boolean emailAlert) {
        this.emailAlert = emailAlert;
    }

    public boolean isMissedCallAlert() {
        return missedCallAlert;
    }

    public void setMissedCallAlert(boolean missedCallAlert) {
        this.missedCallAlert = missedCallAlert;
    }

    public boolean isPrivateAlert() {
        return privateAlert;
    }

    public void setPrivateAlert(boolean privateAlert) {
        this.privateAlert = privateAlert;
    }

    public boolean isVoiceMailAlert() {
        return voiceMailAlert;
    }

    public void setVoiceMailAlert(boolean voiceMailAlert) {
        this.voiceMailAlert = voiceMailAlert;
    }

    public boolean isScheduleAlert() {
        return scheduleAlert;
    }

    public void setScheduleAlert(boolean scheduleAlert) {
        this.scheduleAlert = scheduleAlert;
    }

    public boolean isHighPriorityAlert() {
        return highPriorityAlert;
    }

    public void setHighPriorityAlert(boolean highPriorityAlert) {
        this.highPriorityAlert = highPriorityAlert;
    }

    public boolean isSocialAlert() {
        return socialAlert;
    }

    public void setSocialAlert(boolean socialAlert) {
        this.socialAlert = socialAlert;
    }

    public boolean isLocationAlert() {
        return locationAlert;
    }

    public void setLocationAlert(boolean locationAlert) {
        this.locationAlert = locationAlert;
    }

    public boolean isEntertainmentAlert() {
        return entertainmentAlert;
    }

    public void setEntertainmentAlert(boolean entertainmentAlert) {
        this.entertainmentAlert = entertainmentAlert;
    }

    public boolean isWeatherAlert() {
        return weatherAlert;
    }

    public void setWeatherAlert(boolean weatherAlert) {
        this.weatherAlert = weatherAlert;
    }

    public boolean isHealthAndFitnessAlert() {
        return healthAndFitnessAlert;
    }

    public void setHealthAndFitnessAlert(boolean healthAndFitnessAlert) {
        this.healthAndFitnessAlert = healthAndFitnessAlert;
    }

    public boolean isAlarmAlert() {
        return alarmAlert;
    }

    public void setAlarmAlert(boolean alarmAlert) {
        this.alarmAlert = alarmAlert;
    }

    public boolean isCallActiveAlert() {
        return callActiveAlert;
    }

    public void setCallActiveAlert(boolean callActiveAlert) {
        this.callActiveAlert = callActiveAlert;
    }

    @Override
    public String toString() {
        return String.format("AlertCategories{simpleAlert=%s, emailAlert=%s, newsAlert=%s, callAlert=%s, missedCallAlert=%s, privateAlert=%s, voiceMailAlert=%s, scheduleAlert=%s, highPriorityAlert=%s, socialAlert=%s, locationAlert=%s, entertainmentAlert=%s, weatherAlert=%s, healthAndFitnessAlert=%s, alarmAlert=%s, callActiveAlert=%s}", simpleAlert, emailAlert, newsAlert, callAlert, missedCallAlert, privateAlert, voiceMailAlert, scheduleAlert, highPriorityAlert, socialAlert, locationAlert, entertainmentAlert, weatherAlert, healthAndFitnessAlert, alarmAlert, callActiveAlert);
    }
}

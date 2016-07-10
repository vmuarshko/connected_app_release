package com.cookoo.life.activity;



import android.app.Activity;
import android.app.Notification;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cookoo.life.R;
import com.cookoo.life.notification.NotifCategories;
import com.cookoo.life.service.NotifListenerService;

public class DemoModeActivity extends Activity {
    private final String TAG = getClass().getSimpleName();

    private ImageButton notif;
    private ImageButton notifHigh;
    private ImageButton batteryLow;
    private ImageButton batteryCritical;
    private ImageButton privateNotif;
    private ImageButton social;
    private ImageButton alarm;
    private ImageButton calendar;
    private ImageButton email;
    private ImageButton call;
    private ImageButton missedCall;
    private TextView version;
    private Button   clearAll;

    private boolean bNotif = false;
    private boolean bNotifHigh = false;
    private boolean bBatteryLow = false;
    private boolean bBatteryCritical = false;
    private boolean bPrivateNotif = false;
    private boolean bSocial = false;
    private boolean bAlarm = false;
    private boolean bCalendar = false;
    private boolean bEmail = false;
    private boolean bCall = false;
    private boolean bMissedCall = false;
    public static final int FULL_BATT = 99;
    private final int LOW_BATT = 20;
    private final int CRITICAL_BATT = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_mode_activity);



        notif = (ImageButton) findViewById(R.id.notif);
        notifHigh = (ImageButton) findViewById(R.id.notifHigh);
        batteryLow = (ImageButton) findViewById(R.id.batteryLow);
        batteryCritical = (ImageButton) findViewById(R.id.batteryCritical);
        privateNotif = (ImageButton) findViewById(R.id.privateNotif);
        social = (ImageButton) findViewById(R.id.social);
        alarm = (ImageButton) findViewById(R.id.alarm);
        calendar = (ImageButton) findViewById(R.id.calendar);
        email = (ImageButton) findViewById(R.id.email);
        call = (ImageButton) findViewById(R.id.call);
        missedCall = (ImageButton) findViewById(R.id.missedCall);
        clearAll = (Button) findViewById(R.id.clearAll);

        View.OnClickListener hNotif = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("demo","demo mode click highNotif");
                if (!bNotif){
                    MainActivity.TurnOn_IMA(true,"01");
                    v.setBackgroundColor(getResources().getColor(R.color.gray));
                    bNotif = true;
                }else{
                    MainActivity.TurnOn_IMA(false,"00");
                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    bNotif = false;
                }
            }
        };
        View.OnClickListener hNotifHigh= new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("demo","demo mode click notif");
                if (!bNotifHigh){
                    Log.d("demo","demo mode click notif false");
                    MainActivity.TurnOn_IMA(true,"");
                    v.setBackgroundColor(getResources().getColor(R.color.gray));
                    bNotifHigh = true;
                }else{
                    Log.d("demo","demo mode click notif true");
                    MainActivity.TurnOn_IMA(false,"");
                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    bNotifHigh = false;
                }

            }
        };
        View.OnClickListener hBatteryLow = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("demo","demo mode click batteryLow");
                if (!bBatteryLow){
                    MainActivity.batteryStatusToDevice(LOW_BATT);
                    v.setBackgroundColor(getResources().getColor(R.color.gray));
                    bBatteryLow = true;
                }else{
                    MainActivity.batteryStatusToDevice(FULL_BATT);
                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    bBatteryLow = false;
                }
            }
        };
        View.OnClickListener hBatteryCritical = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("demo","demo mode click batteryCritical");
                if (!bBatteryCritical){
                    MainActivity.batteryStatusToDevice(CRITICAL_BATT);
                    v.setBackgroundColor(getResources().getColor(R.color.gray));
                    bBatteryCritical = true;
                }else{
                    MainActivity.batteryStatusToDevice(FULL_BATT);
                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    bBatteryCritical = false;
                }
            }
        };
        View.OnClickListener hPrivateNotif = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("demo","demo mode click privateNotif");
                if (!bPrivateNotif){
                    Notification noti = new Notification();
                    noti.tickerText = "test demo notification";
                    Log.d("private_notification", NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_PRIVATE]);

                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_PRIVATE],NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_PRIVATE],12332,"isPrivate",
                            12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, true, 1);
                    v.setBackgroundColor(getResources().getColor(R.color.gray));
                    bPrivateNotif = true;
                }else{
                    Notification noti = new Notification();
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_PRIVATE],NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_PRIVATE],12332,"isPrivate",
                            12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, false, 0);
                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    bPrivateNotif = false;
                }
            }
        };
        View.OnClickListener hSocial = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("demo","demo mode click social");
                if (!bSocial){
                    Notification noti = new Notification();
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_SOCIAL],
                            NotifCategories.AndroidNotifCategories[NotifCategories.CDP_CAT_ID_SOCIAL],12332,"noTag"
                            ,12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, true, 1);
                    v.setBackgroundColor(getResources().getColor(R.color.gray));
                    bSocial = true;
                }else{
                    Notification noti = new Notification();
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_SOCIAL],NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_SOCIAL],12332,"noTag",
                            12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, false, 0);
                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    bSocial = false;
                }
            }
        };
        View.OnClickListener hAlarm = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("demo","demo mode click alarm");
                if (!bAlarm){
                    Notification noti = new Notification();
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_ALARMS],
                            NotifCategories.AndroidNotifCategories[NotifCategories.CDP_CAT_ID_ALARMS],
                            12332,"noTag",12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, true, 1);
                    v.setBackgroundColor(getResources().getColor(R.color.gray));
                    bAlarm = true;
                }else{
                    Notification noti = new Notification();
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_ALARMS],NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_ALARMS],12332,"noTag",
                            12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, false, 0);
                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    bAlarm = false;
                }
            }
        };
        View.OnClickListener hCalendar = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("demo","demo mode click calendar");
                if (!bCalendar){
                    Notification noti = new Notification();
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_CALENDAR],NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_CALENDAR],12332,"noTag",
                            12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, true, 1);
                    v.setBackgroundColor(getResources().getColor(R.color.gray));
                    bCalendar = true;
                }else{
                    Notification noti = new Notification();
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_CALENDAR],NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_CALENDAR],12332,"noTag",
                            12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, false, 0);
                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    bCalendar = false;
                }
            }
        };
        View.OnClickListener hEmail = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("demo","demo mode click email");
                if (!bEmail){
                    Notification noti = new Notification();
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_EMAIL],NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_EMAIL],12332,"noTag",
                            12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, true, 1);
                    v.setBackgroundColor(getResources().getColor(R.color.gray));
                    bEmail = true;
                }else{

                    //To Clear alert on watch to OFF
                    Notification noti = new Notification();
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_EMAIL],NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_EMAIL],12332,"noTag",
                            12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, false, 0);

                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    bEmail = false;
                }
            }
        };
        View.OnClickListener hCall = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("demo","demo mode click call");
                if (!bCall){
                    Notification noti = new Notification();
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_CALL],NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_CALL],12332,"noTag",
                            12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, true, 1);
                    v.setBackgroundColor(getResources().getColor(R.color.gray));
                    bCall = true;
                }else{
                    Notification noti = new Notification();
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_CALL],NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_CALL],12332,"noTag",
                            12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, false, 0);
                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    bCall = false;
                }
            }
        };
        View.OnClickListener hMissedCall = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("demo","demo mode click missed call");
                if (!bMissedCall){
                    Notification noti = new Notification();
                    noti.icon = NotifCategories.missed_call_icon;
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_CALL],NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_CALL],12332,"noTag",
                            12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, true, 1);
                    v.setBackgroundColor(getResources().getColor(R.color.gray));
                    bMissedCall = true;
                }else{
                    Notification noti = new Notification();
                    noti.icon = NotifCategories.missed_call_icon;
                    noti.tickerText = "test demo notification";
                    StatusBarNotification testn = new StatusBarNotification(NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_CALL],NotifCategories.
                            AndroidNotifCategories[NotifCategories.CDP_CAT_ID_CALL],12332,"noTag",
                            12321312,231,1,noti,android.os.Process.myUserHandle(),13232213);
                    NotifListenerService.NotifHandler(testn, false, 0);
                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    bMissedCall = false;
                }
            }
        };


        /**
         * Clear all alerts
         */
        View.OnClickListener hClearAll = new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("demo","demo mode clear all");
                bMissedCall = true;
                bCall = true;
                bEmail = true;
                bCalendar = true;
                bSocial = true;
                bNotif = true;
                bBatteryCritical = true;
                bBatteryLow = true;
                bPrivateNotif = true;
                bNotifHigh = true;
                bPrivateNotif = true;
                bAlarm = true;
                notif.performClick();
                notif.setPressed(true);
                notif.invalidate();
                notifHigh.performClick();
                notifHigh.setPressed(true);
                notifHigh.invalidate();
                batteryLow.performClick();
                batteryLow.setPressed(true);
                batteryLow.invalidate();
                batteryCritical.performClick();
                batteryCritical.setPressed(true);
                batteryCritical.invalidate();
                privateNotif.performClick();
                privateNotif.setPressed(true);
                privateNotif.invalidate();
                social.performClick();
                social.setPressed(true);
                social.invalidate();
                alarm.performClick();
                alarm.setPressed(true);
                alarm.invalidate();
                calendar.performClick();
                calendar.setPressed(true);
                calendar.invalidate();
                email.performClick();
                email.setPressed(true);
                email.invalidate();
                call.performClick();
                call.setPressed(true);
                call.invalidate();
                missedCall.performClick();
                missedCall.setPressed(true);
                missedCall.invalidate();
            }
        };

        notif.setOnClickListener(hNotif);
        notifHigh.setOnClickListener(hNotifHigh);
        batteryLow.setOnClickListener(hBatteryLow);
        batteryCritical.setOnClickListener(hBatteryCritical);
        privateNotif.setOnClickListener(hPrivateNotif);
        social.setOnClickListener(hSocial);
        alarm.setOnClickListener(hAlarm);
        calendar.setOnClickListener(hCalendar);
        email.setOnClickListener(hEmail);
        call.setOnClickListener(hCall);
        missedCall.setOnClickListener(hMissedCall);
        clearAll.setOnClickListener(hClearAll);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        MainActivity.TurnOn_IMA(false,"00");
        super.onPause();

    }


    @Override
    public void onBackPressed() {
        MainActivity.TurnOn_IMA(false,"00");
        super.onBackPressed();
    }

}

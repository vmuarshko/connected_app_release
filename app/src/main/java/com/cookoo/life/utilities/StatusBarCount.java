package com.cookoo.life.utilities;


import android.service.notification.StatusBarNotification;

/**
 * Created By: travis.roberson
 * Date: 1/30/14
 * Time: 12:31 PM
 */
public class StatusBarCount {

    int count = 0;
    StatusBarNotification sbn = null;


    public StatusBarCount(StatusBarNotification statusBar, int cnt){
        sbn = statusBar;
        count = cnt;
    }


    public int getCount() {
        return count;
    }


    public void setCount(int count) {
        this.count = count;
    }

    public StatusBarNotification getSbn() {
        return sbn;
    }

    public void setSbn(StatusBarNotification sbn) {
        this.sbn = sbn;
    }









}

package com.cookoo.life.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.push.gcm.BaseIntentReceiver;

/**
 * Created By: travis.roberson
 * Date: 11/25/13
 * Time: 10:59 AM
 */
public class NotificareIntentService extends BaseIntentReceiver {

    private static final String TAG = "NotificareIntentService";


    @Override
    public void onNotificationReceived(String s, String s2, Bundle bundle) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onNotificationOpened(String s, String s2, Bundle bundle) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onRegistrationFinished(String deviceId) {
        //Get the preferences
        Context ctx = Notificare.shared().getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences("com.connected.life", Context.MODE_PRIVATE);

        Notificare.shared().registerDevice(deviceId, prefs.getString("userId", null),
                new NotificareCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d(TAG, "Successfully registered");
                    }

                    @Override
                    public void onError(NotificareError error) {
                        Log.e(TAG, "Error registering device", error);
                    }
                });

    }




}

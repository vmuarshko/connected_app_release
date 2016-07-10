package com.cookoo.life.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.cookoo.life.service.AccountService;
import com.cookoo.life.user.User;
import re.notifica.Notificare;

/**
 * Created By: travis.roberson
 * Date: 11/27/13
 * Time: 11:55 AM
 */
public class UserUtil {

    private static String TAG = UserUtil.class.getSimpleName();

    public static boolean isUserSignedIn() {
        String username = "";
        try {
            username = getUserName();
            User user = new User(username, getPassword());
            return user.isLoggedIn();
        } catch (Exception ex) {
            Log.e(TAG, String.format("exception checking for logged in user %s", username), ex);
        }
        return false;
    }

    public static String getUserName() {
        String username;
        try {
            Context ctx = Notificare.shared().getApplicationContext();
            SharedPreferences prefs = ctx.getSharedPreferences(AccountService.ACCOUNT_PREFS, Context.MODE_PRIVATE);
            username = prefs.getString(AccountService.USER_NAME, "");
            return username;
        } catch (Exception ex) {
            ex.printStackTrace();
            username = "";
        }
        return username;
    }

    public static String getPassword() {
        String password;
        try {
            Context ctx = Notificare.shared().getApplicationContext();
            SharedPreferences prefs = ctx.getSharedPreferences(AccountService.ACCOUNT_PREFS, Context.MODE_PRIVATE);
            password = prefs.getString(AccountService.PASSWORD, "");
        } catch (Exception ex) {
            ex.printStackTrace();
            password = "";
        }
        return password;
    }

}

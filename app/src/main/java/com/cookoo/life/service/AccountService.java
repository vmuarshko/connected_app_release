package com.cookoo.life.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.cookoo.life.activity.AccountActivity;
import com.cookoo.life.activity.NewUserActivity;
import com.cookoo.life.activity.SignInActivity;
import com.cookoo.life.user.User;
import com.cookoo.life.utilities.DevInfo;
import com.stackmob.sdk.api.StackMob;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.exception.StackMobException;
import com.stackmob.sdk.model.StackMobUser;
import org.json.JSONObject;
import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;

/**
 * Service class to handle all account related activities
 * Created By: travis.roberson
 * Date: 11/20/13
 * Time: 3:11 PM
 */
public class AccountService extends IntentService {

    public static final String TAG = "AccountService";
    public static final String USER_NAME = "user_name";
    public static final String PASSWORD = "password";
    public static final String CONFIRM_PASS = "confirm_pass";
    public static final String NEW_PASSWORD = "new_password";
    public static final String TOKEN = "token";
    public static final String RESPONSE_SUCCESS = "response_success";
    public static final String LOGOUT_SUCCESS = "logout_success";
    public static final String FORGOT_PASS_SUCCESS = "forgot_pass_success";
    public static final String CHANGE_PASS_SUCCESS = "change_pass_success";
    public static final String RESPONSE_ERROR = "response_err";
    public static final String LAST_NAME = "last_name";
    public static final String FIRST_NAME = "first_name";
    public static final String INTENT_USER_CREATE = "user_create";
    public static final String INTENT_LOGOUT = "logout";
    public static final String INTENT_LOGIN = "login";
    public static final String INTENT_RESET_PASS = "reset_pass";
    public static final String INTENT_FORGOT_PASS = "forgot_pass";
    public static final String INTENT_CHK_LOGIN_STATUS = "chk_login_status";
    public static final String INTENT_REGEN_TOKEN = "regen_token";
    public static final String VAL_BLANK_USER = "Username cannot be blank";
    public static final String VAL_BLANK_PASS = "Password cannot be blank";
    public static final String VAL_MATCH_PASS = "Passwords must match";
    public static final String VAL_BLANK_FIRST = "First name cannot be blank";
    public static final String VAL_BLANK_LAST = "Last name cannot be blank";
    public static final String ACCOUNT_PREFS = "com.connected.life";
    public static final String VAL_USER_EXIST = "An account has already been created on this device.";
    public static final String INVALID_USER = "Invalid Username/password";
    public static final String USER_CREATE_FAILED = "Failed to create user account";
    private final String NOTIF_ACCESS_TOKEN_REQ = "notificare_accesstoken";
    private final String NOTIF_GEN_NEW_TOKEN_REQ = "notificare_generatetoken";
    private final String ACCESS_TOKEN = "accessToken";
    public static final String CREATE_TOKEN_SUCCESS = "create_token_success";
    public static final String GEN_TOKEN_SUCCESS = "gen_token_success";


    public AccountService() {
        super("AccountService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        if(intent.hasExtra(INTENT_USER_CREATE)){
            createUser(intent.getStringExtra(USER_NAME), intent.getStringExtra(PASSWORD),
                    intent.getStringExtra(CONFIRM_PASS), intent.getStringExtra(FIRST_NAME),
                    intent.getStringExtra(LAST_NAME));
        }
        else if(intent.hasExtra(INTENT_FORGOT_PASS)){
            sendForgotPasswordEmail();
        }
        else if(intent.hasExtra(INTENT_LOGIN)){
            login(intent.getStringExtra(USER_NAME), intent.getStringExtra(PASSWORD));
        }
        else if(intent.hasExtra(INTENT_LOGOUT)){
            logout();
        }
        else if(intent.hasExtra(INTENT_RESET_PASS)){
            changePassword(intent.getStringExtra(PASSWORD), intent.getStringExtra(NEW_PASSWORD));
        }
        else if(intent.hasExtra(INTENT_CHK_LOGIN_STATUS)){
            checkIfLoggedIn();
        }
        else if (intent.hasExtra(INTENT_REGEN_TOKEN)){
            reGenToken();
        }
        else {} //do nothing, no idea what we got here

    }


    /**
     * logout the user currently belonging to the device
     */
    private void logout(){
        final Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(AccountActivity.ResponseReceiver.ACTION_RESP);
        User user = new User(getUserName(), getPassword());
        user.logout(new StackMobModelCallback() {
            @Override
            public void success() {
                broadcastIntent.putExtra(LOGOUT_SUCCESS, true);
                sendBroadcast(broadcastIntent);
            }
            @Override
            public void failure(StackMobException e) {
                e.printStackTrace();
                broadcastIntent.putExtra(LOGOUT_SUCCESS, false);
                sendBroadcast(broadcastIntent);
            }
        });

    }

    /**
     * change a user password
     * @param currentPassword
     * @param newPassword
     */
    private void changePassword(String currentPassword, final String newPassword){
        final Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(AccountActivity.ResponseReceiver.ACTION_RESP);
        User user = new User(getUserName(), currentPassword);
            user.resetPassword(currentPassword, newPassword, new StackMobModelCallback() {
            @Override
            public void success() {
                savePassword(newPassword);
                broadcastIntent.putExtra(CHANGE_PASS_SUCCESS, true);
                broadcastIntent.putExtra(USER_NAME, getUserName());
                sendBroadcast(broadcastIntent);
            }
            @Override
            public void failure(StackMobException e) {
                e.printStackTrace();
                broadcastIntent.putExtra(CHANGE_PASS_SUCCESS, false);
                broadcastIntent.putExtra(USER_NAME, getUserName());
                sendBroadcast(broadcastIntent);
            }
        });
    }


    /**
     * check if user is logged in
     */
    private void checkIfLoggedIn() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(AccountActivity.ResponseReceiver.ACTION_RESP);
        User user = new User(getUserName(), getPassword());
        if(user.isLoggedIn()) {
            broadcastIntent.putExtra(RESPONSE_SUCCESS, true);
            sendBroadcast(broadcastIntent);
        } else {
            broadcastIntent.putExtra(RESPONSE_SUCCESS, false);
            sendBroadcast(broadcastIntent);
        }
    }



    private String getUserName() {
        Context ctx = Notificare.shared().getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
        String username = prefs.getString(USER_NAME, "");
        return username;
    }

    private String getPassword() {
        Context ctx = Notificare.shared().getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
        String password = prefs.getString(PASSWORD, "");
        return password;
    }

    private void savePassword(String password){
        Context ctx = Notificare.shared().getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PASSWORD, password);
        editor.commit();

    }


    /**
     *  create a user
     * @param userName
     * @param password
     * @param confirmPass
     * @param first
     * @param last
     */
    private void createUser(final String userName, final String password, String confirmPass,
                            final String first, final String last){
        final Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(NewUserActivity.ResponseReceiver.ACTION_RESP);
        if(userName.isEmpty()){
            broadcastIntent.putExtra(RESPONSE_ERROR, VAL_BLANK_USER);
            sendBroadcast(broadcastIntent);
            return;
        } else if(first.isEmpty()){
            broadcastIntent.putExtra(RESPONSE_ERROR, VAL_BLANK_FIRST);
            sendBroadcast(broadcastIntent);
            return;
        } else if(last.isEmpty()){
            broadcastIntent.putExtra(RESPONSE_ERROR, VAL_BLANK_LAST);
            sendBroadcast(broadcastIntent);
            return;
        } else if(password.isEmpty()){
            broadcastIntent.putExtra(RESPONSE_ERROR, VAL_BLANK_PASS);
            sendBroadcast(broadcastIntent);
            return;
        }  else if(!password.equals(confirmPass)){
            broadcastIntent.putExtra(RESPONSE_ERROR, VAL_MATCH_PASS);
            sendBroadcast(broadcastIntent);
            return;
        }

        //Save the user in the cloud and on the device

        Context ctx = Notificare.shared().getApplicationContext();
        final SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
        if(prefs.contains(USER_NAME)){
            broadcastIntent.putExtra(RESPONSE_ERROR, VAL_USER_EXIST);
            sendBroadcast(broadcastIntent);
            return;
        }


        User user = new User(userName, password);
        user.save();

        //Trying to combat lag in user.save prior to login
        try {
            Thread.sleep(1000);
        }  catch (InterruptedException ex){
            ex.printStackTrace();
        }

        user.login(new StackMobModelCallback() {
            @Override
            public void success() {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(USER_NAME, userName);
                editor.putString(PASSWORD, password);
                editor.putString(FIRST_NAME, first);
                editor.putString(LAST_NAME, last);
                editor.commit();
                broadcastIntent.putExtra(RESPONSE_SUCCESS, "");
                sendBroadcast(broadcastIntent);
                registerDevice();
            }

            @Override
            public void failure(StackMobException e) {
                e.printStackTrace();
                broadcastIntent.putExtra(RESPONSE_ERROR, USER_CREATE_FAILED);
                sendBroadcast(broadcastIntent);
            }
        });

    }


    /**
     * Register the device
     */
    public void registerDevice() {
        try {
            //Get the preferences
            Context ctx = Notificare.shared().getApplicationContext();
            SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
            Notificare.shared().registerDevice(DevInfo.ID, prefs.getString(USER_NAME, ""),
                    new NotificareCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            generateToken();
                            Log.d(TAG, "Successfully registered with result:" + result);
                        }

                        @Override
                        public void onError(NotificareError error) {
                            Log.e(TAG, "Error registering device", error);
                        }
                    });

        } catch (Exception ex) {
            Log.e(TAG, "Failed to register device.  Error was: " + ex.getMessage());
        }
    }


    /**
     * Generate a stackmob token
     */
   private void generateToken() {
       StackMob.getStackMob().getDatastore().get(NOTIF_ACCESS_TOKEN_REQ, new StackMobCallback() {
           @Override
           public void success(String responseBody) {
               Log.d(TAG, responseBody);
               try {
                   JSONObject obj = new JSONObject(responseBody);
                   String token = obj.getString(ACCESS_TOKEN);
                   Context ctx = Notificare.shared().getApplicationContext();
                   SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
                   SharedPreferences.Editor editor = prefs.edit();
                   editor.putString(TOKEN, token);
                   editor.commit();

                   Intent broadcastIntent = new Intent();
                   broadcastIntent.setAction(AccountActivity.ResponseReceiver.ACTION_RESP);
                   broadcastIntent.putExtra(CREATE_TOKEN_SUCCESS, true);
                   broadcastIntent.putExtra(TOKEN, token);
                   sendBroadcast(broadcastIntent);

               }   catch (Exception ex){
                    ex.printStackTrace();
               }
           }
           @Override
           public void failure(StackMobException ex) {
              ex.printStackTrace();
              Log.e(TAG, "Failed to generate token.  Message was: " + ex.getMessage());
           }
       });
   }


    /**
     * Generate a new token for an existing user
     */
    private void reGenToken() {
        User user = new User(getUserName(), getPassword());
        //Stackmob is a picky about being logged in
        //lets just make sure they are in fact logged in
        if(!user.isLoggedIn()){
            user.login(new StackMobModelCallback() {
                @Override
                public void success() {
                    Log.d(TAG, "User re-logged in");
                }
                @Override
                public void failure(StackMobException ex) {
                    ex.printStackTrace();
                    return;
                }
            });
        }


        StackMob.getStackMob().getDatastore().get(NOTIF_GEN_NEW_TOKEN_REQ, new StackMobCallback() {
            @Override
            public void success(String responseBody) {
                Log.d(TAG, responseBody);
                try {
                    JSONObject obj = new JSONObject(responseBody);
                    String token = obj.getString(ACCESS_TOKEN);
                    Context ctx = Notificare.shared().getApplicationContext();
                    SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(TOKEN, token);
                    editor.commit();

                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(AccountActivity.ResponseReceiver.ACTION_RESP);
                    broadcastIntent.putExtra(GEN_TOKEN_SUCCESS, true);
                    broadcastIntent.putExtra(TOKEN, token);
                    sendBroadcast(broadcastIntent);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            @Override
            public void failure(StackMobException ex) {
                ex.printStackTrace();
                Log.e(TAG, "Failed to generate token.  Message was: " + ex.getMessage());
            }
        });
    }


            /**
     * Send forgot password email
     */
    private void sendForgotPasswordEmail() {
        final Intent broadcastIntent = new Intent();
        final String username = getUserName();
        broadcastIntent.setAction(AccountActivity.ResponseReceiver.ACTION_RESP);
        StackMobUser.sentForgotPasswordEmail(username, new StackMobModelCallback() {
            @Override
            public void success() {
                broadcastIntent.putExtra(FORGOT_PASS_SUCCESS, true);
                broadcastIntent.putExtra(USER_NAME, username);
                sendBroadcast(broadcastIntent);
            }

            @Override
            public void failure(StackMobException e) {
                broadcastIntent.putExtra(FORGOT_PASS_SUCCESS, false);
                broadcastIntent.putExtra(USER_NAME, "");
                sendBroadcast(broadcastIntent);
            }
        });

    }


    /**
     *
     * @param username
     * @param password
     */
   private void login(final String username, final String password) {
       final Intent broadcastIntent = new Intent();
       broadcastIntent.setAction(SignInActivity.ResponseReceiver.ACTION_RESP);
       Context ctx = Notificare.shared().getApplicationContext();
       final SharedPreferences prefs = ctx.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE);
       if(username.isEmpty()){
           broadcastIntent.putExtra(RESPONSE_ERROR, VAL_BLANK_USER);
           sendBroadcast(broadcastIntent);
           return;
       } else if(password.isEmpty()){
           broadcastIntent.putExtra(RESPONSE_ERROR, VAL_BLANK_PASS);
           sendBroadcast(broadcastIntent);
           return;

       }  else {} //nothing else do do here

       User user = new User(username, password);
       user.login(new StackMobModelCallback() {

           @Override
           public void success() {
               SharedPreferences.Editor editor = prefs.edit();
               editor.putString(USER_NAME, username);
               editor.putString(PASSWORD, password);
               editor.commit();
               generateToken();
               broadcastIntent.putExtra(RESPONSE_SUCCESS, "");
               sendBroadcast(broadcastIntent);
           }

           @Override
           public void failure(StackMobException e) {
               e.printStackTrace();
               broadcastIntent.putExtra(RESPONSE_ERROR, INVALID_USER);
               sendBroadcast(broadcastIntent);

           }
       });
   }
}

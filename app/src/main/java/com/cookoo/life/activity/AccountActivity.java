package com.cookoo.life.activity;

import android.app.*;
import android.content.*;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.cookoo.life.service.AccountService;
import com.cookoo.life.R;
import com.cookoo.life.fragment.ChangePasswordFragment;
import com.cookoo.life.user.User;
import com.cookoo.life.utilities.UserUtil;
import re.notifica.Notificare;

/**
 * Created By: travis.roberson
 * Date: 11/23/13
 * Time: 12:04 AM
 */
public class AccountActivity extends FragmentActivity implements View.OnClickListener {

    private TextView userName, accessToken;
    private Button changePassword, forgotPass, signInOut, createUser, generateToken;
    private ResponseReceiver receiver;
    private final String TAG = "AccountActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);


        changePassword = (Button) findViewById(R.id.changePassword);
        forgotPass = (Button) findViewById(R.id.forgotPassword);
        createUser = (Button) findViewById(R.id.create_new_user);
        signInOut = (Button)  findViewById(R.id.account_sign_in_out);
        generateToken = (Button) findViewById(R.id.generateToken);
        userName = (TextView) findViewById(R.id.user_id);
        accessToken = (TextView) findViewById(R.id.access_token);
        changePassword.setOnClickListener(this);
        forgotPass.setOnClickListener(this);
        signInOut.setOnClickListener(this);

        //Currently disabled for future release
        //generateToken.setOnClickListener(this);
        //createUser.setOnClickListener(this);

        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);

        Context ctx = Notificare.shared().getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences(AccountService.ACCOUNT_PREFS, Context.MODE_PRIVATE);
        if(prefs.contains(AccountService.USER_NAME)){
            String username = prefs.getString(AccountService.USER_NAME, "");
            String password = prefs.getString(AccountService.PASSWORD, "");
            String token = prefs.getString(AccountService.TOKEN, "");
            userName.setText(username);
            accessToken.setText(token);
            User user = new User(username, password);
            if(user.isLoggedIn()){
                signInOut.setText(R.string.sign_out);
                signInOut.setBackgroundColor(getResources().getColor(R.color.red));
                changePassword.setVisibility(View.VISIBLE);
                changePassword.setEnabled(true);
            } else {
                signInOut.setText(R.string.sign_in);
                signInOut.setBackgroundColor(getResources().getColor(R.color.green));
                changePassword.setVisibility(View.INVISIBLE);
                changePassword.setEnabled(false);
            }
        } else {
            userName.setText(R.string.no_user_registered);
            changePassword.setVisibility(View.INVISIBLE);
            changePassword.setEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }



    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP = "com.connected.life.BASE_ACCT_RESP";
        @Override
        public void onReceive(Context context, Intent intent) {
             if(intent.hasExtra(AccountService.LOGOUT_SUCCESS)){
                 if(intent.getBooleanExtra(AccountService.LOGOUT_SUCCESS, true)){
                     signInOut.setText(R.string.sign_in);
                     signInOut.setBackgroundColor(getResources().getColor(R.color.green));
                     changePassword.setVisibility(View.INVISIBLE);
                     changePassword.setEnabled(false);
                 }
             }
             else if(intent.hasExtra(AccountService.FORGOT_PASS_SUCCESS)){
                 handleForgotPass(intent.getBooleanExtra(AccountService.FORGOT_PASS_SUCCESS, true),
                                    intent.getStringExtra(AccountService.USER_NAME));
             }
             else if(intent.hasExtra(AccountService.CHANGE_PASS_SUCCESS)){
                 handleChangePass(intent.getBooleanExtra(AccountService.CHANGE_PASS_SUCCESS, true),
                         intent.getStringExtra(AccountService.USER_NAME));
             }
             else if(intent.hasExtra(AccountService.CREATE_TOKEN_SUCCESS)){
                 accessToken.setText(intent.getStringExtra(AccountService.TOKEN));
             }
             else if(intent.hasExtra(AccountService.GEN_TOKEN_SUCCESS)){
                 accessToken.setText(intent.getStringExtra(AccountService.TOKEN));
             }

        }
    }


    /**
     * Display to user if password change successful
     * @param success
     * @param username
     */
    private void handleChangePass(boolean success, String username ){
        AlertDialog.Builder passwordDialog = new AlertDialog.Builder(this);
        passwordDialog.setTitle(R.string.change_password);
        passwordDialog.setCancelable(false);
        Resources res = getResources();
        passwordDialog.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dialog.cancel();
            }
        });
        if(success){
            passwordDialog.setMessage(res.getString(R.string.pass_change_success) + " " + username);
            AlertDialog alert = passwordDialog.create();
            alert.show();
        }

    }


    /**
     * Determine appropriate sign in/out action to take
     */
    private void handleSignInOut() {
        Context ctx = Notificare.shared().getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences(AccountService.ACCOUNT_PREFS, Context.MODE_PRIVATE);
        if(prefs.contains(AccountService.USER_NAME)){
            if(UserUtil.isUserSignedIn()){
                Intent msgIntent = new Intent(this, AccountService.class);
                msgIntent.putExtra(AccountService.INTENT_LOGOUT, "");
                startService(msgIntent);
            } else {
                    Intent signInIntent = new Intent(this, SignInActivity.class);
                    startActivity(signInIntent);
                }
        } else {  //maybe we should try and re-create a user

            Intent createIntent = new Intent(this, NewUserActivity.class);
            startActivity(createIntent);
        }


    }

    /**
     * Send a user reset password
     * @param resetSuccess
     * @param username
     */
   private void handleForgotPass(boolean resetSuccess, String username){

       AlertDialog.Builder passwordDialog = new AlertDialog.Builder(this);
       passwordDialog.setTitle(R.string.success);
       passwordDialog.setCancelable(false);
       Resources res = getResources();
       passwordDialog.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog,int id) {
               dialog.cancel();
           }
       });
        if(resetSuccess){
          passwordDialog.setMessage(res.getString(R.string.password_sent_to) + " " + username);
        }   else {
            passwordDialog.setMessage(R.string.unable_to_send);
        }
       AlertDialog alert = passwordDialog.create();
       alert.show();
   }


    private void confirmForgotPass(){
        AlertDialog.Builder passwordDialog = new AlertDialog.Builder(this);
        passwordDialog.setTitle(R.string.are_you_sure);
        passwordDialog.setMessage(R.string.clicking_ok);
        passwordDialog.setCancelable(false);
        passwordDialog.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                Intent msgIntent = new Intent(getApplicationContext(), AccountService.class);
                msgIntent.putExtra(AccountService.INTENT_FORGOT_PASS, "");
                startService(msgIntent);
                dialog.cancel();
            }
        });
        passwordDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dialog.cancel();
            }

        });
        AlertDialog alert = passwordDialog.create();
        alert.show();
    }



    private void showChangePassDialog() {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentById(R.id.change_password);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = new ChangePasswordFragment();
        newFragment.show(ft, "dialog");

    }

    private void handleGenToken() {
        Log.d(TAG, "Generate a new token");
        Intent msgIntent = new Intent(this, AccountService.class);
        msgIntent.putExtra(AccountService.INTENT_REGEN_TOKEN, "");
        startService(msgIntent);


    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case(R.id.account_sign_in_out):
                handleSignInOut();
                break;
            case(R.id.changePassword):
                showChangePassDialog();
                break;
            case(R.id.create_new_user):
                Intent createIntent = new Intent(this, NewUserActivity.class);
                startActivity(createIntent);
                break;
            case(R.id.forgotPassword):
                confirmForgotPass();
                break;
            case(R.id.generateToken):
                handleGenToken();
                break;
            default:
                Log.d(TAG, "Default no match for id: " + id);
                break;

        }
    }
}

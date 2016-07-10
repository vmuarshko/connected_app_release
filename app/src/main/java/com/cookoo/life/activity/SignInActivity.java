package com.cookoo.life.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.cookoo.life.service.AccountService;
import com.cookoo.life.R;
import com.cookoo.life.utilities.UserUtil;

/**
 * Created By: travis.roberson
 * Date: 11/20/13
 * Time: 5:33 PM
 */
public class SignInActivity extends Activity implements View.OnClickListener, View.OnKeyListener {

    private Button signIn, cancel;
    private TextView errorMessage, forgot, trouble, newUser;
    private EditText signInPassword, signInEmail;
    private ProgressBar progress;
    private ResponseReceiver receiver;
    private static final String TAG = "SignInActivity";
    private final String SUPPORT_URL = "http://www.cookoowatch.com/support.html";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_activity);

        signIn = (Button) findViewById(R.id.signIn);
        cancel = (Button) findViewById(R.id.cancelSignIn);
        forgot = (TextView) findViewById(R.id.forgot);
        trouble = (TextView) findViewById(R.id.trouble);
        progress = (ProgressBar) findViewById(R.id.progress);
        newUser = (TextView) findViewById(R.id.new_user);
        signIn.setOnClickListener(this);
        cancel.setOnClickListener(this);
        forgot.setOnClickListener(this);
        trouble.setOnClickListener(this);
        newUser.setOnClickListener(this);

        errorMessage = (TextView) findViewById(R.id.error_message);
        signInEmail = (EditText) findViewById(R.id.sign_in_email);
        if(UserUtil.getUserName() != null)
            signInEmail.setText(UserUtil.getUserName(), TextView.BufferType.EDITABLE);

        signInPassword = (EditText) findViewById(R.id.sign_in_password);
        signInPassword.setOnKeyListener(this);

        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);

    }


    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP = "com.connected.life.SIGN_IN_RESULT";
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra(AccountService.RESPONSE_ERROR)){
                progress.setVisibility(View.INVISIBLE);
                errorMessage.setText(intent.getStringExtra(AccountService.RESPONSE_ERROR));
                errorMessage.setVisibility(View.VISIBLE);
            }
            else {
                progress.setVisibility(View.INVISIBLE);
                createSignInSuccessDialog();
            }
        }
    }


    public boolean onKey(View v, int keyCode, KeyEvent event) {
            Log.d(TAG, "keycode was" + keyCode);
            if(keyCode == KeyEvent.KEYCODE_ENTER) {
                String email = ((EditText) findViewById(R.id.sign_in_email)).getText().toString();
                String password = ((EditText) findViewById(R.id.sign_in_password)).getText().toString();
                if(!email.isEmpty() && !password.isEmpty()){
                    doHandleSignIn();
                    return true;
                } else
                    return false;
            }
            return false;
    }


    @Override
    public void onBackPressed()
    {
        if(!UserUtil.isUserSignedIn()){
            MainActivity.showSplash = false;
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }else {
            super.onBackPressed();
        }
    }



    private void createWarnDialog() {
        AlertDialog.Builder registerDialog = new AlertDialog.Builder(this);
        registerDialog.setTitle(R.string.please_register);
        registerDialog.setCancelable(false);
        registerDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        registerDialog.setMessage(R.string.must_register);
        AlertDialog alert = registerDialog.create();
        alert.show();
    }


    private void createSignInSuccessDialog() {
        AlertDialog.Builder signInDialog = new AlertDialog.Builder(this);
        signInDialog.setTitle(R.string.sign_in_success);
        signInDialog.setCancelable(false);
        signInDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //hide keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(signInEmail.getWindowToken(), 0);
                dialog.cancel();
                Intent accountIntent = new Intent(getApplicationContext(), MainActivity.class);
                accountIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(accountIntent);
            }
        });
        AlertDialog alert = signInDialog.create();
        alert.show();
    }

    public void doHandleSignIn() {
        Log.d(TAG, "preparing to register a new user");
        progress.setVisibility(View.VISIBLE);
        String email = ((EditText) findViewById(R.id.sign_in_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.sign_in_password)).getText().toString();

        Intent msgIntent = new Intent(this, AccountService.class);
        msgIntent.putExtra(AccountService.INTENT_LOGIN, "");
        msgIntent.putExtra(AccountService.USER_NAME, email);
        msgIntent.putExtra(AccountService.PASSWORD, password);
        startService(msgIntent);
    }


    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }


    private void handleCancelPressed(){
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        errorMessage.setVisibility(View.INVISIBLE);
        switch(id){
            case R.id.signIn:
                  doHandleSignIn();
                  break;
            case R.id.cancelSignIn:
                handleCancelPressed();
                break;
            case R.id.trouble:
                Uri uri = Uri.parse(SUPPORT_URL);
                Intent supportIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(supportIntent);
                break;
            case R.id.forgot:
                Intent accountIntent = new Intent(this, AccountActivity.class);
                startActivity(accountIntent);
                break;
            case R.id.new_user:
                Intent new_user = new Intent(this, NewUserActivity.class);
                startActivity(new_user);
            default:
                break;
        }
    }

}

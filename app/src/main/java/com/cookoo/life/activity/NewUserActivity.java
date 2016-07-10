package com.cookoo.life.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.cookoo.life.service.AccountService;
import com.cookoo.life.service.RegistrationService;
import com.cookoo.life.utilities.UserUtil;
import com.cookoo.life.R;


/**
 * NewUserActivity handles all user creation activities
 * User: travis.roberson
 * Date: 11/20/13
 * Time: 10:58 AM
 */
public class NewUserActivity extends Activity implements View.OnClickListener {


    private EditText firstName, lastName, email, password, confirmPassword;
    private Button signUp, cancel;
    private TextView errorMessage, trouble, existing_user;
    private ProgressBar progress;
    private String TAG = "NewUserActivity";
    private final String SUPPORT_URL = "http://www.cookoowatch.com/support.html";
    private ResponseReceiver receiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText)  findViewById(R.id.lastName);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirmPassword);
        signUp = (Button) findViewById(R.id.signUp);
        cancel = (Button) findViewById(R.id.cancelSignUp);
        errorMessage = (TextView) findViewById(R.id.error_message);
        trouble = (TextView) findViewById(R.id.trouble);
        progress = (ProgressBar) findViewById(R.id.progress);
        existing_user = (TextView) findViewById(R.id.exist_user);

        signUp.setOnClickListener(this);
        cancel.setOnClickListener(this);
        trouble.setOnClickListener(this);
        existing_user.setOnClickListener(this);

        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
    }


    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver);
        super.onDestroy();
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


    private void createRegSuccessDialog() {
        AlertDialog.Builder registerDialog = new AlertDialog.Builder(this);
        registerDialog.setTitle(R.string.regist_success);
        registerDialog.setCancelable(false);
        registerDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //hide keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(firstName.getWindowToken(), 0);
                dialog.cancel();
                Intent accountIntent = new Intent(getApplicationContext(), MainActivity.class);
                accountIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(accountIntent);
            }
        });
        //registerDialog.setMessage("Thank you for signing up!");
        AlertDialog alert = registerDialog.create();
        alert.show();
    }



    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP = "com.connected.life.NEW_USER_RESULT";
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra(AccountService.RESPONSE_ERROR)){
                progress.setVisibility(View.INVISIBLE);
                errorMessage.setText(intent.getStringExtra(AccountService.RESPONSE_ERROR));
                errorMessage.setVisibility(View.VISIBLE);
            } else {
                progress.setVisibility(View.INVISIBLE);
                createRegSuccessDialog();
                Intent registerService = new Intent(getApplicationContext(), RegistrationService.class);
                registerService.putExtra(RegistrationService.INTENT_REGISTER, "");
                startService(registerService);
            }

        }
    }

    /**
     * Get details and fire it off to account service
     */
    public void doHandleSignUp() {
        Log.d(TAG, "preparing to register a new user");
        progress.setVisibility(View.VISIBLE);
        String firstName = ((EditText) findViewById(R.id.firstName)).getText().toString();
        String lastName = ((EditText) findViewById(R.id.lastName)).getText().toString();
        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        String confirmpass = ((EditText) findViewById(R.id.confirmPassword)).getText().toString();

        Intent msgIntent = new Intent(this, AccountService.class);
        msgIntent.putExtra(AccountService.INTENT_USER_CREATE, "");
        msgIntent.putExtra(AccountService.USER_NAME, email);
        msgIntent.putExtra(AccountService.FIRST_NAME, firstName);
        msgIntent.putExtra(AccountService.LAST_NAME, lastName);
        msgIntent.putExtra(AccountService.PASSWORD, password);
        msgIntent.putExtra(AccountService.CONFIRM_PASS, confirmpass);
        startService(msgIntent);
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
            case R.id.signUp:
                doHandleSignUp();
                break;
            case R.id.cancelSignUp:
                handleCancelPressed();
                break;
            case R.id.trouble:
                Uri uri = Uri.parse(SUPPORT_URL);
                Intent supportIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(supportIntent);
                break;
            case R.id.exist_user:
                Intent existing_user = new Intent(this,SignInActivity.class);
                startActivity(existing_user);
                break;
            default:
                break;
        }
    }


}

package com.cookoo.life.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.cookoo.life.R;
import com.cookoo.life.service.BluetoothService;

/**
 * Created with IntelliJ IDEA.
 * User: travis.roberson
 * Date: 11/20/13
 * Time: 12:32 PM
 */
public class GeneralSettingsActivity extends Activity implements View.OnClickListener {

    private Button accounts, support, feedback, legal, store, deviceDemo, watchInfo;
    private TextView version;
    private final String SUPPORT_URL = "http://www.cookoowatch.com/support.html";
    private final String STORE_URL = "https://store-ca36a.mybigcommerce.com";
    private final String PRIVACY_URL = "http://cookoowatch.com/privacy.html";
    private final String TERMS_URL = "http://cookoowatch.com/terms%20and%20conditions.html";
    private final String SUPPORT_EMAIL = "support@cookoowatch.com";
    private final String CONN_FEEDBACK = "Connected Watch Feedback";
    private final String TAG = GeneralSettingsActivity.class.getSimpleName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.general_settings);

        try {
            String versionName = MainActivity.activity.getPackageManager().
                    getPackageInfo(MainActivity.activity.getPackageName(), 0).versionName;
            version = (TextView) findViewById(R.id.version);
            version.setText(versionName);
        }  catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "Failed to get version name for package.  Probably was missing in Manifest");
        }


        accounts = (Button) findViewById(R.id.accounts);
        support = (Button) findViewById(R.id.support);
        feedback = (Button) findViewById(R.id.feedback);
        legal = (Button) findViewById(R.id.legal);
        store = (Button) findViewById(R.id.store);
        deviceDemo = (Button) findViewById(R.id.deviceDemo);
        watchInfo = (Button) findViewById(R.id.watchInfo);


        accounts.setOnClickListener(this);
        support.setOnClickListener(this);
        feedback.setOnClickListener(this);
        legal.setOnClickListener(this);
        store.setOnClickListener(this);
        deviceDemo.setOnClickListener(this);
        watchInfo.setOnClickListener(this);

    }


    @Override
    public void onBackPressed(){
        MainActivity.showSplash = false;
        super.onBackPressed();
    }


    @Override
    public void onClick(View v) {
       int id = v.getId();
       Intent intent = null;
       Uri uri;
       switch(id){
           case R.id.watchInfo:
               if (BluetoothService.getInstance().isConnected()) {
                   intent = new Intent(this, DeviceInfoActivity.class);
               } else {
                   createNotConnectedDialog();
               }
               break;
           case R.id.accounts:
               intent = new Intent(this, AccountActivity.class);
               break;
           case R.id.support:
               uri = Uri.parse(SUPPORT_URL);
               intent = new Intent(Intent.ACTION_VIEW, uri);
               break;
           case R.id.feedback:
               intent = new Intent(android.content.Intent.ACTION_SEND);
               intent.setType("message/rfc822");
               intent.putExtra(Intent.EXTRA_EMAIL, new String[]{SUPPORT_EMAIL});
               intent.putExtra(Intent.EXTRA_SUBJECT, CONN_FEEDBACK);
               break;
           case R.id.legal:
               createLegalDialog();
               break;
           case R.id.store:
               uri = Uri.parse(STORE_URL);
               intent = new Intent(Intent.ACTION_VIEW, uri);
               break;
           case R.id.deviceDemo:
               intent = new Intent(this, DemoModeActivity.class);
               break;
           default:
               intent = new Intent(this, GeneralSettingsActivity.class);
               break;
       }

        if(intent != null)
            startActivity(intent);
    }


    private void createNotConnectedDialog() {
        Log.d(TAG,"creating not connected dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.not_connected_title);
        TextView view = new TextView(this);
        view.setText(R.string.not_connected_text);
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create();
        builder.show();
    }



     private void createLegalDialog() {
         Log.d(TAG, "creating legal dialog");
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.legal);
         builder.setItems(R.array.legal_links, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 if (which == 0) {
                     Uri uri = Uri.parse(PRIVACY_URL);
                     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                     startActivity(intent);
                 } else if (which == 1) {
                     Uri uri = Uri.parse(TERMS_URL);
                     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                     startActivity(intent);
                 } else {
                 }
                 //Do nothing
             }
         });
         builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
             }
         });
         builder.create();
         builder.show();
     }



}

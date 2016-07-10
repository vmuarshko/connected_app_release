package com.cookoo.life.fragment;


import android.app.DialogFragment;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.cookoo.life.service.AccountService;
import com.cookoo.life.R;

/**
 * Created By: travis.roberson
 * Date: 11/25/13
 * Time: 5:49 PM
 */
public class ChangePasswordFragment extends DialogFragment implements View.OnClickListener{

    private EditText oldPassword, newPassword, repeatPassword;
    private Button cancel, submit;
    private TextView error;
    private ResponseReceiver receiver;
    private static final String TAG = "ChangePasswordFragment";

    public ChangePasswordFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(R.string.change_password);
        View v = inflater.inflate(com.cookoo.life.R.layout.change_pass_fragment, container, false);
        oldPassword = (EditText) v.findViewById(R.id.oldPass);
        newPassword = (EditText) v.findViewById(R.id.newPass);
        repeatPassword = (EditText) v.findViewById(R.id.reEnterNewPass);
        cancel = (Button) v.findViewById(R.id.cancelReset);
        submit = (Button) v.findViewById(R.id.confirmReset);
        error = (TextView) v.findViewById(R.id.error_message);
        cancel.setOnClickListener(this);
        submit.setOnClickListener(this);

        return v;
    }



    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP = "com.connected.life.BASE_ACCT_RESP";
        @Override
        public void onReceive(Context context, Intent intent) {
             if(intent.hasExtra(AccountService.CHANGE_PASS_SUCCESS)){
                handleChangePass(intent.getBooleanExtra(AccountService.CHANGE_PASS_SUCCESS, true));
            }
        }
    }



    /**
     * Display to user if password change successful
     * @param success
     */
    private void handleChangePass(boolean success){
        if(success) {
            Log.d(TAG, "password change success");
            this.dismiss();
        }else {
            Log.d(TAG, "error changing password");
            error.setText(R.string.incorrect_pass);
            error.setVisibility(View.VISIBLE);
        }
    }





    @Override
    public void onClick(View v) {
       int id = v.getId();
       switch(id){
           case(R.id.cancelReset):
               this.dismiss();
               break;
           case(R.id.confirmReset):
               validateAndReqReset();
               break;
           default:
               Log.d(TAG, "No matching id found for: " + id);
       }

    }




    private void validateAndReqReset(){
        if(oldPassword.getText().toString().isEmpty()){
            error.setText(R.string.old_pass_not_blank);
            error.setVisibility(View.VISIBLE);
        } else if(newPassword.getText().toString().isEmpty()){
            error.setText(R.string.new_pass_not_blank);
            error.setVisibility(View.VISIBLE);
        } else if(!newPassword.getText().toString().equals(repeatPassword.getText().toString())){
            error.setText(R.string.new_pass_no_match);
            error.setVisibility(View.VISIBLE);
        } else{
            error.setText("");
            error.setVisibility(View.INVISIBLE);
            Intent msgIntent = new Intent(getActivity(), AccountService.class);
            msgIntent.putExtra(AccountService.INTENT_RESET_PASS, "");
            msgIntent.putExtra(AccountService.PASSWORD, oldPassword.getText().toString());
            msgIntent.putExtra(AccountService.NEW_PASSWORD, newPassword.getText().toString());
            getActivity().startService(msgIntent);
        }

    }
}






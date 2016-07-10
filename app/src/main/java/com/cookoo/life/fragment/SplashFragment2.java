package com.cookoo.life.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.cookoo.life.R;
import com.cookoo.life.activity.SplashActivity;

/**
 * Created with IntelliJ IDEA.
 * User: travis.roberson
 * Date: 11/20/13
 * Time: 12:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class SplashFragment2 extends Fragment implements View.OnTouchListener{

    private final static String TAG = "SplashFragment2";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.splash_fragment_2, container, false);
        v.setOnTouchListener(this);
        return v;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                SplashActivity.pressed = true;
                break;

            case MotionEvent.ACTION_MOVE:
                //User is moving around on the screen
                break;

            case MotionEvent.ACTION_UP:
                SplashActivity.pressed = false;
                break;
        }
        Log.d(TAG, "pressed was " + SplashActivity.pressed);
        return SplashActivity.pressed;
    }

}

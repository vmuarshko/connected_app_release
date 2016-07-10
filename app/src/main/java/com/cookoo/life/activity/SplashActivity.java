package com.cookoo.life.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.cookoo.life.fragment.SplashFragment1;
import com.cookoo.life.fragment.SplashFragment2;
import com.cookoo.life.fragment.SplashFragment3;
import com.cookoo.life.utilities.UserUtil;
import com.cookoo.life.R;
import com.cookoo.life.fragment.SplashFragment4;

import java.util.Timer;
import java.util.TimerTask;

/**
 * This class runs the demonstration/howto fragment screens for COOKOO
 * User: travis.roberson
 * Date: 11/20/13
 * Time: 10:59 AM
 */
public class SplashActivity extends FragmentActivity implements View.OnClickListener{
    private SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private Button newUser, existUser;
    private TextView skip;
    private final int TOTAL_PAGE_COUNT = 4;
    private final String SPLASH_ACTIVITY = SplashActivity.class.getSimpleName();
    private final int START_DELAY = 3000;  // <- In milliseconds
    private final int TRANS_DELAY = 4000;  // <- In milliseconds
    private final String TAG = "SplashActivity";
    public final static String SKIP_SIGNIN = "SKIP_SIGNIN";
    public static boolean pressed = false;
    Timer timer;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        newUser = (Button) findViewById(R.id.newUser);
        existUser = (Button) findViewById(R.id.existUser);
        skip = (TextView) findViewById(R.id.skip);
        newUser.setOnClickListener(this);
        existUser.setOnClickListener(this);
        skip.setOnClickListener(this);

        if(UserUtil.isUserSignedIn()){
            newUser.setVisibility(View.GONE);
            existUser.setVisibility(View.GONE);
            skip.setVisibility(View.GONE);
            newUser.setEnabled(false);
            existUser.setEnabled(false);
            skip.setEnabled(false);
        } else {
            newUser.setVisibility(View.VISIBLE);
            existUser.setVisibility(View.VISIBLE);
            skip.setVisibility(View.VISIBLE);
            newUser.setEnabled(true);
            existUser.setEnabled(true);
            skip.setEnabled(true);
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // Code to schedule the page flip timer
        timer = new Timer();
        timer.schedule(new UpdatePageTask(), START_DELAY, TRANS_DELAY);

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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
         if(id == newUser.getId()){
            intent = new Intent(SplashActivity.this, NewUserActivity.class);
        } else if(id == existUser.getId()){
            intent = new Intent(SplashActivity.this, SignInActivity.class);
        }  else if(id == skip.getId()) {
             intent = new Intent(SplashActivity.this, MainActivity.class);
             intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
             MainActivity.showSplash = false;
        } else
            intent = new Intent(SplashActivity.this, SplashActivity.class);

        startActivity(intent);
    }



    /**
     * Page flip timer
     */
    class UpdatePageTask extends TimerTask {
        public void run() {
            Log.d(SPLASH_ACTIVITY, "setting page via timer");
            int currentItem = mViewPager.getCurrentItem();

            if(!pressed){
                if(currentItem == TOTAL_PAGE_COUNT -1){
                    switchPage(0);
                }  else {
                    switchPage(++currentItem);
                }
            }
        }
    }

    /**
     * flip a page based on position
     * @param position
     */
    private void switchPage(final int position){
        this.runOnUiThread(new Runnable() {
            public void run() {
                mViewPager.setCurrentItem(position, true); // <- enable smooth scrolling
            }
        });
    }


    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages. This provides the data for the {@link android.support.v4.view.ViewPager}.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Get fragment corresponding to a specific position. This will be used to populate the
         * contents of the {@link android.support.v4.view.ViewPager}.
         * @param position Position to fetch fragment for.
         * @return Fragment for specified position.
         */
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position){
                case 0:
                    fragment = new SplashFragment1();
                    break;
                case 1:
                    fragment = new SplashFragment2();
                    break;
                case 2:
                    fragment = new SplashFragment3();
                    break;
                case 3:
                    fragment = new SplashFragment4();
                    break;
                default:
                    fragment = new SplashFragment1();
                    break;
            }
            return fragment;
        }

        /**
         * Get number of pages the {@link android.support.v4.view.ViewPager} should render.
         * @return Number of fragments to be rendered as pages.
         */
        @Override
        public int getCount() {
            return TOTAL_PAGE_COUNT;
        }

    }
}

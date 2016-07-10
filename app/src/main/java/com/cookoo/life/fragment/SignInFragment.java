package com.cookoo.life.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cookoo.life.R;

/**
 * Created By: travis.roberson
 * Date: 11/20/13
 * Time: 5:20 PM
 */
public class SignInFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.signin_activity, container, false);

        return v;
    }


}

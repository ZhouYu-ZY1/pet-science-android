package com.zhouyu.android_create.fragments;

import android.app.Activity;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class BaseFragment extends Fragment {
    public View view;
    public final <T extends View> T findViewById(int id) {
        return view.findViewById(id);
    }
    public void runUiThread(Runnable runnable) {
        FragmentActivity activity = getActivity();
        if(activity != null){
            activity.runOnUiThread(runnable);
        }
    }

    public Runnable executeThread(Runnable runnable){
        try {
            new Thread(runnable).start();
        }catch (Exception e){
            e.printStackTrace();
        }
        return runnable;
    }
}

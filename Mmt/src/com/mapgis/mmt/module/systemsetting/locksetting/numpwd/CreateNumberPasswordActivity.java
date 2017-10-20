package com.mapgis.mmt.module.systemsetting.locksetting.numpwd;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.mapgis.mmt.R;

public class CreateNumberPasswordActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_number_password);
        initView();
    }

    private void initView(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_number_password, CreateNumberPasswordFragment.newInstance());
        fragmentTransaction.commit();
    }
}

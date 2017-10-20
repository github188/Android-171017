package com.mapgis.mmt.module.login;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;

import java.io.IOException;

public class SystemSetter extends BaseActivity {
    protected SystemSetterFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    protected void init() {
        findViewById(R.id.baseActionBarImageView).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        fragment = new SystemSetterFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.baseFragment, fragment);
        ft.show(fragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        String server = ((EditText) findViewById(R.id.txtServer)).getText()
                .toString().replaceAll("\\s*","");
        String port = ((EditText) findViewById(R.id.txtPort)).getText()
                .toString().replaceAll("\\s*","");
        String virtualPath = ((EditText) findViewById(R.id.txtVirtualPath))
                .getText().toString().replaceAll("\\s*","");

        try {
            fragment.saveServerSetter(server, port, virtualPath);

        } catch (IOException e) {
            e.printStackTrace();
        }

        this.finish();

        MyApplication.getInstance().finishActivityAnimation(SystemSetter.this);
    }
}

package com.mapgis.mmt.module.gis.toolbar.query.point;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.panoramaview.PanoramaViewListener;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;


public class PanoramaActivity extends BaseActivity {
    private PanoramaView mPanoView;
    private static final String LTAG = "BaiduPanoSDKDemo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panorama);
        this.setSwipeBackEnable(false);
        initBMapManager();

        Intent intent = getIntent();
        if (intent != null){//114.413968,30.47007
            double lat = intent.getDoubleExtra("latitude",30.47007);
            double lon = intent.getDoubleExtra("longitude",114.413968);
            initView(lat,lon);
        }
    }
    private void initBMapManager() {
        MyApplication app = (MyApplication) this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(app);
            app.mBMapManager.init(new  MyApplication.MyGeneralListener());
        }
    }
    private void initView(double lat,double lon) {
        mPanoView = (PanoramaView) findViewById(R.id.panorama);
        mPanoView.setShowTopoLink(true);
        mPanoView.setPanoramaViewListener(new PanoramaViewListener() {

            @Override
            public void onLoadPanoramaBegin() {
                Log.i(LTAG, "onLoadPanoramaStart...");
            }

            @Override
            public void onLoadPanoramaEnd(String json) {
                Log.i(LTAG, "onLoadPanoramaEnd : " + json);
            }

            @Override
            public void onLoadPanoramaError(String error) {
                Log.i(LTAG, "onLoadPanoramaError : " + error);
                Toast.makeText(MyApplication.getInstance().getApplicationContext(),
                        "请在AndoridManifest.xml中输入正确的授权Key,并检查您的网络连接是否正常！error: " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDescriptionLoadEnd(String json) {

            }

            @Override
            public void onMessage(String msgName, int msgType) {

            }

            @Override
            public void onCustomMarkerClick(String key) {

            }
        });
        //textTitle.setText(R.string.demo_desc_geo);

//        double lat1 = 30.470061;
//        double lon1 = 114.413952;
        mPanoView.setPanorama(lon, lat);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}

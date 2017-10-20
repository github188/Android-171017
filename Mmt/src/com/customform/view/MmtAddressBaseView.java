package com.customform.view;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gps.GpsReceiver;

/**
 * Created by zoro at 2017/9/12.
 */
abstract class MmtAddressBaseView extends MmtBaseView{
    MmtAddressBaseView(Context context, GDControl control) {
        super(context, control);
    }

    class BDGeocoderResultTask extends AsyncTask<String, String, BDGeocoderResult> {

        private final Handler handler;

        BDGeocoderResultTask(Handler handler) {
            super();
            this.handler = handler;
        }

        @Override
        protected BDGeocoderResult doInBackground(String... params) {
            Location location = GpsReceiver.getInstance().getLastLocation();

            return BDGeocoder.find(location);
        }

        @Override
        protected void onPostExecute(BDGeocoderResult bdResult) {
            Message msg = handler.obtainMessage();
            msg.what = 1;
            msg.obj = bdResult;
            msg.sendToTarget();
        }
    }
}

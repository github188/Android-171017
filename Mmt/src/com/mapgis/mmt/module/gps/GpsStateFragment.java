package com.mapgis.mmt.module.gps;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;

import java.text.DecimalFormat;

public class GpsStateFragment extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        return inflater.inflate(R.layout.gps_state, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        GpsXYZ xyz = getArguments().getParcelable("xyz");

        if (xyz == null) {
            return;
        }

        LinearLayout layoutRoot = (LinearLayout) view.findViewById(R.id.layoutRoot);

        for (int i = 0; i < layoutRoot.getChildCount(); i++) {
            LinearLayout layout = (LinearLayout) layoutRoot.getChildAt(i);

            String text = "";

            try {
                DecimalFormat decimalFormat = new DecimalFormat(".000");

                switch (i) {
                    case 0:
                        text = xyz.getReportTime();
                        break;
                    case 1:
                        text = String.valueOf(xyz.getLocation().getLatitude());
                        break;
                    case 2:
                        text = String.valueOf(xyz.getLocation().getLongitude());
                        break;
                    case 3:
                        text = String.valueOf(xyz.getLocation().getAccuracy());
                        break;
                    case 4:
                        text = xyz.getLocation() != null && xyz.getLocation().getProvider() != null ? xyz.getLocation().getProvider() : "";
                        break;
                    case 5:
                        text = decimalFormat.format(xyz.getX());
                        break;
                    case 6:
                        text = decimalFormat.format(xyz.getY());
                        break;
                    case 7:
                        text = xyz.battery;
                        break;
                    case 8:
                        text = xyz.cpu;
                        break;
                    case 9:
                        text = xyz.memory;
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();

                text = "";
            }

            ((TextView) layout.getChildAt(2)).setText(text);
        }
    }
}

package com.repair.common;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.global.MmtBaseTask;
import com.mapgis.mmt.module.gis.SelectMapPointCallback;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.Poi;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.List;

public class PositionChoseFragment extends Fragment {
    private Activity context;
    PopupWindow popupWindow = null;
    private TextView xyTxt;
    private EditText BDaddressEdt;
    private String address;
    private List<String> names;

    public TextView getXyTxt() {
        return xyTxt;
    }

    public EditText getBDaddressEdt() {
        return BDaddressEdt;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.context = getActivity();

        View view = inflater.inflate(R.layout.position_chose_view, container, false);

        xyTxt = (TextView) view.findViewById(R.id.xyTxt);
        BDaddressEdt = (EditText) view.findViewById(R.id.BDaddressEdt);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        String pos = getActivity().getIntent().getStringExtra("selectCoordinate");
        String add = getActivity().getIntent().getStringExtra("address");

        view.findViewById(R.id.ivAddrMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (names != null && names.size() > 0) {
                    ListDialogFragment fragment = new ListDialogFragment("请选择所在位置", names);
                    fragment.show(getActivity().getSupportFragmentManager(), "");
                    fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int arg2, String value) {
                            BDaddressEdt.setText(address + "-" + value);
                            popupWindow.dismiss();
                        }
                    });
                } else {
                    Toast.makeText(context, "没有更多位置信息", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (!TextUtils.isEmpty(pos)) {
            xyTxt.setOnClickListener(null);
            xyTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            xyTxt.setText(pos);
            BDaddressEdt.setText(TextUtils.isEmpty(add) ? "" : add);

            String[] xy = pos.split(",");

            showGPSLocation(Double.valueOf(xy[0]), Double.valueOf(xy[1]));
        } else {
            xyTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (popupWindow != null) {
                        popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                    }
                }
            });

            showGPSLocation();
        }

        View contentView = LayoutInflater.from(context).inflate(R.layout.position_chose_pop, null);

        popupWindow = new PopupWindow(contentView, -1, -2, true);
        popupWindow.setContentView(contentView);
        //设置SelectPicPopupWindow弹出窗体动画效果
        popupWindow.setAnimationStyle(R.style.AnimBottom);

        //实例化一个ColorDrawable颜色为半透明,设置SelectPicPopupWindow弹出窗体的背景
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xb0000000));

        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        contentView.findViewById(R.id.currentPosition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGPSLocation();

                popupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.getFromMap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseMapCallback callback = new SelectMapPointCallback(context, xyTxt.getText().toString());

                MyApplication.getInstance().sendToBaseMapHandle(callback);
                popupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    Location location = null;

    private void showGPSLocation(final Double... xy) {
        if (xy == null || xy.length == 0)
            location = GpsReceiver.getInstance().getLastLocation();
        else
            location = GpsReceiver.getInstance().getLastLocationConverse(new GpsXYZ(xy[0], xy[1]));

        new MmtBaseTask<Location, Integer, BDGeocoderResult>(context, false, new MmtBaseTask.OnWxyhTaskListener<BDGeocoderResult>() {

            @Override
            public void doAfter(BDGeocoderResult bdResult) {
                try {
                    GpsXYZ xyz;

                    if (xy == null || xy.length == 0)
                        xyz = GpsReceiver.getInstance().getLastLocation(location);
                    else
                        xyz = new GpsXYZ(xy[0], xy[1]);

                    xyTxt.setText(xyz.getX() + "," + xyz.getY());

                    address = bdResult.result.addressComponent.district + bdResult.result.addressComponent.street
                            + bdResult.result.addressComponent.street_number;

                    names = new ArrayList<>();
                    for (Poi poi : bdResult.result.pois) {
                        names.add(poi.name);
                    }

                    if (names != null && names.size() > 0) {
                        BDaddressEdt.setText(address + "-" + names.get(0));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            protected BDGeocoderResult doInBackground(Location... params) {
                return BDGeocoder.find(params[0]);
            }
        }.mmtExecute(location);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = context.getIntent();

        String loc = intent.getStringExtra("loc");

        if (BaseClassUtil.isNullOrEmptyString(loc))
            return;

        xyTxt.setText(loc);

        intent.removeExtra("loc");

        if (intent.hasExtra("addr") && intent.hasExtra("names")) {
            this.address = intent.getStringExtra("addr");
            this.names = intent.getStringArrayListExtra("names");

            if (names != null && names.size() > 0) {
                BDaddressEdt.setText(address + "-" + names.get(0));
            } else if (!BaseClassUtil.isNullOrEmptyString(address))
                BDaddressEdt.setText(address);
            else
                BDaddressEdt.setText("");
        }
    }
}

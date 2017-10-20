package com.customform.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.UpdateCurrentAddressEvent;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageDotView;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.global.MmtBaseTask;
import com.mapgis.mmt.module.gis.SelectMapPointCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.Poi;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;

import java.util.ArrayList;
import java.util.List;

/**
 * 坐标V3
 * Created by zoro at 2017/9/1.
 */
class MmtDotThreeView extends MmtDotBaseView implements MmtBaseView.ReadonlyHandleable {
    MmtDotThreeView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_coordinate;
    }

    public View build() {
        final ImageDotView view = new ImageDotView(getActivity());

        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        // view.getValueEditView().setEnabled(false);
        view.getValueEditView().setFocusable(false);

        if (control.Validate.equals("1")) {
            view.isMustDo(true);
        }

        if (control.DefaultValues.length() != 0) {
            view.setValue(control.DefaultValues);
        }

        if (control.Value.length() != 0) {
            view.setValue(control.Value);
        }

        if (control.isReadOnly()) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseMapCallback callback = new ShowMapPointCallback(context, view.getValue(),
                            "", "", -1);
                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
            view.getValueEditView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseMapCallback callback = new ShowMapPointCallback(context, view.getValue(),
                            "", "", -1);
                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
            return view;
        }
        if (TextUtils.isEmpty(view.getValue())) {
            view.setValue(GpsReceiver.getInstance().getLastLocalLocation().getX()
                    + "," + GpsReceiver.getInstance().getLastLocalLocation().getY());
        }

        /////////////////

        View contentView = LayoutInflater.from(context).inflate(R.layout.position_chose_pop, null);
        ImageView imageView = (ImageView) contentView.findViewById(R.id.clearDotLine);
        imageView.setVisibility(View.VISIBLE);
        TextView clearTextView = (TextView) contentView.findViewById(R.id.clearDot);
        clearTextView.setVisibility(View.VISIBLE);
        final PopupWindow popupWindow = new PopupWindow(contentView, -1, -2, true);
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
        TextView curDptTextView = (TextView) contentView.findViewById(R.id.currentPosition);
        curDptTextView.setText("当前");
        curDptTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Location location = GpsReceiver.getInstance().getLastLocation();

                new MmtBaseTask<Location, Integer, BDGeocoderResult>(context, false, new MmtBaseTask.OnWxyhTaskListener<BDGeocoderResult>() {

                    @Override
                    public void doAfter(BDGeocoderResult bdResult) {

                        List<String> addrNames = new ArrayList<>();
                        GpsXYZ xyz = GpsReceiver.getInstance().getLastLocation(location);
                        try {

                            view.setValue(xyz.getX() + "," + xyz.getY());

                            String address = bdResult.result.addressComponent.district + bdResult.result.addressComponent.street
                                    + bdResult.result.addressComponent.street_number;

                            addrNames.add(address);
                            for (Poi poi : bdResult.result.pois) {
                                addrNames.add(poi.name);
                            }
                            view.getValueEditView().setTag(addrNames);

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {

                            String x = String.valueOf(xyz.getX());
                            String y = String.valueOf(xyz.getY());
                            String longitude = String.valueOf(location.getLongitude());
                            String latitude = String.valueOf(location.getLatitude());

                            updateCurrentAddress(new UpdateCurrentAddressEvent(addrNames, x, y, longitude, latitude));
                        }


                    }
                }) {
                    @Override
                    protected BDGeocoderResult doInBackground(Location... params) {
                        return BDGeocoder.find(params[0]);
                    }
                }.mmtExecute(location);

                popupWindow.dismiss();
            }
        });
        TextView fromMapTextView = (TextView) contentView.findViewById(R.id.getFromMap);
        fromMapTextView.setText("选点");
        fromMapTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getActivity().getIntent();
                intent.putExtra("controlName", control.Name);
                getActivity().setIntent(intent);
                BaseMapCallback callback = new SelectMapPointCallback(context, view.getValue());
                MyApplication.getInstance().sendToBaseMapHandle(callback);
                popupWindow.dismiss();
            }
        });
        clearTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setValue("");
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        /////////////////
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
            }
        });
        view.getValueEditView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
            }
        });
        return view;
    }
}

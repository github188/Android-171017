package com.customform.view;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageEditButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.global.MmtBaseTask;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.Poi;
import com.mapgis.mmt.module.gps.GpsReceiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 当前地址
 * Created by zoro on 2017/9/1.
 */
class MmtCurrentBDAddressView extends MmtAddressBaseView implements MmtBaseView.ReadonlyHandleable {
    MmtCurrentBDAddressView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_address;
    }

    /**
     * 创建 当前百度地址 类型视图
     */
    public ImageEditButtonView build() {
        final List<String> values = new ArrayList<>();

        if (control.DefaultValues.contains(",")) {
            values.addAll(Arrays.asList(control.DefaultValues.split(",")));
        } else {
            values.add(control.DefaultValues);
        }

        final ImageEditButtonView view = new ImageEditButtonView(context);

        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        view.setValue(values.size() > 0 ? values.get(0) : "");

        if (control.Validate.equals("1")) {
            view.isMustDo(true);
        }

        if (control.Value.length() != 0) {
            view.setValue(control.Value);
        }
        if (control.isReadOnly()) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    control.Value = view.getValue();
                    BaseMapCallback callback = new ShowMapPointCallback(context, control.Value, "", "", -1);
                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
            view.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    control.Value = view.getValue();
                    BaseMapCallback callback = new ShowMapPointCallback(context, control.Value,
                            "", "", -1);
                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
        } else {
            view.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new MmtBaseTask<Void, Void, BDGeocoderResult>(context, true, new MmtBaseTask.OnWxyhTaskListener<BDGeocoderResult>() {
                        @Override
                        public void doAfter(BDGeocoderResult bdGeocoderResult) {
                            values.clear();
                            try {
                                for (Poi poi : bdGeocoderResult.result.pois) {
                                    values.add(poi.addr + poi.name);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            if (values.size() == 0) {
                                Toast.makeText(context, "未获取到地址，点击重新获取!", Toast.LENGTH_SHORT).show();
                            } else {
                                view.setValue(values.get(0));

                                ListDialogFragment fragment = new ListDialogFragment(control.DisplayName, values);
                                fragment.show(getActivity().getSupportFragmentManager(), "");
                                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                                    @Override
                                    public void onListItemClick(int arg2, String value) {
                                        view.setValue(value);
                                    }
                                });
                            }
                        }
                    }) {
                        @Override
                        protected BDGeocoderResult doInBackground(Void... params) {
                            Location location = GpsReceiver.getInstance().getLastLocation();
                            return BDGeocoder.find(location);
                        }
                    }.mmtExecute();
                }
            });
            if (TextUtils.isEmpty(view.getValue())) {
                // 第一次进入初始化一个地址
                new BDGeocoderResultTask(new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        BDGeocoderResult bdResult = (BDGeocoderResult) msg.obj;
                        values.clear();
                        try {
                            for (Poi poi : bdResult.result.pois) {
                                values.add(poi.addr + poi.name);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        if (values.size() != 0) {
                            view.setValue(values.get(0));
                        }
                    }
                }).execute();
            }
        }
        return view;
    }
}

package com.customform.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageEditButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.Poi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 百度地址
 * Created by zoro at 2017/9/1.
 */
class MmtBDAddressView extends MmtAddressBaseView implements MmtBaseView.ReadonlyHandleable {
    MmtBDAddressView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_address;
    }

    /**
     * 创建 百度地址 类型视图
     */
    public ImageEditButtonView build() {
        final List<String> values = new ArrayList<>();

        if (!TextUtils.isEmpty(control.DefaultValues)) {
            if (control.DefaultValues.contains(",")) {
                values.addAll(Arrays.asList(control.DefaultValues.split(",")));
            } else {
                values.add(control.DefaultValues);
            }
        }

        final ImageEditButtonView view = new ImageEditButtonView(context);

        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        if (control.Validate.equals("1")) {
            view.isMustDo(true);
        }
        view.setValue(!TextUtils.isEmpty(control.Value) ? control.Value : (values.size() > 0 ? values.get(0) : ""));

        if (control.isReadOnly()) {
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    control.Value = view.getValue();
                    BaseMapCallback callback = new ShowMapPointCallback(context, control.Value, "", "", -1);
                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            };
            view.setOnClickListener(clickListener);
            view.button.setOnClickListener(clickListener);

        } else {

            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {

                    values.clear();

                    if (msg.obj == null) {
                        Toast.makeText(context, "未获取到地址，请检查网络", Toast.LENGTH_SHORT).show();
                    } else {
                        BDGeocoderResult bdResult = (BDGeocoderResult) msg.obj;
                        try {
                            for (Poi poi : bdResult.result.pois) {
                                values.add(poi.addr + poi.name);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (values.size() == 0) {
                        values.add("未获取到地址，点击重新获取");
                    } else if (TextUtils.isEmpty(control.Value)) {
                        view.setValue(values.get(0));
                    }

                    // 暂存可选地址列表数据
                    view.button.setTag(values);
                }
            };

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Object obj = view.button.getTag();
                    if (obj instanceof List<?>) {
                        List<String> optValues = (List<String>) obj;
                        ListDialogFragment fragment = new ListDialogFragment(control.DisplayName, optValues);
                        fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "");
                        fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                            @Override
                            public void onListItemClick(int arg2, String value) {
                                if (value.equals("未获取到地址，点击重新获取")) {
                                    new BDGeocoderResultTask(handler).execute();
                                } else {
                                    view.setValue(value);
                                }
                            }
                        });
                    }
                }
            };
            view.setOnClickListener(clickListener);
            view.button.setOnClickListener(clickListener);

            // 地址列表初始化查询
            new BDGeocoderResultTask(handler).execute();
        }
        return view;
    }
}

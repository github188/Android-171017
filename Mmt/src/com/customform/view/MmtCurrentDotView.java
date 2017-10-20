package com.customform.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.module.gps.GpsReceiver;

/**
 * 当前坐标
 * Created by zoro at 2017/9/1.
 */
class MmtCurrentDotView extends MmtDotBaseView implements MmtBaseView.ReadonlyHandleable {
    MmtCurrentDotView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_coordinate;
    }

    /**
     * 当前坐标,显示当前位置的坐标，不提供修改
     */
    public View build() {
        final ImageButtonView view = new ImageButtonView(context);

        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        view.getButton().setImageResource(R.drawable.ic_autorenew);


        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        if (control.DefaultValues.length() != 0) {
            view.setValue(control.DefaultValues);
        }

        if (control.isReadOnly()) {
            view.setValue(TextUtils.isEmpty(control.Value) ? "" : control.Value);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseMapCallback callback = new ShowMapPointCallback(context, ((ImageButtonView) v).getValue(),
                            "", "", -1);
                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
            view.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseMapCallback callback = new ShowMapPointCallback(context, control.Value,
                            "", "", -1);
                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
        } else {
            view.setValue(GpsReceiver.getInstance().getLastLocalLocation().toXY());
            view.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view.setValue(GpsReceiver.getInstance().getLastLocalLocation().getX()
                            + "," + GpsReceiver.getInstance().getLastLocalLocation().getY());
                }
            });
        }
        return view;
    }
}

package com.customform.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageDotView;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gps.GpsReceiver;

/**
 * 坐标
 * Created by zoro at 2017/9/1.
 */
class MmtDotView extends MmtDotBaseView implements MmtBaseView.ReadonlyHandleable {

    MmtDotView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_coordinate;
    }

    /**
     * 创建坐标类型视图
     */
    public ImageDotView build() {
        ImageDotView view = new ImageDotView(getActivity());
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        if (control.Validate.equals("1")) {
            view.isMustDo(true);
        }

        String defaultValue = getDefaultValue();
        if (TextUtils.isEmpty(defaultValue)) {
            defaultValue = GpsReceiver.getInstance().getLastLocalLocation().getX()
                    + "," + GpsReceiver.getInstance().getLastLocalLocation().getY();
        }
        view.setValue(defaultValue);

        view.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(context, MapGISFrame.class);

                    intent.putExtra(MapGISFrame.LONG_TAG_FOR_POINT, true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("action", Class.forName(control.locateBackClass));
                    intent.putExtra("controlName", control.Name);

                    context.startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        return view;
    }
}

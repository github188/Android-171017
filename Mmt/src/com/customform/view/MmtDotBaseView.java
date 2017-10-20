package com.customform.view;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.UpdateCurrentAddressEvent;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.ImageDotView;
import com.mapgis.mmt.common.widget.customview.ImageEditButtonView;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zoro at 2017/9/5.
 */
abstract class MmtDotBaseView extends MmtBaseView {
    MmtDotBaseView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    public boolean onStart(Intent intent) {
        String controlName = intent.getStringExtra("controlName");

        if (TextUtils.isEmpty(controlName) || !controlName.equals(this.control.Name))
            return false;

        //坐标控件处理(包含地址的联动)
        String loc = intent.getStringExtra("location");//兼容旧模式（长按选点）

        if (TextUtils.isEmpty(loc)) {
            loc = intent.getStringExtra("loc");//新模式（地图选点）
        }

        if (!TextUtils.isEmpty(loc)) {
            GDControl control = null;

            if (view instanceof ImageDotView) {
                ImageDotView viewTemp = (ImageDotView) view;
                control = (GDControl) viewTemp.getTag();
                control.Value = loc;
                viewTemp.setTag(control);
                viewTemp.setValue(loc);
            }

            //loc 留着，单点采集又用
            // intent.removeExtra("loc");
            if (control == null) {
                return true;
            }

            {//自动填充横坐标和纵坐标
                String[] xyArr = loc.split(",");
                if (xyArr.length != 2) {
                    return true;
                }
                GpsXYZ gpsXYZ = new GpsXYZ(Double.valueOf(xyArr[0]), Double.valueOf(xyArr[1]));
                Location location = GpsReceiver.getInstance().getLastLocationConverse(gpsXYZ);

                String longitude = location == null ? "" : String.valueOf(location.getLongitude());
                String latitude = location == null ? "" : String.valueOf(location.getLatitude());

                autoFillXY(xyArr[0], xyArr[1], longitude, latitude);
            }

            if (intent.hasExtra("addr") && intent.hasExtra("names")) {
                String address = intent.getStringExtra("addr");
                ArrayList<String> names = intent.getStringArrayListExtra("names");
                //需要将返回的地址赋给相应的地址控件
                String links = control.ConfigInfo;
                //如果没有配置联动的地址，默认取第一个地址作为联动地址
                if (TextUtils.isEmpty(links)) {
                    View viewtemp = findViewByType("百度地址");
                    if (viewtemp instanceof ImageEditButtonView) {
                        ImageEditButtonView imageEditButtonViewtemp = (ImageEditButtonView) viewtemp;
                        positionAddressLink(imageEditButtonViewtemp, address, names);
                    }
                    return true;
                }
                String[] linkArr = links.trim().split(",");
                for (String addrsControlName : linkArr) {
                    if (TextUtils.isEmpty(addrsControlName.trim())) {
                        continue;
                    }
                    View addrControl = findViewByName(addrsControlName.trim());
                    if (addrControl == null) {
                        continue;
                    }
                    if (addrControl instanceof ImageEditButtonView) {
                        ImageEditButtonView addrView = (ImageEditButtonView) addrControl;
                        positionAddressLink(addrView, address, names);
                    }
                }
            }
        }

        return true;
    }

    private void autoFillXY(String x, String y, String longitude, String latitude) {
        x = TextUtils.isEmpty(x) ? "" : x;
        y = TextUtils.isEmpty(y) ? "" : y;

        String config = "自动";
        View xView = findViewByName("横坐标");
        if (xView instanceof ImageTextView) {
            ImageTextView imageTextView = (ImageTextView) xView;
            Object object = imageTextView.getTag();
            if (object instanceof GDControl) {
                GDControl control = (GDControl) object;
                if (config.equals(control.ConfigInfo)) {
                    imageTextView.setValue(x);
                }
            }

        }
        if (xView instanceof ImageEditView) {
            ImageEditView imageEditView = (ImageEditView) xView;

            Object object = imageEditView.getTag();
            if (object instanceof GDControl) {
                GDControl control = (GDControl) object;
                if (config.equals(control.ConfigInfo)) {
                    imageEditView.setValue(x);
                }
            }

        }
        View yView = findViewByName("纵坐标");
        if (yView instanceof ImageTextView) {
            ImageTextView imageTextView = (ImageTextView) yView;

            Object object = imageTextView.getTag();
            if (object instanceof GDControl) {
                GDControl control = (GDControl) object;
                if (config.equals(control.ConfigInfo)) {
                    imageTextView.setValue(y);
                }
            }

        }
        if (yView instanceof ImageEditView) {
            ImageEditView imageEditView = (ImageEditView) yView;


            Object object = imageEditView.getTag();
            if (object instanceof GDControl) {
                GDControl control = (GDControl) object;
                if (config.equals(control.ConfigInfo)) {
                    imageEditView.setValue(y);
                }
            }
        }

        View gView = findViewByName("经度");
        if (gView instanceof ImageTextView) {
            ImageTextView imageTextView = (ImageTextView) gView;

            Object object = imageTextView.getTag();
            if (object instanceof GDControl) {
                GDControl control = (GDControl) object;
                if (config.equals(control.ConfigInfo)) {
                    imageTextView.setValue(longitude);
                }
            }

        }
        if (gView instanceof ImageEditView) {
            ImageEditView imageEditView = (ImageEditView) gView;

            Object object = imageEditView.getTag();
            if (object instanceof GDControl) {
                GDControl control = (GDControl) object;
                if (config.equals(control.ConfigInfo)) {
                    imageEditView.setValue(longitude);
                }
            }
        }
        View tView = findViewByName("纬度");
        if (tView instanceof ImageTextView) {
            ImageTextView imageTextView = (ImageTextView) tView;


            Object object = imageTextView.getTag();
            if (object instanceof GDControl) {
                GDControl control = (GDControl) object;
                if (config.equals(control.ConfigInfo)) {
                    imageTextView.setValue(latitude);
                }
            }
        }
        if (tView instanceof ImageEditView) {
            ImageEditView imageEditView = (ImageEditView) tView;

            Object object = imageEditView.getTag();
            if (object instanceof GDControl) {
                GDControl control = (GDControl) object;
                if (config.equals(control.ConfigInfo)) {
                    imageEditView.setValue(latitude);
                }
            }
        }
    }

    void positionAddressLink(ImageEditButtonView addrView, String address, List<String> names) {
        if (!BaseClassUtil.isNullOrEmptyString(address) && names != null && names.size() > 0) {
            addrView.setValue(address + "-" + names.get(0));
        } else if (!BaseClassUtil.isNullOrEmptyString(address))
            addrView.setValue(address);
        else
            addrView.setValue("");

        ArrayList<String> addressList = new ArrayList<>();

        if (names == null || names.size() == 0) {
            names = new ArrayList<>();
            names.add("未获取到地址，点击重新获取");
        }

        for (String name : names) {
            addressList.add(address + "-" + name);
        }

        addrView.button.setTag(addressList);
    }

    void updateCurrentAddress(UpdateCurrentAddressEvent updateCurrentAddressEvent) {
        if (updateCurrentAddressEvent == null) {
            return;
        }

        autoFillXY(updateCurrentAddressEvent.x, updateCurrentAddressEvent.y, updateCurrentAddressEvent.longitude, updateCurrentAddressEvent.latitude);

        if (updateCurrentAddressEvent.adds == null || updateCurrentAddressEvent.adds.size() == 0) {
            return;
        }

        View viewtemp = findViewByType("百度地址");

        if (viewtemp instanceof ImageEditButtonView) {
            ImageEditButtonView imageEditButtonViewtemp = (ImageEditButtonView) viewtemp;

            positionAddressLink(imageEditButtonViewtemp, updateCurrentAddressEvent.adds.get(0), updateCurrentAddressEvent.adds);
        }
    }

    @Override
    public void onViewCreated(Map<String, Integer> controlIds) {
        super.onViewCreated(controlIds);

        GpsXYZ xy = GpsReceiver.getInstance().getLastLocalLocation();

        if (xy == null)
            return;

        //初次打开界面时初始化横纵坐标
        String x = String.valueOf(xy.getX());
        String y = String.valueOf(xy.getY());

        if (xy.getLocation() != null) {
            String longitude = String.valueOf(xy.getLocation().getLongitude());
            String latitude = String.valueOf(xy.getLocation().getLatitude());

            autoFillXY(x, y, longitude, latitude);
        } else
            autoFillXY(x, y, "", "");
    }
}
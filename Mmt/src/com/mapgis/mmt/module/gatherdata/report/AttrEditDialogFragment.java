package com.mapgis.mmt.module.gatherdata.report;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.CacheUtils;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.customview.FeedBackView;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageEditButtonView;
import com.mapgis.mmt.common.widget.customview.ImageFragmentView;
import com.mapgis.mmt.common.widget.customview.ImageLineView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.common.widget.fragment.PhotoFragment;
import com.mapgis.mmt.module.gatherdata.GatherDataLayer;
import com.mapgis.mmt.module.gatherdata.GatherDataUtils;
import com.mapgis.mmt.module.gatherdata.GatherElementBean;
import com.mapgis.mmt.module.gatherdata.GatherProjectBean;
import com.mapgis.mmt.module.gatherdata.operate.GatherDataOperateBar;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.Poi;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class AttrEditDialogFragment extends DialogFragment {
    // private final String title;
    private final GatherElementBean elementBean;

    /**
     * 图层名称，从GatherDataLayer集合中获取
     */
    private final List<String> layerNames = new ArrayList<String>();

    /**
     * 填写的属性信息，key字段名，value填写值，从GatherDataLayer和GatherElementBean中获取
     */
    private final LinkedHashMap<String, String> attrMap = new LinkedHashMap<String, String>();

    private final List<GatherDataLayer> dataLayers = new ArrayList<GatherDataLayer>();

    private LinearLayout parentLayout;

    private PhotoFragment photoFragment;

    /**
     * 取消按钮监听事件
     */
    private OnButtonClickListener onButtonClickListener;

    private boolean canEdit = true;

    private GatherProjectBean projectBean;

    public AttrEditDialogFragment(String title, GatherElementBean elementBean,
                                  List<GatherDataLayer> layers) {
        // this.title = title;
        this.elementBean = elementBean;
        this.dataLayers.addAll(layers);

        // 初始化图层名称
        for (GatherDataLayer layer : dataLayers) {
            layerNames.add(layer.NodeName);
        }

        if (!BaseClassUtil.isNullOrEmptyString(this.elementBean.NewAttr)) {
            attrMap.putAll(elementBean.getNewAttrMap());
        } else {
            attrMap.putAll(elementBean.getOldAttrMap());
        }
    }

    public AttrEditDialogFragment(String title, GatherElementBean elementBean,
                                  List<GatherDataLayer> layers, GatherProjectBean projectBean) {
        // this.title = title;
        this.projectBean = projectBean;
        this.elementBean = elementBean;
        this.dataLayers.addAll(layers);

        // 初始化图层名称
        for (GatherDataLayer layer : dataLayers) {
            layerNames.add(layer.NodeName);
        }

        if (!BaseClassUtil.isNullOrEmptyString(this.elementBean.NewAttr)) {
            attrMap.putAll(elementBean.getNewAttrMap());
        } else {
            attrMap.putAll(elementBean.getOldAttrMap());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gather_data_attr_edit_view,
                container, false);

        ((TextView) v.findViewById(R.id.tv_ok_cancel_dialog_Tips))
                .setText("编辑目标" + elementBean.GeomType);

        LinearLayout layout = (LinearLayout) v
                .findViewById(R.id.layout_ok_cancel_dialog_content);

        if (!canEdit)
            layout.getLayoutParams().height = DimenTool.dip2px(getActivity(),
                    320);

        layout.addView(createContentView());

        v.findViewById(R.id.btn_cancel)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (onButtonClickListener != null)
                            onButtonClickListener.onCancelClick();

                        Toast.makeText(getActivity(), "点击描点可以再次进行添加",
                                Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });

        if (!elementBean.GeomType.equals("线") && !elementBean.isCaptureDot) {
            v.findViewById(R.id.btn_edit).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (onButtonClickListener != null)
                                onButtonClickListener.onMoveClick();

                            saveNewGraphic();
                            dismiss();
                        }
                    });
        } else {
            v.findViewById(R.id.btn_edit).setVisibility(View.GONE);
        }

        v.findViewById(R.id.btn_ok)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onButtonClickListener != null)
                            onButtonClickListener.onOkClick();
                        saveNewGraphic();
                        Toast.makeText(getActivity(), "点击图形可以再次进行编辑",
                                Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });

        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        int pos = layerNames.indexOf(elementBean.LayerName);

        createFeedbackView(dataLayers.get(pos == -1 ? 0 : pos)
                .getFeedbackAttr());
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    /**
     * 创建内容的视图
     */
    private View createContentView() {
        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        scrollView.setBackgroundColor(Color.WHITE);

        parentLayout = new LinearLayout(getActivity());
        parentLayout.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        parentLayout.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(parentLayout);

        // 第一行为图层列表项
        parentLayout.addView(canEdit ? createLayerView() : createTextView("图层",
                elementBean.LayerName));

        return scrollView;
    }

    /**
     * 根据属性信息，创建反馈界面。
     *
     * @param attrs 反馈字段
     * @return
     */
    private void createFeedbackView(List<String> attrs) {
        // 第一行默认为图层列表
        parentLayout.removeViews(1, parentLayout.getChildCount() - 1);

        if (canEdit) {// 可编辑，即为描点
            for (String attr : attrs) {
                parentLayout.addView(new ImageLineView(getActivity()));
                parentLayout.addView(createView(attr, attrMap.get(attr)));
            }
        } else {// 不可编辑，即为捕捉点

            Iterator<String> iterator = attrMap.keySet().iterator();

            while (iterator.hasNext()) {
                String key = iterator.next();
                if (key.equals("位置")) {
                    this.elementBean.address = attrMap.get(key);
                }
                parentLayout.addView(new ImageLineView(getActivity()));
                parentLayout.addView(createTextView(key, attrMap.get(key)));
            }
        }

        parentLayout.addView(new ImageLineView(getActivity()));

        ImageFragmentView fragmentView = new ImageFragmentView(this);
        fragmentView.setKey("拍照");

        ChangeImg(fragmentView.getImageView(), "拍照");

        parentLayout.addView(fragmentView);

        photoFragment = new PhotoFragment.Builder("GatherData/")
                .setAddEnable(true)
                .setSelectEnable(false)
                .setValue(elementBean.Photo)
                .build();

        if (canEdit) {
            parentLayout.addView(new ImageLineView(getActivity()));
            parentLayout.addView(buildBDAddressView(getActivity()));
        }
        fragmentView.replaceFrameLayout(getChildFragmentManager(), photoFragment);
    }

    private ImageEditButtonView buildBDAddressView(
            final FragmentActivity activity) {

        final ImageEditButtonView view = new ImageEditButtonView(activity);

        view.setTag(this);
        view.setKey("位置");
        // view.setImage(getDrawableIdByType(Type));

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String address = "";
                BDGeocoderResult bdResult = (BDGeocoderResult) msg.obj;
                AttrEditDialogFragment.this.projectBean.addressList.clear();
                try {
                    for (Poi poi : bdResult.result.pois) {
                        AttrEditDialogFragment.this.projectBean.addressList
                                .add(poi.addr + poi.name);
                    }
                } catch (Exception e) {

                }

                if (AttrEditDialogFragment.this.projectBean.addressList.size() == 0) {
                    AttrEditDialogFragment.this.projectBean.addressList
                            .add("未获取到位置，点击重新获取");
                    address = "未获取到位置";
                } else {
                    address = AttrEditDialogFragment.this.projectBean.addressList
                            .get(0);
                }
                view.setValue(address);
                AttrEditDialogFragment.this.elementBean.address = address;
            }
        };
        view.button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialogFragment fragment = new ListDialogFragment("地址",
                        AttrEditDialogFragment.this.projectBean.addressList);
                fragment.show(activity.getSupportFragmentManager(), "");
                fragment.setListItemClickListener(new OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {
                        if (value.equals("未获取到位置，点击重新获取")) {
                            new BDGeocoderResultTask(handler,
                                    AttrEditDialogFragment.this.projectBean.dot)
                                    .execute();
                            view.setValue("定位中...");
                            return;
                        }
                        view.setValue(value);
                        AttrEditDialogFragment.this.elementBean.address = value;
                    }
                });
            }
        });

        String address = "";
        if (this.projectBean.addressChanged) {
            address = "定位中...";
        } else {
            if (BaseClassUtil.isNullOrEmptyString(this.projectBean.address)) {
                address = "定位中...";
            } else {
                address = this.projectBean.address;
            }
        }

        // 线采集，后面两个元素地址不能有下拉框且不可编辑
        if (!this.elementBean.ElemSN.equals("1")) {
            view.button.setVisibility(View.GONE);
            view.getEditText().setEnabled(false);
        }
        view.setValue(address);

        if (address.equals("定位中...")) {
            new BDGeocoderResultTask(handler, this.projectBean.dot).execute();
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        photoFragment.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 初始化图层信息填写框
     */
    private ImageButtonView createLayerView() {

        final ImageButtonView layerView = new ImageButtonView(getActivity());

        layerView.setKey("图层");

        String lastValue = null;

        ChangeImg(layerView.getImageView(), "图层");

        if (elementBean.GeomType.equals("点")) {
            lastValue = CacheUtils.getInstance(getActivity()).get(
                    GatherDataOperateBar.GATHER_DATA_LAST_POINT_LAYER);
        } else if (elementBean.GeomType.equals("线")) {
            lastValue = CacheUtils.getInstance(getActivity()).get(
                    GatherDataOperateBar.GATHER_DATA_LAST_LINE_LAYER);
        } else {
            lastValue = CacheUtils.getInstance(getActivity()).get(
                    GatherDataOperateBar.GATHER_DATA_LAST_LAYER);
        }

        String editedValue = attrMap.get("图层");

        String value = null;

        if (!BaseClassUtil.isNullOrEmptyString(editedValue)) {// 数据里存储的图层信息
            value = editedValue;
        } else if (!BaseClassUtil.isNullOrEmptyString(lastValue)) {// 上次列表中选择的信息
            value = lastValue;
        } else if (layerNames.size() > 0) {// 默认第一个图层
            value = layerNames.get(0);
        } else {
            value = "";
        }

        elementBean.LayerName = value;

        layerView.setValue(value);

        layerView.getButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyValues(layerView, "图层", layerNames);
            }
        });

        return layerView;
    }

    /**
     * 创建输入View
     *
     * @param key 显示键值
     * @return view
     */
    private ImageEditButtonView createView(String key, String value) {
        final ImageEditButtonView view = new ImageEditButtonView(getActivity());
        view.setKey(key);
        view.setValue(value);

        ChangeImg(view.getImageView(), key);

        view.setOnButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showSaveValues(view, view.getKey());
            }
        });
        return view;
    }

    private void ChangeImg(ImageView view, String key) {
        if (key.equals("位置") || key.equals("地址")) {
            view.setImageDrawable(getResources().getDrawable(
                    R.drawable.flex_flow_address));
        } else if (key.indexOf("座标") >= 0 || key.indexOf("坐标") >= 0) {
            view.setImageDrawable(getResources().getDrawable(
                    R.drawable.flex_flow_location));

        } else if (key.indexOf("拍照") >= 0) {
            view.setImageDrawable(getResources().getDrawable(
                    R.drawable.flex_flow_takephoto));
        } else {
            view.setImageDrawable(getResources().getDrawable(
                    R.drawable.flex_flow_type));
        }
    }

    /**
     * 创建不可编辑控件
     *
     * @param key
     * @return
     */
    private ImageTextView createTextView(String key, String value) {
        ImageTextView view = new ImageTextView(getActivity());
        view.setKey(key);
        view.setValue(value);

        ChangeImg(view.getImageView(), key);

        return view;
    }

    /**
     * 保存新填写的信息
     */
    private void saveNewGraphic() {
        List<String> keyValues = new ArrayList<String>();

        for (int i = 0; i < parentLayout.getChildCount(); i++) {

            View view = parentLayout.getChildAt(i);

            if (!(view instanceof FeedBackView)
                    || view instanceof ImageButtonView) {
                continue;
            }

            FeedBackView feedBackView = (FeedBackView) view;

            String key = feedBackView.getKey();
            String value = feedBackView.getValue();

            if (view instanceof ImageFragmentView) {
                ImageFragmentView fragmentView = (ImageFragmentView) view;

                HashMap<String, String> filePathMap = fragmentView
                        .getKeyValue();

                elementBean.Photo = filePathMap
                        .get(ImageFragmentView.RELATIVE_KEY_STRING);
                elementBean.photosPaths = filePathMap
                        .get(ImageFragmentView.ABSOLUTE_KEY_STRING);

                continue;
            }

            if (BaseClassUtil.isNullOrEmptyString(key)
                    || BaseClassUtil.isNullOrEmptyString(value)) {
                continue;
            }

            // 将填写的数据加入到数据库中，方便下次直接选择
            GatherDataUtils.insertAttrInfo(key, value);

            String keyValue = key + ":" + value;
            keyValues.add(keyValue);

        }

        if (!elementBean.isCaptureDot) {
            this.elementBean.NewAttr = BaseClassUtil.listToString(keyValues);
        }
    }

    /**
     * 显示历史填写的信息
     */
    private void showSaveValues(final ImageEditButtonView view, String key) {
        if (BaseClassUtil.isNullOrEmptyString(key)) {
            return;
        }

        // 从数据库中查询出填写过的信息
        List<String> values = GatherDataUtils.queryAttrInfo(key);

        if (values == null || values.size() == 0) {
            return;
        }

        ListDialogFragment fragment = new ListDialogFragment(key, values);

        fragment.setListItemClickListener(new OnListItemClickListener() {
            @Override
            public void onListItemClick(int arg2, String value) {
                view.setValue(value);
            }
        });

        fragment.show(getActivity().getSupportFragmentManager(), "");
    }

    /**
     * 显示图层选择信息的信息
     */
    private void showMyValues(final ImageButtonView view, String key,
                              List<String> values) {

        ListDialogFragment fragment = new ListDialogFragment(key, values);

        fragment.setListItemClickListener(new OnListItemClickListener() {
            @Override
            public void onListItemClick(int arg2, String value) {

                if (view.getValue().equals(value)) {
                    return;
                }

                // 控件中填写值
                view.setValue(value);

                createFeedbackView(dataLayers.get(arg2).getFeedbackAttr());

                elementBean.LayerName = value;

                // 缓存填写设备
                if (elementBean.GeomType.equals("点")) {
                    CacheUtils.getInstance(getActivity()).put(
                            GatherDataOperateBar.GATHER_DATA_LAST_POINT_LAYER,
                            value);
                } else if (elementBean.GeomType.equals("线")) {
                    CacheUtils.getInstance(getActivity()).put(
                            GatherDataOperateBar.GATHER_DATA_LAST_LINE_LAYER,
                            value);
                } else {
                    CacheUtils.getInstance(getActivity()).put(
                            GatherDataOperateBar.GATHER_DATA_LAST_LAYER, value);
                }
            }
        });

        fragment.show(getActivity().getSupportFragmentManager(), "");
    }

    public void setOnButtonClickListener(
            OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public interface OnButtonClickListener {
        void onMoveClick();

        void onCancelClick();

        void onOkClick();
    }

    private class BDGeocoderResultTask extends
            AsyncTask<String, String, BDGeocoderResult> {

        private Handler handler;
        private Dot dot;

        public BDGeocoderResultTask(Handler handler, Dot dot) {
            super();
            this.handler = handler;
            this.dot = dot;
        }

        @Override
        protected BDGeocoderResult doInBackground(String... params) {
            if (dot == null) {
                dot = MyApplication.getInstance().mapGISFrame.getMapView()
                        .getCenterPoint();
            }
            Location location = GpsReceiver.getInstance()
                    .getLastLocationConverse(new GpsXYZ(dot.x, dot.y));
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

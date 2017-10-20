package com.mapgis.mmt.module.gis.toolbar.accident;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentAnnotation;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentFeature;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentIdentify;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentResult;
import com.mapgis.mmt.module.gis.toolbar.accident.view.ListItemWithRadioView;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 查询结果显示对话框。用于显示查询的图层以及查询到的结果的数量，并且可以勾选图层显示。
 */
public class AccidentCheckDialogFragment2 extends DialogFragment implements DialogInterface.OnKeyListener {
    private ExpandableListView accidentListView;
    private MyExpandAdapter adapter;
    private Context mContext;

    // 记录每个组中的条目选中的个数,全部初始化为0
    private int [] childCheckedCount = {0,0,0,0,0};

    // 组名
    private List<String> listGroups;
    // 孩子集合
    private List<AccidentIdentify> listChilds;

    /**
     * 爆管分析查询结果
     */
    private final AccidentResult accidentResult;

    private OnOkClickListener onOkClickListener;

    /**
     * 查询结果显示对话框
     *
     * @param accidentResult 爆管分析结果
     */
    public AccidentCheckDialogFragment2(AccidentResult accidentResult) {
        this.accidentResult = accidentResult;
    }

    public AccidentCheckDialogFragment2(Context context,AccidentResult accidentResult) {
        this.mContext = context;
        this.accidentResult = accidentResult;
    }

    public void setOnOkClickListener(OnOkClickListener onOkClickListener) {
        this.onOkClickListener = onOkClickListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.accident_expandlist_view, container, false);
        accidentListView = (ExpandableListView) view.findViewById(R.id.expandListView);
        adapter = new MyExpandAdapter(mContext, accidentResult);
        accidentListView.setAdapter(adapter);

        accidentListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                ListItemWithRadioView itemView = ;
                boolean bool = ((ListItemWithRadioView) v).isChecked();
                bool = !bool;   // 现在的状态

                ((ListItemWithRadioView) v).setChecked(bool);  // 状态置为相反
                listChilds.get(groupPosition).features[childPosition].isShowInMap = bool;

                // 之前的数目
                int countBefor = childCheckedCount[groupPosition];
                /*
                 * 更改子条目的状态
                 *      如果改为true,则将当前所在的组中计数值+1
                 *      如果改为false,则将当前所在的组中计数值-1；
                 */
                // 现在的数目
                childCheckedCount[groupPosition] += bool ? 1 : -1;
                /*
                 * 只有当状态更改之前的数目和更改之后的数目之和为1才通知刷新
                 *      表示子条目中展示到地上的数量从0增加到1或者从1增加到0，这时都需要更改GroupItem的选中状态
                 */
                if (childCheckedCount[groupPosition] + countBefor <= 1) {
                    listChilds.get(groupPosition).isAnnotationShow = bool;
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });

        view.findViewById(R.id.btn_cancel).setVisibility(View.GONE);

        view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkClickListener.onClick();
            }
        });
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        final Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//注意此处
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//        getDialog().getWindow().setLayout(dm.widthPixels, getDialog().getWindow().getAttributes().height);
//        dm.heightPixels - DimenTool.dip2px(mContext,50)
        window.setLayout(-1, -1);  // 除去标题栏
//        window.setLayout(-1, -1);//这2行,和上面的一样,注意顺序就行;

        // 设置底部虚拟按键透明
//        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    /**
     * 监听器，监听确认按钮点击事件
     */
    public interface OnOkClickListener {
        void onClick();
    }

    class MyExpandAdapter extends BaseExpandableListAdapter {
        private AccidentResult accidentResult;


        private Context ctx;

        public MyExpandAdapter(Context ctx, AccidentResult accidentResult) {
            this.ctx = ctx;
            this.accidentResult = accidentResult;
            initData();
        }

        private void initData() {
            if (accidentResult == null) {
                return;
            }
            listGroups = accidentResult.getResultTypesWithCount();

            listChilds = new ArrayList<>();
            listChilds.add(accidentResult.valve);
            listChilds.add(accidentResult.line);
            listChilds.add(accidentResult.source);
            listChilds.add(accidentResult.center);
            listChilds.add(accidentResult.user);
        }

        /**
         * 组
         *
         * @param groupPosition 组索引
         * @param isExpanded    当前组是展开还是关闭的
         * @param convertView   复用的布局
         * @param parent        父布局
         * @return 组布局
         */
        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            final AccidentIdentify identify = accidentResult.getAccidentIdentifyByIndex(groupPosition);

            int drawableId = R.drawable.user_selected;

            // 若该该图层查询的有结果，并且是需要显示的，则采用勾选中的图片
            if (identify != null && identify.totalRcdNum > 0 && identify.isAnnotationShow) {
                drawableId = R.drawable.user_selected;
            } else {// 否则采用未勾选的图片
                drawableId = R.drawable.user_unselected;
            }

            View view = View.inflate(getActivity(),R.layout.item_group_view,null);
            final ImageView imageView = (ImageView) view.findViewById(R.id.iv_group_radioButton);
            TextView tvGroupContent = (TextView) view.findViewById(R.id.tv_group_content);
            ImageView ivExpenderIcon = (ImageView) view.findViewById(R.id.iv_expender_icon);

//            imageView.setBackgroundResource(drawableId);
            imageView.setImageResource(drawableId);
            tvGroupContent.setText(listGroups.get(groupPosition).toString());
            if (isExpanded){
                ivExpenderIcon.setImageResource(R.drawable.expander_open_mtrl_alpha);
            }else{
                ivExpenderIcon.setImageResource(R.drawable.expander_close_mtrl_alpha);
            }

            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (identify != null) {
                        identify.isAnnotationShow = !identify.isAnnotationShow;
                        imageView.setImageResource(identify.isAnnotationShow ? R.drawable.user_selected : R.drawable.user_unselected);
                        // 设置当前组中所有的子布局的状态
                        setChildsStatus(identify.isAnnotationShow, groupPosition);
                    }
                }
            });
            // 将ImageVIew缓存起来，方便调用它的点击事件
            view.setTag(imageView);
            return view;
        }

        private void setChildsStatus(boolean isAnnotationShow, int groupPosition) {
            int count = listChilds.get(groupPosition).totalRcdNum;
            if (count == 0) {
                return;
            }

            childCheckedCount[groupPosition] = isAnnotationShow ? count : 0;

            AccidentFeature[] features = listChilds.get(groupPosition).features;
            for (AccidentFeature info : features) {
                info.isShowInMap = isAnnotationShow;
                adapter.notifyDataSetChanged();
            }
        }

        /**
         * 子列表中的布局
         *
         * @param groupPosition
         * @param childPosition
         * @param isLastChild
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChileViewHolder holder = null;
            if (convertView == null) {
                convertView = new ListItemWithRadioView(ctx);
                holder = new ChileViewHolder();
                holder.itemView = (ListItemWithRadioView) convertView;
                convertView.setTag(holder);
            } else {
                holder = (ChileViewHolder) convertView.getTag();
            }

            // 如果当前图标需要显示到地图上就将选择按钮的图标设置为选中
            AccidentFeature feature = listChilds.get(groupPosition).features[childPosition];
            if (!listChilds.get(groupPosition).isAnnotationShow){
                // 如果整组信息都不能显示，则将孩子也设置为不可显示
                feature.isShowInMap = false;
            }else{
                childCheckedCount[groupPosition] += feature.isShowInMap ? 1 : 0;    // 计数加1
            }
            holder.itemView.setChecked(feature.isShowInMap);
            // 获取详细信息
            final LinkedHashMap<String, String> infos = feature.attributes.attrStrToMap();
            // 每个孩子中只显示位置信息，编号信息
            String id = infos.get("ElemID");
            holder.itemView.setContent(BaseClassUtil.isNullOrEmptyString(id) ? "-" : id);
            // 定位
            holder.itemView.getViewLocate().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    doLocate(groupPosition,childPosition,infos);
                }
            });
            // 详情
            holder.itemView.getViewDetail().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    doDetail((MapGISFrame)mContext,groupPosition,infos);
                }
            });

            return convertView;
        }

        /**
         * 子列表中的条目是否可以点击
         *      返回true表示可以被点击，false不可以
         * @param groupPosition
         * @param childPosition
         * @return
         */
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        /**
         * 组数
         *
         * @return
         */
        @Override
        public int getGroupCount() {
            return accidentResult == null ? 0 : AccidentResult.resultTypes.length;
        }

        /**
         * 不同的组中的孩子数
         *
         * @param groupPosition
         * @return
         */
        @Override
        public int getChildrenCount(int groupPosition) {
            AccidentIdentify identify = listChilds.get(groupPosition);
            return identify == null ? 0 : identify.totalRcdNum;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
//            int id = 0;
//            for (int i = 0 ; i <= groupPosition ;i ++){
//            }
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public void notifyDataSetChanged() {
            // 初始化计数值
            for (int i = 0; i < childCheckedCount.length; i++) {
                childCheckedCount[i] = 0;
            }
            super.notifyDataSetChanged();
        }
    }

    /**
     * 定位
     * @param groupPosition
     * @param infos
     */
    private void doLocate(final int groupPosition,int childPosition, final LinkedHashMap<String, String> infos) {
        final MapGISFrame mapGISFrame = (MapGISFrame) mContext;
        MapView mapView = mapGISFrame.getMapView();

        mapView.getGraphicLayer().removeAllGraphics();
        mapView.getAnnotationLayer().removeAllAnnotations();

            mapView.getAnnotationLayer()
                    .addAnnotation(
                            (listChilds.get(groupPosition).features)[childPosition].createAnnotation(
                                    AccidentResult.resultTypes[groupPosition] + ":" + infos.get("ElemID"),
                                    BitmapFactory.decodeResource(mapGISFrame.getResources(), AccidentQueryTask.resources[groupPosition])
                            ));
                            // 设备类型：设备编号
//                            accidentResult.valve.getAnnotations());

        mapView.getGraphicLayer().addGraphics(accidentResult.valve.getPolylins());

        mapView.setTapListener(null);

        mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
            @Override
            public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
                Annotation annotation = annotationview.getAnnotation();

                if (annotation instanceof AccidentAnnotation) {
                    AccidentAnnotation accidentAnnotation = (AccidentAnnotation) annotation;
//                    toDetailActivity(accidentAnnotation.accidentFeature.attributes.attrStrToMap(), "设备详情");
                    doDetail(mapGISFrame,groupPosition,infos);
                }
            }
        });

        this.dismiss();

        mapView.refresh();
    }

    /**
     * 详情
     * @param groupPosition
     * @param infos
     */
    public static void doDetail(MapGISFrame mapGISFrame,int groupPosition, LinkedHashMap<String, String> infos){
        Intent intent = new Intent(mapGISFrame, PipeDetailActivity.class);

        intent.putExtra("layerName", AccidentResult.resultTypes[groupPosition]);
        intent.putExtra("graphicMap", infos);
        intent.putExtra("graphicMapStr", new Gson().toJson(infos));
        intent.putExtra("groupPosition",groupPosition);
        intent.putExtra(AccidentCheckDialogFragment2.class.getSimpleName(),"爆管分析2");
        (mapGISFrame).startActivityForResult(intent, 0);

//        KeyPoint kp = new KeyPoint();
//
//        kp.GisLayer = item.LayerName;
//        kp.FieldName = item.FieldName;
//        kp.FieldValue = item.FieldValue;
//
//        new ShowGISDetailTask(MaintainGDDetailActivity.this).mmtExecute(kp);
    }

    /**
     * 子布局容器
     */
    static class ChileViewHolder {
        public ListItemWithRadioView itemView;
    }


    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            // 点击返回键时,触发确定按钮的点击事件
            onOkClickListener.onClick();
            this.dismiss();
            return true;
        }
        return false;
    }
}

package com.mapgis.mmt.module.gis.toolbar.accident;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.entity.BaseMapCallback;
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
public class AccidentCheckFragment extends Fragment {
    private ExpandableListView accidentListView;
    private MyExpandAdapter adapter;

    // 记录每个组中的条目选中的个数,全部初始化为0
    private int[] childCheckedCount = {0, 0, 0, 0, 0};

    // 组名
    private List<String> listGroups;
    // 孩子集合
    private List<AccidentIdentify> listChilds;

    /**
     * 爆管分析查询结果
     */
    private AccidentResult accidentResult;

    // 字符串类型的爆管分析结果
    private String strResult;
    private final static String RESULT = "strResult";

    private Button btnOk;

    public AccidentCheckFragment() {
    }

//    /**
//     * 查询结果显示对话框
//     *
//     * @additionalParas accidentResult 爆管分析结果
//     */
//    public AccidentCheckFragment(AccidentResult accidentResult) {
//        this.accidentResult = accidentResult;
//    }
//
//    public AccidentCheckFragment(Context context, AccidentResult accidentResult) {
//        this.mContext = context;
//        this.accidentResult = accidentResult;
//    }

    public static AccidentCheckFragment newInstance(String strResult) {
        AccidentCheckFragment fragment = new AccidentCheckFragment();
        Bundle bundle = new Bundle();
        bundle.putString(RESULT, strResult);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.accident_expandlist_view, container, false);

        PipeAccidentMenu.state = PipeAccidentMenu.STATE_SHOW_LIST_ACTIVITY | PipeAccidentMenu.state;

        if (getArguments() != null) {
            strResult = getArguments().getString("strResult");
        }

        initView(view);
        return view;
    }

    @Override
    public void onDestroy() {
        PipeAccidentMenu.state = PipeAccidentMenu.state & ~PipeAccidentMenu.STATE_SHOW_LIST_ACTIVITY;
        super.onDestroy();
    }

    private void initView(View view) {
        accidentListView = (ExpandableListView) view.findViewById(R.id.expandListView);

        initData();

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

        btnOk = (Button) view.findViewById(R.id.btn_ok);

        btnOk.setVisibility(View.GONE);

        btnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                showOnMap(accidentResult);
                // 去掉没有@Expose注解的属性
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                AccidentQueryTask.strResult = gson.toJson(accidentResult);
                getActivity().finish();
            }
        });
    }

    public Button getBtnOk() {
        return btnOk;
    }

    private void initData() {
        accidentResult = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                .fromJson(strResult, AccidentResult.class);

//        if (accidentResult != null && accidentResult.line != null) {
//            accidentResult.line.isAnnotationShow = false;
//        }

        adapter = new MyExpandAdapter(getActivity(), accidentResult);
        accidentListView.setAdapter(adapter);
    }

    /**
     * 将结果信息绘制到地图上
     */
    public void showOnMap(final AccidentResult accidentResult) {
        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            @Override
            public boolean handleMessage(Message msg) {
                addAnnotationsOnMap(mapGISFrame);
                mapView.refresh();
                return true;
            }
        });
//
//        Intent intent = new Intent(getActivity(),MapGISFrame.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        startActivity(intent);
    }

    private void addAnnotationsOnMap(MapGISFrame mapGISFrame) {
        MapView mapView = mapGISFrame.getMapView();
        mapView.getGraphicLayer().removeAllGraphics();
        mapView.getAnnotationLayer().removeAllAnnotations();

        PipeAccidentMenu.state = PipeAccidentMenu.STATE_SHOW_HANDLE_RESULT | PipeAccidentMenu.state;
        PipeAccidentMenu.state =  PipeAccidentMenu.state | PipeAccidentMenu.STATE_SHOW_RESULT;

        for (int i = 0; i < listChilds.size(); i++) {
            AccidentIdentify identify = listChilds.get(i);
            if (identify == null){
                continue;
            }
            if (identify.isAnnotationShow) {
                mapView.getAnnotationLayer()
                        .addAnnotations(
                                identify.getAnnotations(
                                        BitmapFactory.decodeResource(getResources(), AccidentQueryTask.resources[i])));
            }
            mapView.getGraphicLayer().addGraphics(listChilds.get(i).getPolylins());
        }
    }

    /**
     * 定位的时候会将用户选中的所有标注都绘制到地图上
     *      并将要定位的标注用其他颜色的图标单独标注
     *
     * @param groupPosition 组
     * @param childPosition 孩子
     * @param infos         详细信息
     */
    private void doLocate(final int groupPosition, final int childPosition, final LinkedHashMap<String, String> infos) {
        refreshMap(groupPosition, childPosition, infos);
        PipeAccidentMenu.state = PipeAccidentMenu.STATE_SHOW_LOCATED_RESULT | PipeAccidentMenu.state;
        Intent intent = new Intent(getActivity(), MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(getActivity());
    }

    private void refreshMap(final int groupPosition, final int childPosition, final LinkedHashMap<String, String> infos) {
        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            @Override
            public boolean handleMessage(Message msg) {
                // 重新刷新地图，如果有定位就去掉定位状态
                if ((PipeAccidentMenu.state & PipeAccidentMenu.STATE_SHOW_LOCATED_RESULT) > 0){
                    PipeAccidentMenu.state -= PipeAccidentMenu.STATE_SHOW_LOCATED_RESULT;
                }

                AccidentIdentify identify = listChilds.get(groupPosition);
                AccidentFeature feature = identify.features[childPosition];

                if (feature.isShowInMap) {
                    feature.isShowInMap = false;
                    addAnnotationsOnMap(mapGISFrame);
                    feature.isShowInMap = true;
                } else {
                    addAnnotationsOnMap(mapGISFrame);
                }

                Annotation annotation = (listChilds.get(groupPosition).features)[childPosition].createAnnotation(
                        infos.get("ElemID"),
                        BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_gcoding));
                mapView.getAnnotationLayer().addAnnotation(annotation);
                // 展开标注视图
                annotation.showAnnotationView();
                mapView.panToCenter(annotation.getPoint(), true);
                mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
                    @Override
                    public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
                        Annotation annotation = annotationview.getAnnotation();
                        if (annotation instanceof AccidentAnnotation) {
                            AccidentAnnotation accidentAnnotation = (AccidentAnnotation) annotation;
//                            doDetail(groupPosition, infos);
                            Intent intent = new Intent(mapGISFrame, PipeDetailActivity.class);
                            intent.putExtra("FragmentClass", DetailListFragment.class);
                            LinkedHashMap<String, String> attrMap = accidentAnnotation.accidentFeature.attributes.attrStrToMap();
                            intent.putExtra("layerName", "设备详情");
                            intent.putExtra("graphicMap", attrMap);
                            intent.putExtra("graphicMapStr", new Gson().toJson(attrMap));
                            intent.putExtra("unvisiable_detail_fragment", true);
                            mapGISFrame.startActivity(intent);
                        }
                    }
                });
                mapView.refresh();
                return true;
            }
        });
    }

    /**
     * 详情
     *
     * @param groupPosition 组索引
     * @param infos         数据信息
     */
    public void doDetail(int groupPosition, LinkedHashMap<String, String> infos) {
        Intent intent = new Intent(getActivity(), PipeDetailActivity.class);

        intent.putExtra("FragmentClass", DetailListFragment.class);
        intent.putExtra("layerName", AccidentResult.resultTypes[groupPosition]);
        intent.putExtra("graphicMap", infos);
        intent.putExtra("graphicMapStr", new Gson().toJson(infos));
        intent.putExtra("unvisiable_detail_fragment", true);

        intent.putExtra("list",true);

        getActivity().startActivity(intent);

        MyApplication.getInstance().startActivityAnimation(getActivity());
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

            int drawableId;

            // 若该该图层查询的有结果，并且是需要显示的，则采用勾选中的图片
            if (identify != null && identify.totalRcdNum > 0 && identify.isAnnotationShow) {
                drawableId = R.drawable.user_selected;
            } else {// 否则采用未勾选的图片
                drawableId = R.drawable.user_unselected;
            }

            View view = View.inflate(getActivity(), R.layout.item_group_view, null);
            final ImageView imageView = (ImageView) view.findViewById(R.id.iv_group_radioButton);
            TextView tvGroupContent = (TextView) view.findViewById(R.id.tv_group_content);
            ImageView ivExpenderIcon = (ImageView) view.findViewById(R.id.iv_expender_icon);

//            imageView.setBackgroundResource(drawableId);
            imageView.setImageResource(drawableId);
            tvGroupContent.setText(listGroups.get(groupPosition).toString());
            if (isExpanded) {
                ivExpenderIcon.setImageResource(R.drawable.expander_open_mtrl_alpha);
            } else {
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
         * @param groupPosition 所在组布局的位置
         * @param childPosition 孩子在组中的索引
         * @param isLastChild   是否为最后一个子布局
         * @param convertView   复用的布局
         * @param parent        父布局
         * @return 返回子布局
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
            if (!listChilds.get(groupPosition).isAnnotationShow) {
                // 如果整组信息都不能显示，则将孩子也设置为不可显示
                feature.isShowInMap = false;
            } else {
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
                    doLocate(groupPosition, childPosition, infos);
                }
            });
            // 详情
            holder.itemView.getViewDetail().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 在跳转到详情界面之前先刷新地图界面
                    refreshMap(groupPosition,childPosition,infos);
                    doDetail(groupPosition,infos);
                }
            });

            return convertView;
        }

        /**
         * 子列表中的条目是否可以点击
         * 返回true表示可以被点击，false不可以
         *
         * @param groupPosition 组索引
         * @param childPosition 子索引
         * @return 子布局是否可被选中
         */
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        /**
         * 组数
         *
         * @return 组数
         */
        @Override
        public int getGroupCount() {
            return accidentResult == null ? 0 : AccidentResult.resultTypes.length;
        }

        /**
         * 不同的组中的孩子数
         *
         * @param groupPosition 组索引
         * @return 指定组中孩子数
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
     * 子布局容器
     */
    static class ChileViewHolder {
        public ListItemWithRadioView itemView;
    }
}

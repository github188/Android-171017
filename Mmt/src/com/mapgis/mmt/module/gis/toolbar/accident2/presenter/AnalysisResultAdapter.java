package com.mapgis.mmt.module.gis.toolbar.accident2.presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.treeview.MultiTreeAdapter;
import com.mapgis.mmt.common.widget.treeview.ThreeStatusCheckBox;
import com.mapgis.mmt.common.widget.treeview.TreeNode;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.accident2.AnalysisResultFragment;
import com.mapgis.mmt.module.gis.toolbar.accident2.AttachDataActivity;
import com.mapgis.mmt.module.gis.toolbar.accident2.PipeBrokenAnalysisMenu;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureItem;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaItem;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureItemModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.IFeatureMetaModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.impl.ExportXlsModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.model.impl.FeatureItemModel;
import com.mapgis.mmt.module.gis.toolbar.accident2.util.IconFactory;
import com.mapgis.mmt.module.gis.toolbar.accident2.util.MetaType;
import com.mapgis.mmt.module.gis.toolbar.accident2.view.IAnalysisResultView;
import com.zondy.mapgis.android.mapview.MapView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Comclay on 2017/3/5.
 * 爆管分析结果展示的adapter
 */

public class AnalysisResultAdapter extends MultiTreeAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = "AnalysisResultAdapter";
    private static final int TYPE_META_GROUP = 1;
    private static final int TYPE_META_ITEM = 2;
    private static final int TYPE_CHILD_ITEM = 3;
    private static final int TYPE_NEED_CLOSE_ITEM = 4;

    private IAnalysisResultView mAnalysisResultView;
    private IFeatureMetaModel mFeatureMetaModel;
    private IconFactory iconFactory;
    private List<String> mInvalidateValveList;
    private boolean isNewLoadFlag = false;
    private ArrayList<FeatureMetaGroup> mFeatureMetaGroups;

    AnalysisResultAdapter(IAnalysisResultView analysisResultView, IFeatureMetaModel mFeatureMetaModel) {
        this(analysisResultView.getLayoutInflater(), mFeatureMetaModel.getAdapterData());
        this.mAnalysisResultView = analysisResultView;
        this.mFeatureMetaModel = mFeatureMetaModel;
    }

    private AnalysisResultAdapter(LayoutInflater inflater, List<TreeNode> allNodeList) {
        super(inflater, allNodeList);
        iconFactory = IconFactory.create(MyApplication.getInstance().getResources());
        this.mInvalidateValveList = new ArrayList<>();

        mFeatureMetaGroups = getSelectMetaGroup();
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case TYPE_META_GROUP:
                View viewType1 = inflater.inflate(R.layout.item_broken_meta_group, parent, false);
                holder = new MetaGroupViewHolder(viewType1);
                break;
            case TYPE_META_ITEM:
                View viewType2 = inflater.inflate(R.layout.item_broken_meta_child, parent, false);
                holder = new MetaItemViewHolder(viewType2);
                break;
            case TYPE_CHILD_ITEM:
                View viewType3 = inflater.inflate(R.layout.item_broken_child, parent, false);
                holder = new ChildItemViewHolder(viewType3);
                break;
            case TYPE_NEED_CLOSE_ITEM:
                View viewType4 = inflater.inflate(R.layout.item_broken_nee_close_child, parent, false);
                holder = new NeedCloseItemViewHoder(viewType4);
                break;
        }
        return holder;
    }

    @Override
    protected void setViewStyle(View itemView, int viewType) {
        switch (viewType) {
            case TYPE_META_GROUP:
                itemView.setBackgroundColor(MyApplication.getInstance().getResources().getColor(R.color.color_d8e7f5));
                break;
            case TYPE_META_ITEM:
                itemView.setBackgroundColor(MyApplication.getInstance().getResources().getColor(R.color.color_f6fafe));
                break;
            default:
                itemView.setBackgroundColor(MyApplication.getInstance().getResources().getColor(R.color.color_f6fafe));
                break;
        }
    }

    @Override
    public void onBindContentHolder(RecyclerView.ViewHolder holder, final int position) {
        final TreeNode treeNode = nodeList.get(position);
        final Object objs = treeNode.getObjs();
        if (holder instanceof MetaGroupViewHolder && objs instanceof FeatureMetaGroup) {
            initMetaGroupView((MetaGroupViewHolder) holder, (FeatureMetaGroup) objs);
        } else if (holder instanceof MetaItemViewHolder && objs instanceof FeatureMetaItem) {
            initMetaItemView((MetaItemViewHolder) holder, treeNode);
        } else if (holder instanceof NeedCloseItemViewHoder && objs instanceof FeatureItem) {
            initNeedCloseItemView((NeedCloseItemViewHoder) holder, (FeatureItem) objs);
        } else if (holder instanceof ChildItemViewHolder && objs instanceof FeatureItem) {
            initChildItemView((ChildItemViewHolder) holder, treeNode);
        }

        // 设备
        if (objs instanceof FeatureItem) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TreeNode parent = nodeList.get(position).getParent().getParent();
                    int checkStatus = parent.getCheckStatus();
                    parent.setCheckStatus(ThreeStatusCheckBox.CHECK_ALL);
                    MapView mapView = MyApplication.getInstance().mapGISFrame.getMapView();
                    mapView.panToCenter(((FeatureItem) objs).getDot(), true);
                    mAnalysisResultView.startMapViewToFront();
                    parent.setCheckStatus(checkStatus);
                    BaseMapMenu menu = MyApplication.getInstance().mapGISFrame.getFragment().menu;
                    if (menu instanceof PipeBrokenAnalysisMenu) {
                        ((PipeBrokenAnalysisMenu) menu).setBack(false);
                    }
                }
            });
        }
    }

    private void initChildItemView(ChildItemViewHolder holder, TreeNode treeNode) {
        TreeNode parent = treeNode.getParent();
        FeatureMetaItem metaItem = (FeatureMetaItem) parent.getObjs();
        FeatureItem objs = (FeatureItem) treeNode.getObjs();
        LinkedHashMap<String, String> attributes = objs.getAttributes();
        if (metaItem.layerName.contains("管段")) {
            holder.tvOid.setText(formatString(attributes.get("ID")));
            holder.tvElemNum.setText(formatString(attributes.get("名称")));
            holder.tvName.setText(formatString(attributes.get("管线性质")));
        } else {
            holder.tvOid.setText(formatString(attributes.get("ID")));
            holder.tvElemNum.setText(formatString(attributes.get("编号")));
            holder.tvName.setText(formatString(attributes.get("名称")));
        }
    }

    private void initNeedCloseItemView(NeedCloseItemViewHoder holder, final FeatureItem objs) {
        final LinkedHashMap<String, String> attributes = objs.getAttributes();
        holder.tvOid.setText(formatString(attributes.get("ID")));
        holder.tvElemNum.setText(formatString(attributes.get("编号")));
        holder.cbSetInvalide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String oid = attributes.get("ElemID");
                if (isChecked) {
                    mInvalidateValveList.add(oid);
                } else {
                    mInvalidateValveList.remove(oid);
                }
            }
        });
    }

    private String formatString(String str) {
        return BaseClassUtil.isNullOrEmptyString(str) ? "-" : str;
    }

    private void initMetaItemView(MetaItemViewHolder holder, final TreeNode node) {
        final FeatureMetaItem metaItem = (FeatureMetaItem) node.getObjs();
        int count = metaItem.objectIds == null ? 0 : metaItem.objectIds.size();
        String text = String.format(Locale.CHINA, "%s（%d）", metaItem.layerName, count);
        holder.tvLayerName.setText(text);

        String isExportXls = MyApplication.getInstance().getConfigValue("IsExportXls");
        if (BaseClassUtil.isNullOrEmptyString(isExportXls)) {
            isExportXls = "true";
        }
        if (Boolean.valueOf(isExportXls)) {
            holder.ivExportXls.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exportXls(metaItem);
                }
            });
        } else {
            holder.ivExportXls.setVisibility(View.GONE);
        }
    }

    public void checked(TreeViewHolder viewHolder, int position, int status) {
        RecyclerView.ViewHolder contentHolder = viewHolder.getContentHolder();
        if (status == ThreeStatusCheckBox.CHECK_ALL && contentHolder instanceof MetaGroupViewHolder) {
            final TreeNode node = getNode(position);
            if (node.isRoot() && !node.isLeaf()) {
                delayLoadNode(node);
            }
        }
    }

    private void delayLoadNode(final TreeNode node) {
        Object objs = node.getObjs();
        if (objs instanceof FeatureMetaGroup && node.getChildrenNodes().get(0).isLeaf()) {
            final FeatureMetaGroup metaGroup = (FeatureMetaGroup) objs;
            IFeatureItemModel mItemModel = new FeatureItemModel();
            mAnalysisResultView.showLoadingDialog();
            mItemModel.loadFeatureMetaGroup(metaGroup, new IFeatureItemModel.LoadCallback() {
                int count = 0;

                @Override
                public void onLoadSuccess(FeatureMetaGroup group, int index) {
                    count++;
                    TreeNode treeNode = node.getChildrenNodes().get(index);
                    FeatureMetaItem featureMetaItem = group.getResultList().get(index);
                    ArrayList<FeatureItem> features = featureMetaItem.getFeatureGroup().features;
                    int nodeIndex = allNodeList.indexOf(treeNode);
                    List<TreeNode> childrenNodes = treeNode.getChildrenNodes();
                    for (int i = 0; i < features.size(); i++) {
                        FeatureItem item = features.get(i);
                        TreeNode childNode = new TreeNode(allNodeList.size(), treeNode.getId(), item);
                        childNode.setHideChecked(true);
                        childNode.setHideExpand(true);
                        childNode.setParent(treeNode);
                        childrenNodes.add(childNode);
                        allNodeList.add(nodeIndex + i + 1, childNode);
                    }
                    if (count == metaGroup.resultList.size()) {
                        mAnalysisResultView.post(new Runnable() {
                            @Override
                            public void run() {
                                mAnalysisResultView.hidenLoadingDialog();
                                mAnalysisResultView.showToast("加载成功");
                            }
                        });
                    }
                }

                @Override
                public void onLoadFailed(String msg) {
                    mAnalysisResultView.post(new Runnable() {
                        @Override
                        public void run() {
                            mAnalysisResultView.hidenLoadingDialog();
                            mAnalysisResultView.showToast("加载失败");
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void delayExpandNode(final TreeNode treeNode, final TreeViewHolder<RecyclerView.ViewHolder> holder) {
        mAnalysisResultView.showLoadingDialog();
        if (treeNode.getLevel() != 1) {
            return;
        }
        FeatureMetaItem metaItem = (FeatureMetaItem) treeNode.getObjs();
        TreeNode parent = treeNode.getParent();
        FeatureMetaGroup metaGroup = (FeatureMetaGroup) parent.getObjs();
        FeatureGroup featureGroup = metaItem.getFeatureGroup();
        int index = metaGroup.getResultList().indexOf(metaItem);
        if (featureGroup == null) {
            // 重新加载数据
            mFeatureMetaModel.loadFeatureMetaItem(metaGroup, new IFeatureItemModel.LoadCallback() {
                @Override
                public void onLoadSuccess(FeatureMetaGroup group, int index) {
                    FeatureMetaItem featureMetaItem = group.getResultList().get(index);
                    ArrayList<FeatureItem> features = featureMetaItem.getFeatureGroup().features;
                    int nodeIndex = allNodeList.indexOf(treeNode);
                    List<TreeNode> childrenNodes = treeNode.getChildrenNodes();
                    for (int i = 0; i < features.size(); i++) {
                        FeatureItem item = features.get(i);
                        TreeNode childNode = new TreeNode(allNodeList.size(), treeNode.getId(), item);
                        childNode.setHideChecked(true);
                        childNode.setHideExpand(true);
                        childNode.setParent(treeNode);
                        childrenNodes.add(childNode);
                        allNodeList.add(nodeIndex + i + 1, childNode);
                    }

                    mAnalysisResultView.post(new Runnable() {
                        @Override
                        public void run() {
                            mAnalysisResultView.hidenLoadingDialog();
                            expandNode(treeNode, holder);
                        }
                    });
                }

                @Override
                public void onLoadFailed(String msg) {
                    mAnalysisResultView.post(new Runnable() {
                        @Override
                        public void run() {
                            mAnalysisResultView.hidenLoadingDialog();
                            mAnalysisResultView.showToast("加载失败");
                        }
                    });
                }
            }, index);
        }
    }

    /**
     * 导出excel
     *
     * @param metaItem FeatureMetaItem对象
     */
    private void exportXls(FeatureMetaItem metaItem) {
        new ExportXlsModel(new ExportXlsCallback() {
            @Override
            public void onExportStart() {
                mAnalysisResultView.showExportDialog();
                Log.i(TAG, "开始导出Excel文件");
            }

            @Override
            public void onExportSuccess(String fileName) {
                super.onExportSuccess(fileName);
                Log.i(TAG, "生成Excel文件成功");
                mAnalysisResultView.showToast("导出成功：" + fileName);
                mAnalysisResultView.viewExportXls(fileName);
            }

            @Override
            public void onExportFaild() {
                super.onExportFaild();
                mAnalysisResultView.showToast("导出失败");
            }

            @Override
            public void onExportFinish() {
                super.onExportFinish();
                mAnalysisResultView.hidenExportDialog();
            }
        }).buildXlsTask(getExportXlsUrl(metaItem), getExportUrlPath(metaItem));
    }

    /**
     * 获取导出xls的url
     * <p>
     * http://192.168.12.200:803/cityinterface/rest/services/mapserver.svc/hzbj/1/queryExport
     * ?%5Fts=1488856889735
     * &where=OID%20IN%20%2845237%2C45238%2C45239%2C49511%2C49512%2C49513%2C49524%2C49525%2C49601%2C49602%2C49603%2C49604%2C49605%2C49606%2C49607%2C49608%2C49609%2C49610%2C49611%2C49612%2C49613%2C49614%2C49615%2C49616%2C49617%2C49618%2C49619%2C49620%2C49621%2C49622%2C49623%2C49624%2C49625%2C49626%2C49627%2C49628%2C49748%2C49749%2C49750%2C49751%2C49752%2C49753%2C53709%2C53710%2C53711%2C53712%2C53713%2C53714%2C53715%2C53716%2C53717%2C53718%2C53719%2C53720%2C53721%2C53722%2C53723%2C53724%2C53725%2C53726%2C53727%2C53728%2C53729%2C53730%2C53731%2C53732%2C53733%2C53734%2C53735%2C53736%2C53737%2C54379%2C54380%2C57438%2C59828%2C59829%2C59830%2C59831%2C59832%2C62547%2C62548%2C62630%2C62631%2C62632%2C62633%2C62634%2C62930%29
     * &returnGeometry=false
     * &f=json
     * &returnDistinctValues=false
     * &outSR=1
     * &spatialRel=civSpatialRelIntersects
     *
     * @param metaItem FeatureMetaItem对象
     */
    private String getExportXlsUrl(FeatureMetaItem metaItem) {
        if (metaItem == null || metaItem.objectIds == null || metaItem.objectIds.size() == 0)
            return null;
        // 爆管分析查询的URL地址
        StringBuilder sb = new StringBuilder();
        sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                .append("/rest/services/mapserver.svc/")
                .append(MobileConfig.MapConfigInstance.VectorService).append("/")
                .append(metaItem.layerId)
                .append("/queryExport?")
                .append("&_ts=").append(System.currentTimeMillis())
                .append("&returnGeometry=false")
                .append("&f=json")
                .append("&returnDistinctValues=false")
                .append("&outSR=1")
                .append("&spatialRel=civSpatialRelIntersects")
                .append("&where=").append(String.format(Locale.CHINA, "OID IN (%s)"
                , BaseClassUtil.listToString(metaItem.objectIds)));

        return sb.toString();
    }

    private String getExportUrlPath(FeatureMetaItem metaItem) {
        if (metaItem == null || metaItem.objectIds == null || metaItem.objectIds.size() == 0)
            return null;
        // 爆管分析查询的URL地址
        StringBuilder sb = new StringBuilder();
        sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                .append("/rest/services/mapserver.svc/")
                .append(MobileConfig.MapConfigInstance.VectorService).append("/")
                .append(metaItem.layerId)
                .append("/queryExport/");
        return sb.toString();
    }

    private void initMetaGroupView(MetaGroupViewHolder metaGroupViewHolder, final FeatureMetaGroup metaGroup) {

        Bitmap bitmap = iconFactory.getBitmap(metaGroup.getCivFeatureMetaType());
        metaGroupViewHolder.icon.setImageBitmap(bitmap);
        ArrayList<FeatureMetaItem> resultList = metaGroup.getResultList();
        int count = resultList == null ? 0 : resultList.size();
        String text = String.format(Locale.CHINA, "%s（%d）", metaGroup.civFeatureMetaTypeName, count);
        metaGroupViewHolder.tvTypeName.setText(text);
        ViewParent parent = metaGroupViewHolder.itemView.getParent().getParent();
        if (count == 0) {
            metaGroupViewHolder.icViewAttachData.setVisibility(View.GONE);
            metaGroupViewHolder.tvTypeName.setFocusable(false);
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).setAlpha(0.7f);
                ((ViewGroup) parent).setFocusable(false);
                ((ViewGroup) parent).setClickable(false);
                ((ViewGroup) parent).setBackgroundColor(MyApplication.getInstance().getResources().getColor(R.color.color_dfe9f3));
            }
            return;
        }
        ((ViewGroup) parent).setAlpha(1f);
        metaGroupViewHolder.tvTypeName.setFocusable(true);
        ((ViewGroup) parent).setBackgroundColor(MyApplication.getInstance().getResources().getColor(R.color.color_d8e7f5));

        for (FeatureMetaItem item : metaGroup.resultList) {
            if (item.relationships != null && item.relationships.size() != 0) {
                metaGroupViewHolder.icViewAttachData.setVisibility(View.VISIBLE);
                metaGroupViewHolder.icViewAttachData.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enterViewAttDataActivity(metaGroup);
                    }
                });
                return;
            }
        }
        metaGroupViewHolder.icViewAttachData.setVisibility(View.GONE);
    }

    // 进入附属数据查看界面
    private void enterViewAttDataActivity(FeatureMetaGroup metaGroup) {
        if (mAnalysisResultView instanceof AnalysisResultFragment) {
            Fragment fragment = (AnalysisResultFragment) mAnalysisResultView;
            Intent intent = new Intent(fragment.getActivity(), AttachDataActivity.class);
            intent.putExtra(AttachDataActivity.PARAMS, metaGroup);
            fragment.startActivity(intent);
            MyApplication.getInstance().startActivityAnimation(fragment.getActivity());
        }
    }

    /**
     * 获取选中的数据
     *
     * @return List<FeatureMetaGroup>
     */
    public ArrayList<FeatureMetaGroup> getSelectMetaGroup() {
        ArrayList<FeatureMetaGroup> metaGroupList = new ArrayList<>();
        for (TreeNode treeNode : nodeList) {
            if (treeNode.getCheckStatus() > 0 && treeNode.isRoot()) {
                Object objs = treeNode.getObjs();
                if (objs instanceof FeatureMetaGroup) {
                    metaGroupList.add((FeatureMetaGroup) objs);
                }
            }
        }
        if (metaGroupList.equals(mFeatureMetaGroups)) {
            metaGroupList.clear();
        } else {
            mFeatureMetaGroups = metaGroupList;
        }

        return metaGroupList;
    }

    @Override
    public int getItemViewType(int position) {
        TreeNode treeNode = nodeList.get(position);
        if (treeNode.isRoot()) {
            return TYPE_META_GROUP;
        } else if (treeNode.getObjs() instanceof FeatureMetaItem) {
            return TYPE_META_ITEM;
        } else if (treeNode.getObjs() instanceof FeatureItem) {
            TreeNode parent = treeNode.getParent();
            if (!parent.isRoot()) {
                TreeNode pParent = parent.getParent();
                Object objs = pParent.getObjs();
                if (objs instanceof FeatureMetaGroup) {
                    if (MetaType.TYPE_SWITCH.equals(((FeatureMetaGroup) objs).getCivFeatureMetaType())) {
                        return TYPE_NEED_CLOSE_ITEM;
                    }
                }
            }
            return TYPE_CHILD_ITEM;
        }
        return super.getItemViewType(position);
    }

    /**
     * 返回失效设备
     */
    public String getInvalidateValve() {
        return BaseClassUtil.listToString(mInvalidateValveList);
    }

    /**
     * FeatureMetaGroup
     */
    private static class MetaGroupViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView tvTypeName;
        private ImageView icViewAttachData;

        MetaGroupViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.iv_type_icon);
            tvTypeName = (TextView) itemView.findViewById(R.id.tv_type_name);
            icViewAttachData = (ImageView) itemView.findViewById(R.id.iv_fushu);
        }
    }

    /**
     * FeatureMetaItem
     */
    private static class MetaItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvLayerName;
        private ImageView ivExportXls;

        MetaItemViewHolder(View itemView) {
            super(itemView);
            tvLayerName = (TextView) itemView.findViewById(R.id.tv_layer_name);
            ivExportXls = (ImageView) itemView.findViewById(R.id.iv_export_xls);
        }
    }

    /**
     * 需关断设备的FeatureItem
     */
    private static class NeedCloseItemViewHoder extends RecyclerView.ViewHolder {
        private TextView tvOid;
        private TextView tvElemNum;
        private CheckBox cbSetInvalide;

        NeedCloseItemViewHoder(View itemView) {
            super(itemView);
            tvOid = (TextView) itemView.findViewById(R.id.tv_oid);
            tvElemNum = (TextView) itemView.findViewById(R.id.tv_elemNum);
            cbSetInvalide = (CheckBox) itemView.findViewById(R.id.cb_set_invalide);
        }
    }

    /**
     * 其他类型的FeatrueItem
     */
    private static class ChildItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOid;
        private TextView tvElemNum;
        private TextView tvName;

        ChildItemViewHolder(View itemView) {
            super(itemView);
            tvOid = (TextView) itemView.findViewById(R.id.tv_oid);
            tvElemNum = (TextView) itemView.findViewById(R.id.tv_elemNum);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
        }
    }
}

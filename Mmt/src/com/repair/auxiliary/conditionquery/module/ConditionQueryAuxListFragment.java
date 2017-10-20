package com.repair.auxiliary.conditionquery.module;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.AuxDataResult;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.ConditionQueryAdapterData;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.GetAuxDataIDsResult;
import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/4/20.
 */
public class ConditionQueryAuxListFragment extends Fragment {
    protected String layerID;
    protected String geometry;
    protected String strCon;
    protected String strAuxTableName;
    protected BaseActivity context;
    protected PullToRefreshListView listView;
    protected AuxDataResult auxDataResult = new AuxDataResult();
    protected GetAuxDataIDsResult getAuxDataIDsResult = new GetAuxDataIDsResult();
    protected ConditionQueryAuxAdapter adapter;
    protected List<ConditionQueryAdapterData> dataList = new ArrayList<>();
    protected int pageNum = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (BaseActivity) getActivity();
        Bundle bundle = getArguments();
        layerID = bundle.getString("layerID");
        geometry = bundle.getString("geometry");
        strCon = bundle.getString("strCon");
        strAuxTableName = bundle.getString("strAuxTableName");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_view_conditionquery_aux, container, false);
        listView = (PullToRefreshListView) view.findViewById(R.id.listView);

        listView.setMode(PullToRefreshBase.Mode.BOTH);
        adapter = new ConditionQueryAuxAdapter(context, dataList);
        listView.setAdapter(adapter);
        listView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        listView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        listView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");
        // 上拉加载更多时的提示文本设置
        listView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载");
        listView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
        listView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");

        // listView.getRefreshableView().setAdapter(new ConditionQueryAuxAdapter(context, dataList));
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //下拉刷新
                pageNum = 0;
                auxDataResult.getAuxDataResultFromGisServer(context, getAuxDataIDsResult.OIDs, layerID, strAuxTableName, pageNum, 5, new AuxUtils.AfterOnsucess() {
                    @Override
                    public void afterSucess() {
                        dataList.clear();
                        AuxUtils.auxDataResult2AdapterDataList(dataList, auxDataResult, strAuxTableName, getAuxDataIDsResult.OIDLayerID);
                        adapter.notifyDataSetChanged();
                        listView.onRefreshComplete();
                    }
                }, new AuxUtils.RetrurnBefore() {
                    @Override
                    public void retrurnBefore() {
                        if (!listView.isRefreshing()) {
                            return;
                        }
                        listView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                listView.onRefreshComplete();
                            }
                        }, 1000);
                    }
                });

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //上拉加载更多
                pageNum++;
                auxDataResult.getAuxDataResultFromGisServer(context, getAuxDataIDsResult.OIDs, layerID, strAuxTableName, pageNum, 5, new AuxUtils.AfterOnsucess() {
                    @Override
                    public void afterSucess() {
                        AuxUtils.auxDataResult2AdapterDataList(dataList, auxDataResult, strAuxTableName, getAuxDataIDsResult.OIDLayerID);
                        adapter.notifyDataSetChanged();
                        listView.onRefreshComplete();
                    }
                }, new AuxUtils.RetrurnBefore() {
                    @Override
                    public void retrurnBefore() {
                        if (!listView.isRefreshing()) {
                            return;
                        }
                        listView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                listView.onRefreshComplete();
                            }
                        }, 1000);
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getAuxDataIDsResult.getGetAuxDataIDsResultFromGisServer(context, layerID, geometry, strCon, strAuxTableName, new AuxUtils.AfterOnsucess() {
            @Override
            public void afterSucess() {
                if (getAuxDataIDsResult.OIDs.size() == 0) {
                    context.showErrorMsg("当前条件无附属数据");
                    return;
                }
                listView.setRefreshing(false);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.lookDeatil(dataList.get(position - 1).atts);
            }
        });
    }
}

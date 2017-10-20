package com.patrol.module.myplan;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.patrol.common.MyPlanUtil;
import com.patrol.entity.NoteInfo;
import com.patrol.entity.TaskInfo;
import com.patrol.module.MyPlanMapMenu;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.List;

/**
 * 我的计划
 */
public class PlanFragment extends Fragment {
    private PullToRefreshListView mPullRefreshListView;
    private PlanAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_plan_task_point_list, container, false);

        view.findViewById(R.id.layoutFilterbar).setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button btnFinish = (Button) view.findViewById(R.id.btnFinish);

        int custromBtnStyleResource = AppStyle.getCustromBtnStyleResource();
        if (custromBtnStyleResource > 0) {
            btnFinish.setBackgroundResource(custromBtnStyleResource);
        }

        btnFinish.setText("巡线日志");
        btnFinish.setVisibility(MyApplication.getInstance().getConfigValue("PatrolNote", 0) > 0 ? View.VISIBLE : View.GONE);

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userID = String.valueOf(MyApplication.getInstance().getUserId());

                new MmtBaseTask<String, Integer, ResultData<NoteInfo>>(getActivity()) {
                    @Override
                    protected ResultData<NoteInfo> doInBackground(String... params) {
                        ResultData<NoteInfo> data = null;

                        try {
                            String json = NetUtil.executeHttpGet(MyPlanUtil.getStandardURL() + "/FetchTodayNote", params);

                            data = new Gson().fromJson(json, new TypeToken<ResultData<NoteInfo>>() {
                            }.getType());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        return data;
                    }

                    @Override
                    protected void onSuccess(ResultData<NoteInfo> data) {
                        try {
                            super.onSuccess(data);

                            if (!isResultOK(data))
                                return;

                            final NoteInfo info = data.DataList.get(0);

                            final View noteView = LayoutInflater.from(getActivity()).inflate(R.layout.my_plan_note, null);

                            ((TextView) noteView.findViewById(R.id.tvDetail)).setText(info.Detail);
                            ((EditText) noteView.findViewById(R.id.txtRemark)).setText(info.Remark);

                            OkCancelDialogFragment fragment = new OkCancelDialogFragment("巡线日志", noteView);

                            fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                                @Override
                                public void onRightButtonClick(View view) {
                                    try {
                                        String remark = ((EditText) noteView.findViewById(R.id.txtRemark)).getText().toString();

                                        new MmtBaseTask<String, Integer, ResultWithoutData>(getActivity()) {
                                            @Override
                                            protected ResultWithoutData doInBackground(String... params) {
                                                String json = NetUtil.executeHttpGet(MyPlanUtil.getStandardURL() + "/ReportTodayNoteRemark", params);

                                                return ResultWithoutData.fromJson(json);
                                            }

                                            @Override
                                            protected void onSuccess(ResultWithoutData data) {
                                                super.onSuccess(data);

                                                if (isResultOK(data)) {
                                                    Toast.makeText(getActivity(), data.ResultMessage, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }.mmtExecute("userID", userID, "detail", info.Detail, "remark", remark);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });

                            fragment.show(getActivity().getSupportFragmentManager(), "");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.mmtExecute("userID", userID);
            }
        });

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.lvPoints);

        // 给listview添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity().getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // 更下下拉面板
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                // 执行更新任务,结束后刷新界面
                new UpdatePlanListTask(getActivity()).mmtExecute();
            }
        });

        ListView actualListView = mPullRefreshListView.getRefreshableView();

        registerForContextMenu(actualListView);

        actualListView.setAdapter(adapter);

        if (myItemClickListener != null) {
            mPullRefreshListView.setOnItemClickListener(myItemClickListener);
        }

        mPullRefreshListView.setRefreshing(false);
    }

    /**
     * 更新计划，获取的是计划的文本信息
     */
    class UpdatePlanListTask extends MmtBaseTask<Boolean, Integer, List<TaskInfo>> {
        UpdatePlanListTask(Context context) {
            super(context, false);
        }

        @Override
        protected List<TaskInfo> doInBackground(Boolean... params) {
            try {
                Log.v("巡线计划", "获取计划主体==>功能内获取");

                return ((MyPlanMapMenu) ((MapGISFrame) getActivity()).getFragment().menu).refreshMyPlan();
            } catch (Exception ex) {
                ex.printStackTrace();

                return null;
            }
        }

        @Override
        protected void onSuccess(List<TaskInfo> data) {
            try {
                if (data == null) {
                    Toast.makeText(context, "未获取到巡线任务", Toast.LENGTH_SHORT).show();

                    adapter = null;
                    mPullRefreshListView.setAdapter(null);
                } else {
                    adapter = new PlanAdapter(context, data);

                    mPullRefreshListView.setAdapter(adapter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mPullRefreshListView.onRefreshComplete();
            }
        }
    }

    private AdapterView.OnItemClickListener myItemClickListener;

    public void setOnMyItemClickListener(AdapterView.OnItemClickListener myItemClickListener) {
        this.myItemClickListener = myItemClickListener;
    }

    public void onTaskFinish() {
        mPullRefreshListView.setRefreshing(false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        try {
            super.onHiddenChanged(hidden);

            if (!hidden) {
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onDataSetChanged() {
        this.adapter.notifyDataSetChanged();
    }
}

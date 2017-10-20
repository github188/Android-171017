package com.patrol.module.KeyPoint;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.Receiver.MmtLocationListener;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.patrol.common.MyPlanUtil;
import com.patrol.entity.KeyPoint;
import com.patrol.entity.TaskInfo;
import com.patrol.module.MyPlanMapMenu;
import com.patrol.module.changeman.ChangePatrolManDialog;
import com.mapgis.mmt.global.MmtBaseTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class PointListFragment extends Fragment implements View.OnClickListener {
    private PullToRefreshListView listView;
    private KeyPointAdapter adapter;
    private TaskInfo task;
    private ArrayList<KeyPoint> points = new ArrayList<>();

    private TextView tvFilterDistance;
    private TextView tvFilterStatus;
    private TextView tvFilterOrder;

    private Filter.FilterListener mFilterListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_plan_task_point_list, container, false);

        this.tvFilterDistance = (TextView) view.findViewById(R.id.tvDistance);
        this.tvFilterStatus = (TextView) view.findViewById(R.id.tvStatus);
        this.tvFilterOrder = (TextView) view.findViewById(R.id.tvOrder);

        this.listView = (PullToRefreshListView) view.findViewById(R.id.lvPoints);
        ListView actualListView = listView.getRefreshableView();
        registerForContextMenu(actualListView);
        this.mFilterListener = new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                if (listView != null) {
                    listView.onRefreshComplete();
                }
            }
        };

        try {
            preXY = GpsReceiver.getInstance().getLastLocalLocation();
            task = getArguments().getParcelable("task");
            points.addAll(task.Points);
            this.adapter = new KeyPointAdapter(getActivity(), points);
            actualListView.setAdapter(adapter);

            filterData();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.findViewById(R.id.btnFeedback).performClick();
            }
        });

        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // 更新下拉面板
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                filterData(true);
            }
        });

        view.findViewById(R.id.btnFinish).setOnClickListener(this);
        view.findViewById(R.id.layoutDistance).setOnClickListener(this);
        view.findViewById(R.id.layoutStatus).setOnClickListener(this);
        view.findViewById(R.id.layoutOrder).setOnClickListener(this);

        View btnChangeMan = view.findViewById(R.id.btnChangeMan);
        btnChangeMan.setVisibility(View.GONE);
        if (MyApplication.getInstance().getConfigValue("PatrolChangeMan", 0) == 1) {
            btnChangeMan.setVisibility(View.VISIBLE);
            btnChangeMan.setOnClickListener(this);
        }

        GpsReceiver.getInstance().addObserver(listener);
    }

    private void filterData() {
        filterData(false);
    }

    private void filterData(boolean withRefresh) {
        String filterContent = tvFilterDistance.getText().toString() + ";"
                + tvFilterStatus.getText().toString() + ";"
                + tvFilterOrder.getText().toString();

        adapter.getFilter().setCurrentPosition(preXY).filter(filterContent,
                withRefresh ? mFilterListener : null);
    }

    TextView tvTarget = null;

    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.btnFinish) {
                OkCancelDialogFragment dialogFragment = new OkCancelDialogFragment("是否确认完成巡线？");

                dialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View view) {
                        new MmtBaseTask<String, Integer, ResultWithoutData>(getActivity()) {
                            @Override
                            protected ResultWithoutData doInBackground(String... params) {
                                try {
                                    String json = NetUtil.executeHttpGet(MyPlanUtil.getStandardURL() + "/FinishTask", "id", params[0]);

                                    return ResultWithoutData.fromJson(json);
                                } catch (Exception ex) {
                                    ex.printStackTrace();

                                    return null;
                                }
                            }

                            @Override
                            protected void onSuccess(ResultWithoutData data) {
                                try {
                                    super.onSuccess(data);

                                    if (isResultOK(data)) {
                                        Toast.makeText(context, data.ResultMessage, Toast.LENGTH_SHORT).show();

                                        task.IsFinish = 1;

                                        FragmentManager fm = getActivity().getSupportFragmentManager();
                                        Fragment fragment = fm.findFragmentByTag("PointListFragment");
                                        FragmentTransaction transaction = fm.beginTransaction();
                                        transaction.remove(fragment);
                                        transaction.commitAllowingStateLoss();
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }.mmtExecute(String.valueOf(task.ID));
                    }
                });
                dialogFragment.show(getActivity().getSupportFragmentManager(), "");
                return;
            }

            if (v.getId() == R.id.btnChangeMan) {
                Intent intent = new Intent(getActivity(), ChangePatrolManDialog.class);
                intent.putExtra("taskID", String.valueOf(task.ID));
                intent.putExtra("taskName", task.Name);
                intent.putExtra("taskState", task.getStatus());
                startActivity(intent);
                return;
            }

            String title = "";
            String[] items = null;

            if (v.getId() == R.id.layoutDistance) {
                title = "范围";
                items = new String[]{"500米", "1000米", "2000米", "5000米", "全部"};

                if (getView() != null)
                    tvTarget = tvFilterDistance;
            } else if (v.getId() == R.id.layoutStatus) {
                title = "状态";
                items = new String[]{"已到位", "未到位", "已反馈", "全部"};

                if (getView() != null)
                    tvTarget = tvFilterStatus;
            } else if (v.getId() == R.id.layoutOrder) {
                title = "排序";
                items = new String[]{"距离优先", "到位时间", "反馈时间", "默认"};

                if (getView() != null)
                    tvTarget = tvFilterOrder;
            }

            ListDialogFragment listFragment = new ListDialogFragment(title, items);
            listFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                @Override
                public void onListItemClick(int arg2, String value) {
                    tvTarget.setText(value);

                    filterData();
                }
            });

            listFragment.show(getActivity().getSupportFragmentManager(), "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        GpsReceiver.getInstance().removeObserver(listener);
        if (task.IsFinish == 1) {
            ((MyPlanMapMenu) ((MapGISFrame) getActivity()).getFragment().menu).onTaskFinish();
        }
        super.onDestroy();
    }


    public static class FeedBackSuccessEvent {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFeedBacked(FeedBackSuccessEvent feedBackSuccessEvent) {
        if (feedBackSuccessEvent == null) {
            return;
        }
        filterData();
    }

    GpsXYZ preXY;

    MmtLocationListener listener = new MmtLocationListener() {
        @Override
        public synchronized void onLocationChanged(GpsXYZ xy) {
            if (points == null || points.size() == 0)
                return;

            if (preXY != null) {
                double distance = GisUtil.calcDistance(xy.convertToPoint(), preXY.convertToPoint());
                double span = xy.getLocation().getTime() - preXY.getLocation().getTime();

                if (distance < 1 || span < 60 * 1000)
                    return;
            }

            if (getView() == null)
                return;

            preXY = xy;

            filterData();
        }
    };
}

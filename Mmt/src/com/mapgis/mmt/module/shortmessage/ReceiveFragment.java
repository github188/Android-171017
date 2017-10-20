package com.mapgis.mmt.module.shortmessage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ShortMessageBean;

import java.util.ArrayList;

/**
 * 收件箱Fragment
 */
public class ReceiveFragment extends Fragment {

    private final ArrayList<ShortMessageBean> msgs = new ArrayList<ShortMessageBean>();

    private PullToRefreshListView mPullRefreshListView;
    private BaseAdapter adapter;

    private MsgInterface msgInterface;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MsgInterface) {
            msgInterface = (MsgInterface) context;
        }
    }

    public void notifyListChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.pull_list, container, false);

        mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.order_form_list);

        adapter = new ListAdapter(getActivity(), msgs);
        ((ListAdapter)adapter).setType("发件人");

        mPullRefreshListView.getRefreshableView().setAdapter(adapter);

        // 给listview添加刷新监听器
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                // 更下下拉面板
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                // 执行更新任务,结束后刷新界面
                new RefreashMsgsTask().executeOnExecutor(MyApplication.executorService);

            }
        });

        mPullRefreshListView.setRefreshing(false);

        ListView actualListView = mPullRefreshListView.getRefreshableView();

        registerForContextMenu(actualListView);

        actualListView.setAdapter(adapter);

        mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                ShortMessageBean selectMsg = (ShortMessageBean) arg0.getItemAtPosition(arg2);

                msgInterface.updateState(selectMsg);
                selectMsg.setMsgState(1);
                adapter.notifyDataSetChanged();

                final String detail = selectMsg.getMsgDetail().split("#")[0];
                if (URLUtil.isNetworkUrl(detail)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("是否打开网址");
                    builder.setIcon(R.drawable.navigation_informationcenter);
                    builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.setNegativeButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = Uri.parse(detail);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            startActivity(intent);
                        }
                    });
                    builder.create().show();
                }
            }
        });

        MessagePresenter mp = new MessageOperation(getActivity());
        OnItemLongClickListenerImple listenerImple = new OnItemLongClickListenerImple(getActivity(), msgs, mp);
        mPullRefreshListView.getRefreshableView().setOnItemLongClickListener(listenerImple);

        return view;
    }

    @Override
    public void onDestroy() {
        // 将所有信息设置为已读
        for (ShortMessageBean msg : msgs) {
            msgInterface.updateState(msg);
        }

        super.onDestroy();
    }

    // /////////////////////////////////////////////////////////////////////////////
    /** 更新短消息信息 */
    // /////////////////////////////////////////////////////////////////////////////
    class RefreashMsgsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc/GetShortMessage";

            String result = NetUtil.executeHttpGet(url, "userID", String.valueOf(MyApplication.getInstance().getUserId()));

            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result == null) {
                msgInterface.showErrorMessage("返回结果为空值");
                return;
            }

            try {

                ResultData<MsgEntity> resultData = new Gson().fromJson(result, new TypeToken<ResultData<MsgEntity>>() {
                }.getType());

                if (resultData.ResultCode < 0) {
                    msgInterface.showErrorMessage(resultData.ResultMessage);
                } else {
                    for (MsgEntity entity : resultData.DataList) {
                        String msgDetail = entity.Msg_Detail + "#" + entity.Sender;
                        ShortMessageBean msg = new ShortMessageBean(entity.Msg_Time, msgDetail, 0, MyApplication
                                .getInstance().getUserId());
                        DatabaseHelper.getInstance().insert(msg);
                    }

                    msgs.clear();
                    msgs.addAll(DatabaseHelper.getInstance().query(
                            ShortMessageBean.class,
                            new SQLiteQueryParameters("msgState in (0,1,2) and userId = "
                                    + MyApplication.getInstance().getUserId(), "msgId desc")));

                    adapter.notifyDataSetChanged();

                    if (msgs.size() == 0) {
                        msgInterface.showToastTip("收件箱无信息...");
                    }
                }

            } catch (JsonSyntaxException e) {
                msgInterface.showErrorMessage("返回数据未能正确解析,请确认服务是否为最新版本");
            } finally {
                mPullRefreshListView.onRefreshComplete();
            }

        }

        class MsgEntity {
            public String Sender;
            public String Msg_Time;
            public String Msg_Detail;
        }

    }
}
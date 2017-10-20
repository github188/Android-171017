package com.mapgis.mmt.module.shortmessage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.entity.ShortMessageBean;

import java.util.ArrayList;

/**
 * 发件箱Fragment
 */
public class SendFragment extends Fragment {

    private ArrayList<ShortMessageBean> msgs = new ArrayList<ShortMessageBean>();

    private BaseAdapter adapter;
    private ListView listView;

    private MsgInterface msgInterface;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MsgInterface) {
            msgInterface = (MsgInterface) context;
        }
    }

    public SendFragment() {
        initData();
    }

    public void notifyDataSetChanged() {
        initData();
        ((ListAdapter) adapter).setMsgsList(msgs);
        adapter.notifyDataSetChanged();
    }

    public void notifyListChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        listView = new ListView(getActivity());
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        listView.setCacheColorHint(0);

        adapter = new ListAdapter(getActivity(), msgs);
        ((ListAdapter) adapter).setType("收件人");
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                    builder.setIcon(R.drawable.main_menu_message);
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
        listView.setOnItemLongClickListener(listenerImple);

        return listView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            if (msgs.size() == 0) {
                msgInterface.showToastTip("发件箱无信息...");
            }
        }
    }

    private void initData() {
        msgs.clear();
        msgs.addAll(DatabaseHelper.getInstance().query(
                ShortMessageBean.class,
                new SQLiteQueryParameters("msgState = 3 and userId = " + MyApplication.getInstance().getUserId(),
                        "msgId desc")));
    }
}
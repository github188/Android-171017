package com.mapgis.mmt.module.shortmessage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.adapter.TreeViewAdapter;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 新建信息Fragment
 */
public class CreateFragment extends Fragment {

    // private ArrayList<Hashtable<String, String>> users;
    private TreeViewAdapter treeAdapter;
    private final List<List<Boolean>> checked = new ArrayList<List<Boolean>>();

    private final List<String> parent = new ArrayList<String>();
    private final List<List<UserInfo>> child = new ArrayList<List<UserInfo>>();

    private EditText shortMessageEdit;
    private ExpandableListView userSelectList;
    private ImageButton sendButton;

    private MsgInterface msgInterface;
    private ShortMessageTabs shortMessageTabs;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MsgInterface) {
            this.msgInterface = (MsgInterface) context;
        }

        this.shortMessageTabs = (ShortMessageTabs) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (msgInterface.getMsgUserEntityList() == null || msgInterface.getMsgUserEntityList().size() == 0) {
            getSendMsgUsers();
        } else {
            handler.sendEmptyMessage(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layoutRoot = inflater.inflate(R.layout.msg_user_select_list, null);
        shortMessageEdit = (EditText) layoutRoot.findViewById(R.id.shortMessageEdit);
        userSelectList = (ExpandableListView) layoutRoot.findViewById(R.id.userSelectList);
        sendButton = (ImageButton) layoutRoot.findViewById(R.id.shortMessageButton);

        return layoutRoot;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = shortMessageEdit.getText().toString().trim();
                if (BaseClassUtil.isNullOrEmptyString(text)){
                    FragmentActivity activity = getActivity();
                    if (activity instanceof BaseActivity){
                        ((BaseActivity) activity).showToast("请输入消息！");
                    }
                    return;
                }
                String ReceiverID = "";
                String ReceiverName = "";

                for (int i = 0; i < checked.size(); i++) {
                    for (int j = 0; j < checked.get(i).size(); j++) {
                        if (checked.get(i).get(j)) {
                            ReceiverID = ReceiverID + String.valueOf(child.get(i).get(j).getUserid()) + ",";
                            ReceiverName = ReceiverName + child.get(i).get(j).getTruename() + ",";
                        }
                    }
                }

                if (ReceiverID.length() == 0) {
                    Toast.makeText(getActivity(), "没有选择对象...", Toast.LENGTH_SHORT).show();
                    return;
                }

                new SendMsgTask((BaseActivity) getActivity(), handler).execute(ReceiverID, ReceiverName, text);
            }
        });

    }

    private void getSendMsgUsers() {

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                String uri = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc/GetShortMsgUserList";
                String result = NetUtil.executeHttpGet(uri, "");
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                try {

                    if (result == null || result.trim().length() == 0) {
                        msgInterface.showErrorMessage("返回结果失败,查看是否需要更新服务");
                        return;
                    }

                    ResultData<MsgUserEntity> resultData = new Gson().fromJson(result,
                            new TypeToken<ResultData<MsgUserEntity>>() {
                            }.getType());

                    if (resultData.ResultCode < 0) {
                        msgInterface.showErrorMessage(resultData.ResultMessage);
                    } else {
                        msgInterface.getMsgUserEntityList().clear();
                        msgInterface.getMsgUserEntityList().addAll(resultData.DataList);
                        handler.sendEmptyMessage(0);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }.executeOnExecutor(MyApplication.executorService);
    }

    private void initUsers(ArrayList<MsgUserEntity> users) {
        if (users == null) {
            return;
        }
        for (MsgUserEntity entity : users) {

            // 将用户自身过滤掉
            if (Integer.valueOf(entity.UserId) == MyApplication.getInstance().getUserId()) {
                continue;
            }

            UserInfo user = new UserInfo();
            user.setUserid(Integer.valueOf(entity.UserId));
            user.setTruename(entity.UserName);
            user.setUserDept(entity.GroupName);
            if (!parent.contains(user.getUserDept())) {
                parent.add(user.getUserDept());
                List<UserInfo> list = new ArrayList<UserInfo>();
                child.add(list);
            }
            int i = parent.indexOf(user.getUserDept());
            child.get(i).add(user);
        }
    }

    private void initAdapter() {
        treeAdapter = new TreeViewAdapter(getActivity(), TreeViewAdapter.PaddingLeft >> 1, checked);
        List<TreeViewAdapter.TreeNode> treeNode = treeAdapter.GetTreeNode();
        for (int i = 0; i < parent.size(); i++) {
            TreeViewAdapter.TreeNode node = new TreeViewAdapter.TreeNode();
            List<Boolean> list = new ArrayList<Boolean>();
            node.parent = parent.get(i);
            for (int ii = 0; ii < child.get(i).size(); ii++) {
                node.childs.add(child.get(i).get(ii));
                list.add(false);
            }
            checked.add(list);
            treeNode.add(node);
        }
        treeAdapter.UpdateTreeNode(treeNode);
    }

    private final Handler handler = new Handler() {
        @SuppressLint("ShowToast")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:// 初始化员工列表
                    initUsers(msgInterface.getMsgUserEntityList());
                    initAdapter();
                    userSelectList.setAdapter(treeAdapter);
                    break;
                case 3:// 发送信息后更新界面
                    shortMessageEdit.setText("");

                    if (shortMessageTabs.getSendFragment() != null)
                        shortMessageTabs.getSendFragment().notifyDataSetChanged();
                    break;
            }
        }
    };

    /**
     * 设置发送的内容
     */
    public void setSendContent(String text) {
        shortMessageEdit.setText(text);
    }
}
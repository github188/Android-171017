package com.mapgis.mmt.module.shortmessage;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mapgis.mmt.common.widget.fragment.MultiSwitchFragment;
import com.mapgis.mmt.entity.ShortMessageBean;

import java.util.List;

/**
 * 发件箱和收件箱列表的长按事件监听
 */
public class OnItemLongClickListenerImple implements AdapterView.OnItemLongClickListener {
    private static final String TAG = "OnItemLongClickListener";
    private Context mContext;
    private int mPosition = -1;
    private AlertDialog mDialog;
    private List<ShortMessageBean> mMsgList;
    private String[] mFuncStr = new String[]{"复制", "转发", "删除"};

    private MessagePresenter mMsgPresenter;

    public OnItemLongClickListenerImple(Context mContext, List<ShortMessageBean> mMsgList
            , MessagePresenter mp) {
        this.mContext = mContext;
        this.mMsgList = mMsgList;
        this.mMsgPresenter = mp;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (mContext instanceof ShortMessageTabs) {
            MultiSwitchFragment msgFragment = ((ShortMessageTabs) mContext).getMsgFragment();
            int currentItem = msgFragment.getCurrentItem();
            if (currentItem == 0) {
                mPosition = position;
            } else if (currentItem == 1) {
                mPosition = position - 1;
            } else {
                return false;
            }
        }
        showDialog();
        return true;
    }

    private void showDialog() {
        if (mDialog != null) {
            mDialog.show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle("消息选项");
        ListView listView = new ListView(mContext);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                mContext, android.R.layout.simple_list_item_1, mFuncStr);
        listView.setAdapter(adapter);
        builder.setView(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switchFunc(mFuncStr[position]);
                mDialog.dismiss();
            }
        });

        mDialog = builder.create();
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }

    /**
     * 功能选择
     *
     * @param funcStr 操作
     */
    private void switchFunc(String funcStr) {
        if (mPosition < 0 || mMsgList == null) return;
        ShortMessageBean msg = mMsgList.get(mPosition);
        switch (funcStr) {
            case "复制":
                mMsgPresenter.copyMsgToClipborad(msg);
                break;
            case "转发":
                mMsgPresenter.dispatchMsg(msg);
                break;
            case "删除":
                deleteMsg(msg);
                break;
            default:
                break;
        }
    }

    private void deleteMsg(ShortMessageBean msg) {
        mMsgPresenter.deleteMsg(msg);
        mMsgList.remove(mPosition);
        if (mContext instanceof ShortMessageTabs) {
            MultiSwitchFragment msgFragment = ((ShortMessageTabs) mContext).getMsgFragment();
            int currentItem = msgFragment.getCurrentItem();
            if (currentItem == 0) {
               ((ShortMessageTabs) mContext).getSendFragment().notifyListChanged();
            } else if (currentItem == 1) {
                ((ShortMessageTabs) mContext).getRecieveFragment().notifyListChanged();
            }
        }
    }
}

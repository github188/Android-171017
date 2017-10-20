package com.mapgis.mmt.module.shortmessage;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.ShortMessageBean;

/**
 * Created by Comclay on 2017/2/21.
 * 短信的操作类
 */

public class MessageOperation implements MessagePresenter {
    private static final String TAG = "MessageOperation";

    private Context mContext;

    public MessageOperation(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void copyMsgToClipborad(ShortMessageBean msg) {
        String label = "shortMessage";
        String text = msg.getMsgContent();
        try {
            ClipboardManager clipboard = (ClipboardManager)
                    mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            if (mContext instanceof BaseActivity) {
                ((BaseActivity) mContext).showToast("已复制到剪贴板");
            }
        } catch (Exception e) {
            if (mContext instanceof BaseActivity) {
                ((BaseActivity) mContext).showToast("复制失败");
            }
            Log.w(TAG, "copyMsgToClipborad: 消息复制失败", e);
        }
    }

    @Override
    public void dispatchMsg(ShortMessageBean msg) {
        String text = msg.getMsgContent();
        if (mContext instanceof ShortMessageTabs) {
            CreateFragment createFragment = ((ShortMessageTabs) mContext).getCreateFragment();
            createFragment.setSendContent(text);
            ((ShortMessageTabs) mContext).getMsgFragment().setCurrentPage(2);
        }
    }

    @Override
    public void deleteMsg(ShortMessageBean msg) {
        DatabaseHelper.getInstance().delete(ShortMessageBean.class, "msgId = " + msg.getMsgId());
    }
}

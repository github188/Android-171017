package com.mapgis.mmt.module.shortmessage;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.AdjustSoftKeyboard;
import com.mapgis.mmt.common.widget.fragment.MultiSwitchFragment;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.ShortMessageBean;

import java.util.ArrayList;

public class ShortMessageTabs extends BaseActivity implements MsgInterface {

    public static final String DB_TABLE_NAME = "ShortMessage";
    public static final ArrayList<MsgUserEntity> MSG_USERS = new ArrayList<MsgUserEntity>();

    private MultiSwitchFragment msgFragment;
    private ReceiveFragment recieveFragment;
    private SendFragment sendFragment;
    private CreateFragment createFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] titles = new String[]{"发件箱", "收件箱", "新建信息"};

        recieveFragment = new ReceiveFragment();
        sendFragment = new SendFragment();
        createFragment = new CreateFragment();

        Fragment[] fragments = new Fragment[]{sendFragment, recieveFragment, createFragment};

        msgFragment = new MultiSwitchFragment();
        msgFragment.setDate(titles, fragments);
        msgFragment.setCurrentIndex(1);

        getBaseTextView().setText("即时信息");

        addFragment(msgFragment);

        AdjustSoftKeyboard.assistActivity(this);
    }

    public SendFragment getSendFragment() {
        return sendFragment;
    }

    // 修改短信息状态
    @Override
    public void updateState(ShortMessageBean msg) {
        if (msg.getMsgState() == 0) {
            DatabaseHelper.getInstance().update(ShortMessageBean.class,
                    msg.generateStateContentValues(1), "msgId = " + msg.getMsgId());
        }
    }

    @Override
    public void showErrorMessage(String errMsg) {
        super.showErrorMsg(errMsg);
    }

    @Override
    public void showToastTip(String msg) {
        super.showToast(msg);
    }

    @Override
    public ArrayList<MsgUserEntity> getMsgUserEntityList() {
        return MSG_USERS;
    }

    public CreateFragment getCreateFragment() {
        return createFragment;
    }

    public MultiSwitchFragment getMsgFragment() {
        return msgFragment;
    }

    public ReceiveFragment getRecieveFragment() {
        return recieveFragment;
    }
}

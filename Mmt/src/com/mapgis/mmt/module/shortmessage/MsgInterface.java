package com.mapgis.mmt.module.shortmessage;

import com.mapgis.mmt.entity.ShortMessageBean;

import java.util.ArrayList;

public interface MsgInterface {

    void updateState(ShortMessageBean selectMsg);
    void showErrorMessage(String errMsg);
    void showToastTip(String msg);
    ArrayList<MsgUserEntity> getMsgUserEntityList();
}

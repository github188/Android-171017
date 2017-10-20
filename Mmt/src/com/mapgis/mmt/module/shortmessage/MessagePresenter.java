package com.mapgis.mmt.module.shortmessage;

import com.mapgis.mmt.entity.ShortMessageBean;

/**
 * Created by Comclay on 2017/2/21.
 */

public interface MessagePresenter {

    void copyMsgToClipborad(ShortMessageBean msg);

    void dispatchMsg(ShortMessageBean msg);

    void deleteMsg(ShortMessageBean msg);
}

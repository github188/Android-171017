package com.mapgis.mmt.entity.docment;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Comclay on 2016/12/15.
 * 文档对象的操作类
 */

public interface DocumentOperate {
    /**
     * 将文档对象的缩略图设置给ImageView
     * @param context
     */
    void setIconToView(Context context, ImageView view);

    /**
     * 从服务器上下载文件
     */
    void downloadFromServer(CallBack callBack)throws Exception;

    /**
     * 从本地拷贝文件对象到当前的path路径
     */
    void copyFromLocal(String srcPath,CallBack callBack)throws Exception;

    /**
     * 当前文件的删除操作
     */
    boolean delete();

    interface CallBack<T extends Object>{
        void onSuccess(T t) ;

        void onFailure(Throwable t, int errorNo, String strMsg) ;
    }
}

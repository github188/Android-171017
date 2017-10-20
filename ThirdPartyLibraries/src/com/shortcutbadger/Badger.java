package com.shortcutbadger;

import android.content.ComponentName;
import android.content.Context;

import java.util.List;

/**
 * @author zhouxixiang
 */

public interface Badger {
    /**
     *
     * @param context 上下文
     * @param componentName   包名
     * @param badgerCount     消息数量
     *
     */
    void executeBadge(Context context, ComponentName componentName, int badgerCount) throws ShortCutBadgeException;

    /**
     *
     * @return 支持启动的包名
     */
    List<String> getSupportLaunchers();
    void setLaucherClass(Class clazz);
}

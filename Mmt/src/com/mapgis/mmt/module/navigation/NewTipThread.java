package com.mapgis.mmt.module.navigation;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseThread;
import com.mapgis.mmt.global.MmtNotificationManager;
import com.mapgis.mmt.module.gps.GPSTipUtils;
import com.mapgis.mmt.module.login.UserBean;
import com.shortcutbadger.ShortCutBadger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NewTipThread extends MmtBaseThread {
    private static NewTipThread TipThread;

    private WeakReference<Context> mWeakContext;
    private final NavigationEntity navigationEntity;

    private final List<TipMessage> oldTipMessages = new ArrayList<>();

    public static synchronized NewTipThread getInstance(Context context, NavigationEntity navigationEntity) {
        if (TipThread == null) {
            TipThread = new NewTipThread(context, navigationEntity);
        } else if (!context.equals(TipThread.mWeakContext.get())) {
            TipThread.mWeakContext = new WeakReference<>(context);
        }

        return TipThread;
    }

    private NewTipThread(Context context, NavigationEntity navigationEntity) {
        this.mWeakContext = new WeakReference<>(context);
        this.navigationEntity = navigationEntity;
    }

    /**
     * 手动唤醒线程
     */
    public static void notifyThread() {
        if (TipThread != null) {
            TipThread.interrupt();
        }
    }

    @Override
    public void abort() {
        try {
            super.abort();

            TipThread = null;

            GPSTipUtils.releaseNewTip();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private long preRepeatTipTick = -1;
    private long preNewTipTick = -1;

    private void showRepeatTip(long nowTick, long repeatInterval) {
        try {
            if (nowTick - preRepeatTipTick < repeatInterval * 1000)
                return;

            if (notifyID <= 0)
                return;

            List<String> names = navigationEntity.functionNames;
            boolean hasTip = false;

            for (TipMessage tip : oldTipMessages) {
                if (!names.contains(tip.ModuleName))
                    continue;

                if (tip.Count > 0) {
                    hasTip = true;
                    break;
                }
            }

            if (!hasTip) {
                MmtNotificationManager.cancel(notifyID);
            } else {

                //响铃提示
                Context context = mWeakContext.get();
                if (context == null || ((Activity) context).isFinishing()) {
                    return;
                }
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(ringUri);

                Notification notification = builder.build();

                MmtNotificationManager.notify(notifyID, notification);

                preRepeatTipTick = nowTick;

                //语音提示
                GPSTipUtils.newTip();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean showNewTip(long nowTick, long newInterval, String url) {
        try {
            if (nowTick - preNewTipTick < newInterval * 1000)
                return false;

            preNewTipTick = nowTick;

            List<TipMessage> tips = new ArrayList<>();

            String json = NetUtil.executeHttpGet(url);

            if (TextUtils.isEmpty(json)) {
                return false;
            }

            ResultData<TipMessage> data = new Gson().fromJson(json, new TypeToken<ResultData<TipMessage>>() {
            }.getType());

            if (data.ResultCode <= 0 || data.DataList == null || data.DataList.size() == 0)
                return false;

            for (TipMessage tip : data.DataList) {
                if (tips.contains(tip)) {
                    tips.set(tips.indexOf(tip), tip);
                } else {
                    tips.add(tip);
                }
            }

            showShortCutBadge(tips);

            StringBuilder sb = new StringBuilder();

            for (TipMessage tip : tips) {
                if (!navigationEntity.functionNames.contains(tip.ModuleName) || tip.Count <= 0)
                    continue;

                int count = hasNewTip(tip);

                if (count <= 0)
                    continue;

                sb.append("您有");
                sb.append(count);
                sb.append("条");
                sb.append(tip.Desc);
                sb.append(",");
            }

            refreshNavigationView(tips);

            oldTipMessages.clear();
            oldTipMessages.addAll(tips);

            String msg = sb.toString();

            if (TextUtils.isEmpty(msg))
                return false;

            setNotification(msg + "请注意查收。");

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        }
    }

    private void showShortCutBadge(List<TipMessage> tips) {
        int totalCount = 0;//每次进入先把总数置零
        for (TipMessage tip : tips) {
            if (!navigationEntity.functionNames.contains(tip.ModuleName) || tip.Count <= 0) {
                continue;
            }
            totalCount += tip.Count;
        }
        Context context = mWeakContext.get();
        if (context == null) {
            return;
        }
        ShortCutBadger.setXiaomiLaucherClass(ActivityClassRegistry.getInstance().getActivityClass("主界面"));
        ShortCutBadger.applyCount(context, totalCount);
    }

    @Override
    public void run() {
        int userID = MyApplication.getInstance().getUserId();
        String url;

        if (MyApplication.getInstance().getConfigValue("NewNotification", 0) > 0) {
            url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_NewNotification/REST/NewNotificationREST.svc/GetMyNotifications"
                    + "?userID=" + userID + "&subSystem=手持系统&tag=";
        } else {
            UserBean userBean = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);
            String planID = String.valueOf(userBean != null ? userBean.PatrolPlanID : 0);

            url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/PatrolREST.svc/GetTipMessage"
                    + "?UserID=" + userID + "&flowid=" + planID;
        }

        long tipStyle = MyApplication.getInstance().getConfigValue("RepeatNetTipRing", 0);

        long repeatInterval = MyApplication.getInstance().getConfigValue("PromptInterval", 180);
        long newInterval = MyApplication.getInstance().getConfigValue("newContentDetectInterval", 300);

        //新工单提示
        while (!isExit) {
            try {
                long nowTick = new Date().getTime();

                boolean hasNewTip = showNewTip(nowTick, newInterval, url);

                if (hasNewTip || tipStyle <= 0)//有新消息提醒过了或者不需要重复提醒，都继续下个循环
                    continue;

                showRepeatTip(nowTick, repeatInterval);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断新获取的数据的计数是否大于原有的计数
     */
    private int hasNewTip(TipMessage newComeMsg) {
        if (newComeMsg == null || newComeMsg.ModuleName == null) {
            return -1;
        }

        for (TipMessage oldTipMessage : oldTipMessages) {
            if (oldTipMessage.ModuleName.equals(newComeMsg.ModuleName)) {
                if (oldTipMessage.ids != null && oldTipMessage.ids.length() > 0) { // 如果ids有值，表示需要通过ids判断是否有新数据而进行提醒
                    navigationEntity.getItemByName(newComeMsg.ModuleName).doneNewMsg = false;
                    List<String> oldList = BaseClassUtil.StringToList(oldTipMessage.ids, ",");
                    List<String> newList = BaseClassUtil.StringToList(newComeMsg.ids, ",");
                    newList.removeAll(oldList);
                    MyApplication.getInstance().showMessageWithHandle("" + (newList.size() - oldList.size()));
                    return newList.size();
                } else if (oldTipMessage.Count < newComeMsg.Count) { // 如果ids没有值，则新值count进行对比，count变大时进行提醒其差值
                    navigationEntity.getItemByName(newComeMsg.ModuleName).doneNewMsg = false;
                    return newComeMsg.Count - oldTipMessage.Count;
                } else {
                    return -1;
                }
            }
        }

        return newComeMsg.Count;
    }

    /**
     * 刷新界面
     */
    public void refreshNavigationView(List<TipMessage> msgs) {
        try {
            for (TipMessage msg : msgs) {
                NavigationItem item = navigationEntity.getItemByName(msg.ModuleName);

                if (item != null) {
                    item.Count = (String.valueOf(msg.Count));
                }
            }

            Context context = mWeakContext.get();
            if (context == null || (((NavigationActivity) context)).isFinishing())
                return;

            Log.w("NewTipTHread", "refreshNavigationView:刷新数据 ");
            ((NavigationActivity) context).onNewTipReceive();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 通知来显示信息
     */
    private void setNotification(String content) throws Exception {
        Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Context context = mWeakContext.get();
        if (context == null || ((Activity) context).isFinishing()) {
            return;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

//        builder.setSmallIcon(context.getApplicationInfo().icon);
//
//        // 点击后提示消失
//        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
//        builder.setContentTitle(context.getString(R.string.app_name));
//        builder.setContentText(content);

        builder.setSound(ringUri);
        Notification notification = builder.build();

        //响铃提示
        if (notifyID < 0)
            notifyID = MmtNotificationManager.notify(notification);
        else
            MmtNotificationManager.notify(notifyID, notification);

        //语音提示
        GPSTipUtils.newTip();
    }

    private int notifyID = -1;

    private class TipMessage {
        public String ParentName;
        public String ModuleName;
        public int Count;
        public String Desc;
        public String ids;

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof TipMessage)) {
                return false;
            } else if (BaseClassUtil.isNullOrEmptyString(((TipMessage) o).ParentName)
                    || BaseClassUtil.isNullOrEmptyString(ParentName)) {
                return ((TipMessage) o).ModuleName.equalsIgnoreCase(this.ModuleName);
            } else {
                return ((TipMessage) o).ParentName.equalsIgnoreCase(this.ParentName)
                        && ((TipMessage) o).ModuleName.equalsIgnoreCase(this.ModuleName);
            }
        }

        @Override
        public String toString() {
            return this.ModuleName + ":" + this.Count;
        }
    }
}

package com.mapgis.mmt.doinback;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseThread;
import com.patrolproduct.server.MyPlanReporterThread;

import java.util.List;

public class ReportInBackThread extends MmtBaseThread {

    private static final Object syncRoot = new Object();

    private static ReportInBackThread instance = null;

    public synchronized static ReportInBackThread getInstance() {
        if (instance == null) {
            instance = new ReportInBackThread();
        }
        return instance;
    }

    private final SQLiteQueryParameters sqLiteQueryParameters; // 后台数据库中任务查询参数
    private final long retryInterval; // 重试时间间隔
    private final int maxRetryTimes; // 最大重试次数

    private Runnable planReportRunnable;

    public long getRetryInterval() {
        return retryInterval;
    }

    private ReportInBackThread() {

        String uri = ServerConnectConfig.getInstance().getBaseServerPath();
        this.sqLiteQueryParameters = new SQLiteQueryParameters("status=" + ReportInBackEntity.REPORTING + " and uri like '" + uri + "%'");
        this.maxRetryTimes = (int) MyApplication.getInstance().getConfigValue("ReportInBackRetryTimes", 10);
        this.retryInterval = MyApplication.getInstance().getConfigValue("ReportInBackInterval", 5) * 1000;

        this.planReportRunnable = new MyPlanReporterThread();
    }

    @Override
    public void abort() {
        super.abort();

        instance = null;
    }

    @Override
    public void run() {

        String noTipTypes = ",巡线到位,实时位置,";
        int count;

        while (!isExit) {

            count = 0;

            try {
                // 防止上报过程中（非休眠状态，此时通过通知停在休眠无法达到立即上报的目的）有新的任务加入，设置循环直至无任何缓存信息时才进入长休眠
                while (!isExit) {

                    if (count++ > 2) {
                        break;
                    }

                    if (planReportRunnable != null) {
                        planReportRunnable.run();
                    }

                    List<ReportInBackEntity> data = DatabaseHelper.getInstance().query(
                            ReportInBackEntity.class, sqLiteQueryParameters);

                    if (data == null || data.size() == 0 || !NetUtil.testNetState()) {
                        break;
                    }

                    // 遍历循环<后台反馈表>
                    for (ReportInBackEntity entity : data) {
                        try {

                            // 达到最大重试次数后不再尝试上报，失败的任务这里不移除，而是在后台任务列表中手工移除
                            if (entity.getRetryTimes() >= maxRetryTimes) {
                                continue;
                            }

                            ResultData<Integer> result = entity.report();

                            if (result.ResultCode > 0) {  // Success

                                if (!noTipTypes.contains("," + entity.getType() + ",")) {
                                    MyApplication.getInstance().showMessageWithHandle("[" + entity.getType() + "] 上传成功");
                                }
                                // 上报成功后从数据库中删除该条记录
                                entity.delete();

                            } else {

                                // 失败则更新数据库中记录的重试次数
                                entity.setRetryTimes(entity.getRetryTimes() + 1);
                                entity.update(new String[]{"retryTimes"});

                                // 达到最大重试次数后给出错误提示
                                if (entity.getRetryTimes() == maxRetryTimes && !noTipTypes.contains("," + entity.getType() + ",")) {
                                    MyApplication.getInstance().showMessageWithHandle("[" + entity.getType() + "] " +
                                            (BaseClassUtil.isNullOrEmptyString(result.ResultMessage) ? "服务器处理失败" : result.ResultMessage));
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                /** 主动等待被生产者线程唤醒，或者超时后进入下一个轮询 **/
                synchronized (syncRoot) {
                    try {
                        syncRoot.wait(retryInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 主动通知消费者线程开始工作
     */
    public void notifyDoWork() {
        synchronized (syncRoot) {
            syncRoot.notify();
        }
    }
}

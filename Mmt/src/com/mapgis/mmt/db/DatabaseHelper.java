package com.mapgis.mmt.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.config.CitySystemConfig;
import com.mapgis.mmt.constant.GlobalPathManager;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.AppLogger;
import com.mapgis.mmt.entity.BigFileInfo;
import com.mapgis.mmt.entity.EventReportCache;
import com.mapgis.mmt.entity.IPPortBean;
import com.mapgis.mmt.entity.NetLogInfo;
import com.mapgis.mmt.entity.SavedReportInfo;
import com.mapgis.mmt.entity.ShortMessageBean;
import com.mapgis.mmt.entity.TrafficInfo;
import com.mapgis.mmt.entity.UserPwdBean;
import com.mapgis.mmt.module.flowreport.FlowReportTaskParameters;
import com.mapgis.mmt.module.gps.entity.GPSTraceInfo;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.module.systemsetting.download.DownloadInfo;
import com.mapgis.mmt.module.taskcontrol.TaskControlEntity;
import com.mapgis.mmt.net.multhreaddownloader.FileDownLog;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite基础操作封装帮助类
 *
 * @author Administrator
 */
public class DatabaseHelper {
    private static DatabaseHelper instance = null;

    public static DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }

        return instance;
    }

    private SQLiteDatabase sqLiteDatabase;

    public SQLiteDatabase getSqLiteDatabase() {
        return sqLiteDatabase;
    }

    /**
     * 创建表
     *
     * @param classOfT 对象表的模板类
     */
    public <T extends ISQLiteOper> void createTable(Class<T> classOfT) {
        Cursor cursor = null;

        try {
            ISQLiteOper bean = classOfT.newInstance();

            String sql = "select count(*) from sqlite_master where type='table' and name='" + bean.getTableName() + "' collate nocase";

            cursor = sqLiteDatabase.rawQuery(sql, null);

            // 表不存在则创建
            if (!(cursor.moveToFirst() && cursor.getInt(0) > 0)) {
                sqLiteDatabase.execSQL("create table " + bean.getTableName() + bean.getCreateTableSQL());
            } else {// 表存在但与建表语句列少，则修改表
                List<String> notExistColunms = getNotExistColunms(classOfT);

                // 更改ReportInBackEntity的表结构
                if (ReportInBackEntity.class == classOfT && notExistColunms.contains("_id")) {

                    delete(classOfT, null);// 表结构变更时，强制删除本地旧表缓存的数据
                    sqLiteDatabase.execSQL("drop table " + bean.getTableName());
                    sqLiteDatabase.execSQL("create table " + bean.getTableName() + bean.getCreateTableSQL());

                } else {

                    if (notExistColunms.size() != 0) {
                        delete(classOfT, null);// 表结构变更时，强制删除本地旧表缓存的数据

                        for (String column : notExistColunms) {
                            sqLiteDatabase.execSQL("alter table " + bean.getTableName() + " add " + column);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 获取已存在的表中的列名与建表语句中的列表做对比
     *
     * @param classOfT 建表语句中新增的列名
     */
    public <T extends ISQLiteOper> List<String> getNotExistColunms(Class<T> classOfT) {
        List<String> inexistColunms = new ArrayList<>();

        Cursor cursor = null;

        try {
            ISQLiteOper bean = classOfT.newInstance();

            // 建表语句里的列数目
            String columnsStr = bean.getCreateTableSQL().replace("(", "").replace(")", "").trim();
            String[] columns = columnsStr.split(",");

            cursor = this.sqLiteDatabase.query(bean.getTableName(), null, null, null, null, null, null, "0,1");
            String[] columnsInTable = cursor.getColumnNames();

            for (String c : columns) {
                c = c.trim();

                if (c.contains(" ")) {
                    c = c.split(" ")[0];// id integer primary key
                }

                boolean isExist = false;

                for (String t : columnsInTable) {
                    if (t.equalsIgnoreCase(c)) {
                        isExist = true;

                        break;
                    }
                }

                if (!isExist) {
                    inexistColunms.add(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return inexistColunms;
    }

    /**
     * 初始化数据库
     */
    public void initDB(Context context) {
        try {
            sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(GlobalPathManager.getLocalConfigPath() + "mmt.db", null);

            // 初始化《短消息》表
            createTable(ShortMessageBean.class);

            // 初始化《数据采集反馈》表
            createTable(SavedReportInfo.class);

            // 初始化《历史事件》表
            createTable(FlowReportTaskParameters.class);

            // 初始化《后台反馈》表
            createTable(ReportInBackEntity.class);

            // 初始化《任务监控》表
            createTable(TaskControlEntity.class);

            // 初始化《临时事件大图片、录音附件》表
            createTable(BigFileInfo.class);

            // 初始化《系统设置》表
            createTable(CitySystemConfig.class);

            // 初始化《文件下载日志表》表
            createTable(FileDownLog.class);

            // 初始化《服务ip和port配置》表
            createTable(IPPortBean.class);
            IPPortBean.initData(context);

            // 初始化《用户密码》表
            createTable(UserPwdBean.class);
            UserPwdBean.initData(context);

            // 初始化《上报页面缓存》表
            createTable(EventReportCache.class);

            // 初始化《每个月的流量记录信息》表
            createTable(TrafficInfo.class);

            // 初始化《下载信息》表
            createTable(DownloadInfo.class);

            // 初始化《坐标上传》表
            createTable(GpsXYZ.class);
            // 删除一周之前的坐标数据
            sqLiteDatabase.delete(GpsXYZ.class.newInstance().getTableName(), "reportTime<datetime('now','-7 day')", null);

            //完整轨迹点记录表
            createTable(GPSTraceInfo.class);
            sqLiteDatabase.delete(GPSTraceInfo.class.newInstance().getTableName(), "reportTime<datetime('now','-7 day')", null);

            // 初始化《程序日志记录》表
            createTable(AppLogger.class);
            sqLiteDatabase.delete(AppLogger.class.newInstance().getTableName(), "time<datetime('now','-7 day')", null);

            // 初始化《网络日志记录》表
            createTable(NetLogInfo.class);
            sqLiteDatabase.delete(NetLogInfo.class.newInstance().getTableName(), "startTime<datetime('now','-30 day')", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            sqLiteDatabase.close();
        }
    }

    /**
     * 查询操作
     *
     * @param classOfT 查询对象模板
     * @param para     查询参数
     * @return 查询结果列表
     */
    public <T extends ISQLiteOper> ArrayList<T> query(Class<T> classOfT, SQLiteQueryParameters para) {
        ArrayList<T> beans = new ArrayList<>();
        Cursor cursor = null;

        try {
            T bean = classOfT.newInstance();

            cursor = this.sqLiteDatabase.query(BaseClassUtil.isNullOrEmptyString(para.table) ? bean.getTableName() : para.table,
                    para.columns, para.selection, para.selectionArgs, para.groupBy, para.having, para.orderBy, para.limit);

            while (cursor.moveToNext()) {
                bean.buildFromCursor(cursor);

                beans.add(bean);

                bean = classOfT.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return beans;
    }

    /**
     * 查询操作
     *
     * @param classOfT 查询对象模板
     * @return 查询结果列表
     */
    public <T extends ISQLiteOper> ArrayList<T> query(Class<T> classOfT) {
        try {
            SQLiteQueryParameters para = classOfT.newInstance().getSqLiteQueryParameters();

            return query(classOfT, para);
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * 查询操作
     *
     * @param classOfT  查询对象模板
     * @param selection 查询参数
     * @return 查询结果列表
     */
    public <T extends ISQLiteOper> ArrayList<T> query(Class<T> classOfT, String selection) {
        return query(classOfT, new SQLiteQueryParameters(selection));
    }

    /**
     * 单个查询操作
     *
     * @param classOfT  查询对象模板
     * @param selection 查询参数
     * @return 查询结果集的第一个对象
     */
    public <T extends ISQLiteOper> T queryScalar(Class<T> classOfT, String selection) {
        try {
            ArrayList<T> result = query(classOfT, new SQLiteQueryParameters(selection));

            if (result != null && result.size() > 0) {
                return result.get(0);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * 插入操作
     *
     * @param bean 插入的对象
     * @return 新增记录的rowid, 失败则返回-1
     */
    public long insert(ISQLiteOper bean) {
        try {
            return this.sqLiteDatabase.insert(bean.getTableName(), null, bean.generateContentValues());
        } catch (Exception e) {
            e.printStackTrace();

            return -1;
        }
    }

    /**
     * 更新操作
     */
    public <T extends ISQLiteOper> long update(Class<T> classOfT, ContentValues cv, String where) {
        try {
            return this.sqLiteDatabase.update(classOfT.newInstance().getTableName(), cv, where, null);
        } catch (Exception e) {
            e.printStackTrace();

            return -1;
        }
    }

    /**
     * 删除操作
     *
     * @param classOfT 删除对象模板类
     * @param where    删除条件
     * @return 受影响的行数, 失败则返回-1
     */
    public <T extends ISQLiteOper> long delete(Class<T> classOfT, String where) {
        try {
            return this.sqLiteDatabase.delete(classOfT.newInstance().getTableName(), where, null);
        } catch (Exception e) {
            e.printStackTrace();

            return -1;
        }
    }

    /**
     * @param classOfT 实体对象
     * @param sql      查询语句，多表连查
     * @return 返回的实体对象的List
     */
    public <T extends ISQLiteOper> List<T> queryBySql(Class<T> classOfT, String sql) {
        List<T> beans = new ArrayList<>();
        Cursor cursor = null;

        try {
            T bean = classOfT.newInstance();

            cursor = DatabaseHelper.getInstance().getSqLiteDatabase().rawQuery(sql, null);

            while (cursor.moveToNext()) {
                bean.buildFromCursor(cursor);

                beans.add(bean);

                bean = classOfT.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return beans;
    }
}

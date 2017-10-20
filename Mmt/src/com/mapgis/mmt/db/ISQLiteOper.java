package com.mapgis.mmt.db;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * SQLite操作的基础接口
 * 
 * @author Administrator
 * 
 */
public interface ISQLiteOper {
	/**
	 * 获取表名称
	 * 
	 * @return 表名称
	 */
    String getTableName();

	/**
	 * 获取建表脚本
	 * 
	 * @return 建表脚本
	 */
    String getCreateTableSQL();

	/**
	 * 获取默认的查询参数
	 * 
	 * @return 查询参数
	 */
    SQLiteQueryParameters getSqLiteQueryParameters();

	/**
	 * 插入操作时获取插入对象
	 * 
	 * @return 插入对象
	 */
    ContentValues generateContentValues();

	/**
	 * 查询操作时从Cursor中构建对象
	 * 
	 * @param cursor
	 *            数据库查询游标
	 */
    void buildFromCursor(Cursor cursor);
}

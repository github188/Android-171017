package com.mapgis.mmt.net.multhreaddownloader;

import android.content.ContentValues;
import android.database.Cursor;

import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class FileDownLog implements ISQLiteOper {
	public int ID;
	public String URL;
	public String ServerTime;
	public int ThreadID;
	public int DownLength;

	@Override
	public String getTableName() {
		return "FileDownLog";
	}

	@Override
	public String getCreateTableSQL() {
		return "(id integer primary key, url, serverTime,threadID, downLength)";
	}

	@Override
	public SQLiteQueryParameters getSqLiteQueryParameters() {
		return null;
	}

	@Override
	public ContentValues generateContentValues() {
		ContentValues cv = new ContentValues();

		cv.put("url", this.URL);
		cv.put("serverTime", this.ServerTime);
		cv.put("threadID", this.ThreadID);
		cv.put("downLength", this.DownLength);

		return cv;
	}

	@Override
	public void buildFromCursor(Cursor cursor) {
		this.ID = cursor.getInt(0);

		this.URL = cursor.getString(1);
		this.ServerTime = cursor.getString(2);
		this.ThreadID = cursor.getInt(3);
		this.DownLength = cursor.getInt(4);
	}

	/**
	 * 获取每条线程已经下载的文件长度
	 * 
	 * @param url
	 * @return
	 */
	public Map<Integer, Integer> getData(String url, String serverTime) {
		Map<Integer, Integer> data = new Hashtable<Integer, Integer>();

		// 获取下载记录
		ArrayList<FileDownLog> logs = DatabaseHelper.getInstance().query(FileDownLog.class,
				"url='" + url + "' AND serverTime='" + serverTime + "'");

		if (logs.size() > 0) {
			for (FileDownLog log : logs) {
				data.put(log.ThreadID, log.DownLength);// 把各条线程已经下载的数据长度放入data中
			}
		}

		return data;
	}

	/**
	 * 保存每条线程已经下载的文件长度
	 * 
	 * @param path
	 * @param map
	 */
	public void save(String path, String serverTime, Map<Integer, Integer> map) {
		DatabaseHelper.getInstance().delete(FileDownLog.class, "url='" + path + "'");

		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			FileDownLog log = new FileDownLog();

			log.URL = path;
			log.ServerTime = serverTime;
			log.ThreadID = entry.getKey();
			log.DownLength = entry.getValue();

			DatabaseHelper.getInstance().insert(log);
		}
	}

	/**
	 * 实时更新每条线程已经下载的文件长度
	 * 
	 * @param path
	 * @param map
	 */
	public void update(String path, Map<Integer, Integer> map) {
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			ContentValues cv = new ContentValues();

			cv.put("downlength", entry.getValue());

			DatabaseHelper.getInstance().update(FileDownLog.class, cv, "url='" + path + "' and threadid=" + entry.getKey());
		}
	}

	/**
	 * 当文件下载完成后，删除对应的下载记录
	 * 
	 * @param path
	 */
	public void delete(String path) {
		DatabaseHelper.getInstance().delete(FileDownLog.class, "url='" + path + "'");
	}
}

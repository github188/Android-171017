package com.mapgis.mmt.module.gps.entity;

import android.content.ContentValues;
import android.database.Cursor;

import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

import java.text.DecimalFormat;

public class GpsTraceBean implements ISQLiteOper {
	private String fullString;

	public String getFullString() {
		return fullString;
	}

	public void setFullString(String fullString) {
		this.fullString = fullString;
	}

	@Override
	public String getTableName() {
		return "PositonReporter";
	}

	@Override
	public SQLiteQueryParameters getSqLiteQueryParameters() {
		return null;
	}

	@Override
	public ContentValues generateContentValues() {
		return null;
	}

	@Override
	public void buildFromCursor(Cursor cursor) {
		DecimalFormat decimalFormat = new DecimalFormat("#.00000");

		fullString = "";
		fullString += "序号：" + cursor.getInt(0) + 1 + "\n";

		fullString += "X 值：" + cursor.getString(1) + "\t   ";
		fullString += "Y 值：" + cursor.getString(2) + "\n";

		fullString += "时间：" + cursor.getString(3) + "\n";

		fullString += "纬度：" + decimalFormat.format(cursor.getFloat(5)) + "\t   ";
		fullString += "经度：" + decimalFormat.format(cursor.getFloat(6)) + "\n";

		fullString += "精度：" + cursor.getString(7) + "\t   ";
		fullString += "状态：" + (cursor.getInt(4) == 0 ? "未上报" : "已上报") + "\n";

		fullString += "电量：" + cursor.getString(9) + "\t   ";
		fullString += "cpu：" + cursor.getString(8) + "\n";
		fullString += "内存：" + cursor.getString(10) + "\t   ";
		fullString += "速度：" + cursor.getString(11);
	}

	@Override
	public String getCreateTableSQL() {
		return null;
	}
}

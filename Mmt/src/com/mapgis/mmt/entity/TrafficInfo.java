package com.mapgis.mmt.entity;

import android.content.ContentValues;
import android.database.Cursor;

import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

public class TrafficInfo implements ISQLiteOper {
//	int id;
	String date;
	String totalTraffic;
	
	
//	public int getId() {
//		return id;
//	}
//
//	public void setId(int id) {
//		this.id = id;
//	}

	public TrafficInfo() {
		super();
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTotalTraffic() {
		return totalTraffic;
	}

	public void setTotalTraffic(String totalTraffic) {
		this.totalTraffic = totalTraffic;
	}

	public TrafficInfo( String date ,String totalTraffic) {
		super();
//		this.id = id;
		this.date = date;
		this.totalTraffic = totalTraffic;
	}

	@Override
	public String getTableName() {
		return "TrafficInfo";
	}

	@Override
	public String getCreateTableSQL() {
//		return "(id integer primary key,date,totalTraffic)";  
		return "(date char primary key,totalTraffic)";  
	}

	@Override
	public SQLiteQueryParameters getSqLiteQueryParameters() {
		return new SQLiteQueryParameters();
	}

	@Override
	public ContentValues generateContentValues() {
		ContentValues cv = new ContentValues();
//		cv.put("id", this.id);
		cv.put("date", this.date);
		cv.put("totalTraffic", this.totalTraffic);
		return cv;
	}

	@Override
	public void buildFromCursor(Cursor cursor) {
//		this.id = cursor.getInt(0);
//		this.date = cursor.getString(1);
//		this.totalTraffic = cursor.getString(2);
		this.date = cursor.getString(0);
		this.totalTraffic = cursor.getString(1);
	}

}

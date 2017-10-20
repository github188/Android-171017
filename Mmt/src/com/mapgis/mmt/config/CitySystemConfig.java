package com.mapgis.mmt.config;

import android.content.ContentValues;
import android.database.Cursor;

import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

public class CitySystemConfig implements ISQLiteOper {
	public int ID;

	public String ConfigKey;
	public String ConfigValue;

	public int UserID;

	public CitySystemConfig() {
	}

	public CitySystemConfig(String configKey, String configValue, int userID) {
		ConfigKey = configKey;
		ConfigValue = configValue;
		UserID = userID;
	}

	@Override
	public String getTableName() {
		return "CitySystemConfig";
	}

	@Override
	public String getCreateTableSQL() {
		return "(ID integer primary key,ConfigKey,ConfigValue,UserID)";
	}

	@Override
	public SQLiteQueryParameters getSqLiteQueryParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentValues generateContentValues() {
		ContentValues contentValues = new ContentValues();
		contentValues.put("ConfigKey", ConfigKey);
		contentValues.put("ConfigValue", ConfigValue);
		contentValues.put("UserID", UserID);
		return contentValues;
	}

	@Override
	public void buildFromCursor(Cursor cursor) {
		this.ID = cursor.getInt(0);
		this.ConfigKey = cursor.getString(1);
		this.ConfigValue = cursor.getString(2);
		this.UserID = cursor.getInt(2);
	}

	@Override
	public String toString() {
		return "ConfigKey=" + ConfigKey + " ConfigValue=" + ConfigValue + " UserID=" + UserID;
	}

}

package com.mapgis.mmt.db;

/**
 * SQLite查询参数类
 * 
 * @author Administrator
 * 
 */
public class SQLiteQueryParameters {

	public String table;
	public String[] columns;
	public String selection;
	public String[] selectionArgs;
	public String groupBy;
	public String having;
	public String orderBy;
	public String limit;

	public SQLiteQueryParameters() {
	}

	public SQLiteQueryParameters(String selection) {
		this(null, null, selection, null, null, null, null, null);
	}

	public SQLiteQueryParameters(String selection, String orderBy) {
		this(null, null, selection, null, null, null, orderBy, null);
	}

	public SQLiteQueryParameters(String table, String selection, String orderBy) {
		this(table, null, selection, null, null, null, orderBy, null);
	}

	public SQLiteQueryParameters(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		this.table = table;
		this.columns = columns;
		this.selection = selection;
		this.selectionArgs = selectionArgs;
		this.groupBy = groupBy;
		this.having = having;
		this.orderBy = orderBy;
		this.limit = limit;
	}

}

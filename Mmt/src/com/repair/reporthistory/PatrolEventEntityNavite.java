package com.repair.reporthistory;

import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.repair.eventreport.EventReportEntity;

public class PatrolEventEntityNavite extends ReportInBackEntity {
	private String State;
	private String ReportTime;
	private String data;
	private EventReportEntity dataEntity;
	private PatrolEventEntityTrue trueEntity;

	public PatrolEventEntityTrue getTrueEntity() {
		return trueEntity;
	}

	public void setTrueEntity(PatrolEventEntityTrue trueEntity) {
		this.trueEntity = trueEntity;
	}

	public EventReportEntity getDataEntity() {
		return dataEntity;
	}

	public void setDataEntity(EventReportEntity dataEntity) {
		this.dataEntity = dataEntity;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getState() {
		return State;
	}

	public void setState(String State) {
		this.State = State;
	}

	public String getReportTime() {
		return ReportTime;
	}

	public void setReportTime(String ReportTime) {
		this.ReportTime = ReportTime;
	}

	public PatrolEventEntityNavite() {
		super();
		// TODO Auto-generated constructor stub
	}

	private PatrolEventEntityTrue setTrueEntity() {
		return new PatrolEventEntityTrue(this.dataEntity, this.State,
				this.ReportTime);
	}

	@Override
	public void buildFromCursor(Cursor cursor) {
		// TODO Auto-generated method stub
		this.ReportTime = cursor.getString(0);
		this.data = cursor.getString(1);
		this.State = "未上报";
		this.dataEntity = new Gson().fromJson(this.data,
				new TypeToken<EventReportEntity>() {
				}.getType());
		this.trueEntity = setTrueEntity();

	}
}

package com.maintainproduct.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

import java.util.ArrayList;
import java.util.List;

/** 自定义上报的历史事件数据模型 */
public class BaseReportEntity implements ISQLiteOper, Parcelable {
	public int id;// ID
	public String EventType;// 事件类型
	public String GDBeanData;// 主要内容信息,采用JSON字符串,对应数据模型GDFormBean
	public String Time;// 事件
	public String FilePath;// 所有文件全路径,采用,分割
	public int UserId;// 上报人的用户ID
	public String RelativePath;// 该条记录存放文件的相对路径

	private GDFormBean bean;// 根据GDBeanData转换的数据

	public BaseReportEntity() {
	}

	/** 获取图片文件路径 */
	public List<String> getPicPath() {
		List<String> picPaths = new ArrayList<String>();

		List<String> filePaths = BaseClassUtil.StringToList(FilePath, ",");

		for (String path : filePaths) {
			if (path.endsWith(".jpg")) {
				picPaths.add(path);
			}
		}

		return picPaths;
	}

	/** 将字符串数据转换为对应的数据模型 */
	public GDFormBean toFormBean() {
		if (bean == null) {
			bean = new Gson().fromJson(GDBeanData, GDFormBean.class);
		}
		return bean;
	}

	/** 显示对应数量的信息数据 */
	public String showOverview(int maxCount) {
		toFormBean();

		StringBuilder builder = new StringBuilder();

		int i = 1;

		for (GDGroup group : bean.Groups) {

			for (GDControl control : group.Controls) {

				if (i > maxCount) {// 达到显示的信息数量则跳出
					break;
				}

				// 主要显示需要手动填写的信息
				if (!BaseClassUtil.isNullOrEmptyString(control.DefaultValues) || control.Type.equals("多值")) {
					continue;
				}

				if (!control.isFragmentType()) {// 判断是不是Fragment类型，即是不是照片或者录音等类型
					builder.append(control.DisplayName);// 显示名称
					builder.append(":");
					builder.append(control.Value);// 显示值
					if (i != maxCount) {
						builder.append("\n");
					}
					i++;
				}
			}
		}
		return builder.toString();
	}

	@Override
	public String getTableName() {
		return "BaseReportHistory";
	}

	@Override
	public String getCreateTableSQL() {
		return "(id integer primary key,EventType,GDBeanData,Time,FilePath,UserId,RelativePath)";
	}

	@Override
	public SQLiteQueryParameters getSqLiteQueryParameters() {
		return null;
	}

	@Override
	public ContentValues generateContentValues() {
		ContentValues cv = new ContentValues();
		cv.put("EventType", EventType);
		cv.put("GDBeanData", GDBeanData);
		cv.put("Time", Time);
		cv.put("FilePath", FilePath);
		cv.put("UserId", UserId);
		cv.put("RelativePath", RelativePath);
		return cv;
	}

	@Override
	public void buildFromCursor(Cursor cursor) {
		this.id = cursor.getInt(0);
		this.EventType = cursor.getString(1);
		this.GDBeanData = cursor.getString(2);
		this.Time = cursor.getString(3);
		this.FilePath = cursor.getString(4);
		this.UserId = cursor.getInt(5);
		this.RelativePath = cursor.getString(6);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(EventType);
		out.writeString(GDBeanData);
		out.writeString(Time);
		out.writeString(FilePath);
		out.writeInt(UserId);
		out.writeString(RelativePath);
		out.writeParcelable(bean, flags);
	}

	public static final Parcelable.Creator<BaseReportEntity> CREATOR = new Parcelable.Creator<BaseReportEntity>() {
		@Override
		public BaseReportEntity createFromParcel(Parcel in) {
			return new BaseReportEntity(in);
		}

		@Override
		public BaseReportEntity[] newArray(int size) {
			return new BaseReportEntity[size];
		}
	};

	private BaseReportEntity(Parcel in) {
		id = in.readInt();
		EventType = in.readString();
		GDBeanData = in.readString();
		Time = in.readString();
		FilePath = in.readString();
		UserId = in.readInt();
		RelativePath = in.readString();
		bean = in.readParcelable(GDFormBean.class.getClassLoader());
	}
}

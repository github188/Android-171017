package com.mapgis.mmt.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

import java.util.Date;
import java.util.List;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zoro at 2017/8/24.
 */
public class NetLogInfo implements ISQLiteOper, Parcelable {
    public int id;

    /**
     * 请求类型，GET/POST
     */
    public String requestType;

    /**
     * 请求的服务接口
     */
    public String requestInterface;

    /**
     * 请求的发起时间，精确到毫秒
     */
    public String startTime;

    /**
     * 请求的完结时间，精确到毫秒
     */
    public String endTime;

    /**
     * 请求耗时，单位:ms
     */
    public long timeSpan;

    /**
     * 返回状态码,如：200、404、500
     */
    public int responseCode;

    /**
     * 请求是否成功，1:成功,0:失败
     */
    public int isSuccess;

    /**
     * 发送字节数，单位：Byte
     */
    public long sendBytes;

    /**
     * 接收字节数，单位：Byte
     */
    public long receiveBytes;

    /**
     * 平均下载/上传速度，单位：KB/S
     */
    public double speed;

    /**
     * 全请求URL地址
     */
    public String fullURL;

    public NetLogInfo() {
    }

    private NetLogInfo(Parcel in) {
        id = in.readInt();
        requestType = in.readString();
        requestInterface = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        timeSpan = in.readLong();
        responseCode = in.readInt();
        isSuccess = in.readInt();
        sendBytes = in.readLong();
        receiveBytes = in.readLong();
        speed = in.readDouble();
        fullURL = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(requestType);
        dest.writeString(requestInterface);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeLong(timeSpan);
        dest.writeInt(responseCode);
        dest.writeInt(isSuccess);
        dest.writeLong(sendBytes);
        dest.writeLong(receiveBytes);
        dest.writeDouble(speed);
        dest.writeString(fullURL);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NetLogInfo> CREATOR = new Creator<NetLogInfo>() {
        @Override
        public NetLogInfo createFromParcel(Parcel in) {
            return new NetLogInfo(in);
        }

        @Override
        public NetLogInfo[] newArray(int size) {
            return new NetLogInfo[size];
        }
    };

    private double calcSpeed() {
        double speed = (sendBytes + receiveBytes) * 1.0 / timeSpan;

        return Convert.FormatDoubleSmart(speed);
    }

    @Override
    public String getTableName() {
        return "NetLogInfo";
    }

    @Override
    public String getCreateTableSQL() {
        return "(id integer primary key,requestType,requestInterface,startTime,endTime,timeSpan,responseCode,isSuccess,sendBytes,receiveBytes,speed,fullURL)";
    }

    @Override
    public SQLiteQueryParameters getSqLiteQueryParameters() {
        return new SQLiteQueryParameters();
    }

    @Override
    public ContentValues generateContentValues() {
        ContentValues cv = new ContentValues();

        cv.put("requestType", requestType);
        cv.put("requestInterface", requestInterface);
        cv.put("startTime", startTime);
        cv.put("endTime", endTime);
        cv.put("timeSpan", timeSpan);
        cv.put("responseCode", responseCode);
        cv.put("isSuccess", isSuccess);
        cv.put("sendBytes", sendBytes);
        cv.put("receiveBytes", receiveBytes);
        cv.put("speed", speed);
        cv.put("fullURL", fullURL);

        return cv;
    }

    @Override
    public void buildFromCursor(Cursor cursor) {
        this.id = cursor.getInt(0);
        this.requestType = cursor.getString(1);
        this.requestInterface = cursor.getString(2);
        this.startTime = cursor.getString(3);
        this.endTime = cursor.getString(4);
        this.timeSpan = cursor.getLong(5);
        this.responseCode = cursor.getInt(6);
        this.isSuccess = cursor.getInt(7);
        this.sendBytes = cursor.getLong(8);
        this.receiveBytes = cursor.getLong(9);
        this.speed = cursor.getDouble(10);
        this.fullURL = cursor.getString(11);
    }

    public void insertDB(Response response) {
        try {
            if (!MyApplication.getInstance().getConfigValue("openNetLogger", true))
                return;

            Date startTime = new Date(response.sentRequestAtMillis());
            Date endTime = new Date(response.receivedResponseAtMillis());

            Request request = response.request();

            this.requestType = request.method();
            this.requestInterface = "-";

            List<String> segments = request.url().pathSegments();

            for (int i = segments.size() - 1; i >= 0; i--) {
                if (!TextUtils.isEmpty(segments.get(i))) {
                    this.requestInterface = segments.get(i);

                    break;
                }
            }

            this.startTime = BaseClassUtil.getSystemTime("yyyy-MM-dd HH:mm:ss.SSS", startTime);
            this.endTime = BaseClassUtil.getSystemTime("yyyy-MM-dd HH:mm:ss.SSS", endTime);
            this.timeSpan = endTime.getTime() - startTime.getTime();
            this.responseCode = response.code();
            this.isSuccess = response.isSuccessful() ? 1 : 0;
            this.sendBytes = request.body() != null ? request.body().contentLength() : 0;
            this.receiveBytes = response.body() != null ? response.body().contentLength() : 0;
            this.speed = this.calcSpeed();
            this.fullURL = request.url().toString();

            DatabaseHelper.getInstance().insert(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

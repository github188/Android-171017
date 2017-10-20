package com.mapgis.mmt.module.gps.trans;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.DeviceUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.zondy.mapgis.geometry.Dot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GpsXYZ implements Parcelable, ISQLiteOper {

    public GpsXYZ() {
        this(0, 0);
    }

    public GpsXYZ(double x, double y) {
        this(x, y, 0);
    }

    public GpsXYZ(double x, double y, Location location) {
        this(x, y);

        this.location = location;
    }

    public GpsXYZ(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.reportTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)).format(new Date());
    }

    private double x = 0;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    private double y = 0;

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    private double z = 0;

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    private Location location = new Location("");

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    private String reportTime;

    public String getReportTime() {
        return reportTime;
    }

    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }

    private int isSuccess;

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int userId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String cpu;
    public String memory;
    public String battery;
    public String speed;

    public void readDeviceInfo() {
        this.cpu = DeviceUtil.readUsage();
        this.memory = DeviceUtil.getTotalMemory();
        this.battery = DeviceUtil.getBatteryLevel();

        this.speed = this.location == null ? "0" : String.valueOf(this.location.getSpeed());

        this.userId = MyApplication.getInstance().getUserId();
    }

    // id,x,y,reportTime,isSuccess,latitude,longitude,accuracy)");
    @Override
    public void buildFromCursor(Cursor cursor) {
        try {
            this.id = cursor.getInt(0);

            this.x = cursor.getDouble(1);
            this.y = cursor.getDouble(2);
            this.reportTime = cursor.getString(3);
            this.isSuccess = cursor.getInt(4);

            this.location = new Location("");
            this.location.setLatitude(cursor.getDouble(5));
            this.location.setLongitude(cursor.getDouble(6));

            this.cpu = cursor.getString(8);
            this.battery = cursor.getString(9);
            this.memory = cursor.getString(10);
            this.speed = cursor.getString(11);

            this.userId = cursor.getInt(12);

            String acc = cursor.getString(7);

            if (acc.contains("_")) {
                this.location.setAccuracy(Float.valueOf(acc.split("_")[0]));
                this.location.setProvider(acc.split("_")[1]);
            } else {
                this.location.setAccuracy(Float.valueOf(acc));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ContentValues generateContentValues() {
        ContentValues cv = new ContentValues();

        cv.put("x", this.x);
        cv.put("y", this.y);
        cv.put("reportTime", this.reportTime);
        cv.put("isSuccess", this.isSuccess);
        cv.put("latitude", this.location.getLatitude());
        cv.put("longitude", this.location.getLongitude());
        cv.put("accuracy", this.location.getAccuracy() + "_" + this.location.getProvider());

        cv.put("cpu", this.cpu);
        cv.put("battery", this.battery);
        cv.put("memory", this.memory);
        cv.put("speed", speed);

        cv.put("userId", userId);

        return cv;
    }

    public ResultWithoutData report() {
        String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc/ReportPosition";

        String accuracy = getAccuracy();

        String json = NetUtil.executeHttpGet(url, "f", "json", "reportTime", this.reportTime, "userID", String.valueOf(this.userId), "speed", this.speed,
                "battery", this.battery, "cpu", this.cpu, "memory", this.memory, "y", String.valueOf(this.y), "x", String.valueOf(this.x), "accuracy", accuracy);

        if (TextUtils.isEmpty(json))
            return null;
        else
            return new Gson().fromJson(json, ResultWithoutData.class);
    }

    private String getAccuracy() {
        String accuracy = "0";

        try {
            if (this.location != null) {
                accuracy = location.getAccuracy() + "_" + location.getProvider()
                        + "_" + Convert.FormatDouble(location.getLongitude(), ".0000")
                        + "_" + Convert.FormatDouble(location.getLatitude(), ".0000");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return accuracy;
    }

    @Override
    public String toString() {
        return x + "	" + y + "	" + z;
    }

    public String toXY() {
        return x + "," + y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof GpsXYZ)) {
            return false;
        }

        GpsXYZ o = (GpsXYZ) obj;

        return this.x == o.x && this.y == o.y && this.z == o.z;
    }

    public boolean isRandom() {
        return !TextUtils.isEmpty(MyApplication.getInstance().getConfigValue("RandomGPS"))
                || MyApplication.getInstance().getConfigValue("GpsReceiver").equalsIgnoreCase("Random")
                || MyApplication.getInstance().getConfigValue("GpsReceiver").equalsIgnoreCase("RD");
    }

    public boolean isUsefull() {
        try {
            if (MyApplication.getInstance().getConfigValue("GPSNoCheckUsefull", 0) > 0)
                return true;

            // 随机坐标模式下，要求坐标非零；非随机坐标模式下,要求经纬度必须为中国范围内时才判断为有效坐标
            if (isRandom())
                return x != 0 || y != 0;

            if (location == null)
                return false;
            else {
                double longitude = location.getLongitude(), latitude = location.getLatitude();

                return longitude > 73 && longitude < 136 && latitude > 3 && latitude < 54;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 判断是否精准的GPS坐标
     *
     * @return 判断结果
     */
    public boolean isUsefullGPS() {
        try {
            // 随机坐标模式下,要求坐标非零
            if (isRandom())
                return x != 0 || y != 0;

            if (!isUsefull())
                return false;

            String provider = location.getProvider();

            //过滤网络定位的点，不作为轨迹点
            if (TextUtils.isEmpty(provider) || provider.contains("未知") || provider.toUpperCase().contains("NET"))
                return false;

            long gpsMaxAccuracy = MyApplication.getInstance().getConfigValue("gpsMaxAccuracy", 20);

            //轨迹过滤精度大于20米的点
            return location.getAccuracy() <= gpsMaxAccuracy;
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        }
    }

    public Dot convertToPoint() {
        return new Dot(x, y);
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
    public String getCreateTableSQL() {
        return "(id integer primary key,x,y,reportTime,isSuccess,latitude,longitude,accuracy,cpu,battery,memory,speed,userId)";
    }

    protected GpsXYZ(Parcel in) {
        x = in.readDouble();
        y = in.readDouble();
        z = in.readDouble();
        location = in.readParcelable(Location.class.getClassLoader());
        reportTime = in.readString();
        isSuccess = in.readInt();
        id = in.readInt();
        userId = in.readInt();
        cpu = in.readString();
        memory = in.readString();
        battery = in.readString();
        speed = in.readString();
    }

    public static final Creator<GpsXYZ> CREATOR = new Creator<GpsXYZ>() {
        @Override
        public GpsXYZ createFromParcel(Parcel in) {
            return new GpsXYZ(in);
        }

        @Override
        public GpsXYZ[] newArray(int size) {
            return new GpsXYZ[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.x);
        dest.writeDouble(this.y);
        dest.writeDouble(z);
        dest.writeParcelable(this.location, flags);
        dest.writeString(this.reportTime);
        dest.writeInt(this.isSuccess);
        dest.writeInt(id);
        dest.writeInt(this.userId);
        dest.writeString(this.cpu);
        dest.writeString(this.memory);
        dest.writeString(this.battery);
        dest.writeString(this.speed);
    }
}

package com.patrol.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.patrol.module.PatrolUtils;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Dots;
import com.zondy.mapgis.geometry.Rect;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class KeyPoint implements Parcelable {
    public int ID;
    public int Index;

    /**
     * 1：管点；2：管段
     */
    public int Type;

    public int TaskID;
    public String LayerName;
    public String GisLayer;
    public String FieldName;
    public String FieldValue;
    public String Position;
    public String Geom;
    public double Lenth;
    public int IsArrive;
    public String ArriveMan;
    public String ArriveTime;
    public int IsFeedback;
    public String FeedbackMan;
    public String FeedbackTime;
    public int FeedbackID;

    public String Remark;
    public String KClass;

    public double Distance = 0;

    public KeyPoint() {
    }

    public double getDistance(GpsXYZ xyz) {
        Distance = GisUtil.calcDistance(GisUtil.convertDot(Position), xyz.convertToPoint());

        return Distance;
    }

    public long getArriveTime() {
        try {
            if (TextUtils.isEmpty(ArriveTime))
                return -1;
            else if (ArriveTime.contains("/")) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA);

                return format.parse(ArriveTime).getTime();
            } else {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

                return format.parse(ArriveTime).getTime();
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            return -1;
        }
    }

    public long getFeedbackTime() {
        try {
            if (TextUtils.isEmpty(FeedbackTime))
                return -1;
            else if (FeedbackTime.contains("/")) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA);

                return format.parse(FeedbackTime).getTime();
            } else {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

                return format.parse(FeedbackTime).getTime();
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            return -1;
        }
    }

    private transient Dot dot;

    public Dot getDot() {
        if (dot == null)
            dot = GisUtil.convertDot(this.Position);

        return dot;
    }

    private transient Dots line;

    public Dots getLine() {
        if (line == null) {
            Dots dots = new Dots();

            for (String p : this.Geom.split("\\|")) {
                Dot dot = GisUtil.convertDot(p);

                if (dot == null)
                    continue;

                dots.append(dot);
            }

            line = dots;
        }

        return line;
    }

    public Bitmap getStateBitmap(Context context) {
        int state = PatrolUtils.getIco(this);

        return BitmapFactory.decodeResource(context.getResources(), state);
    }

    public int getStateColor(Context context) {
        int state = R.color.patrol_unarrive;

        if (this.IsFeedback == 1) {
            state = R.color.patrol_feedbacked;
        } else if (this.IsArrive == 1) {
            state = R.color.patrol_arrived;
        }

        return context.getResources().getColor(state);
    }

    public String getState() {
        if (this.IsFeedback == 1)
            return "已反馈";
        else if (this.IsArrive == 1)
            return "已到位";
        else
            return "未到位";
    }

    public boolean isInView(MapView mapView) {
        Rect rect = mapView.getDispRange();

        return rect != null && GisUtil.isInRect(rect, getDot());
    }

    protected KeyPoint(Parcel in) {
        ID = in.readInt();
        Index = in.readInt();
        Type = in.readInt();
        TaskID = in.readInt();
        LayerName = in.readString();
        GisLayer = in.readString();
        FieldName = in.readString();
        FieldValue = in.readString();
        Position = in.readString();
        Geom = in.readString();
        Lenth = in.readDouble();
        IsArrive = in.readInt();
        ArriveMan = in.readString();
        ArriveTime = in.readString();
        IsFeedback = in.readInt();
        FeedbackMan = in.readString();
        FeedbackTime = in.readString();
        FeedbackID = in.readInt();
        Remark = in.readString();
        KClass = in.readString();
        Distance = in.readDouble();
    }

    public static final Creator<KeyPoint> CREATOR = new Creator<KeyPoint>() {
        @Override
        public KeyPoint createFromParcel(Parcel in) {
            return new KeyPoint(in);
        }

        @Override
        public KeyPoint[] newArray(int size) {
            return new KeyPoint[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID);
        dest.writeInt(Index);
        dest.writeInt(Type);
        dest.writeInt(TaskID);
        dest.writeString(LayerName);
        dest.writeString(GisLayer);
        dest.writeString(FieldName);
        dest.writeString(FieldValue);
        dest.writeString(Position);
        dest.writeString(Geom);
        dest.writeDouble(Lenth);
        dest.writeInt(IsArrive);
        dest.writeString(ArriveMan);
        dest.writeString(ArriveTime);
        dest.writeInt(IsFeedback);
        dest.writeString(FeedbackMan);
        dest.writeString(FeedbackTime);
        dest.writeInt(FeedbackID);
        dest.writeString(Remark);
        dest.writeString(KClass);
        dest.writeDouble(Distance);
    }
}

package com.patrol.entity;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.GisUtil;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dots;
import com.zondy.mapgis.geometry.Rect;

public class TaskBaseInfo implements Parcelable {
    public int Index;
    public int ID;
    public String Code;
    public String Name;
    public String CycleName;
    public String EndTime;
    public int IsFinish;
    public int ArriveSum;
    public int FeedbackSum;
    public int TotalSum;
    public double PipeLenth;
    public double TotalLength;
    AreaInfo Area;

    public boolean IsStart = true;
    public int TotalFBSum = 0;
    public String TaskState;

    TaskBaseInfo() {
    }

    TaskBaseInfo(Parcel in) {
        Index = in.readInt();
        ID = in.readInt();
        Code = in.readString();
        Name = in.readString();
        CycleName = in.readString();
        EndTime = in.readString();
        IsFinish = in.readInt();
        ArriveSum = in.readInt();
        FeedbackSum = in.readInt();
        TotalSum = in.readInt();
        PipeLenth = in.readDouble();
        TotalLength = in.readDouble();
        Area = in.readParcelable(AreaInfo.class.getClassLoader());
        IsStart = in.readByte() != 0;
        TotalFBSum = in.readInt();
        TaskState = in.readString();
    }

    public static final Creator<TaskBaseInfo> CREATOR = new Creator<TaskBaseInfo>() {
        @Override
        public TaskBaseInfo createFromParcel(Parcel in) {
            return new TaskBaseInfo(in);
        }

        @Override
        public TaskBaseInfo[] newArray(int size) {
            return new TaskBaseInfo[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj instanceof TaskBaseInfo) && this.ID == ((TaskBaseInfo) obj).ID;
    }

    public String getStatus() {
        return "到位：" + ArriveSum + "；反馈:" + FeedbackSum + "；总数:" + TotalSum;
    }

    public String getPointState() {
        if (this.TotalSum <= 0)
            return "设备数目: -";

        StringBuilder sb = new StringBuilder();
        sb.append("设备数目:");

        if (TotalFBSum > 0) {
            sb.append("反馈");
            sb.append(String.valueOf(this.FeedbackSum));
            sb.append("/");
            sb.append(String.valueOf(this.TotalFBSum));
            sb.append(",");
        }
        sb.append("到位");
        sb.append(String.valueOf(this.ArriveSum));
        sb.append("/");
        sb.append(String.valueOf(this.TotalSum));

        return sb.toString();
    }

    public String getLineState() {
        return "管线长度: " + (this.TotalLength > 0 ? ("到位" + Convert.FormatLength(this.PipeLenth) + "/总长" + Convert.FormatLength(this.TotalLength)) : "-");
    }

    public Rect drawOnMap(MapView mapView) {
        Rect rect = null;

        try {
            //绘制区域
            if (!TextUtils.isEmpty(this.Area.AreaPolygon)) {
                String[] lines = this.Area.AreaPolygon.split(";");
                GraphicPolylin line = new GraphicPolylin(GisUtil.buildDots(lines));

                line.setLineWidth(5);
                line.setColor(Color.BLUE);

                line.setAttributeValue("图形显示类别", "巡检任务");
                line.setAttributeValue("巡检任务编号", String.valueOf(this.ID));

                mapView.getGraphicLayer().addGraphic(line);
                rect = line.getBoundingRect();
            }

            //绘制路径
            if (!TextUtils.isEmpty(this.Area.PathPolygon)) {
                String[] lineStrings = this.Area.PathPolygon.split(";");

                Dots dots = GisUtil.buildDots(lineStrings);

                int cur = mapView.getGraphicLayer().getGraphicCount();

                Rect rect2 = GisUtil.showLine(mapView, dots);

                if (rect == null)
                    rect = rect2;

                for (int i = cur; i < mapView.getGraphicLayer().getGraphicCount(); i++) {
                    mapView.getGraphicLayer().getGraphic(i).setAttributeValue("图形显示类别", "巡检任务");
                    mapView.getGraphicLayer().getGraphic(i).setAttributeValue("巡检任务编号", String.valueOf(this.ID));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rect;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Index);
        dest.writeInt(ID);
        dest.writeString(Code);
        dest.writeString(Name);
        dest.writeString(CycleName);
        dest.writeString(EndTime);
        dest.writeInt(IsFinish);
        dest.writeInt(ArriveSum);
        dest.writeInt(FeedbackSum);
        dest.writeInt(TotalSum);
        dest.writeDouble(PipeLenth);
        dest.writeDouble(TotalLength);
        dest.writeParcelable(Area, flags);
        dest.writeByte((byte) (IsStart ? 1 : 0));
        dest.writeInt(TotalFBSum);
        dest.writeString(TaskState);
    }
}
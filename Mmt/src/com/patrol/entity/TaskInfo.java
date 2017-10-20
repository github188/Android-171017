package com.patrol.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class TaskInfo extends TaskBaseInfo {
    public ArrayList<KeyPoint> Points;

    public TaskInfo() {
    }

    public TaskInfo(TaskBaseInfo info) {
        Index = info.Index;
        ID = info.ID;
        Code = info.Code;
        Name = info.Name;
        CycleName = info.CycleName;
        EndTime = info.EndTime;
        IsFinish = info.IsFinish;
        ArriveSum = info.ArriveSum;
        FeedbackSum = info.FeedbackSum;
        TotalSum = info.TotalSum;
        PipeLenth = info.PipeLenth;
        TotalLength = info.TotalLength;
        Area = info.Area;
        IsStart = info.IsStart;
        TotalFBSum = info.TotalFBSum;
        TaskState = info.TaskState;
    }

    public TaskInfo toBaseInfo() {
        TaskInfo info = new TaskInfo();

        info.Index = Index;
        info.ID = ID;
        info.Code = Code;
        info.Name = Name;
        info.CycleName = CycleName;
        info.EndTime = EndTime;
        info.IsFinish = IsFinish;
        info.ArriveSum = ArriveSum;
        info.FeedbackSum = FeedbackSum;
        info.TotalSum = TotalSum;
        info.PipeLenth = PipeLenth;
        info.TotalLength = TotalLength;
        info.Area = Area;
        info.IsStart = IsStart;
        info.TotalFBSum = TotalFBSum;
        info.TaskState = TaskState;
        return info;
    }

    protected TaskInfo(Parcel in) {
        super(in);

        Points = in.createTypedArrayList(KeyPoint.CREATOR);
    }

    public static final Parcelable.Creator<TaskInfo> CREATOR = new Creator<TaskInfo>() {
        @Override
        public TaskInfo createFromParcel(Parcel in) {
            return new TaskInfo(in);
        }

        @Override
        public TaskInfo[] newArray(int size) {
            return new TaskInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeTypedList(Points);
    }

    public KeyPoint findPointByID(int id) {
        try {
            for (KeyPoint kp : this.Points) {
                if (kp.ID != id)
                    continue;

                return kp;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
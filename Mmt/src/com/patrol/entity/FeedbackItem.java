package com.patrol.entity;

public class FeedbackItem {
    public FeedbackItem() {
        this(0, "", "");
    }

    public FeedbackItem(int pointID) {
        this(pointID, "", "");
    }

    public FeedbackItem(String name, String value) {
        this(0, name, value);
    }

    public FeedbackItem(int pointID, String name, String value) {
        this.PointID = pointID;
        this.Name = name;
        this.Value = value;
    }

    public int ID;
    public int PointID;
    public String Name;
    public String Value;
}

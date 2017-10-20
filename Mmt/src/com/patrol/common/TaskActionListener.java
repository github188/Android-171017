package com.patrol.common;

import com.patrol.entity.KeyPoint;

import java.util.List;

public interface TaskActionListener {
    void onFeedback(KeyPoint kp);

    void onShowGISDetail(KeyPoint kp);

    void onLocate(KeyPoint kp);

    void onStateChanged();

    void onTaskFinish();

    List<KeyPoint> fetchPipeLines(int id, double xmin, double ymin, double xmax, double ymax);
}
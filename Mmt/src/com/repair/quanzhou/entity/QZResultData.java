package com.repair.quanzhou.entity;

import java.util.List;

/**
 * Created by liuyunfan on 2016/1/21.
 */
public class QZResultData extends QZResultWithoutData {
    public WorkTaskBean workTaskBean;
    public List<DealPerson> dealPersonList;
    public List<FeedBack> feedbackList;
    public List<TaskDoing> taskDoingList;
    public List<MalfunctionType> malfunctionTypeList;
}

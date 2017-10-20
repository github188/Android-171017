package com.customform.module;

import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.GDGroup;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.FeedBackView;
import com.mapgis.mmt.common.widget.customview.ImageFragmentView;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.EventReportCache;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * region Logic about data cache:
 * 1.Save in onPause(Activity);
 * 2.Restore in onCreate(Fragment, Activity should pass parameter "CacheSearchParam" to Fragment);
 * 3.Delete after reporting successfully(Activity).
 * Created by zoro at 2017/9/5.
 */
public class BeanCacheManager {
    public static void load(String cacheSearchParam, GDFormBean data) {
        if (!TextUtils.isEmpty(cacheSearchParam)) {
            try {
                // 本地数据库缓存的表单数据
                List<EventReportCache> eventReportCaches = DatabaseHelper.getInstance().query(EventReportCache.class,
                        new SQLiteQueryParameters(cacheSearchParam));

                if (eventReportCaches != null && eventReportCaches.size() != 0) {
                    String formCacheData = eventReportCaches.get(0).getValue();
                    if (!TextUtils.isEmpty(formCacheData)) {
                        ArrayList<FeedItem> feedbackItems = new Gson().fromJson(formCacheData,
                                new TypeToken<ArrayList<FeedItem>>() {
                                }.getType());

                        for (GDGroup gdGroup : data.Groups) {
                            for (GDControl gdControl : gdGroup.Controls) {
                                for (FeedItem feedItem : feedbackItems) {
                                    if (gdControl.Name.equals(feedItem.Name)) {
                                        gdControl.Value = feedItem.Value;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void delete(int userId, String key) {
        try {
            DatabaseHelper.getInstance().delete(EventReportCache.class, "userId=" + userId +
                    " and key='" + key + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getRecordId(int userId, String key) {
        ArrayList<EventReportCache> list = DatabaseHelper.getInstance().query(EventReportCache.class,
                new SQLiteQueryParameters("userId=" + userId + " and key='" + key + "'"));

        if (list.size() == 1) {
            return list.get(0).getRecordId();
        }

        return -1;
    }

    public static void save(int userId, String key, int recordId, List<FeedItem> feedbackItems) {
        try {
            if (feedbackItems == null) return;

            EventReportCache eventReportCache = new EventReportCache(userId, key, new Gson().toJson(feedbackItems), recordId);

            eventReportCache.insert();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 存储所有表单信息到获取的数据中
     *
     * @param status            操作状态:主要作用是在立即上报状态下，为必填项做检查
     * @param excludeTypes      排除特定的控件类型列表
     * @param includeFieldNames 包含的字段名称列表
     * @return 反馈信息列表
     */
    public static List<FeedItem> getFeedbackItems(FlowBeanFragment flowBeanFragment, int status, List<String> excludeTypes, List<String> includeFieldNames) {
        List<FeedItem> items = new ArrayList<>();

        String tempStr;

        flowBeanFragment.relativePaths = "";
        flowBeanFragment.absolutePaths = "";

        if (flowBeanFragment.getEventReportMainForm() == null) {
            return null;
        }
        // 遍历所有视图
        for (int i = 0, count = flowBeanFragment.getEventReportMainForm().getChildCount(); i < count; i++) {

            View view = flowBeanFragment.getEventReportMainForm().getChildAt(i);

            // 若不是需要反馈的视图，则继续循环
            if (!(view instanceof FeedBackView)) {
                continue;
            }

            GDControl control = (GDControl) view.getTag();

            // 有些字段不能在上报界面缓存，需要显示实时最新
            if (status == ReportInBackEntity.SAVING) {
                if (control.Type.equals("当前坐标") || control.Type.equals("距离")) {
                    continue;
                }
            }

            // 类型为 保留字 的反馈视图不需要反馈
            if (control.Type.equals("保留字") && status == ReportInBackEntity.REPORTING) {
                continue;
            }

            if (excludeTypes != null && excludeTypes.contains(control.Type)) {
                continue;
            }
            if (includeFieldNames != null && !includeFieldNames.contains(control.Name)) {
                continue;
            }

            FeedItem item = new FeedItem();

            FeedBackView feedBackView = (FeedBackView) view;

            // 对应服务器端数据库所要存储的表的列名
            item.Name = control.Name;

            // 用户所填写的值
            item.Value = feedBackView.getValue();
            item.Type = control.Type;

            // 若反馈视图是Fragment的类型
            if (view instanceof ImageFragmentView) {

                ImageFragmentView fragmentView = (ImageFragmentView) view;

                HashMap<String, String> dataMap = fragmentView.getKeyValue();

                // 若反馈视图为视频类型,则还要上传对应的缩略图到服务器
                /*if (control.Type.equals("视频") || control.Type.equals("附件")) {
                    item.Value = dataMap.get(ImageFragmentView.FILENAME_KEY_STRING);
                } else {
                    item.Value = dataMap.get(ImageFragmentView.RELATIVE_KEY_STRING);
                }*/
                item.Value = dataMap.get(ImageFragmentView.FILENAME_KEY_STRING);

                // 绝对路径，文件存储在手持本地的真实路径
                if (!TextUtils.isEmpty(tempStr = dataMap.get(ImageFragmentView.ABSOLUTE_KEY_STRING))) {
                    flowBeanFragment.absolutePaths = flowBeanFragment.absolutePaths + tempStr + ",";
                }

                // 相对路径
                if (!TextUtils.isEmpty(tempStr = dataMap.get(ImageFragmentView.RELATIVE_KEY_STRING))) {
                    flowBeanFragment.relativePaths = flowBeanFragment.relativePaths + tempStr + ",";
                }
            }

            // 判断是否为必填项，若是并且没有填写，则给出提示
            // 是上报状态并且是必填写项并且未填写任何信息，给出提示
            if (status == ReportInBackEntity.REPORTING && control.Validate.equals("1") && BaseClassUtil.isNullOrEmptyString(item.Value)) {
//                ((BaseActivity) getActivity()).showErrorMsg("<" + control.DisplayName + "> 为必填项，请填写后再上报!");
                Toast.makeText(flowBeanFragment.getActivity(), "[ " + control.DisplayName + " ] 为必填项，请填写后再上报!", Toast.LENGTH_SHORT).show();
                return null;
            }

            items.add(item);
        }

        return items;
    }
}

package com.maintainproduct.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.maintainproduct.module.BeanFragment;
import com.mapgis.mmt.common.util.BaseClassUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 维修养护表单界面搭建结构
 */
public class GDFormBean implements Parcelable {
    /**
     * 中部视图
     */
    public GDGroup[] Groups;

    /**
     * 底部视图
     */
    public GDButton[] BottomButtons;

    public String BottomURL;

    public String MiddleURL;

    public String TableName;

    public String TopURL;

    public String Desc;

    /**
     * 用于生成简单的单Group表单（Group名称不指定固Title不显示）
     *
     * @param args
     * @return
     */
    public static GDFormBean generateSimpleForm(String[]... args) {

        GDFormBean gdFormBean = new GDFormBean();
        gdFormBean.BottomButtons = new GDButton[0];
        gdFormBean.BottomURL = "";
        gdFormBean.MiddleURL = "";
        gdFormBean.TableName = "";
        gdFormBean.TopURL = "";
        gdFormBean.Desc = "";

        gdFormBean.Groups = new GDGroup[]{new GDGroup()};

        GDControl[] gdControls = new GDControl[args.length];
        Map<String, String> controlParam = new HashMap<String, String>();
        for (int i = 0; i < gdControls.length; i++) {
            controlParam.clear();
            try {
                for (int j = 0; j < args[i].length; ) {
                    controlParam.put(args[i][j++], args[i][j++]);
                }
                gdControls[i] = new GDControl(controlParam);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        gdFormBean.Groups[0].Controls = gdControls;

        return gdFormBean;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelableArray(Groups, flags);
        out.writeParcelableArray(BottomButtons, flags);
        out.writeString(BottomURL);
        out.writeString(MiddleURL);
        out.writeString(TableName);
        out.writeString(TopURL);
        out.writeString(Desc);
    }

    public static final Parcelable.Creator<GDFormBean> CREATOR = new Parcelable.Creator<GDFormBean>() {
        @Override
        public GDFormBean createFromParcel(Parcel in) {
            return new GDFormBean(in);
        }

        @Override
        public GDFormBean[] newArray(int size) {
            return new GDFormBean[size];
        }
    };

    public GDFormBean() {
    }

    private GDFormBean(Parcel in) {
        try {
            Parcelable[] groupsPars = in.readParcelableArray(GDGroup.class.getClassLoader());
            Groups = groupsPars == null ? null : Arrays.asList(groupsPars).toArray(new GDGroup[groupsPars.length]);

            Parcelable[] buttonsPars = in.readParcelableArray(GDButton.class.getClassLoader());

            BottomButtons = buttonsPars == null ? null : Arrays.asList(buttonsPars).toArray(new GDButton[buttonsPars.length]);

            BottomURL = in.readString();
            MiddleURL = in.readString();
            TableName = in.readString();
            TopURL = in.readString();
            Desc = in.readString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public BeanFragment createFragment() {
        BeanFragment fragment = new BeanFragment(this);
        return fragment;
    }

    /**
     * 是否含有对应的组名
     */
    public boolean hasGroupName(String name) {
        if (Groups == null || Groups.length == 0 || BaseClassUtil.isNullOrEmptyString(name)) {
            return false;
        }

        for (GDGroup group : Groups) {
            if (group.Name.equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 将需要填写文本信息的空间变为只读，作为只显示控件
     */
    public void setOnlyShow() {
        setOnlyShow(true, null);
    }

    /**
     * 将需要填写文本信息的空间变为只读，作为只显示控件
     */
    public void setOnlyShow(boolean allShow, List<String> exceptFieldList) {
        setEditable(!allShow, exceptFieldList);
    }

    /**
     * 设置界面能够被编辑
     *
     * @param editable        界面中的控件是否可以编辑
     * @param excludeNameList 排除的字段名称数组
     */
    public void setEditable(final boolean editable, List<String> excludeNameList) {

        for (GDGroup group : Groups) {

            if (group == null || group.Controls == null) {
                continue;
            }

            for (GDControl control : group.Controls) {

                if (control == null) {
                    continue;
                }

                boolean isRead=!editable;
                if (excludeNameList != null && excludeNameList.contains(control.Name)) {
                    isRead = !isRead;
                }

                control.setReadOnly(isRead);
            }
        }
    }

    /**
     * 根据缓存在本地后台上报数据，填充已填写的值
     */
    public void setValueByFeedItems(List<FeedItem> items) {
        // 将本地缓存信息填充到表单数据中
        for (FeedItem feedItem : items) {

            for (GDGroup gdGroup : this.Groups) {

                GDControl control = gdGroup.findControlByControlName(feedItem.Name);

                if (control != null) {
                    control.setValue(feedItem.Value);
                }
            }

        }
    }

}

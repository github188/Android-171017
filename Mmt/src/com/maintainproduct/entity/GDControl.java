package com.maintainproduct.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.View;

import com.customform.view.MmtBaseView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Convert;

import java.util.Map;
import java.util.UUID;

public class GDControl implements Parcelable {
    public String IsRead = "false";

    public String DisplayName = "";

    public String Type = "";

    public String DefaultValues = "";

    public String Value = "";

    public String UploadArgs = "";

    public int DisplayColSpan = 1;

    public String Validate = "0"; // 1 必填项

    public String TableName = "";

    public String Name = "";

    /**
     * true 显示，false 不显示. 标识此组件在手机界面上是否需要显示，因为有些上报的字段需要往数据库里填，但又不需要显示出来
     */
    public String IsVisible = "";

    ///////////////////////////////////////
    /**
     * Text hint or secondary selector
     */
    public String ConfigInfo = "";

    /**
     * 单位
     */
    public String Unit = "";
    /**
     * 验证规则
     */
    public String ValidateRule = "";

    /**
     * 触发异常值
     */
    public String TriggerProblemValue;
    /**
     * 触发事件
     */
    public String TriggerEvent;
    /**
     * 触发事件字段集
     */
    public String TriggerEventFields;

    ////////////////////////////////////////
    /**
     * 服务没有该字段，而在 ValidateRule中固定提供，固单独提出来，会参与序列化
     */
    public int MaxLength;

    /**
     * 类似于 View中的 Tag字段的作用，用于存放运行时数据，不参与序列化
     */
    public Object mTag = null;

    public OnSelectedChangedListener onSelectedChangedListener;

    public interface OnSelectedChangedListener {
        void onSelectedChanged(GDControl gdControl, String selectedValue);
    }

    public OnAsyncSelectorLoadFinishedListener onAsyncSelectorLoadFinishedListener;

    public void setOnAsyncSelectorLoadFinishedListener(OnAsyncSelectorLoadFinishedListener listener) {
        this.onAsyncSelectorLoadFinishedListener = listener;
    }

    public Object getTag() {
        return mTag;
    }

    public boolean isReadOnly() {
        return "true".equals(IsRead.toLowerCase());
    }

    public void setReadOnly(boolean isReadOnly) {
        this.IsRead = isReadOnly ? "true" : "false";
    }

    /**
     * 对Control的Value属性填充指定值value
     */
    public void setValue(String value) {
        this.Value = value;
    }

    public GDControl() {
        this("", "");
    }

    public GDControl(String name, String type) {
        this(name, name, type, "");
    }

    public GDControl(String name, String type, String value) {
        this(name, name, type, value);
    }

    public GDControl(String name, String type, String value, boolean IsRead) {
        this(name, name, type, value);
        this.setReadOnly(IsRead);
    }

    public GDControl(String name, String displayName, String type, String value) {
        this(name, displayName, type, value, "");
    }

    public GDControl(String name, String displayName, String type, String value, String defaultValues) {
        ArrayMap<String, String> args = new ArrayMap<>();

        args.put("Name", name);
        args.put("DisplayName", displayName);
        args.put("Type", type);
        args.put("Value", value);
        args.put("DefaultValues", defaultValues);

        initFromMap(args);
    }

    public GDControl(Map<String, String> args) {
        initFromMap(args);
    }

    private void initFromMap(Map<String, String> args) {
        this.Name = Convert.FormatString(args.get("Name"));
        this.DisplayName = Convert.FormatString(args.get("DisplayName"));
        this.Type = Convert.FormatString(args.get("Type"));
        this.Value = Convert.FormatString(args.get("Value"));
        this.DefaultValues = Convert.FormatString(args.get("DefaultValues"));
        this.ConfigInfo = Convert.FormatString(args.get("ConfigInfo"));
        this.Unit = Convert.FormatString(args.get("Unit"));
        this.ValidateRule = Convert.FormatString(args.get("ValidateRule"));
        this.Validate = Convert.FormatString(args.get("Validate"));
        this.IsRead = Convert.FormatString(args.get("IsRead"), "false");

        this.DisplayColSpan = Convert.FormatInt(args.get("DisplayColSpan"), 1);
    }

    boolean isFragmentType() {
        return Type.equals("图片") || Type.equals("录音") || Type.equals("拍照") || Type.equals("视频");
    }

    public boolean addEnable = false;

    public void setAddEnable(boolean addEnable) {
        this.addEnable = addEnable;
    }

    public boolean canSelect = false;

    public String locateBackClass = "";

    public void setLocateBackClass(Class<?> cls) {
        if (cls != null)
            this.locateBackClass = cls.getName();
    }

    public String relativePath = "";

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String calRelativePath(String fragmentFileRelativePath) {
        String relativePath = "";

        if (!TextUtils.isEmpty(this.Value)) {
            String[] paths = this.Value.split(",");
            if (paths.length > 0 && paths[0].contains("/")) {
                // 为了避免视频类型中只有一个视频时导致错误的发生，所以在此再将第一个元素用“##”分割病区第一个子元素
                String path = paths[0].split(MyApplication.getInstance().getString(R.string.split_flag_video_photo))[0];

                relativePath = path.substring(0, path.lastIndexOf('/') + 1).trim();
            }
        }

        if (TextUtils.isEmpty(relativePath)) {
            // 路径规则：/[ConfigInfo]/TableName/FieldName/EventCode|GUID/FileName.jpg (ConfigInfo有时没有，EventCode上报时没有用GUID代替)
            relativePath = "/"
                    + (TextUtils.isEmpty(this.ConfigInfo) ? "" : (this.ConfigInfo + "/"))
                    + (TextUtils.isEmpty(this.TableName) ? "" : (this.TableName + "/"))
                    + (this.Name + "/")
                    + ((TextUtils.isEmpty(fragmentFileRelativePath) ? UUID.randomUUID().toString() : fragmentFileRelativePath) + "/");
        }

        this.relativePath = relativePath;

        return relativePath;
    }

    public void setAdditionalParas(Class<?> cls, boolean addEnable, String relativePath) {
        setLocateBackClass(cls);
        setAddEnable(addEnable);
        setRelativePath(relativePath);
    }

    /**
     * 根据类型创建对应的视图
     * <p/>
     * (新版产品不使用 仅日期V2 日期框V2 )
     * <p/>
     * 仅日期V2 和 仅日期的区别是仅日期V2没有默认取当前日期
     * 日期框V2 和 日期框的区别是日期框V2没有默认取当前时间
     * 坐标V3 和 坐标v2 的区别是坐标V3新增了一个清空坐标的按钮
     *
     * @param activity 宿主窗体
     */
    public View createView(FragmentActivity activity) {
        return MmtBaseView.newInstance(activity, this).generate();
    }

    @Override
    public String toString() {
        return this.DisplayName + "[" + this.Type + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(IsRead);
        out.writeString(DisplayName);
        out.writeString(Type);
        out.writeString(DefaultValues);
        out.writeString(Value);
        out.writeString(UploadArgs);
        out.writeInt(DisplayColSpan);
        out.writeString(Validate);
        out.writeString(TableName);
        out.writeString(Name);
        out.writeString(IsVisible);
        out.writeString(ConfigInfo);
        out.writeString(Unit);
        out.writeString(ValidateRule);
        out.writeString(TriggerProblemValue);
        out.writeString(TriggerEvent);
        out.writeString(TriggerEventFields);

        out.writeInt(MaxLength);
    }

    public static final Parcelable.Creator<GDControl> CREATOR = new Parcelable.Creator<GDControl>() {
        @Override
        public GDControl createFromParcel(Parcel in) {
            return new GDControl(in);
        }

        @Override
        public GDControl[] newArray(int size) {
            return new GDControl[size];
        }
    };

    private GDControl(Parcel in) {
        IsRead = in.readString();
        DisplayName = in.readString();
        Type = in.readString();
        DefaultValues = in.readString();
        Value = in.readString();
        UploadArgs = in.readString();
        DisplayColSpan = in.readInt();
        Validate = in.readString();
        TableName = in.readString();
        Name = in.readString();
        IsVisible = in.readString();
        ConfigInfo = in.readString();
        Unit = in.readString();
        ValidateRule = in.readString();
        TriggerProblemValue = in.readString();
        TriggerEvent = in.readString();
        TriggerEventFields = in.readString();

        MaxLength = in.readInt();
    }
}

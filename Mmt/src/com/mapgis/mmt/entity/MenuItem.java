package com.mapgis.mmt.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 模块配置
 *
 * @author Administrator
 */
public class MenuItem implements Parcelable {
    /**
     * 显示别名
     */
    public String Alias;

    /**
     * 功能模块名称
     */
    public String Name;

    /**
     * 显示图标资源名称
     */
    public String Icon;

    /**
     * 显示图标资源 ID
     */
    public int IconRes;

    /**
     * 可选参数，格式：模块名称-参数 或者 别名?参数 推荐模块名称方式
     */
    public String ModuleParam;

    //region for resolve ModuleParam

    // Local only
    private Map<String, String> moduleParamMap;

    public String getModuleParamValue(String paramName) {
        if (ModuleParam == null || ModuleParam.trim().length() == 0) {
            return "";
        }
        if (moduleParamMap == null) {
            resolveModuleParam();
        }
        return moduleParamMap.get(paramName);
    }

    public boolean getModuleParamValueAsBoolean(String moduleParam) {
        String paramValue = getModuleParamValue(moduleParam);
        if (TextUtils.isEmpty(paramValue)) {
            return false;
        }
        boolean result = false;
        try {
            result = Boolean.parseBoolean(paramValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean containsModuleParam(String paramName) {
        String paramValue = getModuleParamValue(paramName);
        return !TextUtils.isEmpty(paramValue);
    }

    private void resolveModuleParam() {
        if (moduleParamMap != null) {
            return;
        }
        moduleParamMap = new HashMap<>();
        // Replace chinese comma
        ModuleParam = ModuleParam.replace("，", ",").trim();
        // Separator "&"
        String[] params = ModuleParam.split("&");
        for (String param : params) {
            int sepIndex = param.indexOf("=");
            if (sepIndex != -1) {
                String key = param.substring(0, sepIndex).trim();
                String value = param.substring(sepIndex + 1).trim();
                moduleParamMap.put(key, value);
            } else {
                // Simple config, key is same to the value.
                moduleParamMap.put(param.trim(), param.trim());
            }
        }
    }
    //endregion

    @Override
    public String toString() {
        return Alias;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(Alias);
        out.writeString(Name);
        out.writeString(Icon);
        out.writeInt(IconRes);
        out.writeString(ModuleParam);
    }

    public static final Parcelable.Creator<MenuItem> CREATOR = new Parcelable.Creator<MenuItem>() {
        @Override
        public MenuItem createFromParcel(Parcel in) {
            return new MenuItem(in);
        }

        @Override
        public MenuItem[] newArray(int size) {
            return new MenuItem[size];
        }
    };

    protected MenuItem(Parcel in) {
        Alias = in.readString();
        Name = in.readString();
        Icon = in.readString();
        IconRes = in.readInt();
        ModuleParam = in.readString();
    }
}


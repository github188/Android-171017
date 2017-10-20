package com.repair.zhoushan.entity;

import android.text.TextUtils;

import com.maintainproduct.entity.GDButton;
import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.GDGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlowNodeMeta implements Serializable {

    /**
     * 流程表配置信息
     */
    public ArrayList<TableGroup> Groups = new ArrayList<TableGroup>();
    public ArrayList<TableValue> Values = new ArrayList<TableValue>();

    private static final Matcher matcherMaxLength;

    static {
        matcherMaxLength = Pattern.compile("maxlength\\s*:\\s*\\d+").matcher("");
    }

    /**
     * 表组结构-对应以前的GDGroup
     */
    public class TableGroup implements Serializable {

        public String GroupName;
        public ArrayList<FieldSchema> Schema = new ArrayList<FieldSchema>();
        public int Visible;


        /**
         * 将新的界面结构TableGroup转化为旧的界面结构GDGroup
         */
        public GDGroup mapToGDGroup() {

            GDGroup gdGroup = new GDGroup();
            gdGroup.Name = this.GroupName;

            // For save the visibility of this group.
            gdGroup.Url = String.valueOf(Visible);

            // 循环将TableGroup中的FieldSchema列表转化为GDControl数组
            GDControl[] gdControls = new GDControl[Schema.size()];
            for (int i = 0; i < gdControls.length; i++) {
                gdControls[i] = Schema.get(i).mapToGDControl();
            }

            // 需要的特殊处理的在这里
            // 二级选择器的数据库配置
            // 事件类型 选择器 维修工单上报类型
            // 事件内容 选择器 事件类型.维修工单上报内容.n
            Set<String> levelOneFieldNameSet = new HashSet<String>();
            for (GDControl gdControl : gdControls) {
                if (gdControl.Type.equals("选择器") && gdControl.ConfigInfo.contains(".")) {
                    gdControl.Type = "二级选择器";
                    levelOneFieldNameSet.add(gdControl.ConfigInfo.split("\\.")[0]);
                }

//                // Error: 语法不报错，运行时提示NullPointerException，即此处无法访问Values
//                for (TableValue tableValue : Values) {
//                    if (tableValue.FieldName.equals(gdControl.DisplayName)) {
//                        gdControl.Value = tableValue.FieldValue;
//                    }
//                }
            }
            for (GDControl gdControl : gdControls) {
                if (gdControl.Type.equals("选择器") && levelOneFieldNameSet.contains(gdControl.Name)) {
                    gdControl.Type = "二级选择器";
                }
            }

            gdGroup.Controls = gdControls;

            return gdGroup;
        }

        public TableGroup() {
            this.GroupName = "";
            this.Visible = 1;
        }

    }

    /**
     * 表属性结构
     */
    public class FieldSchema implements Serializable {

        public FieldSchema() {
            Shape = "文本框";
            RowSpan = 1;
            ColSpan = 1;
        }

        public FieldSchema(String fieldName, String shape, String tableName) {
            this.FieldName = this.Alias = fieldName;
            this.Shape = shape;
            this.TableName = tableName;
        }

        /**
         * 表名
         */
        public String TableName;

        /**
         * 字段名
         */
        public String FieldName;

        /**
         * 别名
         */
        public String Alias;

        /**
         * 界面分组
         */
        public String UIGroup;

        /**
         * 信息分组
         */
        public String TableGroup;

        /**
         * 类型
         */
        public String Type;

        /**
         * 形态
         */
        public String Shape;

        /**
         * 单位
         */
        public String Unit;

        /**
         * 配置信息
         * 输入框此字段则表示hint信息
         * 拍照、录音、附件则表示路径信息
         * 选择器则表示字典名称（二级选择器还有加点分隔的处理）
         */
        public String ConfigInfo;

        /**
         * 预设值
         */
        public String PresetValue;

        /**
         * 横跨距
         */
        public int RowSpan;

        /**
         * 纵跨距
         */
        public int ColSpan;

        /**
         * 只读
         */
        public int ReadOnly;

        /**
         * 验证规则
         */
        public String ValidateRule = "";

        /**
         * 显示顺序
         */
        public int Order;

        /**
         * 是否显示
         */
        public int Visible;
        /**
         * 是否同步到事件
         */
        public int IsSync = 0;

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

        public GDControl mapToGDControl() {

            GDControl gdControl = new GDControl();

            gdControl.setReadOnly(this.ReadOnly == 1);
            gdControl.Name = this.FieldName;
            gdControl.DisplayName = !TextUtils.isEmpty(this.Alias) ? this.Alias : this.FieldName;
            gdControl.DefaultValues = this.PresetValue;
            gdControl.ConfigInfo = this.ConfigInfo;

            switch (this.Shape) {
                case "选择器":
                    gdControl.Type = "选择器";
                    break;
                case "值选择器":
                    gdControl.Type = "值选择器";
                    break;
                case "动态值选择器":
                    gdControl.Type = "动态值选择器";
                    break;
                case "值复选器":
                case "值复选择器":
                    gdControl.Type = "值复选器";
                    break;
                case "平铺值选择器":
                    gdControl.Type = "平铺值选择器";
                    break;
                case "可编辑值选择器":
                    gdControl.Type = "可编辑值选择器";
                    break;
                case "站点选择器":
                    gdControl.Type = "站点选择器";
                    break;
                case "多行文本":
                    gdControl.Type = "短文本";
                    break;
                case "可预览图片":
                case "图片":
                    if ("拍照相册".equals(PresetValue)) {
                        gdControl.Type = "图片";//拍照和从相册选取
                    } else {
                        gdControl.Type = "拍照";//仅拍照
                    }
                    break;
                case "拍照":
                    //测试类型(Mobile端自己添加的类型（Web端没有），仅供Mobile内部使用)
                    gdControl.Type = "拍照";
                    break;
                case "拍照相册":
                    //测试类型(Mobile端自己添加的类型（Web端没有），仅供Mobile内部使用)
                    gdControl.Type = "图片";
                    break;
                case "录音":
                    gdControl.Type = "录音";
                    break;
                case "视频":
                    gdControl.Type = "视频";
                    break;
                case "坐标控件":
                    gdControl.Type = "坐标V2";

                    if ("当前坐标".equals(ConfigInfo)) {
                        gdControl.Type = "当前坐标";
                        gdControl.setReadOnly(false);
                    }
                    break;
                case "坐标控件V3":
                    gdControl.Type = "坐标V3"; //可以清空坐标值的控件
                    break;
                case "地址":
                    gdControl.Type = "百度地址"; //当前地址
                    break;
                case "附件":
                case "可预览附件":
                    gdControl.Type = "附件";
                    break;
                case "仅时间":
                    gdControl.Type = "仅时间";
                    resolveTimeControlConfig(gdControl);
                    break;
                case "日期":
                    gdControl.Type = "仅日期";
                    resolveTimeControlConfig(gdControl);
                    break;
                case "时间":
                    gdControl.Type = "日期框";
                    resolveTimeControlConfig(gdControl);
                    break;
                case "参数":
                    gdControl.Type = "短文本";
                    gdControl.setReadOnly(true);
                    break;
                case "文本":
                    gdControl.Type = "短文本";
                    if (!TextUtils.isEmpty(ConfigInfo)) {
                        if (ConfigInfo.startsWith("距离=")) {
                            gdControl.Type = "距离";
                        } else if (ConfigInfo.startsWith("浓度")) {
                            gdControl.Type = "浓度";
                        }
                    }
                    break;
                case "坐标V2":
                case "距离":
                case "常用语":
                case "本人部门":
                case "本人姓名":
                case "区域控件":
                case "设备选择":
                case "人员选择器":
                    gdControl.Type = this.Shape;
                    break;
                case "场站设备选择器":
                    gdControl.Type = this.Shape;
                    if (!"设备类型".equals(FieldName)) {
                        this.Visible = 0;
                    }
                    break;
                default:
                    gdControl.Type = "短文本";
                    break;
            }

            gdControl.DisplayColSpan = this.ColSpan;

            gdControl.Validate = this.ValidateRule.contains("required") ? "1" : "0";
            gdControl.IsVisible = this.Visible == 0 ? "false" : "true";

            gdControl.Unit = this.Unit;
            gdControl.TableName = this.TableName;
            gdControl.ValidateRule = this.ValidateRule;
            gdControl.MaxLength = getMaxValueLength();

            gdControl.TriggerProblemValue = this.TriggerProblemValue;
            gdControl.TriggerEvent = this.TriggerEvent;
            gdControl.TriggerEventFields = this.TriggerEventFields;

//            // Error: 语法不报错，运行时提示NullPointerException，即此处无法访问Values
//            for (TableValue tableValue : Values) {
//                if (tableValue.FieldName.equals(gdControl.DisplayName)) {
//                    gdControl.Value = tableValue.FieldValue;
//                }
//            }

            return gdControl;
        }

        void resolveTimeControlConfig(GDControl gdControl) {

            if (TextUtils.isEmpty(ConfigInfo)) {
                // 配置为空默认取当前时间
                gdControl.ConfigInfo = "";
                return;
            }

            if ("默认为空".equals(ConfigInfo) || "empty".equalsIgnoreCase(ConfigInfo)) {
                gdControl.ConfigInfo = "默认为空";
                return;
            }

            if ("不可选择".equals(ConfigInfo)) {
                if ("仅时间".equals(Shape)) {
                    gdControl.Type = "时间";
                    gdControl.ConfigInfo = "仅时间_不可选择";
                    return;
                }
                if ("日期".equals(Shape)) {
                    gdControl.Type = "时间";
                    gdControl.ConfigInfo = "仅日期_不可选择";
                }
                if ("时间".equals(Shape)) {
                    gdControl.Type = "时间";
                    gdControl.ConfigInfo = "日期_不可选择";
                }
            }

        }

        private int getMaxValueLength() {

            if (ValidateRule.contains("maxlength")) {
                matcherMaxLength.reset(ValidateRule);
                if (matcherMaxLength.find()) {
                    String lengthStr = matcherMaxLength.group();
                    lengthStr = lengthStr.substring(lengthStr.indexOf(":") + 1).trim();
                    try {
                        int maxLength = Integer.parseInt(lengthStr);
                        if (maxLength > 0) {
                            // 服务粗暴的除2，本地还原后自己做长度判断
                            return maxLength * 2;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
            return 0;
        }

    }

    /**
     * 表值
     */
    public class TableValue implements Serializable {
        /**
         * 字段名称
         */
        public String FieldName;

        /**
         * 字段值
         */
        public String FieldValue;

        public TableValue(String fieldName, String fieldValue) {
            this.FieldName = fieldName;
            this.FieldValue = fieldValue;
        }
    }

    public void addValue(String fieldName, String fieldValue) {
        Values.add(new TableValue(fieldName, fieldValue));
    }

    public GDFormBean mapToGDFormBean() {
        return mapToGDFormBean(false);
    }

    public GDFormBean mapToGDFormBean(boolean isAllOnlyRead) {

        GDFormBean gdFormBean = new GDFormBean();

        GDGroup[] gdGroups = new GDGroup[Groups.size()];
        for (int i = 0; i < gdGroups.length; i++) {
            gdGroups[i] = Groups.get(i).mapToGDGroup();

            // 将Values中的值映射存储到GDControl中去
            for (GDControl gdControl : gdGroups[i].Controls) {
                gdControl.setReadOnly(isAllOnlyRead || gdControl.isReadOnly());

                for (TableValue tableValue : Values) {
                    if (tableValue.FieldName.equals(gdControl.Name)) {
                        gdControl.Value = tableValue.FieldValue;
                        break;
                    }
                }
            }
        }
        gdFormBean.Groups = gdGroups;
        gdFormBean.BottomButtons = new GDButton[0];

        return gdFormBean;
    }

    public String getValueByName(String name) {

        String value = "";

        if (TextUtils.isEmpty(name)) {
            return value;
        }

        for (TableValue tableValue : Values) {
            if (tableValue.FieldName.equals(name)) {
                value = tableValue.FieldValue;
            }
        }
        return value;
    }
}

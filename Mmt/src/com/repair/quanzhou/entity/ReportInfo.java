package com.repair.quanzhou.entity;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.BitmapUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyunfan on 2016/1/21.
 */
public class ReportInfo {
    public String workTaskSeq;//工单号
    public String inputWorkerID;// = MyApplication.getInstance().getUserId() + "";//当前录入信息人员
    public int operateType;//处理类型
    public String isCompleteConstruct;//是否处理好
    public String malfunctionTypeId;//故障主要原因
    public String localeDealPerson;//现场处理人
    public String localeDealPersonTel;//移动电话
    public String localeDealTime;//现场处理时间
    public String endLocaleDealTime;//结束时间
    public String seed;//事件原由
    public String memo;//处理过程
    public String stateMemo;//处理结果
    public int isDealRerutn;//用户回单
    public String feedBackStateId;//用户意见
    public String feedBackMemo;//无回单原因
//    public File file;//文件上传
    public String file;//文件上传


    public void setValues2Params(List<String> params, Map<String, File> files) {

        Field[] fields = this.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {

            String key = fields[i].getName();

            String value = "";
            try {
                Object valueo = fields[i].get(this);
                value = valueo != null ? valueo.toString() : "";

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (key.equals("file")) {
                if (!BaseClassUtil.isNullOrEmptyString(value)) {

                    String[] fileStrs = value.split(",");
                    for (int j = 0; j < fileStrs.length; j++) {
                        File filetemp = new File(fileStrs[j]);
                        files.put("file[" + j + "]", BitmapUtil.convertBitmap2File(BitmapUtil.getBitmapFromFile(filetemp, 300, 500), filetemp));
                    }
                }
                continue;
            }
            params.add(key);
            params.add(value);
        }
    }

    public void setValue(String chosedName, String chosedValue) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (chosedName.equals(fields[i].getName())) {
                String type = fields[i].getGenericType().toString();
                try {
                    if (type.equals("class java.lang.String")) {
                        fields[i].set(this, chosedValue);
                    } else if (type.equals("int")) {
                        if (!BaseClassUtil.isNullOrEmptyString(chosedValue)) {
                            fields[i].set(this, Integer.valueOf(chosedValue));
                        }
                    } else if (type.equals("class java.io.File")) {
                        fields[i].set(this, new File(chosedValue));
                    }
                } catch (Exception ex) {

                }
                break;
            }
        }
    }

    public void setValues2Map(Map<String, String> map) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {

            String key = fields[i].getName();
            if (key.equals("file")) {
                continue;
            }
            String value = "";
            try {
                Object valueo = fields[i].get(this);
                value = valueo != null ? valueo.toString() : "";

            } catch (Exception ex) {
            }
            map.put(key, value);
        }
    }

}

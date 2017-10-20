package com.android.business.entity;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.UUID;


/**
 * 文件描述：实体基类，支持克隆、序列化 功能说明： 版权申明：
 * 
 * @author：ding_qili
 * @version:2015-3-7下午3:36:10
 */

public class DataInfo implements Cloneable, Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String uuid = UUID.randomUUID().toString(); // 对象唯一标示
    private Hashtable<String, Object> extandAttributeTable = null; // 扩展属性列表


    // 获得扩展属性值
    public Object getExtandAttributeValue(String name) {
        if (extandAttributeTable == null || !extandAttributeTable.containsKey(name)) {
            return null;
        }

        return extandAttributeTable.get(name);
    }

    // 设置扩展属性
    public void setExtandAttributeValue(String name, Object value) {
        if (extandAttributeTable == null) {
            extandAttributeTable = new Hashtable<String, Object>();
        }
        if (value != null) {
            extandAttributeTable.put(name, value);
        }
    }

    // 移除扩展属性
    public Object removeExtandAttributeValue(String key) {
        if (extandAttributeTable == null) {
            return null;
        }
        return extandAttributeTable.remove(key);
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return uuid;
    }

}

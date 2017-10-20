package com.repair.zhoushan.module.devicecare.consumables.entity;

public class SAPBean {

    private String Code;
    private String Name;

    public SAPBean() {
        this.Code = "";
        this.Name = "";
    }

    public SAPBean(String code, String name) {
        this.Code = code;
        this.Name = name;
    }

    public SAPBean(SAPBean sapBean) {
        this(sapBean.getCode(), sapBean.getName());
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj instanceof SAPBean) {
            SAPBean bean = (SAPBean) obj;

            boolean isEqual;

            if (this.Code == null) {
                isEqual = (bean.getCode() == null);
            } else {
                isEqual = this.Code.equals(bean.getCode());
            }

            if (!isEqual)
                return false;

            if (this.Name == null) {
                isEqual = (bean.getName() == null);
            } else {
                isEqual = this.Name.equals(bean.getName());
            }

            return isEqual;
        }

        return false;
    }
}

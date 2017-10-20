package com.mapgis.mmt.common.widget.pinyinsearch.lib;

import java.util.ArrayList;
import java.util.List;

public class PinyinSearchUnit implements Cloneable {

    private String mBaseData;  //the original string, like "牛奶"
    private List<PinyinUnit> mPinyinUnits;
    private StringBuffer mMatchKeyword; //the sub string of base data which search by key word

    public PinyinSearchUnit() {
        super();
        initPinyinSearchUnit();
    }

    public PinyinSearchUnit(String baseData) {
        super();
        mBaseData = baseData;
        initPinyinSearchUnit();
    }

    public String getBaseData() {
        return mBaseData;
    }

    public void setBaseData(String baseData) {
        mBaseData = baseData;
    }

    public List<PinyinUnit> getPinyinUnits() {
        return mPinyinUnits;
    }

    public void setPinyinUnits(List<PinyinUnit> pinyinUnits) {
        mPinyinUnits = pinyinUnits;
    }

    public StringBuffer getMatchKeyword() {
        return mMatchKeyword;
    }

    public void setMatchKeyword(StringBuffer matchKeyword) {
        mMatchKeyword = matchKeyword;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {

        PinyinSearchUnit obj = (PinyinSearchUnit) super.clone();
        obj.mPinyinUnits = new ArrayList<PinyinUnit>();

        for (PinyinUnit pu : mPinyinUnits) {
            obj.mPinyinUnits.add((PinyinUnit) pu.clone());
        }

        return obj;
    }

    private void initPinyinSearchUnit() {
        mPinyinUnits = new ArrayList<PinyinUnit>();
        mMatchKeyword = new StringBuffer();
    }
}


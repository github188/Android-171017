package com.mapgis.mmt.common.widget.pinyinsearch;

import com.mapgis.mmt.common.widget.pinyinsearch.lib.PinyinSearchUnit;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Model used for searching.
 */
public class SearchEntity implements Cloneable {

    public enum SearchByType {
        SearchByNull, SearchByLabel
    }

    private BaseEntity mBaseEntity; // base data

    private String mSortKey; // as the sort key word

    private PinyinSearchUnit mPinyinSearchUnit; // save the mLabel converted to Pinyin characters.
    private SearchByType mSearchByType; // Used to save the type of search
    private StringBuffer mMatchKeywords;// Used to save the type of Match Keywords.(label)
    private int mMatchStartIndex;		// the match start position of mMatchKeywords in original string(label).
    private int mMatchLength;			// the match length of mMatchKeywords in original string(name or phoneNumber).

    public SearchEntity() {
        super();

        this.mPinyinSearchUnit = new PinyinSearchUnit();
        this.mSearchByType = SearchByType.SearchByNull;
        this.mMatchKeywords = new StringBuffer();
        this.mMatchStartIndex = -1;
        this.mMatchLength = 0;
    }

    private static Comparator<Object> mChineseComparator = Collator.getInstance(Locale.CHINA);

    public static Comparator<SearchEntity> mDesComparator = new Comparator<SearchEntity>() {
        @Override
        public int compare(SearchEntity lhs, SearchEntity rhs) {
            return mChineseComparator.compare(rhs.mSortKey, lhs.mSortKey);
        }
    };

    public static Comparator<SearchEntity> mAscComparator = new Comparator<SearchEntity>() {
        @Override
        public int compare(SearchEntity lhs, SearchEntity rhs) {
            return mChineseComparator.compare(lhs.mSortKey, rhs.mSortKey);
        }
    };

    public static Comparator<SearchEntity> mSearchComparator = new Comparator<SearchEntity>() {
        @Override
        public int compare(SearchEntity lhs, SearchEntity rhs) {
            int compareMatchStartIndex = (lhs.mMatchStartIndex - rhs.mMatchStartIndex);
            int compareMatchLength = rhs.mMatchLength - lhs.mMatchLength;

            return ((0 != compareMatchStartIndex) ? (compareMatchStartIndex)
                    : ((0 != compareMatchLength) ? (compareMatchLength) : (lhs.getBaseEntity().getKeyField().length() - rhs.getBaseEntity().getKeyField().length())));
        }
    };

    public BaseEntity getBaseEntity() {
        return mBaseEntity;
    }

    public void setBaseEntity(BaseEntity baseEntity) {
        this.mBaseEntity = baseEntity;
    }

    public PinyinSearchUnit getPinyinSearchUnit() {
        return mPinyinSearchUnit;
    }

    public void setPinyinSearchUnit(PinyinSearchUnit labelPinyinSearchUnit) {
        mPinyinSearchUnit = labelPinyinSearchUnit;
    }

    public String getSortKey() {
        return mSortKey;
    }

    public void setSortKey(String sortKey) {
        mSortKey = sortKey;
    }

    public SearchByType getSearchByType() {
        return mSearchByType;
    }

    public void setSearchByType(SearchByType searchByType) {
        mSearchByType = searchByType;
    }

    public StringBuffer getMatchKeywords() {
        return mMatchKeywords;
    }

    public void setMatchKeywords(StringBuffer matchKeywords) {
        mMatchKeywords = matchKeywords;
    }

    public void setMatchKeywords(String matchKeywords) {
        mMatchKeywords.delete(0, mMatchKeywords.length());
        mMatchKeywords.append(matchKeywords);
    }

    public void clearMatchKeywords() {
        mMatchKeywords.delete(0, mMatchKeywords.length());
    }

    public int getMatchStartIndex() {
        return mMatchStartIndex;
    }

    public void setMatchStartIndex(int matchStartIndex) {
        mMatchStartIndex = matchStartIndex;
    }

    public int getMatchLength() {
        return mMatchLength;
    }

    public void setMatchLength(int matchLength) {
        mMatchLength = matchLength;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {

        SearchEntity obj = null;

        try {
            obj = (SearchEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        if (obj != null) {
            obj.mBaseEntity = (BaseEntity) mBaseEntity.clone();
            obj.mPinyinSearchUnit = (PinyinSearchUnit) mPinyinSearchUnit.clone();
            obj.mSearchByType = mSearchByType;
            obj.mMatchKeywords = new StringBuffer(mMatchKeywords);
        }

        return obj;
    }

    @Override
    public String toString() {
        return mBaseEntity.getKeyField(); // determine the content shown in the list.
    }
}

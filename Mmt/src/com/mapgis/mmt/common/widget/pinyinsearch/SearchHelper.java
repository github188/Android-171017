package com.mapgis.mmt.common.widget.pinyinsearch;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.mapgis.mmt.common.widget.pinyinsearch.lib.PinyinSearchUnit;
import com.mapgis.mmt.common.widget.pinyinsearch.lib.PinyinUtil;
import com.mapgis.mmt.common.widget.pinyinsearch.lib.QwertyUtil;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchHelper {

    private static final String TAG = "SearchHelper";
    private static Character THE_LAST_ALPHABET = 'z';

    private static SearchHelper mSearchHelper;

    private List<BaseEntity> mBaseEntityList; // original data (generated outside).
    private List<SearchEntity> mBaseAllEntityInfos; // all searchable data.
    private List<SearchEntity> mQwertySearchEntityInfos; // current matched data.

    private StringBuffer mFirstNoQwertySearchResultInput;

    private MmtBaseTask<Void, Void, List<SearchEntity>> resolveDataTask;
    private Context mContext;
    private OnOriginalDataParse mOnOriginalDataParse;

    private SearchHelper() {
        initHelper();
    }

    private void initHelper() {
        this.mBaseEntityList = new ArrayList<BaseEntity>();
        this.mBaseAllEntityInfos =  new ArrayList<SearchEntity>();
        this.mQwertySearchEntityInfos = new ArrayList<SearchEntity>();
        this.mFirstNoQwertySearchResultInput = new StringBuffer();
    }

    private void resetHelper() {
        mBaseEntityList.clear();
        mBaseAllEntityInfos.clear();
        mQwertySearchEntityInfos.clear();
        mFirstNoQwertySearchResultInput.delete(0, mFirstNoQwertySearchResultInput.length());
    }

    void cleanHelper() {
        resetHelper();
        mContext = null;
        mSearchHelper = null;
    }

    public static SearchHelper getInstance() {
        if (null == mSearchHelper) {
            mSearchHelper = new SearchHelper();
        }
        return mSearchHelper;
    }

    public void setBaseData(List<BaseEntity> baseEntityList) {
        resetHelper();
        mBaseEntityList.addAll(baseEntityList);
        resoleData();
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public interface OnOriginalDataParse {
        void onOriginalDataParseSuccess();
        void onOriginalDataParseFailed();
    }

    public void setOnOriginalDataParse(OnOriginalDataParse onOriginalDataParse) {
        this.mOnOriginalDataParse = onOriginalDataParse;
    }

    public List<SearchEntity> getQwertySearchEntityInfos() {
        return mQwertySearchEntityInfos;
    }

    private void resoleData() {

        if ((null != resolveDataTask) && resolveDataTask.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }

        this.resolveDataTask = new MmtBaseTask<Void, Void, List<SearchEntity>>(mContext) {
            @Override
            protected List<SearchEntity> doInBackground(Void... params) {

                return parseOrignalData();
            }

            @Override
            protected void onSuccess(List<SearchEntity> entityList) {

                if (entityList.size() == 0) {
                    if (null != mOnOriginalDataParse) {
                        mOnOriginalDataParse.onOriginalDataParseFailed();
                    }
                } else {

                    mBaseAllEntityInfos.clear();
                    mBaseAllEntityInfos.addAll(entityList);

                    if (null != mOnOriginalDataParse) {
                        mOnOriginalDataParse.onOriginalDataParseSuccess();
                    }
                }
                resolveDataTask = null;
            }
        };
        resolveDataTask.setCancellable(false);
        resolveDataTask.mmtExecute();
    }

    private List<SearchEntity> parseOrignalData() {

        List<SearchEntity> entityInfos = new ArrayList<SearchEntity>();
        List<SearchEntity> kanjiStartEntityInfos = new ArrayList<SearchEntity>();
        List<SearchEntity> nonKanjiStartEntityInfos = new ArrayList<SearchEntity>();

        SearchEntity searchEntity;
        for (BaseEntity baseEntity : mBaseEntityList) {
            searchEntity = new SearchEntity();
            searchEntity.setBaseEntity(baseEntity);

            searchEntity.getPinyinSearchUnit().setBaseData(searchEntity.getBaseEntity().getKeyField());
            PinyinUtil.parse(searchEntity.getPinyinSearchUnit());
            String sortKey = PinyinUtil.getSortKey(searchEntity.getPinyinSearchUnit()).toUpperCase();
            searchEntity.setSortKey(praseSortKey(sortKey));

            boolean isKanji = PinyinUtil.isKanji(searchEntity.getBaseEntity().getKeyField().charAt(0));
            if (isKanji) {
                kanjiStartEntityInfos.add(searchEntity);
            } else {
                nonKanjiStartEntityInfos.add(searchEntity);
            }
        }

        Collections.sort(kanjiStartEntityInfos, SearchEntity.mAscComparator);
        Collections.sort(nonKanjiStartEntityInfos, SearchEntity.mAscComparator);

        //appInfos.addAll(nonKanjiStartAppInfos);
        entityInfos.addAll(kanjiStartEntityInfos);

		/*Start: merge nonKanjiStartAppInfos and kanjiStartAppInfos*/
        int lastIndex = 0;
        boolean shouldBeAdd = false;
        for (int i = 0; i < nonKanjiStartEntityInfos.size(); i++) {
            String nonKanfirstLetter = PinyinUtil.getFirstLetter(nonKanjiStartEntityInfos.get(i).getPinyinSearchUnit());
            //Log.i(TAG, "nonKanfirstLetter=["+nonKanfirstLetter+"]["+nonKanjiStartAppInfos.get(i).getLabel()+"]["+Integer.valueOf(nonKanjiStartAppInfos.get(i).getLabel().charAt(0))+"]");
            int j = 0;
            for (j = 0 + lastIndex; j < entityInfos.size(); j++) {
                String firstLetter = PinyinUtil.getFirstLetter(entityInfos.get(j).getPinyinSearchUnit());
                lastIndex++;
                if (nonKanfirstLetter.charAt(0) < firstLetter.charAt(0) || nonKanfirstLetter.charAt(0) > THE_LAST_ALPHABET) {
                    shouldBeAdd = true;
                    break;
                } else {
                    shouldBeAdd = false;
                }
            }

            if (lastIndex >= entityInfos.size()) {
                lastIndex++;
                shouldBeAdd = true;
                //Log.i(TAG, "lastIndex="+lastIndex);
            }

            if (shouldBeAdd) {
                entityInfos.add(j, nonKanjiStartEntityInfos.get(i));
                shouldBeAdd = false;
            }
        }
		/*End: merge nonKanjiStartAppInfos and kanjiStartAppInfos*/

        return entityInfos;
    }

    public void qwertySearch(String keyword) {

        List<SearchEntity> baseAppInfos = mBaseAllEntityInfos;

        mQwertySearchEntityInfos.clear();

        if (TextUtils.isEmpty(keyword)) {
            for (SearchEntity ai : baseAppInfos) {
                ai.setSearchByType(SearchEntity.SearchByType.SearchByNull);
                ai.clearMatchKeywords();
                ai.setMatchStartIndex(-1);
                ai.setMatchLength(0);
            }
            mQwertySearchEntityInfos.addAll(baseAppInfos);

            mFirstNoQwertySearchResultInput.delete(0, mFirstNoQwertySearchResultInput.length());
            Log.i(TAG, "null==search,mFirstNoQwertySearchResultInput.length()=" + mFirstNoQwertySearchResultInput.length());
            return;
        }

        if (mFirstNoQwertySearchResultInput.length() > 0) {
            if (keyword.contains(mFirstNoQwertySearchResultInput.toString())) {
                Log.i(TAG,
                        "no need  to search,null!=search,mFirstNoQwertySearchResultInput.length()="
                                + mFirstNoQwertySearchResultInput.length() + "["
                                + mFirstNoQwertySearchResultInput.toString() + "]"
                                + ";searchlen=" + keyword.length() + "["
                                + keyword + "]");
                return;
            } else {
                Log.i(TAG,
                        "delete  mFirstNoQwertySearchResultInput, null!=search,mFirstNoQwertySearchResultInput.length()="
                                + mFirstNoQwertySearchResultInput.length()
                                + "["
                                + mFirstNoQwertySearchResultInput.toString()
                                + "]"
                                + ";searchlen="
                                + keyword.length()
                                + "["
                                + keyword + "]");
                mFirstNoQwertySearchResultInput.delete(0, mFirstNoQwertySearchResultInput.length());
            }
        }

        mQwertySearchEntityInfos.clear();
        int baseAppInfosCount = baseAppInfos.size();
        for (int i = 0; i < baseAppInfosCount; i++) {
            PinyinSearchUnit labelPinyinSearchUnit = baseAppInfos.get(i).getPinyinSearchUnit();
            boolean match = QwertyUtil.match(labelPinyinSearchUnit, keyword);


            if (true == match) {// search by LabelPinyinUnits;
                SearchEntity appInfo = baseAppInfos.get(i);
                appInfo.setSearchByType(SearchEntity.SearchByType.SearchByLabel);
                appInfo.setMatchKeywords(labelPinyinSearchUnit.getMatchKeyword().toString());
                appInfo.setMatchStartIndex(appInfo.getBaseEntity().getKeyField().indexOf(appInfo.getMatchKeywords().toString()));
                appInfo.setMatchLength(appInfo.getMatchKeywords().length());

                mQwertySearchEntityInfos.add(appInfo);

                continue;
            }
        }

        if (mQwertySearchEntityInfos.size() <= 0) {
            if (mFirstNoQwertySearchResultInput.length() <= 0) {
                mFirstNoQwertySearchResultInput.append(keyword);
                Log.i(TAG,
                        "no search result,null!=search,mFirstNoQwertySearchResultInput.length()="
                                + mFirstNoQwertySearchResultInput.length() + "["
                                + mFirstNoQwertySearchResultInput.toString() + "]"
                                + ";searchlen=" + keyword.length() + "["
                                + keyword + "]");
            } else {

            }
        } else {
            Collections.sort(mQwertySearchEntityInfos, SearchEntity.mSearchComparator);
        }
        return;
    }

    private String praseSortKey(String sortKey) {
        if (null == sortKey || sortKey.length() <= 0) {
            return null;
        }

        if ((sortKey.charAt(0) >= 'a' && sortKey.charAt(0) <= 'z')
                || (sortKey.charAt(0) >= 'A' && sortKey.charAt(0) <= 'Z')) {
            return sortKey;
        }

        return String.valueOf('#') + sortKey;
    }
}

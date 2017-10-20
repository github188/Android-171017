package com.mapgis.mmt.common.widget;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.CharacterParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 查询关键字历史记录
 */
public class SearchHistoryFragment extends ListFragment {
    private final List<String> historyWords = new ArrayList<>();// 查询历史记录列表

    private final String searchHistoryKey = "searchHistory";

    private ArrayAdapter<String> adapter;

    private static SearchHistoryFragment fragment = new SearchHistoryFragment();

    public static SearchHistoryFragment getInstance() {
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();

        adapter = new ArrayAdapter<>(getActivity(), R.layout.simple_list_item_1, R.id.text1, historyWords);
        setListAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setCacheColorHint(0);
    }

    private void initData() {
        String historyWord = MyApplication.getInstance().getSystemSharedPreferences().getString(searchHistoryKey, "");

        historyWords.clear();

        if (historyWord.contains(",")) {
            List<String> list = Arrays.asList(historyWord.split(","));
            historyWords.addAll(list);
        }

        historyWords.add("清空历史记录");
    }

    public void notifyDataSetChanged(String inputStr) {
        if (historyWords.size() == 0) {
            initData();
        }

        if (!historyWords.contains(inputStr)) {
            // 保存信息
            String historyWord = MyApplication.getInstance().getSystemSharedPreferences().getString(searchHistoryKey, "");
            historyWord += inputStr + ",";

            Editor editor = MyApplication.getInstance().getSystemSharedPreferences().edit();

            editor.putString(searchHistoryKey, historyWord);

            editor.apply();

            historyWords.set(historyWords.size() - 1, inputStr);
            historyWords.add("清空历史记录");

            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 根据输入文字解析成拼音，然后跟记录做拼配，拼配成功的显示
     *
     * @param key 输入值
     */
    public void parserAdapter(String key) {
        try {
            String historyWord = MyApplication.getInstance().getSystemSharedPreferences().getString(searchHistoryKey, "");
            historyWords.clear();
            historyWords.addAll(Arrays.asList(historyWord.split(",")));

            if (key.trim().length() != 0) {
                List<String> list = parseChinese(historyWords, key);
                if (list.size() == 0) {
                    String pinyin = CharacterParser.getInstance().getSelling(key.trim());
                    list = parsePinyin(historyWords, pinyin);
                }
                historyWords.clear();
                historyWords.addAll(list);
            } else {
                initData();
            }

            if (adapter != null)
                adapter.notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<String> parsePinyin(List<String> list, String pinyin) {
        List<String> l = new ArrayList<>();
        for (String str : list) {
            String py = CharacterParser.getInstance().getSelling(str);
            if (py.toLowerCase().startsWith(pinyin.toLowerCase())) {
                l.add(str);
            }
        }
        return l;
    }

    private List<String> parseChinese(List<String> list, String chinese) {
        List<String> l = new ArrayList<>();
        for (String str : list) {
            if (str.startsWith(chinese)) {
                l.add(str);
            }
        }
        return l;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position == historyWords.size() - 1) {
            SharedPreferences.Editor editor = MyApplication.getInstance().getSystemSharedPreferences().edit();

            editor.putString(searchHistoryKey, "");

            editor.apply();

            historyWords.clear();
            historyWords.add("清空历史记录");
            adapter.notifyDataSetChanged();
        } else {
            View customView = ((BaseActivity) getActivity()).getCustomView();
            ((EditText) (customView.findViewById(R.id.edittext_seach_str))).setText(historyWords.get(position));
            customView.findViewById(R.id.btn_search).performClick();
        }
    }
}

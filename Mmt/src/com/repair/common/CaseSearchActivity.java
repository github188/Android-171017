package com.repair.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 设备养护搜索界面
 */
public class CaseSearchActivity extends BaseActivity implements View.OnClickListener {
    private final List<String> historyWords = new ArrayList<>();// 查询历史记录列表
    private String searchHistoryKey = "WxyhSearchHistory";
    private EditText txtKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mmt_search_page);

        ListView lvHistory = (ListView) findViewById(R.id.lvHistory);

        String searchKey = getIntent().getStringExtra("searchHistoryKey");
        if (!TextUtils.isEmpty(searchKey)) {
            this.searchHistoryKey = searchKey;
        }

        initData();

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_list_item_1, R.id.text1, historyWords);
        lvHistory.setAdapter(adapter);
        lvHistory.setCacheColorHint(0);

        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == historyWords.size() - 1) {
                    SharedPreferences.Editor editor = MyApplication.getInstance().getSystemSharedPreferences().edit();
                    editor.putString(searchHistoryKey, "");
                    editor.apply();

                    historyWords.clear();
                    historyWords.add("清空历史记录");

                    adapter.notifyDataSetChanged();
                } else {
                    txtKey.setText(historyWords.get(position));
                    txtKey.setSelection(txtKey.length());

                    txtKey.requestFocus();
                }
            }
        });

        lvHistory.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hideKeyboard();
                }

                return false;
            }
        });

        String searchHint = getIntent().getStringExtra("searchHint");
        this.txtKey = (EditText) findViewById(R.id.edittext_seach_str);
        txtKey.setSingleLine();
        txtKey.setHint(TextUtils.isEmpty(searchHint) ? "工单编号/事件编号" : searchHint);

        String key = getIntent().getStringExtra("key");
        View btnReset = findViewById(R.id.btnReset);

        if (!TextUtils.isEmpty(key)) {
            txtKey.setText(key);
            txtKey.setSelection(txtKey.length());

            btnReset.setOnClickListener(this);
        } else {
            btnReset.setVisibility(View.GONE);
        }

        txtKey.requestFocus();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(txtKey, 0);
            }
        }, 200);

        findViewById(R.id.btn_search).setOnClickListener(this);
        findViewById(R.id.edittext_seach_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_search) {
            String key = txtKey.getEditableText().toString();

            if (!TextUtils.isEmpty(key) && !historyWords.contains(key)) {
                // 保存信息
                String historyWord = MyApplication.getInstance().getSystemSharedPreferences().getString(searchHistoryKey, "");

                historyWord += key + ",";

                SharedPreferences.Editor editor = MyApplication.getInstance().getSystemSharedPreferences().edit();

                editor.putString(searchHistoryKey, historyWord);

                editor.apply();
            }

            setResult(1, getIntent().putExtra("key", key));
        } else if (v.getId() == R.id.btnReset) {
            setResult(1, getIntent().putExtra("key", ""));
        }

        onBackPressed();
    }

    private void initData() {
        String historyWord = MyApplication.getInstance().getSystemSharedPreferences().getString(searchHistoryKey, "");

        historyWords.clear();

        if (!TextUtils.isEmpty(historyWord) && historyWord.contains(",")) {
            List<String> list = Arrays.asList(historyWord.split(","));

            historyWords.addAll(list);
        }

        historyWords.add("清空历史记录");
    }

    private void hideKeyboard() {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(txtKey.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        hideKeyboard();

        super.onBackPressed();
    }
}

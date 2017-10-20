package com.mapgis.mmt.common.widget.pinyinsearch;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;

import java.util.List;

public abstract class PinyinSearchActivity extends BaseActivity implements SearchHelper.OnOriginalDataParse {

    private ArrayAdapter<SearchEntity> mArrayAdapter;

    private EditText txtSearch; // ActionBar搜索框
    private ListView mListView;
    private TextView emptyTextView;

    @Override
    protected void setDefaultContentView() {

        setContentView(R.layout.pinyin_search_list_view);

        setSwipeBackEnable(false);

        List<BaseEntity> dataList = getBaseEntities();

        SearchHelper.getInstance().setOnOriginalDataParse(this);
        SearchHelper.getInstance().setContext(this);
        SearchHelper.getInstance().setBaseData(dataList);

        this.mArrayAdapter = new ArrayAdapter<SearchEntity>(this, R.layout.pinyin_search_list_item,
                SearchHelper.getInstance().getQwertySearchEntityInfos());

        initView();
        initListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SearchHelper.getInstance().cleanHelper();
    }

    private void initView() {

        // search box.
        findViewById(R.id.btnOtherAction).setVisibility(View.GONE);
        this.txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.setFocusable(true);
        txtSearch.setFocusableInTouchMode(true);
        txtSearch.setHint("");

        // list view.
        this.mListView = (ListView) findViewById(R.id.list_view);
        mListView.setAdapter(mArrayAdapter);

        this.emptyTextView = (TextView) findViewById(R.id.tv_empty_result_prompt);
        emptyTextView.setVisibility(View.GONE);
    }

    private void initListener() {

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchEntity entity = mArrayAdapter.getItem(position);
                onResultSelected(entity.getBaseEntity());
            }
        });

        addBackBtnListener(findViewById(R.id.btnBack));

        txtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });
    }

    /**
     * initialize data.
     */
    protected abstract List<BaseEntity> getBaseEntities();

    /**
     * callback method to return select result.
     */
    protected abstract void onResultSelected(BaseEntity baseEntity);

    @Override
    public void onOriginalDataParseSuccess() {
        search(null);
    }

    @Override
    public void onOriginalDataParseFailed() {
        refreshView();
    }

    private void refreshView() {

        mArrayAdapter.notifyDataSetChanged();

        if (mArrayAdapter.getCount() > 0) {
            mListView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        } else {
            mListView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    private void search(String keyword) {

        String curCharacter = (null != keyword) ? keyword.trim() : null;

        if (TextUtils.isEmpty(curCharacter)) {
            SearchHelper.getInstance().qwertySearch(null);
        } else {
            SearchHelper.getInstance().qwertySearch(curCharacter);
        }
        refreshView();
    }
}

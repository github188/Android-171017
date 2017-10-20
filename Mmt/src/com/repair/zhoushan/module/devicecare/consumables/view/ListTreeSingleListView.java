package com.repair.zhoushan.module.devicecare.consumables.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.mapgis.mmt.R;
import com.repair.zhoushan.module.devicecare.consumables.entity.SAPBean;

import java.util.List;

public class ListTreeSingleListView extends RelativeLayout implements ViewBaseAction {

    private ListView mListView;
    private List<SAPBean> items = null;// 显示字段
    private OnSelectListener mOnSelectListener;
    private TextAdapter adapter;

    private String showText = "item1";

    public String getShowText() {
        return showText;
    }

    public ListTreeSingleListView(Context context, List<SAPBean> items) {
        super(context);
        this.items = items;
        init(context);
    }

    public ListTreeSingleListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ListTreeSingleListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.list_tree_single_list, this, true);
        setBackgroundDrawable(getResources().getDrawable(R.drawable.list_tree_bg));
        mListView = (ListView) findViewById(R.id.listView);

        adapter = new TextAdapter(context, items, R.drawable.list_tree_selected_item_bg, R.drawable.list_tree_normal_item_selector);
        adapter.setTextSize(17);
        mListView.setAdapter(adapter);

        adapter.setOnItemClickListener(new TextAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (mOnSelectListener != null) {
                    showText = items.get(position).getName();
                    mOnSelectListener.getValue(position, items.get(position).getName());
                }
            }
        });
    }

    public void setSelectedPosition(int pos) {
        adapter.setSelectedPosition(pos);
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    public void clearSelection() {
        adapter.resetSelectedState();
    }

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        mOnSelectListener = onSelectListener;
    }

    public interface OnSelectListener {
        void getValue(int selectIndex, String showText);
    }

    @Override
    public void hideMenu() {
        // TODO Auto-generated method stub
    }

    @Override
    public void showMenu() {
        // TODO Auto-generated method stub
    }

}

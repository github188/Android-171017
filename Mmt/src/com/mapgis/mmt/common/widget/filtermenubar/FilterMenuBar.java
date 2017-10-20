package com.mapgis.mmt.common.widget.filtermenubar;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DeviceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterMenuBar extends LinearLayout implements View.OnClickListener {

    private Context mContext;

    private PopupWindow mPopupWindow;
    // Menu 展开的ListView
    private ListView mMenuList;
    // Menu 展开的ListView 下部的阴影
    private RelativeLayout mShadow;

    private MenuItemListAdapter mAdapter;

    // 所有菜单的数据
    private List<FilterMenuItem> filterMenuItemList = new ArrayList<FilterMenuItem>();
    // 菜单项的宽度
    private int mItemWidth;
    // 选中的过滤条件的数目
    private int selectedItemCount = 0;
    // 当前点击选中的 TextView
    private TextView curSelectedTextView;

    private Map<String, String> selectResult = new HashMap<String, String>();

    public FilterMenuBar(Context context) {
        this(context, null);
    }

    public FilterMenuBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;

        View popupView = LayoutInflater.from(mContext).inflate(R.layout.popupwindow_menu, null);
        mPopupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mMenuList = (ListView) popupView.findViewById(R.id.lv_menu);
        mShadow = (RelativeLayout) popupView.findViewById(R.id.rl_menu_shadow);

        this.mItemWidth = DeviceUtil.getWindowsWidth((Activity) mContext) / 4;

        this.mAdapter = new MenuItemListAdapter(mContext);

        // initData();
    }

    // 设置 Menu的item
    public void setMenuItems(List<FilterMenuItem> filterMenuList) {

        this.filterMenuItemList.clear();
        this.filterMenuItemList.addAll(filterMenuList);

        initView();
        drawMenuBar();
    }

    public void setMenuItems(String[] titles, String[][] contents, String[] defValues)  {

        if (titles == null || contents == null || titles.length != contents.length) {
            throw new IllegalArgumentException("The length of titles must be equals to contents' length.");
        }

        List<FilterMenuItem> filterMenu = new ArrayList<FilterMenuItem>();

        ArrayList<FilterMenuItem> menuItems;
        for (int i = 0; i < titles.length; i++) {

            menuItems = new ArrayList<FilterMenuItem>();
            for (int j = 0; j < contents[i].length; j++) {
                menuItems.add(new FilterMenuItem(contents[i][j], contents[i][j], titles[i], contents[i][j], 0, null));
            }

            filterMenu.add(new FilterMenuItem(titles[i], titles[i], "", (defValues == null ? "" : defValues[i]), i, menuItems));
        }

        setMenuItems(filterMenu);
    }

    public void addMenuItem(String title, String[] content, String defValue) {

        ArrayList<FilterMenuItem> menuItems = new ArrayList<FilterMenuItem>();
        for (int i = 0; i < content.length; i++) {
            menuItems.add(new FilterMenuItem(content[i], content[i], title, content[i], 0, null));
        }

        defValue = (defValue == null ? "" : defValue);
        FilterMenuItem filterMenuItem = new FilterMenuItem(title, title, "", defValue, filterMenuItemList.size(), menuItems);
        filterMenuItemList.add(filterMenuItem);

        addMenuItem(filterMenuItem);
    }

    private void initData() {

        // Init data.
        ArrayList<FilterMenuItem> menu1SubMenu = new ArrayList<>();
        FilterMenuItem menu1 = new FilterMenuItem("City", "City", "", "", 0, menu1SubMenu);
        menu1SubMenu.add(new FilterMenuItem("BeiJing", "BeiJing", "City", "BeiJing", 0, null));
        menu1SubMenu.add(new FilterMenuItem("ShangHai", "ShangHai", "City", "ShangHai", 0, null));
        menu1SubMenu.add(new FilterMenuItem("ShengZheng", "ShengZheng", "City", "ShengZheng", 0, null));

        ArrayList<FilterMenuItem> menu2SubMenu = new ArrayList<>();
        FilterMenuItem menu2 = new FilterMenuItem("Nationality", "Nationality", "", "Chinese", 1, menu2SubMenu);
        menu2SubMenu.add(new FilterMenuItem("Chinese", "Chinese", "Nationality", "Chinese", 0, null));
        menu2SubMenu.add(new FilterMenuItem("English", "English", "Nationality", "English", 0, null));
        menu2SubMenu.add(new FilterMenuItem("American", "American", "Nationality", "American", 0, null));

        ArrayList<FilterMenuItem> menu3SubMenu = new ArrayList<>();
        FilterMenuItem menu3 = new FilterMenuItem("Gender", "Gender", "", "", 2, menu3SubMenu);
        menu3SubMenu.add(new FilterMenuItem("Male", "Male", "Gender", "Male", 0, null));
        menu3SubMenu.add(new FilterMenuItem("Female", "Female", "Gender", "Female", 0, null));

        FilterMenuItem menu4 = new FilterMenuItem("Gender", "Gender", "", "", 3, menu3SubMenu);
        FilterMenuItem menu5 = new FilterMenuItem("Gender", "Gender", "", "Female", 4, menu3SubMenu);
        FilterMenuItem menu6 = new FilterMenuItem("Gender", "Gender", "", "", 5, menu3SubMenu);
        FilterMenuItem menu7 = new FilterMenuItem("Gender", "Gender", "", "", 6, menu3SubMenu);

        filterMenuItemList.add(menu1);
        filterMenuItemList.add(menu2);
        filterMenuItemList.add(menu3);
        filterMenuItemList.add(menu4);
        filterMenuItemList.add(menu5);
        filterMenuItemList.add(menu6);
        filterMenuItemList.add(menu7);
    }

    private void initView() {

        // mPopupWindow.setTouchable(true);
        mPopupWindow.setFocusable(false);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        mShadow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
            }
        });
    }

    @Override
    public void onClick(View view) {

        TextView textView = (TextView) view;
        FilterMenuItem item = (FilterMenuItem) view.getTag();

        if (TextUtils.isEmpty(item.getValue())) {

            if (textView == curSelectedTextView) {
                hidePopupWindow();
                return;
            }

            curSelectedTextView = textView;

            for (int i = 0, count = FilterMenuBar.this.getChildCount(); i < count; i++) {
                View localView = FilterMenuBar.this.getChildAt(i);
                if (TextUtils.isEmpty(((FilterMenuItem) localView.getTag()).getValue())) {

                    if (localView != view) {
                        localView.setSelected(false);
                    } else {
                        localView.setSelected(true);
                    }
                }
            }

            mAdapter.setData(item);

            if (mMenuList.getAdapter() == null || mMenuList.getAdapter() != mAdapter) {
                mMenuList.setAdapter(mAdapter);
            }

            mAdapter.notifyDataSetChanged();

            if (Build.VERSION.SDK_INT < 24) {
                mPopupWindow.showAsDropDown(this, 0, 0);
            } else {
                int[] location = new int[2];
                getLocationOnScreen(location);
                mPopupWindow.showAtLocation(this, Gravity.NO_GRAVITY, 0, location[1] + getHeight());
            }

        } else {

            selectedItemCount--;

            item.clearValue();
            view.setSelected(false);
            FilterMenuBar.this.removeView(view);
            FilterMenuBar.this.addView(view,
                    item.getOrderIndex() >= selectedItemCount ? item.getOrderIndex() : selectedItemCount);

            setUnSelectedState(textView, item.getShowName());

            notifyFilterChanged();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(mPopupWindow.isShowing()){
                hidePopupWindow();
            return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void hidePopupWindow() {

        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        curSelectedTextView.setSelected(false);
        curSelectedTextView = null;
    }

    private void notifyFilterChanged() {

        if (mOnMenuItemSelectedListener != null) {
            selectResult.clear();
            for (FilterMenuItem filterMenuItem : filterMenuItemList) {
                selectResult.put(filterMenuItem.getName(), filterMenuItem.getValue());
            }
            mOnMenuItemSelectedListener.onItemSelected(selectResult);
        }
    }

    private void drawMenuBar() {

        this.removeAllViews();
        int count = filterMenuItemList.size();

        for (int i = 0; i < count; i++) {
            addMenuItem(filterMenuItemList.get(i));
        }
    }

    private void addMenuItem(FilterMenuItem filterMenuItem) {

        LayoutParams params = new LayoutParams(mItemWidth, LayoutParams.WRAP_CONTENT);
        params.leftMargin = 5;
        params.rightMargin = 5;

        TextView columnTextView = new TextView(mContext);
        columnTextView.setSingleLine(true);
        columnTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        columnTextView.setGravity(Gravity.CENTER);
        columnTextView.setPadding(5, 5, 5, 5);
        columnTextView.setId(BaseClassUtil.generateViewId());
        columnTextView.setTag(filterMenuItem);

        String curMenuValue = filterMenuItem.getValue();

        int viewIndex;
        if (TextUtils.isEmpty(curMenuValue)) {
            viewIndex = -1;
            setUnSelectedState(columnTextView, filterMenuItem.getShowName());
        } else {
            viewIndex = 0;
            setSelectedState(columnTextView, curMenuValue);
        }
        addView(columnTextView, viewIndex, params);
        columnTextView.setOnClickListener(this);
    }

    private void setSelectedState(TextView textView, String displayName) {

        textView.setBackgroundResource(R.drawable.search_prop_close);
        textView.setText(displayName);
        textView.setTextColor(0xfffd4c00);

        selectedItemCount++;
    }

    private void setUnSelectedState(TextView textView, String displayName) {
        textView.setBackgroundResource(R.drawable.menubar_scroll_text_bg);
        textView.setText(displayName);
        textView.setTextColor(getResources().getColorStateList(R.color.menubar_scroll_text_color));
    }

    public interface OnMenuItemSelectedListener {
        void onItemSelected(Map<String, String> selectResult);
    }

    private OnMenuItemSelectedListener mOnMenuItemSelectedListener;

    public void setOnMenuItemSelectedListener(OnMenuItemSelectedListener onMenuItemSelectedListener) {
        this.mOnMenuItemSelectedListener = onMenuItemSelectedListener;
    }

    private final View.OnClickListener listItemClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            int position = Integer.valueOf(view.getTag().toString());
            FilterMenuItem item = mAdapter.getData();

            item.setValue(item.getSubMenuItemList().get(position).getValue());

            FilterMenuBar.this.removeView(curSelectedTextView);
            FilterMenuBar.this.addView(curSelectedTextView, 0);

            setSelectedState(curSelectedTextView, item.getValue());

            curSelectedTextView = null;

            mPopupWindow.dismiss();

            notifyFilterChanged();
        }
    };

    public class MenuItemListAdapter extends BaseAdapter {

        private FilterMenuItem data;
        private LayoutInflater mLayoutInflater;

        public void setData(FilterMenuItem filterMenuItem) {
            this.data = filterMenuItem;
        }

        public FilterMenuItem getData() {
            return data;
        }

        public MenuItemListAdapter(Context context) {
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return data.getSubMenuItemList().size();
        }

        @Override
        public Object getItem(int position) {
            return data.getSubMenuItemList().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView view;
            if (convertView == null) {
                view = (TextView) mLayoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = (TextView) convertView;
            }

            view.setText(data.getSubMenuItemList().get(position).getShowName());
            view.setOnClickListener(listItemClick);
            view.setTag(position);

            return view;
        }
    }

}

package com.mapgis.mmt.common.widget.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.entity.LevelItem;
import com.mapgis.mmt.entity.LevelItemBase;

import java.util.ArrayList;
import java.util.List;

public class LevelItemFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private ArrayList<LevelItem> checkedItemList = new ArrayList<>();

    LevelItem item;
    boolean allowMultiSelect;

    DialogInterface.OnKeyListener onKeyListener = null;
    private LevelOneAdapt levelOneAdapter;

    public void setOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
        this.onKeyListener = onKeyListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.item = (LevelItem) getArguments().getSerializable("item");
        ArrayList<LevelItem> itemList = getArguments().getParcelableArrayList("checkedItemList");
        checkedItemList.clear();
        checkedItemList.addAll(itemList);
        this.allowMultiSelect = getArguments().getBoolean("allowMultiSelect", false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (onKeyListener == null) {
            onKeyListener = new DialogInterface.OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent arg2) {
                    return backEvent(arg0, keyCode, arg2);
                }
            };
        }
        this.getDialog().setOnKeyListener(onKeyListener);


        View view = inflater.inflate(R.layout.one_two, container, false);
        ((TextView) view.findViewById(R.id.tvTitleForOne)).setText(item.name);
        view.findViewById(R.id.ibtnCloseOne).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });

        levelOneAdapter = new LevelOneAdapt(getActivity(), this.item.children, allowMultiSelect);
        ListView lvOne = (ListView) view.findViewById(R.id.lvOne);
        lvOne.setAdapter(levelOneAdapter);
        lvOne.setOnItemClickListener(this);

        view.findViewById(R.id.layoutTwo).setVisibility(View.GONE);
        view.findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                levelTwoBackToOne();
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        };
        view.findViewById(R.id.ibtnConfirmOne).setOnClickListener(listener);
        view.findViewById(R.id.ibtnConfirmTwo).setOnClickListener(listener);

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() != null) {
            ((OnItemCheckedListener) getActivity()).onItemChecked(checkedItemList);
        }
    }

    public boolean backEvent(DialogInterface arg0, int keyCode, KeyEvent arg2) {
        if (keyCode == KeyEvent.KEYCODE_BACK && arg2.getAction() == KeyEvent.ACTION_UP && findViewById(R.id.layoutTwo).getVisibility() == View.VISIBLE) {
            levelTwoBackToOne();
            return true;
        }

        return false;
    }

    private void levelTwoBackToOne() {
        findViewById(R.id.layoutTwo).setVisibility(View.GONE);
        levelOneAdapter.notifyDataSetChanged();
    }

    private View findViewById(int id) {
        return getView().findViewById(id);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ArrayList<LevelItem> data = this.item.children;

        LevelItem item = data.get(position);

        if (item.children == null || item.children.size() == 0) {
            for (LevelItem li : data)
                li.setUnChecked();

            item.isChecked = true;

            checkedItemList.clear();
            checkedItemList.add(item);
            dismissAllowingStateLoss();

            return;
        }
        ((TextView) findViewById(R.id.tvTitleForTwo)).setText(item.name);

        final LevelTwoAdapt adapter = new LevelTwoAdapt(getActivity(), item, allowMultiSelect) {
            @Override
            public void afterItemClick(boolean isMultiSelect, LevelItem li) {

                if (!isMultiSelect) {
                    if (checkedItemList.size() > 0) {
                        checkedItemList.get(0).setUnChecked();
                        checkedItemList.clear();
                    }
                    li.isChecked = true;
                    checkedItemList.add(li);
                    dismissAllowingStateLoss();

                } else {
                    if (checkedItemList.size() > 0) {
                        LevelItemBase parent = checkedItemList.get(0).parent;
                        LevelItemBase lParent = li.parent;
                        if (parent == null || lParent == null || !parent.name.equals(lParent.name)) {
                            for (LevelItem levelItem : checkedItemList) {
                                levelItem.setUnChecked();
                            }
                            checkedItemList.clear();
                        }
                    }
                    if (li.isChecked) {
                        if (!checkedItemList.contains(li)) {
                            checkedItemList.add(li);
                        }
                    } else {
                        if (checkedItemList.contains(li)) {
                            checkedItemList.remove(li);
                        }
                    }
                }
            }
        };

        ListView lvTwo = (ListView) findViewById(R.id.lvTwo);
        lvTwo.setAdapter(adapter);
        lvTwo.setOnItemClickListener(adapter);

        findViewById(R.id.layoutTwo).setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();

        int w = getDialog().getWindow().getAttributes().width;
        int h = DimenTool.getHeightPx(getActivity()) - DimenTool.dip2px(getActivity(), 100);

        getDialog().getWindow().setLayout(w, h);
    }

    public interface OnItemCheckedListener {
        void onItemChecked(ArrayList<LevelItem> checkedItemList);
    }

    static class LevelOneAdapt extends BaseAdapter {
        Context context;
        ArrayList<LevelItem> data;
        boolean allowMultiSelect;

        public LevelOneAdapt(Context context, ArrayList<LevelItem> data, boolean allowMultiSelect) {
            this.context = context;
            this.data = data;
            this.allowMultiSelect = allowMultiSelect;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            List<LevelItem> items = data.get(position).children;

            ArrayList<String> names = new ArrayList<>();

            for (LevelItem li : items) {
                if (li.isChecked)
                    names.add(li.name);
            }

            return TextUtils.join(",", names);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(context).inflate(R.layout.one_item, parent, false);

            LevelItem item = data.get(position);

            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            tvName.setText(item.name);
            TextView tvOper = (TextView) convertView.findViewById(R.id.tvOper);

            if (item.children == null || item.children.size() == 0) {
                tvOper.setText("");
                if (item.isChecked) {
                    tvName.setTextColor(Color.RED);
                    tvOper.setTextColor(Color.RED);
                    tvOper.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.order_pickup_butn_seleted_icon, 0);
                } else {
                    tvName.setTextColor(Color.BLACK);
                    tvOper.setTextColor(Color.BLACK);
                    tvOper.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            } else {
                String value = getItem(position);
                tvOper.setText(value);
                tvOper.setTextColor(TextUtils.isEmpty(value) ? Color.BLACK : Color.RED);
                tvOper.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.button_for_more, 0);
            }

            return convertView;
        }
    }

    static abstract class LevelTwoAdapt extends BaseAdapter implements AdapterView.OnItemClickListener {
        Context context;
        LevelItem item;
        boolean allowMultiSelect;

        public LevelTwoAdapt(Context context, LevelItem item, boolean allowMultiSelect) {
            this.context = context;
            this.item = item;
            this.allowMultiSelect = allowMultiSelect;
        }

        @Override
        public int getCount() {
            return this.item.children.size();
        }

        @Override
        public LevelItem getItem(int position) {
            return this.item.children.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            AbsListView.LayoutParams params = new AbsListView.LayoutParams(-1, DimenTool.dip2px(context, 50));
            TextView tv = new TextView(context);
            tv.setLayoutParams(params);
            tv.setGravity(Gravity.CENTER_VERTICAL);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);

            LevelItem child = getItem(position);
            tv.setText(child.name);

            if (child.isChecked) {
                tv.setTextColor(Color.RED);
                tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.order_pickup_butn_seleted_icon, 0);
            } else {
                tv.setTextColor(Color.BLACK);
                tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            return tv;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final LevelItem li = getItem(position);

            if (!allowMultiSelect) {
                for (LevelItem levelItem : item.children) {

                    levelItem.isChecked = li.name.equals(levelItem.name);
                }
            } else {
                li.isChecked = !li.isChecked;
            }

            this.notifyDataSetChanged();
            afterItemClick(allowMultiSelect, li);
        }

        public abstract void afterItemClick(boolean isMultiSelect, LevelItem li);
    }

}

package com.mapgis.mmt.common.widget.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.MmtViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ListCheckBoxDialogFragment extends DialogFragment {

    private List<String> dataList = null;
    private List<String> defDataList = null;
    private OnRightButtonClickListener onRightButtonClickListener;
    private String title;

    private boolean singleMode = false;
    private int selectedIndex = -1;
    private boolean disallowEmpty = false; // allow empty or not. default allow.

    private ListCheckBoxDialogAdapter adapter;

    public void setSingleMode(boolean singleMode) {
        this.singleMode = singleMode;
    }

    public void setDisallowEmpty(boolean disallowEmpty) {
        this.disallowEmpty = disallowEmpty;
    }

    public static ListCheckBoxDialogFragment newInstance(String title, ArrayList<String> dataList, ArrayList<String> defCheckedDataList) {

        if (TextUtils.isEmpty(title)) {
            throw new NullPointerException("title cannot be empty.");
        }
        if (dataList == null || dataList.size() < 1) {
            throw new NullPointerException("dataList cannot be empty.");
        }

        Bundle args = new Bundle();
        args.putString("Title", title);
        args.putStringArrayList("DataList", dataList);
        args.putStringArrayList("DefCheckedDataList", defCheckedDataList);

        ListCheckBoxDialogFragment fragment = new ListCheckBoxDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ListCheckBoxDialogFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Bundle args = getArguments();
        this.title = args.getString("Title");
        this.dataList = args.getStringArrayList("DataList");
        this.defDataList = args.getStringArrayList("DefCheckedDataList");
    }

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.listdialog, container, false);

        ((TextView) v.findViewById(R.id.listDialogTitle)).setText(this.title);

        if ((this.dataList != null) && (this.dataList.size() > 0)) {

            v.findViewById(R.id.layoutOkCancel).setVisibility(View.VISIBLE);
            final ListView listView = (ListView) v.findViewById(R.id.listDialog);

            this.adapter = new ListCheckBoxDialogAdapter();
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        CheckBox checkBox = (CheckBox) view.findViewById(R.id.listItemCheckbox);
                        if (!singleMode) {
                            checkBox.performClick();
                        } else {
                            if (position != selectedIndex) {
                                checkBox.setChecked(true);
                                int firstVisiblePosition = listView.getFirstVisiblePosition();
                                int lastVisiblePosition = listView.getLastVisiblePosition();

                                if (selectedIndex >= firstVisiblePosition && selectedIndex <= lastVisiblePosition) {
                                    View v = listView.getChildAt(selectedIndex - firstVisiblePosition);
                                    if (v != null) {
                                        ((CheckBox) v.findViewById(R.id.listItemCheckbox)).setChecked(false);
                                    } else {
                                        adapter.checkedStates[selectedIndex] = false;
                                    }
                                } else {
                                    adapter.checkedStates[selectedIndex] = false;
                                }
                                selectedIndex = position;
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            v.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    List<String> results = ((ListCheckBoxDialogAdapter) listView.getAdapter()).getSelectedNodes();
                    
                    if (disallowEmpty && results.size() == 0) {
                        Toast.makeText(view.getContext(), "请先选择条目", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (onRightButtonClickListener != null) {
                        onRightButtonClickListener.onRightButtonClick(view, results);
                    }

                    dismiss();
                }

            });
            v.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    dismiss();
                }
            });
        }

        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        return v;
    }

    public void setOnRightButtonClickListener(OnRightButtonClickListener onRightButtonClickListener) {
        this.onRightButtonClickListener = onRightButtonClickListener;
    }

    public interface OnRightButtonClickListener {
        void onRightButtonClick(View view, List<String> selectedItems);
    }

    final class ListCheckBoxDialogAdapter extends BaseAdapter {

        final boolean[] checkedStates = new boolean[dataList.size()];

        public ListCheckBoxDialogAdapter() {

            int defValueIndex = -1;

            if (defDataList != null && defDataList.size() > 0) {
                for (String item : defDataList) {

                    // 单选模式下只允许有一个处于选中状态
                    if (singleMode && defValueIndex != -1) break;

                    for (int i = 0; i < dataList.size(); i++) {
                        if (dataList.get(i).equals(item) && !checkedStates[i]) {
                            checkedStates[i] = true;
                            defValueIndex = i;
                            break;
                        }
                    }
                }
            }
            // 单选模式下没有默认值则默认选中第一个
            if (singleMode) {
                if (defValueIndex == -1) {
                    checkedStates[0] = true;
                    selectedIndex = 0;
                } else {
                    selectedIndex = defValueIndex;
                }
            }
        }

        public int getCount() {
            return dataList.size();
        }

        public Object getItem(int position) {
            return dataList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public List<String> getSelectedNodes() {
            ArrayList<String> selectedNodes = new ArrayList<>();
            for (int i = 0; i < checkedStates.length; i++) {
                if (checkedStates[i]) {
                    selectedNodes.add(dataList.get(i));
                }
            }
            return selectedNodes;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.listcheckboxdialog_item, parent, false);
            }

            CheckBox checkBox = MmtViewHolder.get(convertView, R.id.listItemCheckbox);
            checkBox.setText(dataList.get(position));
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkedStates[position] = isChecked;
                }
            });

            // 此操作必须在设置监听器之后执行
            checkBox.setChecked(checkedStates[position]);

            return convertView;

        }
    }
}

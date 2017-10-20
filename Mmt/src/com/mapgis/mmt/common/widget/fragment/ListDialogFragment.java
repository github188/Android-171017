package com.mapgis.mmt.common.widget.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListDialogFragment extends DialogFragment {
    private String title;
    private List<String> dataList = null;

    public String getTitle() {
        return title;
    }

    public List<String> getDataList() {
        return dataList;
    }

    public OnListItemClickListener getListItemClickListener() {
        return listItemClickListener;
    }

    private OnListItemClickListener listItemClickListener;

    public ListDialogFragment(String title, List<String> dataList) {
        this.title = title;
        this.dataList = dataList;
    }

    public ListDialogFragment(String title, String[] dataArray) {
        this.title = title;

        if (dataArray != null && dataArray.length > 0) {
            dataList = new ArrayList<>();
            dataList.addAll(Arrays.asList(dataArray));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.listdialog, container, false);

        ((TextView) v.findViewById(R.id.listDialogTitle)).setText(title);

        if (this.dataList != null && this.dataList.size() > 0) {
            ListView listView = (ListView) v.findViewById(R.id.listDialog);

            listView.setAdapter(new ListDialogAdapter());

            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if (listItemClickListener != null) {
                        listItemClickListener.onListItemClick(arg2, dataList.get(arg2));
                    }

                    dismiss();
                }
            });
        }

        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        return v;
    }

    public void setListItemClickListener(OnListItemClickListener listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
    }

    public interface OnListItemClickListener {
        void onListItemClick(int arg2, String value);
    }

    class ListDialogAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getActivity().getLayoutInflater().inflate(R.layout.listdialog_item, parent, false);

            ((TextView) convertView.findViewById(R.id.listDialogItemText)).setText(dataList.get(position));

            return convertView;
        }
    }
}

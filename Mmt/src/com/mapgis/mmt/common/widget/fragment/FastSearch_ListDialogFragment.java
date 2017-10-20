package com.mapgis.mmt.common.widget.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.List;

public class FastSearch_ListDialogFragment extends ListDialogFragment {
    private List<String> bakDataList = new ArrayList<>();

    public FastSearch_ListDialogFragment(String hint, List<String> dataList) {
        super(hint, dataList);
        bakDataList.addAll(dataList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fastsearch_listdialog, container, false);
        final EditText editText = (EditText) v.findViewById(R.id.listDialogEditText);
        editText.setHint(getTitle());

        if (getDataList() != null && getDataList().size() > 0) {
            ListView listView = (ListView) v.findViewById(R.id.listDialog);
            final ListDialogAdapter adapter = new ListDialogAdapter();
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if (getListItemClickListener() != null) {
                        getListItemClickListener().onListItemClick(arg2, getDataList().get(arg2));
                    }

                    dismiss();
                }
            });

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String val = editText.getText().toString();

                    List<String> curDataList = new ArrayList<String>();

                    if (TextUtils.isEmpty(val)) {
                        curDataList.addAll(bakDataList);
                    } else {
                        for (String item : bakDataList) {
                            if (item.contains(val)) {
                                curDataList.add(item);
                            }
                        }
                    }
                    if (curDataList.size() != getDataList().size()) {
                        getDataList().clear();
                        getDataList().addAll(curDataList);
                        adapter.notifyDataSetChanged();
                    }

                }
            });
        }

        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        return v;
    }
}

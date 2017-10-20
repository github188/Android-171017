package com.repair.zhoushan.module.devicecare.consumables.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.repair.zhoushan.module.devicecare.consumables.entity.SAPBean;

import java.util.LinkedList;
import java.util.List;

public class TextAdapter extends ArrayAdapter<SAPBean> {

    private Context mContext;
    private LayoutInflater mInflater;

    private int selectedPos = -1;
    private String selectedText = "";

    private int normalDrawbleId;
    private Drawable selectedDrawble;

    private float textSize;

    private View.OnClickListener onClickListener;
    private OnItemClickListener mOnItemClickListener;

    private List<SAPBean> mListData;

    public TextAdapter(Context context, List<SAPBean> listData, int sId, int nId) {
        super(context, 0, listData);

        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.selectedDrawble = mContext.getResources().getDrawable(sId);
        this.normalDrawbleId = nId;
        this.mListData = listData;

        this.onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPos = (Integer) view.getTag();
                setSelectedPosition(selectedPos);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, selectedPos);
                }
            }
        };
    }

    public TextAdapter(Context context, String[] strArrData, int sId, int nId) {

        super(context, 0);

        LinkedList<SAPBean> listData = new LinkedList<SAPBean>();
        for (String value : strArrData) {
            listData.add(new SAPBean(value, value));
        }

        setNotifyOnChange(false);
        addAll(listData);
        setNotifyOnChange(true);

        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.selectedDrawble = mContext.getResources().getDrawable(sId);
        this.normalDrawbleId = nId;
        this.mListData = listData;

        this.onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPos = (Integer) view.getTag();
                setSelectedPosition(selectedPos);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, selectedPos);
                }
            }
        };

    }

    public void setSelectedPosition(int pos) {

        if (mListData != null && pos < mListData.size()) {
            selectedPos = pos;
            selectedText = mListData.get(pos).getName();
            notifyDataSetChanged();
        }
    }

    public void resetSelectedState() {
        selectedPos = -1;
        selectedText = "";
        notifyDataSetChanged();
    }

    public void setSelectedPositionNoNotify(int pos) {
        selectedPos = pos;
        if (mListData != null && pos < mListData.size()) {
            selectedText = mListData.get(pos).getName();
        }
    }

    public int getSelectedPosition() {
        if (mListData != null && selectedPos < mListData.size()) {
            return selectedPos;
        }

        return -1;
    }

    public void setTextSize(float tSize) {
        textSize = tSize;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView view;
        if (convertView == null) {
            view = (TextView) mInflater.inflate(R.layout.list_tree_choose_item, parent, false);
        } else {
            view = (TextView) convertView;
        }

        view.setTag(position);

        String mString = "";
        if (mListData != null && position < mListData.size()) {
            mString = mListData.get(position).getName();
        }

        if (mString.contains("娑揿秹妾"))
            view.setText("娑揿秹妾");
        else
            view.setText(mString);

        if (textSize != 0) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        }

        if (selectedText != null && selectedText.equals(mString)) {
            view.setBackgroundDrawable(selectedDrawble);
        } else {
            // view.setBackgroundResource(normalDrawbleId);
            view.setBackgroundDrawable(mContext.getResources().getDrawable(normalDrawbleId));
        }

        view.setPadding(20, 0, 0, 0);
        view.setOnClickListener(onClickListener);
        return view;
    }

    // 列表项选中后,回掉外部的接口
    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}


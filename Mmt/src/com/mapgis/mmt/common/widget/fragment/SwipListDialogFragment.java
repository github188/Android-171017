package com.mapgis.mmt.common.widget.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwipListDialogFragment extends DialogFragment {

    private SwipeListView mSwipeListView;
    private SwipeAdapter mAdapter;

    private final String title;
    private final List<String> list;
    private TestBaseSwipeListViewListener swipeListViewListener;
    private OnListItemDeleteClickListener onListItemDeleteClickListener;
    private OnListItemClickListener onListItemClickListener;

    public TestBaseSwipeListViewListener getSwipeListViewListener() {
        return swipeListViewListener;
    }

    public SwipListDialogFragment(String title, List<String> list) {
        this.title = title;
        this.list = new ArrayList<String>(list);
    }

    public SwipListDialogFragment(String title, String[] strs) {
        this.title = title;
        list = new ArrayList<String>(Arrays.asList(strs));
    }

    public void setOnListItemDeleteClickListener(OnListItemDeleteClickListener onListItemDeleteClickListener) {
        this.onListItemDeleteClickListener = onListItemDeleteClickListener;
    }

    public void setOnListItemClickListener(OnListItemClickListener onListItemClickListener) {
        this.onListItemClickListener = onListItemClickListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.swip_list_dialog_main, container, false);

        ((TextView) v.findViewById(R.id.swipListDialogTitle)).setText(title);
        swipeListViewListener = new TestBaseSwipeListViewListener();
        mSwipeListView = (SwipeListView) v.findViewById(R.id.swipListDialogListView);
        mAdapter = new SwipeAdapter(getActivity(), R.layout.swip_list_dialog_item, list, mSwipeListView);
        mSwipeListView.setAdapter(mAdapter);
        mSwipeListView.setSwipeListViewListener(swipeListViewListener);

        //去掉滑动
        mSwipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
        //  mSwipeListView.setSwipeMode(SwipeListView.SWIPE_MODE_NONE);

        mSwipeListView.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
        // mSwipeListView.setSwipeActionRight(settings.getSwipeActionRight());
        mSwipeListView.setOffsetLeft(DimenTool.dip2px(getActivity(), 240));
        // mSwipeListView.setOffsetRight(convertDpToPixel(settings.getSwipeOffsetRight()));
        mSwipeListView.setAnimationTime(0);
        mSwipeListView.setSwipeOpenOnLongPress(false);

        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        return v;
    }

    class TestBaseSwipeListViewListener extends BaseSwipeListViewListener {

        @Override
        public void onClickFrontView(int position) {
            super.onClickFrontView(position);

            if (onListItemClickListener != null) {
                onListItemClickListener.setOnListItemClick(position, list.get(position));
                dismiss();
            }

        }

        @Override
        public void onDismiss(int[] reverseSortedPositions) {
            for (int position : reverseSortedPositions) {
                list.remove(position);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    class SwipeAdapter extends ArrayAdapter<String> {
        private final LayoutInflater mInflater;
        private final SwipeListView mSwipeListView;

        public SwipeAdapter(Context context, int textViewResourceId, List<String> objects, SwipeListView mSwipeListView) {
            super(context, textViewResourceId, objects);
            this.mSwipeListView = mSwipeListView;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.swip_list_dialog_item, parent, false);
                holder = new ViewHolder();
                holder.mFrontText = (TextView) convertView.findViewById(R.id.example_row_tv_title);
                holder.mBackDelete = (ImageButton) convertView.findViewById(R.id.example_row_b_action_2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mBackDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSwipeListView.closeAnimate(position);
                    mSwipeListView.dismiss(position);

                    if (onListItemDeleteClickListener != null) {
                        onListItemDeleteClickListener.setOnListItemDeleteClick(position);
                    }

                }
            });
            String item = getItem(position);
            holder.mFrontText.setText(item);
            return convertView;
        }

        class ViewHolder {
            TextView mFrontText;
            ImageButton mBackDelete;
        }
    }

    public interface OnListItemDeleteClickListener {
        void setOnListItemDeleteClick(int position);
    }

    public interface OnListItemClickListener {
        void setOnListItemClick(int position, String value);
    }

}

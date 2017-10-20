package com.mapgis.mmt.common.widget.customview;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.R;

import java.util.List;

/**
 * 从底部弹出的自定义选择对话框
 *
 * Created by Comclay on 2016/10/8.
 */

public class BottomAlertDialog {

    private Context mContext;

    private ViewGroup contentContainer;
    private ViewGroup decorView;//activity的根View
    private ViewGroup rootView;//AlertView 的 根View
    private ViewGroup loAlertHeader;//窗口headerView

    private TextView mTextViewTitle;
    private TextView mTextViewCancel;

    private String mTitle;
    private String mBottom;
    private List<String> contents;

    private final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM
    );

    private Animation outAnim;
    private Animation inAnim;
    private int gravity = Gravity.CENTER;

    private OnItemClickListener onItemClickListener;

    private BottomAlertDialog() {

    }

    public BottomAlertDialog(Context context,String title,String bottom,List<String> contents) {
        this.mContext = context;
        this.mTitle = title;
        this.mBottom = bottom;
        this.contents = contents;

        initView();
        initData();
        initListener();
    }

    public BottomAlertDialog build(Context context,String title,String bottom,List<String> contents) {
        BottomAlertDialog dialog = new BottomAlertDialog(context,title,bottom,contents);
        initView();
        initData();
        initListener();
        return dialog;
    }


    protected void initView() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        decorView = (ViewGroup) ((Activity) mContext).getWindow().getDecorView().findViewById(android.R.id.content);
        rootView = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview, decorView, false);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        contentContainer = (ViewGroup) rootView.findViewById(R.id.content_container);
        int margin_alert_left_right = 0;
        params.gravity = Gravity.BOTTOM;
        margin_alert_left_right = mContext.getResources().getDimensionPixelSize(R.dimen.margin_actionsheet_left_right);
        params.setMargins(margin_alert_left_right, 0, margin_alert_left_right, margin_alert_left_right);
        contentContainer.setLayoutParams(params);
        gravity = Gravity.BOTTOM;

        mTextViewTitle = (TextView) rootView.findViewById(R.id.tvAlertTitle);
        mTextViewTitle.setText(mTitle);
        mTextViewCancel = (TextView) rootView.findViewById(R.id.tvAlertCancel);
        mTextViewCancel.setText(mBottom);

//        decorView.addView(rootView);
    }

    private void initData() {
        outAnim = AnimationUtils.loadAnimation(mContext,R.anim.bottom_out);

        inAnim = AnimationUtils.loadAnimation(mContext,R.anim.bottom_in);

        initListView();
    }

    private void initListView() {
        ListView alertButtonListView = (ListView) contentContainer.findViewById(R.id.alertButtonListView);
        AlertViewAdapter adapter = new AlertViewAdapter(contents,null);
        alertButtonListView.setAdapter(adapter);
        alertButtonListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                dismiss();
                clickPosition = position;
            }
        });
    }

    private int clickPosition = -1;

    protected void initListener(){
        mTextViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                decorView.removeView(rootView);
                contentContainer.clearAnimation();
                if(onItemClickListener != null)onItemClickListener.onItemClick(BottomAlertDialog.this,clickPosition);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void dismiss() {
        contentContainer.startAnimation(outAnim);
    }

    public void show(){
        decorView.addView(rootView);
        contentContainer.startAnimation(inAnim);
    }

    /**
     * 监听器
     */
    public interface OnItemClickListener{
        void onItemClick(BottomAlertDialog dialog,int position);
    }

    /**
     * 设置监听
     * @param onItemClickListener 监听器
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    class AlertViewAdapter extends BaseAdapter {
        private List<String> mDatas;
        private List<String> mDestructive;
        public AlertViewAdapter(List<String> datas,List<String> destructive){
            this.mDatas =datas;
            this.mDestructive =destructive;
        }
        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String data= mDatas.get(position);
            Holder holder=null;
            View view =convertView;
            if(view==null){
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                view=inflater.inflate(R.layout.item_alertbutton, null);
                holder=creatHolder(view);
                view.setTag(holder);
            }
            else{
                holder=(Holder) view.getTag();
            }
            holder.UpdateUI(parent.getContext(),data,position);
            return view;
        }
        public Holder creatHolder(View view){
            return new Holder(view);
        }
        class Holder {
            private TextView tvAlert;

            public Holder(View view){
                tvAlert = (TextView) view.findViewById(R.id.tvAlert);
            }
            public void UpdateUI(Context context,String data,int position){
                tvAlert.setText(data);
                if (mDestructive!= null && mDestructive.contains(data)){
                    tvAlert.setTextColor(context.getResources().getColor(R.color.textColor_alert_button_destructive));
                }
                else{
                    tvAlert.setTextColor(context.getResources().getColor(R.color.textColor_alert_button_others));
                }
            }
        }
    }
}



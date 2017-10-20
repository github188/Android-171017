package com.mapgis.mmt.module.gis.toolbar.accident.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;

/**
 * Created by KANG on 2016/8/30.
 *
 * ListView 中 带单选按钮的Item
 */
public class ListItemWithRadioView extends RelativeLayout{
    // 上下文菜单
    private Context mContext;
    /*
     * 标记Item是否被选中，默认不被选中
     */
    private boolean isChecked = false;
    /*
     * 描述信息
     */
    private TextView mTVContent;

    /*
     * 显示是否选中的图片
     */
    private ImageView mIVRadioButton;

    /**
     * 定位
     */
    private ImageView ivLocate;

    /**
     * 详情
     */
    private ImageView ivDetail;

    private final int ITEM_CHECKED = R.drawable.user_selected;
    private final int ITEM_UNCHECKED = R.drawable.user_unselected;

    public ListItemWithRadioView(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    public ListItemWithRadioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    public ListItemWithRadioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        View.inflate(mContext, R.layout.item_radio_view,this);
        mTVContent = (TextView) this.findViewById(R.id.tv_content);
        mIVRadioButton = (ImageView) this.findViewById(R.id.iv_radioButton);
        ivLocate = (ImageView) this.findViewById(R.id.iv_locate);
        ivDetail = (ImageView) this.findViewById(R.id.iv_detail);
    }

    /**
     * 定位图标
     * @return
     */
    public ImageView getViewLocate(){
        return ivLocate;
    }

    /**
     * 详情图标
     * @return
     */
    public ImageView getViewDetail(){
        return ivDetail;
    }

    /**
     * 判断当前Item是否被选中
     * @return
     */
    public boolean isChecked(){
        return isChecked;
    }

    /**
     * 设置选中状态
     */
    public void setChecked(boolean checked){
        if (checked && isChecked){
            // 说明两个布尔值相等
            return;
        }
        isChecked = checked;
        if (checked){
            // 设置的状态与当前状态相同
            mIVRadioButton.setBackgroundResource(ITEM_CHECKED);
        }else{
            mIVRadioButton.setBackgroundResource(ITEM_UNCHECKED);
        }
    }

    /**
     * 将状态置为相反
     */
    public void setChecked(){
        setChecked(!isChecked);
    }

    /**
     * 设置描述信息
     * @param desc 描述信息
     */
    public void setContent(String desc){
        mTVContent.setText(desc);
    }
}

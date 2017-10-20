package com.customform.module;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.mapgis.mmt.R;

/**
 * Created by zoro at 2017/9/5.
 */
public class MmtBaseFragment extends Fragment {
    protected ViewGroup eventReportMainForm;
    protected ScrollView mScrollView;

    // 控制软键盘的相关变量
    private View bottomView;
    protected ViewGroup contentFormView;
    private boolean lastStatus = false;
    private boolean isShowKeyboard = false;
    private int dy = 0;
    private boolean isClearOnGlobalLayoutListener = false;

    /**
     * 去除掉对键盘状态的监听
     */
    public void removeOnChangedKeyBoardListener() {
        isClearOnGlobalLayoutListener = true;
    }

    public void addOnChangedKeyBoardListener() {
        isClearOnGlobalLayoutListener = false;
    }

    /**
     * 监听软键盘弹出状态
     */
    protected void setOnChangedKeyBoardListener() {
        bottomView = new View(getActivity());
//        final View rootView = getActivity().findViewById(R.id.base_root_relative_layout);

        mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                if (isClearOnGlobalLayoutListener) {
                    return;
                }

                /*
                 * 1，计算表单布局可见部分变化的大小
                 */

                // 这个获取的是布局在屏幕内部展示的区域
                Rect screenVisibleRect = new Rect();
                mScrollView.getGlobalVisibleRect(screenVisibleRect);

                // 获取用户可见的区域
                Rect userVisibleRect = new Rect();
                mScrollView.getWindowVisibleDisplayFrame(userVisibleRect);
                // 表单数据被隐藏的高度
                int changeHeight = (screenVisibleRect.bottom) - (userVisibleRect.bottom);

                isShowKeyboard = changeHeight > 300;

                // 判断软键盘上次的状态和这次的状态是否都是弹出
                if (lastStatus == isShowKeyboard) {
                    return;
                }

                lastStatus = isShowKeyboard;

                if (bottomView == null) {
                    bottomView = new View(getActivity());
                }

                /*
                 * 判断当前软键盘的弹出状态
                 *      如果弹出，就在表单的底部添加一个布局将整个表单布局顶起来
                 *      如果隐藏软键盘，就将添加的布局去除掉
                 */
                if (isShowKeyboard) {
                    // 软键盘已经弹出来了
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, changeHeight);
                    bottomView.setLayoutParams(params);
                    if (bottomView.getParent() == null) {
                        contentFormView.addView(bottomView);
                        contentFormView.requestLayout();
                    }
                } else {
                    // 软键盘隐藏了
                    contentFormView.removeView(bottomView);
                    mScrollView.smoothScrollBy(0, -dy);
                    dy = 0;
                    return;
                }

                /*
                 * 判断当前拥有焦点的布局是否被软键盘遮挡住：
                 *      如果遮挡住，就要移动mScrollView
                 *      否则，不做处理
                 */
                View itemView = findFoucusItemView();
                // 拥有焦点的Item离mScrollView底部的高度
                int foucusHeight = 0;
                if (itemView != null) {
                    foucusHeight = (eventReportMainForm.getHeight() - itemView.getBottom()) -
                            (eventReportMainForm.getHeight() - mScrollView.getScrollY() - mScrollView.getHeight());
                }

                // 如果布局不是被软键盘所遮挡就不做任何处理，
                if (changeHeight <= foucusHeight) {
                    return;
                }
                // Y方向移动的距离
                dy = changeHeight - foucusHeight;

                System.out.println("bottomView=" + bottomView.getHeight()
                        + " ,  dy=" + dy
                        + " ,  eventReportMainForm=" + eventReportMainForm.getHeight()
                        + " ,  contentFormView=" + contentFormView.getHeight());

                // bottomView的不一定绘制完成了
                if (itemView != null && (eventReportMainForm.getHeight() - itemView.getBottom() >= changeHeight)) {
                    mScrollView.smoothScrollBy(0, dy);
                    return;
                }
                mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        mScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mScrollView.smoothScrollBy(0, dy);
                    }
                });
            }
        });
    }

    /**
     * 得到当前拥有焦点的View
     */
    private View findFoucusItemView() {
        View foucusView = eventReportMainForm.findFocus();

        if (foucusView == null) {
            return null;
        }
        View itemView = null;

        // 当前拥有焦点的布局离父布局底部的高度
        View parentView = foucusView;
        while (parentView != null && parentView.getId() != R.id.eventReportMainForm) {
            itemView = parentView;
            parentView = (View) parentView.getParent();
        }

        return itemView;
    }
}

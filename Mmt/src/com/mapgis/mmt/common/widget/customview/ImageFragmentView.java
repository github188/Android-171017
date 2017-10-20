package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.attach.IUploadFile;

import java.util.HashMap;

/***********************
 * 下载图片后显示 点击按钮,下载图片,然后查看
 ************************/
public class ImageFragmentView extends LinearLayout implements FeedBackView {
    private ImageView imageView;
    private TextView keyTextView;
    private FrameLayout frameLayout;
    private TextView mustView;

    private final FragmentActivity activity;
    private Fragment fragment;

    public static final String ABSOLUTE_KEY_STRING = "FRAGMENT_ABSOLUTE_KEY_STRING";
    public static final String RELATIVE_KEY_STRING = "FRAGMENT_RELATIVE_KEY_STRING";
    public static final String FILENAME_KEY_STRING = "FRAGMENT_FILENAME_KEY_STRING";

    public ImageView getImageView() {
        return imageView;
    }

    public TextView getMustView() {
        return mustView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public ImageFragmentView(FragmentActivity activity) {
        super(activity);
        this.activity = activity;

        initView(activity);
    }

    public ImageFragmentView(Fragment fragment) {
        super(fragment.getActivity());
        this.activity = fragment.getActivity();
        this.fragment = fragment;

        initView(activity);
    }

    private void initView(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        View contentView = LayoutInflater.from(context).inflate(R.layout.view_media_recorder, this);

        this.imageView = (ImageView) contentView.findViewById(R.id.iv_media_icon);
        this.keyTextView = (TextView) contentView.findViewById(R.id.tv_media_name);
        this.mustView = (TextView) contentView.findViewById(R.id.tv_media_required);
        this.frameLayout = (FrameLayout) contentView.findViewById(R.id.fl_media_container);

        frameLayout.setId(Math.abs(frameLayout.hashCode()));
        mustView.setVisibility(View.INVISIBLE);
    }

    public void addContentFragment(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(frameLayout.getId(), fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void setImage(int id) {
        imageView.setImageDrawable(getResources().getDrawable(id));
    }

    @Override
    public void setKey(String str) {
        keyTextView.setText(str);
    }

    public FrameLayout getFrameLayout() {
        return frameLayout;
    }

    @Override
    public String getValue() {
        return null;
    }

    public HashMap<String, String> getKeyValue() {
        HashMap<String, String> hashMap = new HashMap<String, String>();

        Fragment childFragment = activity.getSupportFragmentManager().findFragmentById(frameLayout.getId());
        if (childFragment == null && fragment != null) {
            childFragment = fragment.getChildFragmentManager().findFragmentById(frameLayout.getId());
        }

        String absolute = "";
        String relative = "";
        String names = "";

        if (childFragment instanceof IUploadFile) {  // 附件类型
            names = ((IUploadFile) childFragment).getDatabaseValue();

            absolute = ((IUploadFile) childFragment).getLocalAbsolutePaths();

            relative = ((IUploadFile) childFragment).getServerRelativePaths();
        }

        hashMap.put(ABSOLUTE_KEY_STRING, absolute);
        hashMap.put(RELATIVE_KEY_STRING, relative);
        hashMap.put(FILENAME_KEY_STRING, names);

        return hashMap;
    }

    public void replaceFrameLayout(FragmentManager manager, Fragment fragment) {
        manager.beginTransaction().replace(frameLayout.hashCode(), fragment).commitAllowingStateLoss();
    }

    @Override
    public String getKey() {
        return keyTextView.getText().toString();
    }

    @Override
    public void setValue(String value) {
        // Noop
    }

    public void isMustDo(boolean isMust) {
        mustView.setVisibility(isMust ? View.VISIBLE : View.INVISIBLE);
    }
}

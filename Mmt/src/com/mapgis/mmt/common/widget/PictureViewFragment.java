package com.mapgis.mmt.common.widget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.config.ServerConnectConfig;

import java.io.File;
import java.util.ArrayList;

public class PictureViewFragment extends Fragment {
    private final ArrayList<String> fileList = new ArrayList<>();

    //被删除的照片
    private final ArrayList<String> delList = new ArrayList<>();
    private ViewPager viewPager;
    private ImageView leftRotateButton;
    private ImageView rightRotateButton;

    private ImageButton rightImg;
    private MyAdaper adaper;
    private int pos = 0;

    boolean isCanDel = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.picture_view_layout, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            isCanDel = bundle.getBoolean("canDelete", false);
        }
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setPageMargin(50);
        viewPager.setOffscreenPageLimit(3);
        rightImg = ((BaseActivity) getActivity()).getBaseRightImageView();

        leftRotateButton = (ImageView) view.findViewById(R.id.leftRotate);
        leftRotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageFragment fragment = (ImageFragment) adaper.instantiateItem(viewPager, pos);
                fragment.rotate(-90);
            }
        });

        rightRotateButton = (ImageView) view.findViewById(R.id.rightRotate);
        rightRotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageFragment fragment = (ImageFragment) adaper.instantiateItem(viewPager, pos);
                fragment.rotate(90);
            }
        });

        fileList.addAll(getActivity().getIntent().getStringArrayListExtra("fileList"));
        pos = getActivity().getIntent().getIntExtra("pos", 0);

        adaper = new MyAdaper(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(adaper);
        viewPager.setCurrentItem(pos);
        changeTitle(pos);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                pos = arg0;
                changeTitle(pos);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        return view;
    }

    public void changeTitle(int pos) {
        String fileFullurl = fileList.get(pos);
        //判断是否是可以删除图片
        if (isCanDel) {
            if (BaseClassUtil.isImg(fileFullurl)) {
                ((BaseActivity) getActivity()).getBaseTextView().setText("图片预览");
            }else{
                String fileurl = fileFullurl.substring(fileFullurl.lastIndexOf("/") + 1);
                ((BaseActivity) getActivity()).getBaseTextView().setText(fileurl);
            }
            return;
        }

        if (BaseClassUtil.isImg(fileFullurl)) {
            ((BaseActivity) getActivity()).getBaseTextView().setText("图片预览");
            leftRotateButton.setVisibility(View.VISIBLE);
            rightRotateButton.setVisibility(View.VISIBLE);
            rightImg.setVisibility(View.GONE);
            return;
        }

        String fileurl = fileFullurl.substring(fileFullurl.lastIndexOf("/") + 1);
        ((BaseActivity) getActivity()).getBaseTextView().setText(fileurl);
        leftRotateButton.setVisibility(View.GONE);
        rightRotateButton.setVisibility(View.GONE);
        rightImg.setVisibility(View.VISIBLE);
        rightImg.setImageResource(R.drawable.download_white);

        final String newurl = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath()
                + "/OutFiles/UpLoadFiles"
                + fileFullurl.substring(fileFullurl.indexOf("edia") + 4);
        rightImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(newurl);
                intent.setData(content_url);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        File file = new File(Battle360Util.getFixedPath("Temp"));

        if (!file.exists()) {
            return;
        }

        File[] files = file.listFiles();

        if (files == null || files.length == 0) {
            return;
        }

        for (File f : files) {
            if (f.getName().startsWith("url-temp-"))
                f.delete();
        }
    }

    public class MyAdaper extends FragmentStatePagerAdapter {

        MyAdaper(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return fileList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFragment.newInstance(fileList.get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }


    /**
     * 删除当前图片
     */
    void doDelete() {
        // 当前图片的角标
        int pos = viewPager.getCurrentItem();
        // 当前图片的路径
        String url = fileList.get(pos);

        File file = new File(url);
        if (file.exists()) {
            if (!file.delete()) {
                // Toast.makeText(this,"删除成功", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        delList.add(url);
        fileList.remove(pos);

        if (fileList.size() == 0) {
            doFinish();
            return;
        }

        adaper.notifyDataSetChanged();

        pos = pos - 1;

        viewPager.setCurrentItem(pos < 0 ? 0 : pos);
    }

    void doFinish() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra("fileList", fileList);
        intent.putStringArrayListExtra("delList", delList);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
        MyApplication.getInstance().finishActivityAnimation(getActivity());
    }

}

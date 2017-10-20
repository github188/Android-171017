package com.mapgis.mmt.module.gis.toolbar.mapselect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 书架功能,因风格不符,未使用
 */

public class MapSelectShelfActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addFragment(new MapSelectFragment());

        getBaseTextView().setText("打开地图");
    }

    class MapSelectFragment extends Fragment {

        List<MapFile> mapFileList = new ArrayList<MapSelectShelfActivity.MapFile>();

        private final int columCount = 3;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            File[] mapFiles = getMapFiles();

            if (mapFiles != null) {
                for (File file : mapFiles) {
                    MapFile mapFile = new MapFile();
                    mapFile.fileName = file.getName();
                    mapFile.mapXmlPath = getMapfileXmlPath(file);
                    mapFileList.add(mapFile);
                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return initView(mapFileList);
        }

        private View initView(List<MapFile> mapFileList) {
            LayoutParams rootParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            ScrollView scrollView = new ScrollView(getActivity());
            scrollView.setLayoutParams(rootParams);
            scrollView.setBackgroundResource(R.drawable.book_shelf_bg);

            LinearLayout parentLayout = new LinearLayout(getActivity());
            parentLayout.setLayoutParams(rootParams);
            parentLayout.setOrientation(LinearLayout.VERTICAL);
            parentLayout.setBackgroundResource(R.color.default_light_dark);

            scrollView.addView(parentLayout);

            for (int i = 0; i < mapFileList.size(); i++) {
                if (i % columCount == 0) {
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);

                    LinearLayout linearLayout = new LinearLayout(getActivity());
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    linearLayout.setLayoutParams(layoutParams);
                    linearLayout.setBackgroundResource(R.drawable.bookshelf_layer_center);

                    parentLayout.addView(linearLayout);
                }

                LinearLayout linearLayout = (LinearLayout) parentLayout.getChildAt(parentLayout.getChildCount() - 1);

                View childView = getUnitView(mapFileList.get(i), View.VISIBLE);

                linearLayout.addView(childView);

            }

            if (mapFileList.size() != 0 && mapFileList.size() % columCount != 0) {
                int nullSize = columCount - mapFileList.size() % columCount;
                for (int i = 0; i < nullSize; i++) {
                    LinearLayout linearLayout = (LinearLayout) parentLayout.getChildAt(parentLayout.getChildCount() - 1);

                    View childView = getUnitView(mapFileList.get(0), View.INVISIBLE);

                    linearLayout.addView(childView);
                }
            }

            return scrollView;
        }

        /**
         * 获取单元视图
         *
         * @param mapFile
         * @param visibility
         * @return
         */
        private View getUnitView(final MapFile mapFile, int visibility) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
            layoutParams.weight = 1;
            // layoutParams.gravity = Gravity.CENTER;
            layoutParams.setMargins(0, DimenTool.dip2px(getActivity(), 10), 0, DimenTool.dip2px(getActivity(), 10));

            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setGravity(Gravity.CENTER);

            TextView textView = new TextView(getActivity());
            textView.setLayoutParams(new LayoutParams(DimenTool.dip2px(getActivity(), 90), DimenTool.dip2px(getActivity(), 110)));
            textView.setBackgroundResource(R.drawable.book_bg);
            textView.setPadding(DimenTool.dip2px(getActivity(), 15), DimenTool.dip2px(getActivity(), 15),
                    DimenTool.dip2px(getActivity(), 15), DimenTool.dip2px(getActivity(), 15));
            textView.setTextAppearance(getActivity(), R.style.default_text_medium_1);
            textView.setText(mapFile.fileName);
            linearLayout.addView(textView);

            linearLayout.setVisibility(visibility);

            linearLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(Activity.RESULT_OK, new Intent().putExtra("mapXmlPath", mapFile.mapXmlPath));
                    AppManager.finishActivity(getActivity());
                }
            });

            return linearLayout;
        }

        /**
         * 获取所有地图文件夹
         *
         * @return 所有地图文件夹
         */
        private File[] getMapFiles() {
            File f = new File(MyApplication.getInstance().getMapFilePath());

            if (f.exists()) {
                return f.listFiles();
            }

            return null;
        }

        /**
         * 获取地图文件夹下的xml配置文件
         *
         * @param file 地图文件夹
         * @return xml配置文件
         */
        private String getMapfileXmlPath(File file) {
            File[] files = file.listFiles();

            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".xml")) {
                        return f.getAbsolutePath();
                    }
                }
            }

            return null;

        }
    }

    class MapFile {
        public String fileName;
        public String mapXmlPath;

    }
}

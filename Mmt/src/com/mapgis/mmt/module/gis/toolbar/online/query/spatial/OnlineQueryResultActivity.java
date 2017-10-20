package com.mapgis.mmt.module.gis.toolbar.online.query.spatial;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.constant.Constants;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineGermetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class OnlineQueryResultActivity extends BaseActivity {
    protected List<OnlineFeature> onlineFeatures = new ArrayList<>();
    protected List<OnlineFeature> mPageFeatures = new ArrayList<>();
    protected int currentPage = 1;
    protected int totalPage;
    protected int clickWhichIndex;
    protected int totalRcdNum;
    protected ProgressDialog loadDialog;
    protected MyAdapter mAdapter;

    protected final int[] icons = {R.drawable.icon_marka
            , R.drawable.icon_markb, R.drawable.icon_markc, R.drawable.icon_markd
            , R.drawable.icon_marke, R.drawable.icon_markf, R.drawable.icon_markg
            , R.drawable.icon_markh, R.drawable.icon_marki, R.drawable.icon_markj};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra("dataStr")) {
            onlineFeatures = new Gson().fromJson(getIntent().getStringExtra("dataStr"), new TypeToken<List<OnlineFeature>>() {
            }.getType());
        } else {
            Parcelable[] features = getIntent().getParcelableArrayExtra("data");

            if (features != null && features.length > 0) {
                for (Parcelable p : features) {
                    onlineFeatures.add((OnlineFeature) p);
                }
            }
        }

        currentPage = getIntent().getIntExtra("currentPage", 1);
        clickWhichIndex = getIntent().getIntExtra("clickWhichIndex", -1);

        totalRcdNum = getIntent().getIntExtra(getString(R.string.online_query_totalrcdnum),onlineFeatures.size());
        totalPage = totalRcdNum % Constants.PAGE_ITEM_NUMBER;
        if (totalPage != 0){
            totalPage = totalRcdNum / Constants.PAGE_ITEM_NUMBER + 1;
        }else {
            totalPage = totalRcdNum / Constants.PAGE_ITEM_NUMBER;
        }

        // 防止数据过多时,有短暂的黑屏出现
        final OnlineQueryResultFragment fragment = new OnlineQueryResultFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.baseFragment, fragment);
        ft.show(fragment);
        ft.commit();

        BottomUnitView preButton = new BottomUnitView(this);
        preButton.setContent("上一页");
        preButton.setImageResource(R.drawable.mapview_back);
        addBottomUnitView(preButton, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage - 1 < 1){
                    return;
                }
                currentPage--;
                fragment.queryAndShowResult(currentPage);
            }
        });

        BottomUnitView nextButton = new BottomUnitView(this);
        nextButton.setContent("下一页");
        nextButton.setImageResource(R.drawable.mapview_back_reverse);
        addBottomUnitView(nextButton, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage + 1 > totalPage){
                    return;
                }
                currentPage++;
                fragment.queryAndShowResult(currentPage);
            }
        });

        getBaseLeftImageView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                locateToRefresh(clickWhichIndex);
            }
        });
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        if (arg1 == ResultCode.RESULT_PIPE_LOCATE)
            locateToRefresh(clickWhichIndex);
    }

    /**
     * 返回地图界面并刷新地图
     *
     * @param position 需要标蓝显示的点
     */
    private void locateToRefresh(int position) {
        Intent intent = new Intent();

        intent.putExtra("page", currentPage);
        intent.putExtra("clickWhichIndex", position);
        intent.putExtra("dataStr", new Gson().toJson(onlineFeatures));

        setResult(ResultCode.RESULT_PIPE_LOCATE, intent);

        AppManager.finishActivity(this);
        MyApplication.getInstance().finishActivityAnimation(this);
    }

    class OnlineQueryResultFragment extends Fragment {
        private ListView listView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            listView = new ListView(getActivity());
            listView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            listView.setCacheColorHint(0);
            listView.setBackgroundResource(R.color.white);
            listView.setSelector(R.drawable.item_focus_bg);

            mAdapter = new MyAdapter(mPageFeatures);
            listView.setAdapter(mAdapter);
            return listView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            queryAndShowResult(currentPage);

            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    clickWhichIndex = arg2;

                    Intent intent = new Intent(OnlineQueryResultActivity.this, PipeDetailActivity.class);

                    OnlineFeature onlineFeature = ((MyAdapter) listView.getAdapter()).getItem(clickWhichIndex);

//                    onlineFeature.attributes.put("编号", (!TextUtils.isEmpty(onlineFeature.value) ? onlineFeature.value : onlineFeature.attributes.get("OID")));
                    onlineFeature.attributes.put("$图层名称$", onlineFeature.layerName);
                    onlineFeature.attributes.put("<图层名称>", onlineFeature.layerName);
                    onlineFeature.attributes.put("$geometryType$", onlineFeature.geometryType);

                    intent.putExtra("graphicMap", onlineFeature.attributes);
                    intent.putExtra("graphicMapStr", new Gson().toJson(onlineFeature.attributes));

                    String xy = null;
                    OnlineGermetry geometry = onlineFeature.geometry;
                    if (geometry != null && geometry.x != 0 && geometry.y != 0) {
                        xy = Double.toString(geometry.x) + "," + Double.toString(geometry.y);
                    }
                    intent.putExtra("xy", xy != null ? xy : "");

                    startActivityForResult(intent, 0);
                }
            });
        }

        /**
         * 查询指定页的数据,并显示在列表上
         *
         * @param page 页码
         */
        protected void queryAndShowResult(int page) {
            try {
                getBaseTextView().setText(String.format("当前第%s页/共%s页"
                        , String.valueOf(currentPage)
                        , String.valueOf(totalPage)));

                if (onlineFeatures == null || currentPage < 1 || currentPage > totalPage) {
                    return;
                }

                if (loadDialog == null) {
                    loadDialog = MmtProgressDialog.getLoadingProgressDialog(OnlineQueryResultActivity.this, " 正在查询信息");
                    loadDialog.setCanceledOnTouchOutside(false);
                }
                loadDialog.show();

                mPageFeatures.clear();
                mAdapter.notifyDataSetChanged();

                int startIndex = (page - 1) * Constants.PAGE_ITEM_NUMBER;
                int endIndex = startIndex + Constants.PAGE_ITEM_NUMBER;

                if (endIndex > totalRcdNum) {
                    endIndex = totalRcdNum;
                }

                if (endIndex <= onlineFeatures.size()) {
                    // 不执行异步任务
                    for (int i = startIndex; i < endIndex; i++) {
                        mPageFeatures.add(onlineFeatures.get(i));
                    }
                    mAdapter.notifyDataSetChanged();
                    loadDialog.cancel();
                    return;
                }

                getPageData(startIndex, endIndex);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loadDialog != null && loadDialog.isShowing()) {
            loadDialog.dismiss();
            loadDialog = null;
        }
    }

    protected void getPageData(int startIndex, int endIndex) {

    }

    public class MyAdapter extends BaseAdapter {
        private List<OnlineFeature> myFeatures;

        MyAdapter(List<OnlineFeature> myFeatures) {
            this.myFeatures = myFeatures;
        }

        @Override
        public int getCount() {
            return myFeatures.size();
        }

        @Override
        public OnlineFeature getItem(int position) {
            return myFeatures.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(OnlineQueryResultActivity.this).inflate(R.layout.spatial_search_item, parent, false);

                holder = new ViewHolder();

                holder.item_loc = (ImageView) convertView.findViewById(R.id.ItemImage);
                holder.primaryListTitle = (TextView) convertView.findViewById(R.id.ItemTitle);
                holder.secondListTile = (TextView) convertView.findViewById(R.id.ItemText);
                holder.thirdListTile = (TextView) convertView.findViewById(R.id.ItemDistance);
                holder.item_detail = (ImageView) convertView.findViewById(R.id.BtnDefault);
                holder.position = position;

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            LinkedHashMap<String, String> attributes = myFeatures.get(position).attributes;

            holder.item_loc.setImageResource(icons[position]);

            if (position == clickWhichIndex) {
                clickWhichIndex = -1;
                holder.item_loc.setImageResource(R.drawable.icon_gcoding);
            }

            holder.item_loc.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickWhichIndex = position;
                    locateToRefresh(clickWhichIndex);
                }
            });

            String title = attributes.get("编号");

            if (TextUtils.isEmpty(title))
                title = "编号:-";
            else
                title = "编号:" + title;

            holder.primaryListTitle.setText(title);

            String detail = showInfo(attributes);

            holder.secondListTile.setText(detail);

            holder.secondListTile.setVisibility(View.VISIBLE);

            return convertView;
        }

        class ViewHolder {
            public ImageView item_loc;
            public TextView primaryListTitle;
            public TextView secondListTile;
            public TextView thirdListTile;
            public ImageView item_detail;
            public int position;
        }
    }

    protected String showInfo(HashMap<String, String> map) {
        StringBuilder builder = new StringBuilder();
        for (String key : map.keySet()) {
            if (key.equals("编号"))
                continue;

            // 跳过类似这种自定义的字段 $图层名称$
            if (key.startsWith("$") && key.endsWith("$")) {
                continue;
            }

            // 判断key是否包含中文，如果没有中文不做显示
            boolean isExistChinese = false;

            for (char k : key.toCharArray()) {
                isExistChinese = String.valueOf(k).matches("[\\u4e00-\\u9fa5]+");

                if (isExistChinese) {
                    break;
                }
            }

            if (!isExistChinese) {
                continue;
            }

            String value = map.get(key);

            if (TextUtils.isEmpty(value))
                value = "-";

            builder.append(key).append(":").append(value).append(";");
        }

        return builder.toString();
    }
}

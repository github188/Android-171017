package com.mapgis.mmt.module.gis.spatialquery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.toolbar.query.spatial.SpatialQueryMapMenu;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.featureservice.Feature;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SpatialSearchResultList extends BaseActivity {
    /**
     * 当前第几页
     */
    private int currentPage = 1;

    /**
     * 总的结果数
     */
    private int totalPage;

    private int clickWhichIndex;

    private String layerName;

    int[] icons = {R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc, R.drawable.icon_markd, R.drawable.icon_marke,
            R.drawable.icon_markf, R.drawable.icon_markg, R.drawable.icon_markh, R.drawable.icon_marki, R.drawable.icon_markj};

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.layerName = getIntent().getStringExtra("layerName");

        setCustomView(initMyTitleView());

        progressDialog = MmtProgressDialog.getLoadingProgressDialog(this, "正在查询数据，请稍等...");

        final SpatialSearchResultListFragment fragment = new SpatialSearchResultListFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.baseFragment, fragment);
        ft.show(fragment);
        ft.commit();

        BottomUnitView preButton = new BottomUnitView(SpatialSearchResultList.this);
        preButton.setContent("上一页");
        preButton.setImageResource(R.drawable.mapview_back);
        addBottomUnitView(preButton, new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage = --currentPage <= 1 ? 1 : currentPage;
                fragment.query(currentPage);
            }
        });

        BottomUnitView nextButton = new BottomUnitView(SpatialSearchResultList.this);
        nextButton.setContent("下一页");
        nextButton.setImageResource(R.drawable.mapview_back_reverse);
        addBottomUnitView(nextButton, new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage = ++currentPage >= totalPage ? totalPage : currentPage;
                fragment.query(currentPage);
            }
        });

    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        switch (arg1) {
            case ResultCode.RESULT_PIPE_LOCATE:
                locateToRefreash(currentPage, clickWhichIndex, true);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        locateToRefreash(currentPage, clickWhichIndex, false);
    }

    /**
     * 返回地图界面并刷新地图
     *
     * @param pageIndex 当前第几页
     * @param position  需要标蓝显示的点
     */
    private void locateToRefreash(int pageIndex, int position, boolean isLocate) {
        Intent intent = new Intent();
        intent.putExtra("page", currentPage);
        intent.putExtra("clickWhichIndex", position);

        int resultCode = isLocate ? ResultCode.RESULT_PIPE_LOCATE : ResultCode.RESULT_PIPE_REFREASH;
        setResult(resultCode, intent);

        super.onBackPressed();
    }

    /**
     * 自定义标题视图
     *
     * @return
     */
    private View initMyTitleView() {
        View view = LayoutInflater.from(SpatialSearchResultList.this).inflate(R.layout.header_bar_plan_name, null);

        try {
            // 返回按钮
            view.findViewById(R.id.tvPlanBack).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            // 显示图层名称
            ((TextView) view.findViewById(R.id.tvPlanName)).setText("\"" + this.layerName + "\"的查询结果");

            ((TextView) view.findViewById(R.id.tvTaskState)).setText("当前第" + currentPage + "页  共" + totalPage + "页");

            view.findViewById(R.id.ivPlanDetail).setVisibility(View.INVISIBLE);

            view.setBackgroundResource(AppStyle.getActionBarStyleResource());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return view;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 属性查询结果，实体片段
     */
    // //////////////////////////////////////////////////////////////////////////////////////////////
    class SpatialSearchResultListFragment extends Fragment {

        private final List<Feature> featureSet = new ArrayList<Feature>();
        private String field;

        private PullToRefreshListView mPullRefreshListView;
        private final ESSpatialSearchResultAdapter adapter = new ESSpatialSearchResultAdapter();

        private FeaturePagedResult featurePagedResult;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            try {
                field = LayerConfig.getInstance().getConfigInfo(layerName).HighlightField;

                mPullRefreshListView = new PullToRefreshListView(getActivity());
                mPullRefreshListView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                ListView actualListView = mPullRefreshListView.getRefreshableView();
                registerForContextMenu(actualListView);
                actualListView.setAdapter(adapter);
                mPullRefreshListView.setMode(Mode.DISABLED);
                mPullRefreshListView.setOnItemClickListener(onItemClickListener);
                mPullRefreshListView.setBackgroundResource(R.color.white);

                featurePagedResult = SpatialQueryMapMenu.featurePagedResult;

                clickWhichIndex = getActivity().getIntent().getIntExtra("clickWhichIndex", -1);
                currentPage = getActivity().getIntent().getIntExtra("page", 1);

                totalPage = featurePagedResult.getPageCount();

                query(currentPage, clickWhichIndex);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return mPullRefreshListView;
        }

        public void query(int pageIndex) {
            query(pageIndex, -1);
        }

        private void query(int pageIndex, int clickedIndex) {
            clickWhichIndex = clickedIndex;
            new UpdateTask().executeOnExecutor(MyApplication.executorService, pageIndex);
        }

        /**
         * ListView每一项的点击事件
         */
        OnItemClickListener onItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                clickWhichIndex = arg2 - 1;

                Intent intent = new Intent(SpatialSearchResultList.this, PipeDetailActivity.class);

                Graphic itemClickGraphic = ((Feature) arg0.getItemAtPosition(arg2)).toGraphics(true).get(0);

                HashMap<String, String> graphicMap = new LinkedHashMap<>();

                for (int m = 0; m < itemClickGraphic.getAttributeNum(); m++) {
                    graphicMap.put(itemClickGraphic.getAttributeName(m), itemClickGraphic.getAttributeValue(m));
                }

                intent.putExtra("xy", itemClickGraphic.getCenterPoint().toString());
                intent.putExtra("graphicMap", graphicMap);
                intent.putExtra("graphicMapStr", new Gson().toJson(graphicMap));
                intent.putExtra("layerName", SpatialSearchResultList.this.layerName);

                startActivityForResult(intent, 0);
            }
        };

        /**
         * 刷新下一页数据，累加显示
         */
        private class UpdateTask extends AsyncTask<Integer, String, String> {

            @Override
            protected void onPreExecute() {
                progressDialog.show();
                SpatialSearchResultList.this.findViewById(R.id.baseBottomView).setEnabled(false);
            }

            @Override
            protected String doInBackground(Integer... params) {
                int page = params[0];

                featureSet.clear();
                featureSet.addAll(featurePagedResult.getPage(page));

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    adapter.notifyDataSetChanged();

                    ((TextView) getCustomView().findViewById(R.id.tvTaskState)).setText("当前第" + currentPage + "页  共" + totalPage + "页");

                    if (currentPage == totalPage) {
                        mPullRefreshListView.setMode(Mode.DISABLED);
                        Toast.makeText(getActivity(), "数据已加载完毕!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    progressDialog.dismiss();
                    SpatialSearchResultList.this.findViewById(R.id.baseBottomView).setEnabled(true);
                }
            }
        }

        private class ESSpatialSearchResultAdapter extends BaseAdapter {
            @Override
            public int getCount() {
                return featureSet.size();
            }

            @Override
            public Object getItem(int position) {
                return featureSet.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                ViewHolder holder = null;
                if (convertView == null) {
                    convertView = LayoutInflater.from(getActivity()).inflate(R.layout.spatial_search_item, null);
                    holder = new ViewHolder();
                    holder.item_loc = (ImageView) convertView.findViewById(R.id.ItemImage);
                    holder.primaryListTitle = (TextView) convertView.findViewById(R.id.ItemTitle);
                    holder.secondListTile = (TextView) convertView.findViewById(R.id.ItemText);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                final Graphic graphic = featureSet.get(position).toGraphics(true).get(0);

                if (position > 9) {
                    holder.item_loc.setImageResource(R.drawable.icon_lcoding);
                } else {
                    holder.item_loc.setImageResource(icons[position]);
                }

                if (clickWhichIndex != -1 && position == clickWhichIndex) {
                    holder.item_loc.setImageResource(R.drawable.icon_gcoding);
                }

                holder.item_loc.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        locateToRefreash(currentPage, position, true);
                    }
                });


                holder.primaryListTitle.setText(BaseClassUtil.isNullOrEmptyString(field) ? layerName : graphic
                        .getAttributeValue(field));
                holder.secondListTile.setText(getInfo(graphic));
                holder.secondListTile.setVisibility(View.VISIBLE);

                return convertView;
            }

            private String getInfo(Graphic graphic) {
                StringBuilder builder = new StringBuilder();

                for (int i = 0; i < graphic.getAttributeNum(); i++) {
                    String key = graphic.getAttributeName(i);
                    String value = graphic.getAttributeValue(i);

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

                    builder.append(key);
                    builder.append(":");
                    builder.append(value);
                    builder.append(";");
                }

                return builder.toString();
            }

            class ViewHolder {
                public ImageView item_loc;
                public TextView primaryListTitle;
                public TextView secondListTile;
            }
        }
    }
}
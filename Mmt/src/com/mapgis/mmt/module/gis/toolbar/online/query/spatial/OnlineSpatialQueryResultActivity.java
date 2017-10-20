package com.mapgis.mmt.module.gis.toolbar.online.query.spatial;

import android.os.Bundle;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineQueryResult;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineSpatialQueryTask;
import com.zondy.mapgis.geometry.Rect;
import java.util.Collections;

public class OnlineSpatialQueryResultActivity extends OnlineQueryResultActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void getPageData(int startIndex, int endIndex) {
        new OnlineSpatialQueryTask(MyApplication.getInstance().mapGISFrame
                , (Rect) getIntent().getSerializableExtra(getResources().getString(R.string.online_query_rect))
                , getIntent().getStringExtra(getResources().getString(R.string.online_query_objectids))
                , String.format("mid:%d,%d", startIndex + 1, endIndex)){
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                OnlineSpatialQueryResultActivity.this.loadDialog.cancel();
            }

            @Override
            protected void onTaskDone(OnlineQueryResult data) {
                // 将查询到的当前页的数据拷贝到集合中
                Collections.addAll(onlineFeatures, data.features);
                Collections.addAll(mPageFeatures, data.features);
                mAdapter.notifyDataSetChanged();
                OnlineSpatialQueryResultActivity.this.loadDialog.cancel();
            }
        }.executeOnExecutor(MyApplication.executorService);
    }
}

package com.mapgis.mmt.module.gis.toolbar.online.query.spatial;

import android.os.Bundle;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineQueryResult;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineWhereQueryTask;

import java.util.Collections;

public class OnlineWhereQueryResultActivity extends OnlineQueryResultActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void getPageData(int startIndex, int endIndex) {
        new OnlineWhereQueryTask(MyApplication.getInstance().mapGISFrame
                , getIntent().getStringExtra("layerName")
                , getIntent().getStringExtra("where")
                , String.format("mid:%d,%d", startIndex + 1, endIndex)){

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                OnlineWhereQueryResultActivity.this.loadDialog.cancel();
            }

            @Override
            protected void onTaskDone(OnlineQueryResult data) {
                // 将查询到的当前页的数据拷贝到集合中
                Collections.addAll(onlineFeatures, data.features);

                Collections.addAll(mPageFeatures, data.features);
                mAdapter.notifyDataSetChanged();
                OnlineWhereQueryResultActivity.this.loadDialog.cancel();
            }
        }.executeOnExecutor(MyApplication.executorService);
    }
}

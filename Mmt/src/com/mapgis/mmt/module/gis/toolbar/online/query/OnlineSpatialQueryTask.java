package com.mapgis.mmt.module.gis.toolbar.online.query;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.geometry.Rect;

public abstract class OnlineSpatialQueryTask extends AsyncTask<String, String, String> {
	private final MapGISFrame mapGISFrame;

	private final String geometry;

	private final String inSR = "1";
	private final String geometryType = "esriGeometryEnvelope";
	private final String spatialRel = "esriSpatialRelIntersects";
	private final String returnGeometry = "true";
	private final String returnIdsOnly = "false";
	private final String outFields = "*";
	private final String objectIds;
	private String paging = "all";
	private final String f = "json";

	public OnlineSpatialQueryTask(MapGISFrame mapGISFrame, Rect rect, String objectIds) {
		this.mapGISFrame = mapGISFrame;
		this.objectIds = objectIds;
		this.geometry = "{\"xmin\":" + rect.getXMin() + ",\"xmax\":" + rect.getXMax() + ",\"ymin\":" + rect.getYMin()
				+ ",\"ymax\":" + rect.getYMax() + ",\"spatialReference\":{\"wkid\":1}}";
	}

	public OnlineSpatialQueryTask(MapGISFrame mapGISFrame, Rect rect, String objectIds,String blockPage) {
		this.mapGISFrame = mapGISFrame;
		this.objectIds = objectIds;
		this.geometry = "{\"xmin\":" + rect.getXMin() + ",\"xmax\":" + rect.getXMax() + ",\"ymin\":" + rect.getYMin()
				+ ",\"ymax\":" + rect.getYMax() + ",\"spatialReference\":{\"wkid\":1}}";
		this.paging = blockPage;
	}

	protected ProgressDialog loadDialog;

	@Override
	protected void onPreExecute() {
		loadDialog = MmtProgressDialog.getLoadingProgressDialog(mapGISFrame, " 正在查询信息");
		loadDialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		String result = NetUtil.executeHttpGet(OnlineQueryService.getOnlineQueryService(objectIds), "geometry", geometry, "inSR",
				inSR, "geometryType", geometryType, "spatialRel", spatialRel, "returnGeometry", returnGeometry, "returnIdsOnly",
				returnIdsOnly, "outFields", outFields, "objectIds", objectIds, "paging", paging, "f", f);
		return result;
	}

	@Override
	protected void onPostExecute(String result) {

		try {
			if (result == null || result.length() == 0) {
				mapGISFrame.showToast("未查询到信息!");
				return;
			}

			result = result.replace("\\", "");

			String[] attArrTemp = result.split("attributes");

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			OnlineQueryResult data = null;

			if (attArrTemp.length > 1 && attArrTemp[1].indexOf("\"ID") == attArrTemp[1].lastIndexOf("\"ID")) {
				data = gson.fromJson(result, OnlineQueryResult.class);
			} else {
				result = "";
				for (int i = 0; i < attArrTemp.length; i++) {
					attArrTemp[i] = attArrTemp[i].replaceFirst("\"ID", "\"DUMPLICATE_ID");
					result += attArrTemp[i] + "attributes";
				}
				result = result.substring(0, result.lastIndexOf("attributes"));
				data = gson.fromJson(result, OnlineQueryResult.class);

			}

			if (data == null || data.features.length == 0) {
				mapGISFrame.showToast("未查询到信息!");
			} else {
				onTaskDone(data);
			}
		} catch (Exception e) {

			mapGISFrame.showToast("查询结果异常:" + e.toString());

			e.printStackTrace();

		} finally {
			loadDialog.cancel();
		}

	}

	protected abstract void onTaskDone(OnlineQueryResult data);

}

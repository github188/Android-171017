package com.mapgis.mmt.module.relevance;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.PictureViewActivity;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;

import net.tsz.afinal.FinalBitmap;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class RelevanceExplorerActivity extends BaseActivity {
	private String layerName;
	private String fldName;
	private String GUID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getBaseTextView().setText("多媒体查看");

		layerName = getIntent().getStringExtra("layerName");
		fldName = getIntent().getStringExtra("fieldName");
		GUID = getIntent().getStringExtra("deviceKey");

		addFragment(new RelevanceExplorerFragment());
	}

	class RelevanceExplorerFragment extends Fragment {
		private LinearLayout parentLayout;
		private FinalBitmap fb;

		/** 大图的URL路径 */
		private final ArrayList<String> paths = new ArrayList<String>();

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			ScrollView scrollView = new ScrollView(getActivity());
			scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			scrollView.setBackgroundResource(R.color.white);

			parentLayout = new LinearLayout(getActivity());
			parentLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			parentLayout.setOrientation(LinearLayout.VERTICAL);

			scrollView.addView(parentLayout);
			return scrollView;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			fb = FinalBitmap.create(getActivity());
			// fb.configBitmapMaxHeight(200);
			// fb.configBitmapMaxWidth(200);
			fb.configBitmapLoadThreadSize(2);
			fb.configDiskCachePath(Battle360Util.getFixedPath("Temp"));
			fb.configLoadfailImage(R.drawable.load_error);

			List<MyPhoto> myPhotos = new ArrayList<MyPhoto>();

			createView(myPhotos);

			new GetMediaListTask().executeOnExecutor(MyApplication.executorService, layerName, GUID, fldName);
		}

		private void createView(final List<MyPhoto> mediaPaths) {
			// 计算每排多少张照片
			@SuppressWarnings("deprecation")
			int unit = getWindowManager().getDefaultDisplay().getWidth() / 200;

			LinearLayout layout = null;

			for (int i = 0; i < mediaPaths.size(); i++) {

				if (i % unit == 0) {
					layout = addLinearLayout(unit);
					parentLayout.addView(layout);
				}

				// paths.add("http://" +
				// ServerConnectConfig.getInstance().getServerConfigInfo().IpAddress
				// + "/"
				// + mediaPaths.get(i).OriginalPhoto);

				paths.add(ServerConnectConfig.getInstance().getHostPath() + "/" + mediaPaths.get(i).OriginalPhoto);

				ImageView imageView = (ImageView) layout.getChildAt(i % unit);
				imageView.setVisibility(View.VISIBLE);
				imageView.setTag(i);
				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						showOriginalPhoto(v);
					}
				});

				fb.display(imageView, ServerConnectConfig.getInstance().getHostPath() + "/" + mediaPaths.get(i).MiniPhoto);
			}

			if (mediaPaths.size() > 0) {// 退出界面时,删除WEB端的缓存文件
				getBaseLeftImageView().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String filePath = mediaPaths.get(0).OriginalPhoto;
						String path = filePath.substring(0, filePath.lastIndexOf("/"));

						AppManager.finishActivity(getActivity());
						MyApplication.getInstance().finishActivityAnimation(getActivity());

						new DeleteTask().execute(path);
					}
				});
			}
		}

		private LinearLayout addLinearLayout(int count) {
			LinearLayout layout = new LinearLayout(getActivity());
			layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			layout.setOrientation(LinearLayout.HORIZONTAL);

			for (int i = 0; i < count; i++) {
				ImageView imageView = new ImageView(getActivity());
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, DimenTool.dip2px(getActivity(), 100));
				layoutParams.weight = 1;
				imageView.setPadding(DimenTool.dip2px(getActivity(), 4), DimenTool.dip2px(getActivity(), 4),
						DimenTool.dip2px(getActivity(), 4), DimenTool.dip2px(getActivity(), 4));
				imageView.setLayoutParams(layoutParams);
				// imageView.setBackgroundResource(R.drawable.map_pager_bg_rect);
				imageView.setVisibility(View.INVISIBLE);

				layout.addView(imageView);
			}

			return layout;
		}

		private void showOriginalPhoto(View v) {
			int pos = (Integer) v.getTag();
			Intent intent = new Intent(getActivity(), PictureViewActivity.class);
			intent.putStringArrayListExtra("fileList", paths);
			intent.putExtra("pos", pos);
			intent.putExtra("canDelete", false);
			startActivity(intent);
		}

		@Override
		public void onPause() {
			super.onPause();

			fb.pauseWork(true);
		}

		@Override
		public void onResume() {
			super.onResume();

			fb.pauseWork(false);
		}

		@Override
		public void onDestroy() {
			fb.clearCache();
			fb.exitTasksEarly(false);

			super.onDestroy();
		}

		class GetMediaListTask extends AsyncTask<String, Void, String> {
			@Override
			protected void onPreExecute() {
				setBaseProgressBarVisibility(true);
			}

			@Override
			protected String doInBackground(String... params) {
				String url = ServerConnectConfig.getInstance().getBaseServerPath()
						+ "/Services/Zondy_MapGISCitySvr_Media/REST/MediaREST.svc/"
						+ MobileConfig.MapConfigInstance.VectorService + "/MediaServer/QueryMobileMediaFile";

				String resultStr = NetUtil.executeHttpGet(url, "layerName", params[0], "elemGuid", params[1], "fldName",
						params[2]);

				return resultStr;
			}

			@Override
			protected void onPostExecute(String result) {
				try {
					if (BaseClassUtil.isNullOrEmptyString(result)) {
						showErrorMsg("获取服务失败,未返回结果");
						return;
					}

					ResultData<String> resultData = new Gson().fromJson(result, new TypeToken<ResultData<String>>() {
					}.getType());

					if (result == null) {
						showErrorMsg("解析返回数据失败");
						return;
					}

					if (resultData.ResultCode < 0) {
						showErrorMsg("未查询到该设备关联的多媒体信息");
						return;
					}

					List<MyPhoto> myPhotos = new ArrayList<MyPhoto>();

					for (String paths : resultData.DataList) {
						myPhotos.add(new MyPhoto(paths));
					}

					createView(myPhotos);

				} catch (Exception e) {
					showErrorMsg("异常: " + e.getMessage());
					e.printStackTrace();
				} finally {
					setBaseProgressBarVisibility(false);
				}
			}
		}
	}

	class DeleteTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			String url = ServerConnectConfig.getInstance().getBaseServerPath()
					+ "/Services/Zondy_MapGISCitySvr_Media/REST/MediaREST.svc/" + MobileConfig.MapConfigInstance.VectorService
					+ "/MediaServer/DeleteMobileMediaTempDir";
			NetUtil.executeHttpGet(url, "dirPath", params[0]);
			return null;
		}
	}

	class MyPhoto {
		public String OriginalPhoto;
		public String MiniPhoto;

		public MyPhoto() {
		}

		public MyPhoto(String resultPath) {
			OriginalPhoto = "";
			MiniPhoto = "";

			try {
				resultPath = resultPath.replace("\\", "/");

				String[] str = resultPath.split("\\|");

				OriginalPhoto = str[0];

				int i = OriginalPhoto.lastIndexOf("/");
				String name = OriginalPhoto.substring(i + 1, OriginalPhoto.length());

				MiniPhoto = OriginalPhoto.replace(name, URLEncoder.encode(str[1], "utf-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}

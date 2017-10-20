package com.mapgis.mmt.module.gis.toolbar.mapselect;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MobileConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapSelectActivity extends Activity {

	private ListView listView;

	private final List<MapFile> mapFileList = new ArrayList<MapSelectActivity.MapFile>();

	private MyAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AppManager.addActivity(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.listdialog);

		initMapFile();

		adapter = new MyAdapter(mapFileList, MapSelectActivity.this);

		((TextView) findViewById(R.id.listDialogTitle)).setText("打开地图");

		listView = (ListView) findViewById(R.id.listDialog);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				MapFile mapFile = (MapFile) arg0.getItemAtPosition(arg2);

				String emsName = MobileConfig.MapConfigInstance.getMapType(MapConfig.MOBILE_EMS).Name;

				if (BaseClassUtil.isNullOrEmptyString(mapFile.mapXmlPath)) {
					Toast.makeText(MapSelectActivity.this, "选择的地图文档不包含有效的地图数据", Toast.LENGTH_SHORT).show();
					return;
				} else if (mapFile.fileName.equals(emsName)) {
					Toast.makeText(MapSelectActivity.this, "选择的地图文档与当前文档一致", Toast.LENGTH_SHORT).show();
					return;
				} else if (!BaseClassUtil.isNullOrEmptyString(emsName)) {
					Toast.makeText(MapSelectActivity.this, "热切换地图文档可能会失败，失败后请重新打开程序即可", Toast.LENGTH_SHORT).show();
				}

				adapter.notifyDataSetChanged();

				MobileConfig.MapConfigInstance.getMapType(MapConfig.MOBILE_EMS).Name = mapFile.fileName;
				MyApplication.getInstance().getSystemSharedPreferences().edit().putString("地图文档名称", mapFile.fileName).commit();

				setResult(Activity.RESULT_OK);

				AppManager.finishActivity(MapSelectActivity.this);
			}
		});
	}

	private void initMapFile() {
		List<File> mapFiles = getMapFiles();

		if (mapFiles != null) {

			String name = MobileConfig.MapConfigInstance.getMapType(MapConfig.MOBILE_EMS).Name;

			for (File file : mapFiles) {
				MapFile mapFile = new MapFile();
				mapFile.fileName = file.getName();
				mapFile.mapXmlPath = getMapfileXmlPath(file);

                mapFile.isOpen = name != null && name.length() != 0 && name.equals(mapFile.fileName);

				mapFileList.add(mapFile);
			}
		}
	}

	/**
	 * 获取所有地图文件夹
	 * 
	 * @return 所有地图文件夹
	 */
	private List<File> getMapFiles() {
		List<File> mapxFile = new ArrayList<File>();

		String rootPath = MyApplication.getInstance().getMapFilePath();

		File mapsDir = new File(rootPath);

		if (mapsDir.exists()) {
			// map文件夹下的文件
			File[] mapDir = mapsDir.listFiles();

			for (File file : mapDir) {
				if (file.isDirectory()) {
					// 某一地图文件夹下的文件
					File[] files = file.listFiles();

					for (File f : files) {
						if (f.getAbsolutePath().endsWith(".mapx"))
							mapxFile.add(f.getParentFile());
					}
				}
			}
		}
		return mapxFile;
	}

	/**
	 * 获取地图文件夹下的xml配置文件
	 * 
	 * @param file
	 *            地图文件夹
	 * @return xml配置文件
	 */
	private String getMapfileXmlPath(File file) {
		File[] files = file.listFiles();

		if (files != null) {
			for (File f : files) {
				if (f.getName().endsWith(".mapx") || f.getName().endsWith(".xml")) {
					return f.getAbsolutePath();
				}
			}
		}

		return null;
	}

	class MyAdapter extends BaseAdapter {

		private final List<MapFile> mapFiles;
		private final Context context;

		public MyAdapter(List<MapFile> mapFiles, Context context) {
			this.mapFiles = mapFiles;
			this.context = context;
		}

		@Override
		public int getCount() {
			return mapFiles.size();
		}

		@Override
		public Object getItem(int position) {
			return mapFiles.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			LinearLayout linearLayout = new LinearLayout(context);
			linearLayout.setLayoutParams(params);
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			linearLayout.setGravity(Gravity.CENTER);

			LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			cParams.setMargins(DimenTool.dip2px(context, 10), DimenTool.dip2px(context, 8), DimenTool.dip2px(context, 10),
					DimenTool.dip2px(context, 8));
			cParams.gravity = Gravity.CENTER;

			ImageView imageView = new ImageView(context);
			imageView.setLayoutParams(cParams);
			imageView.setImageResource(R.drawable.undo);

			TextView textView = new TextView(context);
			textView.setLayoutParams(cParams);
			textView.setTextAppearance(context, R.style.default_text_medium_1);
			textView.setText(((MapFile) getItem(position)).fileName);

			boolean isOpen = ((MapFile) getItem(position)).isOpen;
			if (isOpen) {
				imageView.setVisibility(View.VISIBLE);
			} else {
				imageView.setVisibility(View.INVISIBLE);
			}

			linearLayout.addView(imageView);
			linearLayout.addView(textView);

			return linearLayout;
		}
	}

	class MapFile {
		public String fileName;
		public String mapXmlPath;
		public boolean isOpen;
	}
}

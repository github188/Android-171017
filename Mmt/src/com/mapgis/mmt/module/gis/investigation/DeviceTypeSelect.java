package com.mapgis.mmt.module.gis.investigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceTypeSelect extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.list_investigation);

		ListView layer_select_listview = (ListView) findViewById(R.id.select_listview);
		
		List<String> deviceTypeList = new ArrayList<String>();
		deviceTypeList.add("属性编辑");
		deviceTypeList.add("点设备普查");
		deviceTypeList.add("线设备普查");

		DeviceTypeAdapter adapter = new DeviceTypeAdapter(getBaseContext(), (ArrayList<String>)deviceTypeList);
		layer_select_listview.setAdapter(adapter);

		layer_select_listview.setOnItemClickListener(onItemClickListener);
	}

	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Intent intent = new Intent();
			intent.putExtra("deviceType", (String) arg0.getItemAtPosition(arg2));
			setResult(DeviceInvestigationMapMenu.RESULT_DEVICE_TYPE_SELECTED, intent);
			finish();
		}
	};
	
	public class DeviceTypeAdapter extends BaseAdapter {
		
		private LayoutInflater mInflater;
		private ArrayList<String> deviceTypeList;

		public DeviceTypeAdapter(Context context, ArrayList<String> deviceTypeList) {
			mInflater = LayoutInflater.from(context);
			this.deviceTypeList = deviceTypeList;
		}
		
		public void addItem(String layer) {
			deviceTypeList.add(layer);
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return deviceTypeList.size();
		}

		@Override
		public Object getItem(int position) {
			return deviceTypeList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.layer_list_item, null);

				holder = new ViewHolder();
				holder.listItem = (LinearLayout) convertView.findViewById(R.id.layer_listItem);
				holder.leftImage = (ImageView) convertView.findViewById(R.id.left_image);
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.rightImage = (ImageView) convertView.findViewById(R.id.right_image);

				holder.position = position;
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.leftImage.setBackgroundResource(R.drawable.ic_feature_layer);
			holder.text.setText(deviceTypeList.get(position));

			return convertView;
		}
		
		class ViewHolder{
			public LinearLayout listItem;
			public ImageView leftImage;
			public TextView text;
			public ImageView rightImage;
			public int position;
		}
	}
	
}

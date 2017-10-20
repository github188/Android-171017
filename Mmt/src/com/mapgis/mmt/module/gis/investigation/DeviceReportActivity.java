package com.mapgis.mmt.module.gis.investigation;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.net.BaseStringTask;
import com.mapgis.mmt.net.BaseTaskListener;
import com.mapgis.mmt.net.BaseTaskParameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DeviceReportActivity extends FragmentActivity {
	private final int result = 1;
	private final int none = 2;
	private final int getRoleName = 12;

	private ButtonToolbarFragment attEdtToolFrgmt;
	private int deviceType; // 1点设备上报，　２线设备上报
	private String dotStr; // 点设备 坐标 字符串
	private String dotList; // 线设备 坐标List ，格式 [ x1, y1]

	private LinearLayout rootLayout;
	private RelativeLayout deviceTypeLayout;
	private TextView deviceTypeTV;
	private ImageButton deviceTypeBtn;

	private LinkedList<View> attViewList;// 保存 属性 布局数组， 以便 上报时 获取 填写 的 属性 值
	private ProgressDialog progressDialog;

	private final ButtonToolbarFragment.OnBtnClickListener onDeviceRepotBtnClick = new ButtonToolbarFragment.OnBtnClickListener() {
		@Override
		public void onClick() {
			// MyApplication.getInstance().submitExecutorService(getAuditRoleNameRunable);
			reportPointDevice("");
		}
	};

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.investigation_report);

		deviceType = getIntent().getIntExtra("DeviceType", -1);

		((TextView) findViewById(R.id.textview_Title)).setText("设备普查");

		progressDialog = new ProgressDialog(DeviceReportActivity.this);
		progressDialog.setTitle("属性编辑");
		progressDialog.setMessage("正在上报属性编辑");

		findViewById(R.id.detail_revert_btn).setOnClickListener(revertOnClickListener);
		findViewById(R.id.detail_loc_btn).setOnClickListener(locOnClickListener);

		attEdtToolFrgmt = new ButtonToolbarFragment("上报设备");
		attEdtToolFrgmt.setSaveBtnClickListener(onDeviceRepotBtnClick);
		getSupportFragmentManager().beginTransaction().replace(R.id.frag_pipe_detail_toolbar, attEdtToolFrgmt).commit();

		if (deviceType == 1) {
			dotStr = getIntent().getStringExtra("dot");
			initPointDeviceLayout();
		} else if (deviceType == 2) {
			dotList = getIntent().getStringExtra("dotList");
			initPointDeviceLayout();
		}
	}

	/**
	 * 返回地图
	 */
	OnClickListener revertOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};

	/**
	 * 管件定位
	 */
	OnClickListener locOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setResult(ResultCode.RESULT_PIPE_LOCATE);
			finish();
		}
	};

	/**
	 * 创建 点设备 上报 的布局
	 */
	private void initPointDeviceLayout() {
		rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
		deviceTypeLayout = (RelativeLayout) findViewById(R.id.deviceTypeLayout);
		deviceTypeTV = (TextView) findViewById(R.id.deviceType);
		deviceTypeBtn = (ImageButton) findViewById(R.id.deviceTypeBtn);

		if (deviceType == 1) {
			// MyApplication.getInstance().putConfigValue("pointDeviceTypes",
			// "点设备1,点设备2,点设备3");
			final String[] deviceTypes = MyApplication.getInstance().getConfigValue("pointDeviceTypes").split(",");
			if (deviceTypes.length > 0)
				deviceTypeTV.setText(deviceTypes[0]);
			/************************************** 选择 设备类型 的按钮点击事件 *********************************************/
			deviceTypeBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ListDialogFragment fragment = new ListDialogFragment("设备类型", Arrays.asList(deviceTypes));

					fragment.show(DeviceReportActivity.this.getSupportFragmentManager(), "1");

					fragment.setListItemClickListener(new OnListItemClickListener() {
						@Override
						public void onListItemClick(int arg2, String value) {
							deviceTypeTV.setText(value);
						}
					});
				}
			});
			// 加载 点设备 属性填写 的 布局
			// MyApplication.getInstance().putConfigValue("pointDeviceAtt",
			// "属性1,属性2,属性3,属性4,属性5,属性6");
			final String[] attributes = MyApplication.getInstance().getConfigValue("pointDeviceAtt").split(",");
			attViewList = new LinkedList<View>();

			LayoutInflater inflater = this.getLayoutInflater();
			for (int i = 0; i < attributes.length; i++) {
				View attLayout = inflater.inflate(R.layout.device_report_att_item, null);
				((TextView) attLayout.findViewById(R.id.attName)).setText(attributes[i] + "：");
				rootLayout.addView(attLayout);
				attViewList.add(attLayout);
			}
		} else if (deviceType == 2) {
			// MyApplication.getInstance().putConfigValue("lineDeviceTypes",
			// "线设备1");
			deviceTypeLayout.setVisibility(View.GONE);

			// 加载 点设备 属性填写 的 布局
			// MyApplication.getInstance().putConfigValue("lineDeviceAtt",
			// "线属性1,线属性2,线属性3,线属性4,线属性5,线属性6");
			final String[] attributes = MyApplication.getInstance().getConfigValue("lineDeviceAtt").split(",");
			attViewList = new LinkedList<View>();

			LayoutInflater inflater = this.getLayoutInflater();
			for (int i = 0; i < attributes.length; i++) {
				View attLayout = inflater.inflate(R.layout.device_report_att_item, null);
				((TextView) attLayout.findViewById(R.id.attName)).setText(attributes[i] + "：");
				rootLayout.addView(attLayout);
				attViewList.add(attLayout);
			}
		}

	}

	private void reportPointDevice(String roleName) {
		String newValueString = "", attrFieldString = "";
		Map<String, String> paraMap = new HashMap<String, String>();

		for (View one : attViewList) {
			attrFieldString += ((TextView) one.findViewById(R.id.attName)).getText().toString().replace("：", "") + ","; // 　冒号　是　中文的
			newValueString += ((TextView) one.findViewById(R.id.attEditText)).getText().toString().replace("：", "") + ",";
		}
		if (attViewList.size() > 0) {
			attrFieldString = attrFieldString.substring(0, attrFieldString.length() - 1);
			newValueString = newValueString.substring(0, newValueString.length() - 1);
		}

		paraMap.put("NewValue", newValueString);
		paraMap.put("OldValue", "");
		paraMap.put("EquipFlag", "");
		if (deviceType == 1) {
			paraMap.put("MapFlag", deviceTypeTV.getText().toString());
			paraMap.put("Geometry", dotStr);
			paraMap.put("GeometryType", "点");
		} else {
			paraMap.put("MapFlag", MyApplication.getInstance().getConfigValue("lineDeviceTypes").split(",")[0]);
			paraMap.put("Geometry", dotList.toString());
			paraMap.put("GeometryType", "线");
		}
		paraMap.put("Reporter", MyApplication.getInstance().getUserId() + "");
		paraMap.put("Oper", "数据录入");
		paraMap.put("Time", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
		paraMap.put("AttrField", attrFieldString);
		paraMap.put("AuditUser", roleName);

		BaseStringTask task = new BaseStringTask(new BaseTaskParameters("http://"
				+ ServerConnectConfig.getInstance().getServerConfigInfo().IpAddress + ":"
				+ ServerConnectConfig.getInstance().getServerConfigInfo().Port
				+ "/CityInterface/Services/zondy_mapgiscitysvr_audit/REST/auditrest.svc/InsertAttrForEquip", paraMap),
				new BaseTaskListener<String>() {
					@Override
					public void onError(Throwable paramThrowable) {
						handler.obtainMessage(1, "设备普查上报失败").sendToTarget();
					}

					@Override
					public void onCompletion(short completFlg, String localObject1) {
						handler.obtainMessage(1, localObject1 != null && localObject1.length() > 0 ? localObject1 : "设备普查上报成功")
								.sendToTarget();
					}
				});
		progressDialog.show();
		MyApplication.getInstance().submitExecutorService(task);

	}

	Runnable getAuditRoleNameRunable = new Runnable() {
		@Override
		public void run() {
			try {
				String serviceUrl = "http://" + ServerConnectConfig.getInstance().getServerConfigInfo().IpAddress + ":"
						+ ServerConnectConfig.getInstance().getServerConfigInfo().Port + "/"
						+ ServerConnectConfig.getInstance().getServerConfigInfo().VirtualPath
						+ "/Services/Zondy_MapGISCitySvr_Audit/REST/AuditREST.svc/GetAuditRoleName";

				String result = NetUtil.executeHttpGet(serviceUrl, "roleName",
						MyApplication.getInstance().getConfigValue("roleName"));

				JSONObject jsonObject = new JSONObject(result);
				JSONArray jsonArray = jsonObject.getJSONArray("NameList");

				if (jsonArray == null || jsonArray.length() == 0) {
					handler.obtainMessage(0, "未配置审核角色或角色下无审核人员").sendToTarget();
				} else {
					String[] roleNames = new String[jsonArray.length()];

					for (int i = 0; i < jsonArray.length(); i++) {
						roleNames[i] = jsonArray.get(i).toString();
					}

					handler.obtainMessage(getRoleName, roleNames).sendToTarget();
				}
			} catch (Exception e) {
				handler.obtainMessage(0, "查询审核人员列表异常").sendToTarget();
			}
		}
	};

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// progressDialog.dismiss();

			switch (msg.what) {
			case result:
				progressDialog.dismiss();
				String result = (String) msg.obj;
				Toast.makeText(DeviceReportActivity.this, result, Toast.LENGTH_SHORT).show();
				break;
			case getRoleName:
				createRoleNamaDlg(msg.obj);
				break;
			case none:
				Toast.makeText(DeviceReportActivity.this, "未做任何修改....", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private void createRoleNamaDlg(Object msgObj) {

		List<String> items = Arrays.asList((String[]) msgObj);

		ListDialogFragment fragment = new ListDialogFragment("选择处理人", items);

		fragment.show(this.getSupportFragmentManager(), "1");

		fragment.setListItemClickListener(new OnListItemClickListener() {
			@Override
			public void onListItemClick(int arg2, String value) {
				reportPointDevice(value);
			}
		});
	}
}

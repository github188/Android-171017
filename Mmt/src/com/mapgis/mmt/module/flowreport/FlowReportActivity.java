package com.mapgis.mmt.module.flowreport;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.common.widget.fragment.PhotoFragment;
import com.mapgis.mmt.common.widget.fragment.RecorderFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.ExpressionManager;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.Poi;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.net.BaseTaskListener;
import com.mapgis.mmt.net.BaseTaskParameters;
import com.swipebacklayout.lib.SwipeBackLayout.SwipeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowReportActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final FlexFlowReportFragment fragment = new FlexFlowReportFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.baseFragment, fragment);
		ft.show(fragment);
		ft.commit();

		getBaseTextView().setText("临时事件");

		getBaseLeftImageView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		getBaseRightImageView().setVisibility(View.VISIBLE);
		getBaseRightImageView().setImageResource(R.drawable.flex_flow_report);
		getBaseRightImageView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fragment.reportEvent();
			}
		});

		mSwipeBackLayout.setSwipeListener(new SwipeListener() {
			@Override
			public void onScrollStateChange(int state, float scrollPercent) {
			}

			@Override
			public void onScrollOverThreshold() {
			}

			@Override
			public void onEdgeTouch(int edgeFlag) {
			}

			@Override
			public void onActivityFinish() {// 自己添加的方法...
				backToMenu();
			}
		});

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	public void backToMenu() {
		AppManager.finishActivity(FlowReportActivity.this);
		MyApplication.getInstance().finishActivityAnimation(FlowReportActivity.this);
	}

	@Override
	public void onBackPressed() {
		TextView tv = new TextView(FlowReportActivity.this);
		tv.setTextAppearance(FlowReportActivity.this, R.style.default_text_medium_1);
		tv.setText("确定退出上报功能吗?");
		OkCancelDialogFragment fragment = new OkCancelDialogFragment("退出上报", tv);
		fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
			@Override
			public void onRightButtonClick(View view) {
				backToMenu();
			}
		});
		fragment.show(getSupportFragmentManager(), "");
	}
}

@SuppressLint("ShowToast")
class FlexFlowReportFragment extends Fragment {
	private static ArrayList<EventType> types = new ArrayList<EventType>();

	String pathString;

	List<String> recordList = new ArrayList<String>();

	boolean isLocationByGPS = false;

	private int position = 0;// 事件列表点击的位置

	private PhotoFragment takePhotoFragment;
	private RecorderFragment recorderFragment;

	public FlexFlowReportFragment() {

	}

	Intent intent;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		/************************************ 通过GPS坐标定位当前位置信息填充到位置栏 *******************************************/
		if (((EditText) getView().findViewById(R.id.flowReportAddress)).getText().toString().trim().length() == 0) {
			new BDGeocoderResultTask().executeOnExecutor(MyApplication.executorService);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.flowreporter, null);

		try {
			loadingDialog = MmtProgressDialog.getLoadingProgressDialog(getActivity(), "向服务器请求数据中，请稍候...");

			loadingDialog.show();

			intent = getActivity().getIntent();

			/************************************ 通过获取指定信息来填充GPS坐标值 ************************************************/
			if (intent != null && intent.hasExtra("fromWhere") && intent.getStringExtra("fromWhere").equals("gisDevice")) {
				((EditText) view.findViewById(R.id.flowReportAddress)).setText(intent.getStringExtra("place"));

				String xy = intent.getStringExtra("xy");
				double x = Convert.FormatDouble(Double.valueOf(xy.split(",")[0]));
				double y = Convert.FormatDouble(Double.valueOf(xy.split(",")[1]));
				((EditText) view.findViewById(R.id.flowReportLocation)).setText(x + "," + y);
			} else {
				isLocationByGPS = true;

				GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

				((EditText) view.findViewById(R.id.flowReportLocation)).setText(Convert.FormatDouble(xyz.getX()) + ","
						+ Convert.FormatDouble(xyz.getY()));
			}

			/************************************** 事件信息获取的按钮点击事件 *********************************************/
			view.findViewById(R.id.flowReportEventButton).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ArrayList<String> items = new ArrayList<String>();

					for (EventType eventType : types) {
						items.add(eventType.TypeDesc);
					}

					ListDialogFragment fragment = new ListDialogFragment("事件", items);

					fragment.show(getActivity().getSupportFragmentManager(), "1");

					fragment.setListItemClickListener(new OnListItemClickListener() {
						@Override
						public void onListItemClick(int arg2, String value) {
							((TextView) getView().findViewById(R.id.flowReportEvent)).setText(value);
							position = arg2;
						}
					});
				}
			});

			view.findViewById(R.id.flowReportExpression).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ListDialogFragment fragment = new ListDialogFragment("选择惯用语", new ExpressionManager()
							.getExpression(ExpressionManager.EXPRESSION_FLOWREPORTER));
					fragment.show(getActivity().getSupportFragmentManager(), "");
					fragment.setListItemClickListener(new OnListItemClickListener() {
						@Override
						public void onListItemClick(int arg2, String value) {
							((TextView) getView().findViewById(R.id.flowReportDescription)).setText(value);
						}
					});
				}
			});

			view.findViewById(R.id.flowReportExpression).setVisibility(View.GONE);

			takePhotoFragment = new PhotoFragment.Builder("FlowReporter/").build();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(R.id.flowReportTakephotoFragment, takePhotoFragment);
			ft.show(takePhotoFragment);

			// MyApplication.getInstance().putConfigValue("isShowRecord", "1");
			if (MyApplication.getInstance().getConfigValue("isShowRecord").equals("1")) {
				recorderFragment = RecorderFragment.newInstance("相对路径名称/");
				ft.add(R.id.flowRecorderFragment, recorderFragment);
				ft.show(recorderFragment);
			} else {
				view.findViewById(R.id.recordLine).setVisibility(View.GONE);
				view.findViewById(R.id.recordLayout).setVisibility(View.GONE);
			}

			ft.commit();

			requestFlowType();
		} catch (Exception e) {
			e.printStackTrace();
		}

		view.findViewById(R.id.flowReportLocationButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getActivity(), "长按以获取坐标", 0).show();

				Intent intent = new Intent(getActivity(), MapGISFrame.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
						.putExtra(MapGISFrame.LONG_TAG_FOR_POINT, true).putExtra("action", FlowReportActivity.class);

				startActivity(intent);
			}
		});

		return view;
	}

	/**
	 * 重新同步事件类型
	 */
	@SuppressWarnings("unused")
	private void syncEventType() {
		loadingDialog.show();

		requestFlowType();
	}

	FlowReportTaskParameters parameters;

	private final int SAVE_EVENT_SUCCESS = 2;
	private final int SAVE_EVENT_FAILED = 3;

	private void confirmReport() {
		try {
			loadingDialog.show();

			if (isLocationByGPS) {
				GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();
				if (!(xyz.getX() + "," + xyz.getY()).equals("0.0,0.0")) {
					((EditText) getView().findViewById(R.id.flowReportLocation)).setText(xyz.getX() + "," + xyz.getY());
				}
			}

			// String url =
			// ServerConnectConfig.getInstance().getCityServerMobilePath();
			String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc";
			parameters = new FlowReportTaskParameters(url + "/ReportEvent");

			parameters.setUserId(MyApplication.getInstance().getUserId());

			parameters.setFlowId("0");
			parameters.setFlowName("<类型不详>");

			EventType eventType = types.get(position);

			parameters.setCaseName(eventType.TypeID);
			parameters.setCaseDesc(eventType.TypeDesc);

			parameters.setRoadName(((EditText) getView().findViewById(R.id.flowReportAddress)).getText().toString().trim());

			parameters.setPosition(((EditText) getView().findViewById(R.id.flowReportLocation)).getText().toString().trim());

			parameters.setContent(((EditText) getView().findViewById(R.id.flowReportDescription)).getText().toString().trim());
			parameters.setEventSituation(((EditText) getView().findViewById(R.id.flowReportAdvice)).getText().toString().trim());

			parameters.setTime(BaseClassUtil.getSystemTime());

			if (intent != null && intent.hasExtra("fromWhere") && intent.getStringExtra("fromWhere").equals("gisDevice")) {
				parameters.setLayerName(intent.getStringExtra("layerName"));
				parameters.setPipeId(intent.getStringExtra("pipeNo"));
			} else {
				parameters.setPipeId("");
				parameters.setLayerName("");
			}

			if (takePhotoFragment.getRelativePhoto().trim().length() > 0) {
				parameters.setMediaString(takePhotoFragment.getAbsolutePhoto());
				parameters.photoNames = takePhotoFragment.getNames();
			}

			if (MyApplication.getInstance().getConfigValue("isShowRecord").equals("1")) {
				parameters.setRecordString(recorderFragment.getNames());
				parameters.recPaths = recorderFragment.getAbsoluteRec();
			}

			FlowReportTask task = new FlowReportTask(parameters, new BaseTaskListener<String>() {

				@Override
				public void onCompletion(short completFlg, String localObject1) {
					if (BaseClassUtil.isNullOrEmptyString(localObject1)) {
						handler.sendEmptyMessage(SAVE_EVENT_FAILED);
					} else {
						handler.obtainMessage(SAVE_EVENT_SUCCESS, localObject1).sendToTarget();
					}
				}

				@Override
				public void onError(Throwable paramThrowable) {
					handler.sendEmptyMessage(SAVE_EVENT_FAILED);
				}
			});
			// 标识是否在Wifi 状态 下 上传 大图和录音
			boolean uploadBigPicRec = MyApplication.getInstance().getConfigValue("uploadBigPicByWifi", 0) == 1;
			task.setReportBigFileFlag(uploadBigPicRec);

			MyApplication.getInstance().submitExecutorService(task);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void confirmPosition() {
		if (((EditText) getView().findViewById(R.id.flowReportLocation)).getText().toString().equals("0.0,0.0")) {
			OkCancelDialogFragment fragment = new OkCancelDialogFragment("坐标未正确获取,是否继续上报");
			fragment.setCancelable(false);

			fragment.setLeftBottonText("取消");

			fragment.setRightBottonText("确认");
			fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
				@Override
				public void onRightButtonClick(View view) {
					confirmReport();
				}
			});

			fragment.show(getActivity().getSupportFragmentManager(), "2");
		} else {
			confirmReport();
		}
	}

	/**
	 * 上报事件
	 */
	public void reportEvent() {

		long isReportDisplay = MyApplication.getInstance().getConfigValue("isReportDisplay", 0);
		long isInfoConfirm = MyApplication.getInstance().getConfigValue("isInfoConfirm", 0);

		if (isInfoConfirm == 1) {
			if (isReportDisplay == 1) {
				if (valueConfirm() == true) {
					OkCancelDialogFragment fragment = new OkCancelDialogFragment("确认上报");
					fragment.setCancelable(false);

					fragment.setLeftBottonText("取消");

					fragment.setRightBottonText("确认");
					fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
						@Override
						public void onRightButtonClick(View view) {
							confirmPosition();
						}
					});

					fragment.show(getActivity().getSupportFragmentManager(), "1");
				}
			} else {
				if (valueConfirm() == true) {
					confirmPosition();
				}
			}
		} else {
			if (isReportDisplay == 1) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("确认上报");
				builder.setIcon(R.drawable.navigation_eventreport);
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						confirmPosition();
					}
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
				builder.create().show();
			} else {
				confirmPosition();
			}
		}

	}

	private boolean valueConfirm() {
		if (((EditText) getView().findViewById(R.id.flowReportAddress)).getText().toString().trim().equals("")) {
			Toast.makeText(getActivity(), "地址栏未填写", 0).show();
			return false;
		} else if (((EditText) getView().findViewById(R.id.flowReportDescription)).getText().toString().trim().equals("")) {
			Toast.makeText(getActivity(), "内容栏未填写", 0).show();
			return false;
		} else {
			return true;
		}
	}

	ProgressDialog loadingDialog;

	class EventType {
		public String TypeID;

		public String TypeDesc;

		public EventType(String desc) {
			TypeID = desc.split(",")[0];
			TypeDesc = desc.split(",")[1];
		}

		@Override
		public String toString() {
			return TypeDesc;
		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case GET_EVENT_DESC:
					if (types.size() == 0) {
						Toast.makeText(getActivity(), "未获取到事件描述，请检查监控端配置", Toast.LENGTH_SHORT).show();
						return;
					} else {
						((TextView) getView().findViewById(R.id.flowReportEvent)).setText(types.get(0).TypeDesc);
					}
					break;
				case SAVE_EVENT_FAILED:
					Toast.makeText(getActivity(), "事件保存过程出现异常", Toast.LENGTH_SHORT).show();
					break;
				case SAVE_EVENT_SUCCESS:
					Toast.makeText(getActivity(), "事件保存成功", Toast.LENGTH_SHORT).show();
					parameters.setReportRowId((String) msg.obj);
					DatabaseHelper.getInstance().insert(parameters);
					((EditText) getView().findViewById(R.id.flowReportAddress)).setText("");
					((EditText) getView().findViewById(R.id.flowReportDescription)).setText("");
					((EditText) getView().findViewById(R.id.flowReportAdvice)).setText("");

					takePhotoFragment.clear();
					recorderFragment.clear();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				loadingDialog.cancel();
			}
		}
	};

	Map<String, Object> map;

	@Override
	public void onResume() {
		super.onResume();
		if (getActivity().getIntent().getStringExtra("location") != null
				&& !getActivity().getIntent().getStringExtra("location").equals("")) {

			String xy = getActivity().getIntent().getStringExtra("location");
			double x = Convert.FormatDouble(Double.valueOf(xy.split(",")[0]));
			double y = Convert.FormatDouble(Double.valueOf(xy.split(",")[1]));

			((EditText) getView().findViewById(R.id.flowReportLocation)).setText(x + "," + y);

			isLocationByGPS = false;

			getActivity().getIntent().removeExtra("location");

			AppManager.resetActivityStack(getActivity());
		}
	}

	private final int GET_EVENT_DESC = 1;

	private void requestFlowType() {
		try {
			if (types.size() == 0) {
				String httpUrl = ServerConnectConfig.getInstance().getMobileBusinessURL()
						+ "/BaseREST.svc/GetUserCanCreateCaseFlow";

				Map<String, String> paraMap = new HashMap<String, String>();

				paraMap.put("userId", String.valueOf(MyApplication.getInstance().getUserId()));

				FlowTypeTask task = new FlowTypeTask(new BaseTaskParameters(httpUrl, paraMap),
						new BaseTaskListener<ArrayList<FlowTypeModel>>() {

							@Override
							public void onError(Throwable paramThrowable) {
								handler.sendEmptyMessage(GET_EVENT_DESC);
							}

							@Override
							public void onCompletion(short completFlg, ArrayList<FlowTypeModel> localObject1) {
								types.clear();

								for (String desc : localObject1.get(0).Descriptions) {
									types.add(new EventType(desc));
								}

								handler.sendEmptyMessage(GET_EVENT_DESC);
							}
						});

				MyApplication.getInstance().submitExecutorService(task);
			} else {
				handler.sendEmptyMessage(GET_EVENT_DESC);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class BDGeocoderResultTask extends AsyncTask<String, String, BDGeocoderResult> {

		@Override
		protected BDGeocoderResult doInBackground(String... params) {
			Location location = GpsReceiver.getInstance().getLastLocation();
			return BDGeocoder.find(location);
		}

		@Override
		protected void onPostExecute(BDGeocoderResult bdResult) {

			try {

				if (bdResult == null && getActivity() != null) {
					Toast.makeText(getActivity(), "根据当前位置进行地址查询未查询到结果，请检测GPS状态或者网络状况", Toast.LENGTH_SHORT).show();
					return;
				}

				final String address = bdResult.result.addressComponent.district + bdResult.result.addressComponent.street
						+ bdResult.result.addressComponent.street_number;

				final List<String> names = new ArrayList<String>();

				for (Poi poi : bdResult.result.pois) {
					names.add(poi.name);
				}

				((EditText) getView().findViewById(R.id.flowReportAddress)).setText(address + "-" + names.get(0));

				getView().findViewById(R.id.flowReportAddressButton).setVisibility(View.VISIBLE);
				getView().findViewById(R.id.flowReportAddressButton).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						ListDialogFragment fragment = new ListDialogFragment("请选择所在位置", names);
						fragment.show(getActivity().getSupportFragmentManager(), "");
						fragment.setListItemClickListener(new OnListItemClickListener() {
							@Override
							public void onListItemClick(int arg2, String value) {
								((EditText) getView().findViewById(R.id.flowReportAddress)).setText(address + "-" + value);
							}
						});
					}
				});

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}

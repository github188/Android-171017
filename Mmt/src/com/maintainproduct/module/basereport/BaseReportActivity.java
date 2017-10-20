package com.maintainproduct.module.basereport;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.BaseReportEntity;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.module.BeanFragment;
import com.maintainproduct.module.BeanFragment.BeanFragmentOnCreate;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.module.taskcontrol.TaskControlDBHelper;
import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BaseReportActivity extends BaseActivity {
	public GDFormBean gdFormBean;
	public BeanFragment fragment;

	public String name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		name = getIntent().getStringExtra("Alias");

		getBaseTextView().setText(name);

		new BaseFlowFormTask(handler, this).executeOnExecutor(MyApplication.executorService, name);
	}

	// BeanFragment 所有的 View 显示完成后 调用 此 方法， 此时 BeanFragment 的 GroupFragment 的
	// 交LinearLayout 已经被初始化
	// 因此， 继承 BaseReportActivity的类 覆写 此方法，即处理 任意 View
	protected void onnBeanFragmentViewCreated() {

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	/** 此类的Uri */
	public String getActionUri() {
		return "com.maintainproduct.module.basereport.BaseReportActivity";
	}

    /** 次类所存放文件的路径 */
	public String getFileRelativePath() {
		return "Repair/" + name + "/" + MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName + "/";
	}

    /**
	 * 界面自动生成完成
	 */
	protected void onBeanFragmentCreate() {
	}

	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			gdFormBean = (GDFormBean) msg.obj;

			// 根据信息创建Fragment
			fragment = gdFormBean.createFragment();

			fragment.setBeanFragmentOnCreate(new BeanFragmentOnCreate() {

				@Override
				public void onCreated() {
					onBeanFragmentCreate();
				}
			});

			fragment.setCls(BaseReportActivity.class);
			fragment.setFragmentFileRelativePath(getFileRelativePath());
			addFragment(fragment);

			BottomUnitView reportButton = new BottomUnitView(BaseReportActivity.this);
			reportButton.setContent("上报");
			reportButton.setImageResource(R.drawable.handoverform_report);
			addBottomUnitView(reportButton, new OnClickListener() {
				@Override
				public void onClick(View v) {
					long isReportDisplay = MyApplication.getInstance().getConfigValue("isReportDisplay", 0);

					if (isReportDisplay == 1) { // 显示确认对话框
						OkCancelDialogFragment conFragment = new OkCancelDialogFragment("确认上报");
						conFragment.setCancelable(false);
						conFragment.setLeftBottonText("取消");
						conFragment.setRightBottonText("确认");
						conFragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
							@Override
							public void onRightButtonClick(View view) {
								reportItem();
							}
						});
						conFragment.show(getSupportFragmentManager(), "1");
					} else {
						reportItem();
					}
				}
			});
		}
	};

	String data = "";

	public void reportItem() {
		List<FeedItem> items = fragment.getFeedbackItems(ReportInBackEntity.REPORTING);
        if(items==null){
        	return;
        }
		// 创建服务路径
		String uri = ServerConnectConfig.getInstance().getBaseServerPath()
				+ "/Services/MapgisCity_WorkFlowForm/REST/WorkFlowFormREST.svc/ReportGDForm?table=" + gdFormBean.TableName;

		// 将对信息转换为JSON字符串
		data = new Gson().toJson(items, new TypeToken<ArrayList<FeedItem>>() {
		}.getType());

		// 将所有信息封装成后台上传的数据模型
		ReportInBackEntity entity = new ReportInBackEntity(data, MyApplication.getInstance().getUserId(), ReportInBackEntity.REPORTING,
				uri, UUID.randomUUID().toString(), name, fragment.getAbsolutePaths(), fragment.getRelativePaths());

		// 用填写的信息填充原有数据模型
		gdFormBean.setValueByFeedItems(items);
		// 将控件设为只读，只用于显示
		// GDFormBean.setOnlyShow();

		BaseReportEntity baseReportEntity = new BaseReportEntity();
		baseReportEntity.EventType = name;
		baseReportEntity.Time = BaseClassUtil.getSystemTime();
		baseReportEntity.GDBeanData = new Gson().toJson(gdFormBean, GDFormBean.class);
		baseReportEntity.FilePath = fragment.getAbsolutePaths();
		baseReportEntity.RelativePath = getFileRelativePath();
		baseReportEntity.UserId = MyApplication.getInstance().getUserId();
		// 将本次事件保存到历史事件中
		DatabaseHelper.getInstance().insert(baseReportEntity);

		// 若本地数据存在，则修改保存数据，否则插入新数据
		long row = entity.insert();

		if (row == -1) {
			showToast("操作本地数据库失败");
		} else {
			showToast("<" + name + ">反馈信息保存成功");

			int taskId = entity.getIdInSQLite();

			if (taskId != -1) {
				TaskControlDBHelper.getIntance().createControlData(taskId + "", name);
			}

			setResult(ResultCode.RESULT_CUSTOM_REPORT, new Intent().putExtra("data", data));
			finish();
		}
	}
}

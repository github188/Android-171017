package com.maintainproduct.v2.caselist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.common.widget.fragment.PhotoFragment;
import com.mapgis.mmt.common.widget.fragment.RecorderFragment;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 工单反馈   基Activity
 * @author meikai
 */
public class BaseFeedBackActivity extends BaseActivity {
	
	private BaseFeedBackFragment fragment;
	private int resultCode = MaintainConstant.GDOpeFailed;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String CaseID = getIntent().getStringExtra("CaseID");
		String feedBackUrl = getIntent().getStringExtra("feedBackUrl");
		String feedBackType = getIntent().getStringExtra("feedBackType");
		getBaseTextView().setText(feedBackType);
		
		getBaseLeftImageView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult( resultCode );
				BaseFeedBackActivity.this.overridePendingTransition(R.anim.anim_empty, R.anim.anim_empty);
				AppManager.finishActivity(BaseFeedBackActivity.this);
			}
		});
		
		fragment = new BaseFeedBackFragment( CaseID, feedBackUrl , feedBackType );
		addFragment( fragment );
	}
	
	@Override
	public void onBackPressed() {
		setResult( resultCode );
		super.onBackPressed();
		BaseFeedBackActivity.this.overridePendingTransition(R.anim.anim_empty, R.anim.anim_empty);
	}
	
	class BaseFeedBackFragment extends Fragment implements OnClickListener {
		
		private String CaseID;
		private String feedBackUrl;
		private String feedBackType;
		
		private ImageEditView descriptionImgEdt;
		private PhotoFragment takePhotoFragment;
		private RecorderFragment recorderFragment;
		private ImageTextView reportNameImgTV;
		private Button feedBackReportBtn;
		
		public BaseFeedBackFragment(String CaseID, String feedBackUrl, String feedBackType) {
			super();
			this.CaseID = CaseID;
			this.feedBackUrl = feedBackUrl;
			this.feedBackType = feedBackType;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return inflater.inflate(R.layout.gd_feedback, null);
		}
		
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			descriptionImgEdt = (ImageEditView)view.findViewById(R.id.description);
			descriptionImgEdt.setImage(R.drawable.flex_flow_type);
			descriptionImgEdt.setKey("描述");
			reportNameImgTV = (ImageTextView) view.findViewById(R.id.reporterName);
			reportNameImgTV.setKey("上报人");
			String userName = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;
			reportNameImgTV.setValue( userName );
			feedBackReportBtn = (Button)view.findViewById(R.id.feedBackReportBtn);
			feedBackReportBtn.setOnClickListener(this);

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd"); // 设置日期格式
			String currentDate = df.format(new Date()); // new Date()为获取当前系统时间
			
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			takePhotoFragment = new PhotoFragment.Builder(feedBackType + "/" + currentDate + "/" + userName + "/").build();
			ft.add(R.id.photoFrameLayout, takePhotoFragment);
			ft.show(takePhotoFragment);

			recorderFragment = RecorderFragment.newInstance( feedBackType + "/" + currentDate + "/" + userName + "/");
			ft.add(R.id.recorderFrameLayout, recorderFragment);
			ft.show(recorderFragment);
			ft.commit();
		}

		@Override
		public void onClick(View v) {
			
			if( descriptionImgEdt.getValue().length()==0 ){
				Toast.makeText(getActivity(), "请填写反馈内容", Toast.LENGTH_SHORT).show();
				return ;
			}
			
			ReportItem reportItem = new ReportItem();
			reportItem.CaseID = this.CaseID;
			reportItem.Description = descriptionImgEdt.getValue();
			reportItem.ReportMan = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;
			reportItem.ReportManID = MyApplication.getInstance().getUserId() + "";
			reportItem.ReportType = this.feedBackType;
			reportItem.Picture = takePhotoFragment.getRelativePhoto();
			reportItem.Recording = recorderFragment.getRelativeRec();
			String data = new Gson().toJson(reportItem, ReportItem.class);
			
			String absolutePath = "";
			String relativePath = "";
			absolutePath += takePhotoFragment.getAbsolutePhoto();
			if (recorderFragment.getAbsoluteRec().length() > 0)
				absolutePath += "," + recorderFragment.getAbsoluteRec();

			relativePath += takePhotoFragment.getRelativePhoto();
			if (recorderFragment.getRelativeRec().length() > 0)
				relativePath += "," + recorderFragment.getRelativeRec();

			ReportInBackEntity backEntity = new ReportInBackEntity(data, MyApplication.getInstance().getUserId(),
					ReportInBackEntity.REPORTING, this.feedBackUrl, UUID.randomUUID().toString(), feedBackType, absolutePath, relativePath);

			long count = backEntity.insert();
			if ( count > 0) {
				Toast.makeText(getActivity(), feedBackType+"保存成功", Toast.LENGTH_SHORT).show();
				resultCode = MaintainConstant.GDOpeSuccess;
				
				descriptionImgEdt.setValue("");
				takePhotoFragment.clear();
				recorderFragment.clear();
				
				getActivity().setResult( resultCode );
				getActivity().finish();
			}
		}
		
	}
	
}

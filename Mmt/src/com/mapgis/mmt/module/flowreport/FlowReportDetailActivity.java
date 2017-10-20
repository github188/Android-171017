package com.mapgis.mmt.module.flowreport;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.fragment.PhotoFragment;
import com.mapgis.mmt.common.widget.fragment.RecorderFragment;

import java.util.List;

public class FlowReportDetailActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getBaseTextView().setText("事件详情");

		final FlexFlowReportFragment fragment = new FlexFlowReportFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.baseFragment, fragment);
		ft.show(fragment);
		ft.commit();

	}

	class FlexFlowReportFragment extends Fragment {

		private PhotoFragment takePhotoFragment;

		private RecorderFragment recorderFragment;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.flowreporter, container, false);

			((TextView) view.findViewById(R.id.flowReportEvent)).setText(getIntent().getStringExtra("event"));

			((EditText) view.findViewById(R.id.flowReportLocation)).setText(getIntent().getStringExtra("position"));
			view.findViewById(R.id.flowReportLocation).setFocusable(false);

			((EditText) view.findViewById(R.id.flowReportAddress)).setText(getIntent().getStringExtra("address"));
			view.findViewById(R.id.flowReportAddress).setFocusable(false);

			((EditText) view.findViewById(R.id.flowReportDescription)).setText(getIntent().getStringExtra("description"));
			view.findViewById(R.id.flowReportDescription).setFocusable(false);

			((EditText) view.findViewById(R.id.flowReportAdvice)).setText(getIntent().getStringExtra("advice"));
			view.findViewById(R.id.flowReportAdvice).setFocusable(false);

			view.findViewById(R.id.flowReportExpression).setVisibility(View.GONE);
			view.findViewById(R.id.flowReportEventButton).setVisibility(View.GONE);
			view.findViewById(R.id.flowReportLocationButton).setVisibility(View.GONE);

			takePhotoFragment = new PhotoFragment.Builder("FlowReporter/").build();
			recorderFragment = RecorderFragment.newInstance("");

			FragmentTransaction ft = getChildFragmentManager().beginTransaction();

			if (MyApplication.getInstance().getConfigValue("isShowRecord").equals("1")) {
				ft.replace(R.id.flowRecorderFragment, recorderFragment);
			} else {
				view.findViewById(R.id.recordLine).setVisibility(View.GONE);
				view.findViewById(R.id.recordLayout).setVisibility(View.GONE);
			}

			ft.replace(R.id.flowReportTakephotoFragment, takePhotoFragment);

			ft.commit();

			return view;
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			// 将已有图片初始到界面上
			String medias = getIntent().getStringExtra("media");
			if (medias != null) {
				List<String> mediaList = BaseClassUtil.StringToList(medias, ",");
				takePhotoFragment.setAbsolutePhoto(mediaList);
				takePhotoFragment.setAddEnable(false);
			}

			// 将已有录音初始化到界面上
			String recorder = getIntent().getStringExtra("recorder");
			if (recorder != null) {
				List<String> recorderList = BaseClassUtil.StringToList(recorder, ",");
				recorderFragment.setRecoderEnable(false);
				recorderFragment.setAbsoluteRec(recorderList);
			}
		}
	}

}

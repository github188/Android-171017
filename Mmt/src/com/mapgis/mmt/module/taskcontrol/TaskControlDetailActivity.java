package com.mapgis.mmt.module.taskcontrol;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

public class TaskControlDetailActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addFragment(new TaskControlDetailFragment());

		getBaseTextView().setText("数据信息");
	}

	class TaskControlDetailFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			ScrollView scrollView = new ScrollView(getActivity());
			scrollView.setLayoutParams(params);

			LinearLayout linearLayout = new LinearLayout(getActivity());
			linearLayout.setLayoutParams(params);
			linearLayout.setOrientation(LinearLayout.VERTICAL);

			scrollView.addView(linearLayout);

			TextView textView = new TextView(getActivity());
			textView.setPadding(DimenTool.dip2px(getActivity(), 20), DimenTool.dip2px(getActivity(), 10),
					DimenTool.dip2px(getActivity(), 20), DimenTool.dip2px(getActivity(), 20));

			textView.setLayoutParams(params);
			textView.setTextAppearance(getActivity(), R.style.default_text_medium_1);

			linearLayout.addView(textView);

			String showDataStr = getActivity().getIntent().getStringExtra("showData");
			if (showDataStr == null) {
				showErrorMsg("该信息已经上传成功!");
			} else {
				showDataStr = showDataStr.replaceAll(",", ",\n");
				textView.setText(showDataStr);
			}

			return scrollView;
		}
	}

}

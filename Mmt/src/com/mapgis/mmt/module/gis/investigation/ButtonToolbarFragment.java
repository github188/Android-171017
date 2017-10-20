package com.mapgis.mmt.module.gis.investigation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapgis.mmt.R;

/**
 * 用于 显示按钮 的 Fragment
 * 
 * @author meikai
 */
public class ButtonToolbarFragment extends Fragment {

	private final String btnName;
	protected Intent activityIntent;
	protected View view;

	private OnBtnClickListener saveBtnClickListener;

	public void setSaveBtnClickListener(OnBtnClickListener saveBtnClickListener) {
		this.saveBtnClickListener = saveBtnClickListener;
	}

	public interface OnBtnClickListener {
		void onClick();
	}

	public ButtonToolbarFragment(String btnName) {
		super();
		this.btnName = btnName;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		activityIntent = getActivity().getIntent();

		view = inflater.inflate(R.layout.pipe_detail_toolbar, null);

		view.findViewById(R.id.pipeDetailToolbarLeftLayout).setVisibility(View.GONE);

		view.findViewById(R.id.pipeDetailToolbarRightLayout).setVisibility(View.VISIBLE);
		((TextView) view.findViewById(R.id.pipeDetailToolbarRightText)).setText(btnName);

		setViewsEventListener();

		return view;
	}

	/**
	 * 绑定各控件的事件响应函数
	 */
	protected void setViewsEventListener() {
		view.findViewById(R.id.pipeDetailToolbarRightImage).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveBtnClickListener.onClick();
			}
		});

	}
}

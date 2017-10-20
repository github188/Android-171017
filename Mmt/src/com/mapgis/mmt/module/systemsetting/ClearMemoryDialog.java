package com.mapgis.mmt.module.systemsetting;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.doinback.ReportInBackEntity;

public class ClearMemoryDialog extends DialogFragment {
	private final boolean[] isCheckeds = new boolean[5];

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().requestWindowFeature(STYLE_NO_TITLE);

		View view = inflater.inflate(R.layout.clear_memory_dialog, null);

		view.findViewById(R.id.chkClearMyPlan).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((ImageView) ((LinearLayout) v).getChildAt(0)).setImageResource(isCheckeds[0] ? R.drawable.user_unselected
						: R.drawable.user_selected);
				isCheckeds[0] = !isCheckeds[0];
			}
		});

		view.findViewById(R.id.chkClearFlowType).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((ImageView) ((LinearLayout) v).getChildAt(0)).setImageResource(isCheckeds[1] ? R.drawable.user_unselected
						: R.drawable.user_selected);
				isCheckeds[1] = !isCheckeds[1];
			}
		});

		view.findViewById(R.id.chkClearOwnTask).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((ImageView) ((LinearLayout) v).getChildAt(0)).setImageResource(isCheckeds[2] ? R.drawable.user_unselected
						: R.drawable.user_selected);
				isCheckeds[2] = !isCheckeds[2];
			}
		});

		view.findViewById(R.id.chkClearMapData).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((ImageView) ((LinearLayout) v).getChildAt(0)).setImageResource(isCheckeds[3] ? R.drawable.user_unselected
						: R.drawable.user_selected);
				isCheckeds[3] = !isCheckeds[3];
			}
		});

		view.findViewById(R.id.chkClearReportInBack).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((ImageView) ((LinearLayout) v).getChildAt(0)).setImageResource(isCheckeds[4] ? R.drawable.user_unselected
						: R.drawable.user_selected);
				isCheckeds[4] = !isCheckeds[4];
			}
		});

		view.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isCheckeds[0]) {
					SharedPreferences preferences = MyApplication.getInstance().getSharedPreferences("MyPlanList", 0);

					String userId = String.valueOf(MyApplication.getInstance().getUserId());

					if (preferences != null && preferences.contains(userId)) {
						Editor editor = preferences.edit();

						editor.remove(userId);

						editor.commit();
					}

					preferences = MyApplication.getInstance().getSharedPreferences("PatrolEquipment", 0);

					if (preferences != null) {
						preferences.edit().clear().commit();
					}
				}

				if (isCheckeds[3]) {
                    Battle360Util.clearTileCache();
				}

				if (isCheckeds[4]) {
					DatabaseHelper.getInstance().delete(ReportInBackEntity.class, "");
				}

				Toast.makeText(getActivity(), "清理完成", Toast.LENGTH_SHORT).show();

				dismiss();
			}
		});

		return view;
	}
}

package com.mapgis.mmt.module.gatherdata.report;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.MutilTreeView;
import com.mapgis.mmt.entity.Node;

import java.util.List;

public class UserSelectDialogFragment extends DialogFragment {

	private MutilTreeView treeView;

	private Node node;

	private int editTextVisibility = View.VISIBLE;

	/** 是否允许多选 */
	private boolean isMutilEnable;

	private OnSelectedClickListener onSelectedClickListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void setNodeEntity(Node node) {
		this.node = node;
	}

	public void setEditTextVisibility(int visibility) {
		editTextVisibility = visibility;
	}

	/** 是否允许多选 */
	public boolean isMutilEnable() {
		return isMutilEnable;
	}

	public void setMutilEnable(boolean isMutilEnable) {
		this.isMutilEnable = isMutilEnable;
	}

	public void setOnSelectedClickListener(OnSelectedClickListener onSelectedClickListener) {
		this.onSelectedClickListener = onSelectedClickListener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.case_handover_user_select, container, false);

		treeView = (MutilTreeView) view.findViewById(R.id.caseHandoverList);

		view.findViewById(R.id.caseHandoverEdit).setVisibility(editTextVisibility);

		treeView.setData(getActivity(), node);

		view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				List<Node> nodes = treeView.getSeletedNodes();

				if (nodes == null || nodes.size() == 0) {
					Toast.makeText(getActivity(), "请选择承办人", Toast.LENGTH_SHORT).show();
					return;
				}

				if (!isMutilEnable && nodes.size() > 1) {
					Toast.makeText(getActivity(), "不允许选择多个人员", Toast.LENGTH_SHORT).show();
					return;
				}

				String undertakeman = "";

				for (Node node : nodes) {
					undertakeman = undertakeman + node.getValue() + ",";
				}

				undertakeman = undertakeman.substring(0, undertakeman.length() - 1);

				if (onSelectedClickListener != null)
					onSelectedClickListener.OnClick(undertakeman);

				dismiss();
			}
		});

		view.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		getDialog().requestWindowFeature(STYLE_NO_TITLE);

		return view;
	}

	public interface OnSelectedClickListener {
		void OnClick(String undertakeman);
	}
}

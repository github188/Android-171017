package com.repair.mycase.detail;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.mapgis.mmt.common.widget.customview.MutilTreeView;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.R;

import java.util.List;

public class UserSelectFragment extends DialogFragment {

	private MutilTreeView treeView;
	private EditText editText;

	private final Node node;

	/** 是否允许多选 */
	private boolean isMutilEnable;

	private OnSelectListener onSelectListener;

	public UserSelectFragment(Node node) {
		this.node = node;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.case_handover_user_select, container, false);

		treeView = (MutilTreeView) view.findViewById(R.id.caseHandoverList);
		treeView.setData(getActivity(), node);

		editText = (EditText) view.findViewById(R.id.caseHandoverEdit);
		editText.setVisibility(View.GONE);

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

				if (onSelectListener != null)
					onSelectListener.onClick(v, undertakeman, editText.getText().toString());

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

	/** 是否允许多选 */
	public boolean isMutilEnable() {
		return isMutilEnable;
	}

	public void setOnSelectListener(OnSelectListener onSelectListener) {
		this.onSelectListener = onSelectListener;
	}

	public interface OnSelectListener {
		void onClick(View view, String undertakeman, String opinion);
	}
}

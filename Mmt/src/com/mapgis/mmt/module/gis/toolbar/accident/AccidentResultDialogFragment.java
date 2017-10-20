package com.mapgis.mmt.module.gis.toolbar.accident;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.GsonBuilder;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentFeature;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentIdentify;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentResult;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.mapview.MapView;

import java.util.ArrayList;
import java.util.List;

/** 可以进行二次关阀的界面，暂不使用 */
public class AccidentResultDialogFragment extends DialogFragment {
	private ListView accidentDialogLeftView;
	private ListView accidentDialogRightView;

	/** 左边初始数据转换后的符合显示要求的集合,主要增加一个标识来判定是否被选中 */
	private final List<SplitItem> splitItems = new ArrayList<SplitItem>();

	private final MapView mapView;
	private final AccidentResult accidentResult;

	/** 被选中的设备采用的图片 */
	private final Bitmap selectedBitmap;

	/** 设备信息，针对该设备进行二次关阀 */
	private final List<AccidentFeature> twiceAccidentFeatures = new ArrayList<AccidentFeature>();

	private OnTwiceCloseButtonClickListener onTwiceCloseButtonClickListener;

	public void setOnTwiceCloseButtonClickListener(OnTwiceCloseButtonClickListener onTwiceCloseButtonClickListener) {
		this.onTwiceCloseButtonClickListener = onTwiceCloseButtonClickListener;
	}

	public AccidentResultDialogFragment(MapView mapView, AccidentResult accidentResult) {
		this.mapView = mapView;
		this.accidentResult = accidentResult;

		selectedBitmap = BitmapFactory.decodeResource(mapView.getResources(), R.drawable.icon_gcoding);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.accident_split_list_view, container, false);

		accidentDialogLeftView = (ListView) view.findViewById(R.id.accidentDialogLeftView);

		accidentDialogRightView = (ListView) view.findViewById(R.id.accidentDialogRightView);
		accidentDialogRightView.setBackgroundResource(R.color.default_light_blue);

		view.findViewById(R.id.accidentDialogBackButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		view.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		((TextView) view.findViewById(R.id.btn_ok)).setText("二次关阀");
		view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onTwiceCloseButtonClickListener != null) {
					onTwiceCloseButtonClickListener.onButtonClick(twiceAccidentFeatures);
				}
			}
		});

		getDialog().requestWindowFeature(STYLE_NO_TITLE);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		// 需要进行爆管分析的设备类型的集合
		List<String> typList = new ArrayList<String>(accidentResult.getResultTypesWithCount());

		// 爆管分析结果的设备属性信息，采用长字符串的形式单排显示
		final List<List<String>> resultEquipesAttrsStr = new ArrayList<List<String>>();

		for (String type : typList) {
			resultEquipesAttrsStr.add(accidentResult.valveEquipeStrInfos(type));
		}

		splitItems.clear();
		// 初始化左边的数据模型列表
		for (String string : typList) {
			SplitItem splitItem = new SplitItem();
			splitItem.value = string;
			splitItem.isChoiced = false;

			splitItems.add(splitItem);
		}

		// 若有数据，默认显示第一个数据
		if (splitItems.size() > 0) {
			splitItems.get(0).isChoiced = true;
		}

		// 点击坐标的Item时，将点中的item着重显示，并在右边显示出对应的数据
		final AccidentLeftViewAdapter leftAdapter = new AccidentLeftViewAdapter(splitItems);

		accidentDialogLeftView.setAdapter(leftAdapter);

		accidentDialogLeftView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < splitItems.size(); i++) {
                    splitItems.get(i).isChoiced = i == arg2;
				}

				leftAdapter.notifyDataSetChanged();

				AccidentRightViewAdapter rightAdapter = new AccidentRightViewAdapter(getActivity(), accidentResult
						.getAccidentIdentifyByIndex(arg2), accidentResult.twiceCloseValve
						.contains(AccidentResult.resultTypes[arg2]));

				accidentDialogRightView.setAdapter(rightAdapter);
			}
		});

		// 点击右边的Item时，跳转的地图界面，并将点中的设备居中显示，并弹出Tip
		AccidentRightViewAdapter rightAdapter = new AccidentRightViewAdapter(getActivity(),
				accidentResult.getAccidentIdentifyByIndex(0),
				accidentResult.twiceCloseValve.contains(AccidentResult.resultTypes[0]));

		accidentDialogRightView.setAdapter(rightAdapter);

		accidentDialogRightView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AccidentFeature feature = (AccidentFeature) arg0.getItemAtPosition(arg2);

				String featureJsonStr = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
						.toJson(feature, AccidentFeature.class);

				for (Annotation annotation : mapView.getAnnotationLayer().getAllAnnotations()) {
					// AccidentFeature被转换成Json字符串存储在Annotation中
					// 如果存储的有数据，并且当前选中的数据等于Annotation中的数据，则被认为是要显示的数据
					if (annotation.getUid() != null && annotation.getUid().equals(featureJsonStr)) {
						annotation.showAnnotationView();
						mapView.panToCenter(annotation.getPoint(), true);
						break;
					}
				}

				dismiss();
			}
		});
	}

	class AccidentLeftViewAdapter extends BaseAdapter {
		private final List<SplitItem> splitItems;

		public AccidentLeftViewAdapter(List<SplitItem> splitItems) {
			this.splitItems = splitItems;
		}

		@Override
		public int getCount() {
			return splitItems.size();
		}

		@Override
		public Object getItem(int position) {
			return splitItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = getActivity().getLayoutInflater().inflate(R.layout.simple_list_split_item, null);
			TextView textView = (TextView) convertView.findViewById(R.id.splitListViewItemText);
			textView.setText(splitItems.get(position).value);

			convertView.setBackgroundResource(splitItems.get(position).isChoiced ? R.color.default_light_blue : R.color.white);

			return convertView;
		}
	}

	class AccidentRightViewAdapter extends BaseAdapter {
		private final AccidentFeature[] features;
		private final Context context;
		private final boolean isNeedTwiceClose;

		/**
		 * @param isNeedTwiceClose
		 *            是否是需要二次关阀的设备
		 */
		public AccidentRightViewAdapter(Context context, AccidentIdentify identify, boolean isNeedTwiceClose) {
			this.context = context;
			this.isNeedTwiceClose = isNeedTwiceClose;

			if (identify == null || identify.features == null) {
				features = new AccidentFeature[] {};
			} else {
				this.features = identify.features;
			}
		}

		@Override
		public int getCount() {
			return features.length;
		}

		@Override
		public Object getItem(int position) {
			return features[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout linearLayout = new LinearLayout(context);
			linearLayout.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			linearLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

			final AccidentFeature feature = (AccidentFeature) getItem(position);
			String showResult = (feature.attributes == null || feature.attributes.ID == null) ? "" : feature.attributes.ID;

			if (isNeedTwiceClose) {// 可以进行二次关阀的设备，加一个复选框
				CheckBox checkBox = new CheckBox(context);
				checkBox.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				checkBox.setFocusable(false);
				checkBox.setGravity(Gravity.CENTER_VERTICAL);

				checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						feature.isChecked = isChecked;

						if (feature.annotation != null) {
							feature.annotation.setImage(isChecked ? selectedBitmap : feature.bitmap);
							mapView.refresh();

							if (isChecked && !twiceAccidentFeatures.contains(feature)) {
								twiceAccidentFeatures.add(feature);
							} else if (!isChecked && twiceAccidentFeatures.contains(feature)) {
								twiceAccidentFeatures.remove(feature);
							}
						}
					}
				});

				if (feature.isChecked) {
					checkBox.setChecked(true);
				} else {
					checkBox.setChecked(false);
				}

				linearLayout.addView(checkBox);
			}

			TextView textView = new TextView(context);
			textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			textView.setMinimumHeight(DimenTool.dip2px(context, 40));
			textView.setTextAppearance(getActivity(), R.style.default_text_medium_1);
			textView.setSingleLine(true);
			textView.setGravity(Gravity.CENTER_VERTICAL);
			textView.setText(showResult);

			linearLayout.addView(textView);

			linearLayout.setTag(feature);

			return linearLayout;
		}
	}

	class SplitItem {
		public boolean isChoiced;
		public String value;
	}

	public interface OnTwiceCloseButtonClickListener {

		/** 二次关阀时选择选择的设备 */
		void onButtonClick(List<AccidentFeature> twiceAccidentFeatures);
	}
}

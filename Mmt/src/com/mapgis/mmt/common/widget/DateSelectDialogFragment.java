package com.mapgis.mmt.common.widget;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.mapgis.mmt.R;

import java.util.Calendar;

public class DateSelectDialogFragment extends DialogFragment {

	Calendar c = Calendar.getInstance();
	String startDate;
	String endDate;

	private int mYear;
	private int mMonth;
	private int mDay;

	private long startTime;
	private long endTime;

	private OnDateSelectPositiveClick onDateSelectPositiveClick;
	private OnDateSelectNegativeClick onDateSelectNegativeClick;

	public void setOnDateSelectPositiveClick(OnDateSelectPositiveClick onDateSelectPositiveClick) {
		this.onDateSelectPositiveClick = onDateSelectPositiveClick;
	}
	public void setOnDateSelectNegativeClick(OnDateSelectNegativeClick onDateSelectNegativeClick) {
		this.onDateSelectNegativeClick = onDateSelectNegativeClick;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.date_select, container, false);

		getDialog().requestWindowFeature(STYLE_NO_TITLE);

		if (getActivity().getIntent().getLongExtra("startTime", 0) == 0) {
			c.setTimeInMillis(System.currentTimeMillis() - 86400000);
		} else {
			c.setTimeInMillis(getActivity().getIntent().getLongExtra("startTime", 0));
		}

		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		((TextView) view.findViewById(R.id.year_start)).setText(mYear + "");
		((TextView) view.findViewById(R.id.month_start)).setText((mMonth + 1) + "");
		((TextView) view.findViewById(R.id.day_start)).setText(mDay + "");

		view.findViewById(R.id.start_date_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 日期对话框
				new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						c.set(Calendar.YEAR, year);
						c.set(Calendar.MONTH, monthOfYear);
						c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						startDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

						startTime = c.getTimeInMillis();

						((TextView) getView().findViewById(R.id.year_start)).setText(year + "");
						((TextView) getView().findViewById(R.id.month_start)).setText((monthOfYear + 1) + "");
						((TextView) getView().findViewById(R.id.day_start)).setText(dayOfMonth + "");

					}
				}, mYear, mMonth, mDay).show();
			}
		});

		if (getActivity().getIntent().getLongExtra("endTime", 0) == 0) {
			c.setTimeInMillis(System.currentTimeMillis());
		} else {
			c.setTimeInMillis(getActivity().getIntent().getLongExtra("endTime", 0));
		}

		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		((TextView) view.findViewById(R.id.year_end)).setText(mYear + "");
		((TextView) view.findViewById(R.id.month_end)).setText((mMonth + 1) + "");
		((TextView) view.findViewById(R.id.day_end)).setText(mDay + "");

		view.findViewById(R.id.end_date_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 日期对话框
				new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						c.set(Calendar.YEAR, year);
						c.set(Calendar.MONTH, monthOfYear);
						c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						endDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

						endTime = c.getTimeInMillis();

						((TextView) getView().findViewById(R.id.year_end)).setText(year + "");
						((TextView) getView().findViewById(R.id.month_end)).setText((monthOfYear + 1) + "");
						((TextView) getView().findViewById(R.id.day_end)).setText(dayOfMonth + "");

					}
				}, mYear, mMonth, mDay).show();
			}
		});

		view.findViewById(R.id.date_dialog_ok).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startDate = ((TextView) view.findViewById(R.id.year_start)).getText() + "-"
						+ ((TextView) view.findViewById(R.id.month_start)).getText() + "-"
						+ ((TextView) view.findViewById(R.id.day_start)).getText();
				endDate = ((TextView) view.findViewById(R.id.year_end)).getText() + "-"
						+ ((TextView) view.findViewById(R.id.month_end)).getText() + "-"
						+ ((TextView) view.findViewById(R.id.day_end)).getText();

				onDateSelectPositiveClick.setOnDateSelectPositiveClickListener(getView(), startDate, endDate, startTime, endTime);

				dismiss();
			}
		});

		view.findViewById(R.id.date_dialog_cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onDateSelectNegativeClick != null) {
					onDateSelectNegativeClick.setOnDateSelectNegativeClickListener(getView());
				}
				dismiss();
			}
		});

		return view;
	}

	public interface OnDateSelectPositiveClick {
		void setOnDateSelectPositiveClickListener(View view, String startDate, String endDate, long startTime, long endTime);
	}

	public interface OnDateSelectNegativeClick {
		void setOnDateSelectNegativeClickListener(View view);
	}
}

package com.customform.view;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.sleepbot.base.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.mapgis.mmt.common.util.BaseClassUtil.validateDateValue;

/**
 * "仅时间"、"仅日期"、"日期框"、"仅日期V2"、"日期框V2"
 */
public class MmtDateTimeView extends MmtBaseView {

    public MmtDateTimeView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        switch (control.Type) {
            case "日期框":
            case "日期框V2":
                return R.drawable.form_date_time;
            case "仅日期":
            case "仅日期V2":
                return R.drawable.form_date;
            case "仅时间":
                return R.drawable.form_time;
            default:
                return R.drawable.form_date_time;
        }
    }

    public ImageButtonView build() {
        final ImageButtonView view = new ImageButtonView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        view.setValue(getDefaultDateTimeValue());

        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }
        view.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDateTime(view);
            }
        });

        return view;
    }

    @Override
    protected View buildReadonlyView() {
        ImageTextView view = new ImageTextView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        String defaultValue = getDefaultValue();
        view.setValue(BaseClassUtil.validateDateValue(defaultValue) ? defaultValue : EMPTY_STRING);
        return view;
    }

    private void pickDateTime(final ImageButtonView view) {

        final boolean showTimePicker = !"仅日期".equals(control.Type);

        final StringBuilder result = new StringBuilder();
        Calendar calendar = Calendar.getInstance();

        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout v, int hourOfDay, int minute) {

                        result.append(result.length() > 0 ? " " : "")
                                .append(hourOfDay >= 10 ? hourOfDay : "0" + hourOfDay).append(":")
                                .append(minute >= 10 ? minute : "0" + minute).append(":00");
                        view.setValue(result.toString());
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true, false);
        timePickerDialog.setCloseOnSingleTapMinute(false);

        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog datePickerDialog,
                                          int year, int month, int day) {

                        if (showTimePicker) {
                            timePickerDialog.show(getActivity().getSupportFragmentManager(), "");
                        }

                        month += 1;
                        result.append(year).append("-")
                                .append(month >= 10 ? month : "0" + month)
                                .append("-").append(day >= 10 ? day : "0" + day);
                        view.setValue(result.toString());
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), false);

        datePickerDialog.setYearRange(1985, 2028);
        datePickerDialog.setCloseOnSingleTapDay(false);

        DialogFragment shownDialogFragment = "仅时间".equals(control.Type) ? timePickerDialog : datePickerDialog;
        shownDialogFragment.show(getActivity().getSupportFragmentManager(), "");
    }

    private String getDefaultDateTimeValue() {

        // have default value
        if (!TextUtils.isEmpty(control.Value)) {
            return validateDateValue(control.Value) ? control.Value : "";
        }
        // default empty
        if ("默认为空".equals(control.ConfigInfo)) {
            return "";
        }
        // default current datetime
        String dateFormat;
        switch (control.Type) {
            case "日期框":
                dateFormat = "yyyy-MM-dd HH:mm:ss";
                break;
            case "仅日期":
                dateFormat = "yyyy-MM-dd";
                break;
            case "仅时间":
                dateFormat = "HH:mm:ss";
                break;
            default:
                dateFormat = "yyyy-MM-dd HH:mm:ss";
                break;
        }
        return new SimpleDateFormat(dateFormat, Locale.CHINA).format(new Date());
    }
}

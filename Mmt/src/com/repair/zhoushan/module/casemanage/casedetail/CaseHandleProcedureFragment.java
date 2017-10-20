package com.repair.zhoushan.module.casemanage.casedetail;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.EventLogItem;

import java.util.List;
import java.util.UUID;

public class CaseHandleProcedureFragment extends Fragment {

    private Activity mActivity;

    private LinearLayout parentLayout;
    private CaseItem itemEntity;
    private LineLinearLayout mLineLinearLayout;

    public CaseHandleProcedureFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (Activity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.itemEntity = getArguments().getParcelable("ListItemEntity");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CommonModule/EventLogQuery";

                return NetUtil.executeHttpGet(url, "caseNo", params[0], "token", params[1]);
            }

            @Override
            protected void onPostExecute(String jsonResult) {

                ResultData<EventLogItem> resultData = Utils.json2ResultDataActivity(
                        EventLogItem.class, (BaseActivity) mActivity, jsonResult, "获取办理过程信息失败", true);
                if (resultData == null) return;

                setContent(resultData.DataList);
            }

        }.executeOnExecutor(MyApplication.executorService, itemEntity.CaseNo, UUID.randomUUID().toString());

//        ScrollView scrollView = new ScrollView(mActivity);
//        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//
//        parentLayout = new LinearLayout(mActivity);
//        parentLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));
//        parentLayout.setMinimumHeight(500);
//        parentLayout.setOrientation(LinearLayout.VERTICAL);
//        parentLayout.setGravity(Gravity.CENTER_HORIZONTAL);
//        parentLayout.setPadding(0, DimenTool.dip2px(mActivity, 10), 0, 0);
//
//        scrollView.addView(parentLayout);
//        return scrollView;

        View view = inflater.inflate(R.layout.handle_procedure_view, null);
        mLineLinearLayout = (LineLinearLayout) view.findViewById(R.id.line_layout);
        return view;

    }

    public void setContent(List<EventLogItem> caseProcedures) {

        if (mActivity == null) {
            return;
        }

        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);

        for (EventLogItem eventLogItem : caseProcedures) {

            View view = layoutInflater.inflate(R.layout.handle_procedure_item, mLineLinearLayout, false);
            ((TextView) view.findViewById(R.id.txt_action)).setText(eventLogItem.FlowName + " - " + eventLogItem.NodeName);
            ((TextView) view.findViewById(R.id.txt_action_undertakeman)).setText("承办人：" + eventLogItem.OperDepart + " - " + eventLogItem.OperName);
            ((TextView) view.findViewById(R.id.txt_action_desc)).setText("描述：" + (TextUtils.isEmpty(eventLogItem.Description) ? "无" : eventLogItem.Description));
            ((TextView) view.findViewById(R.id.txt_action_time)).setText(eventLogItem.OperTime);

            mLineLinearLayout.addView(view);

        }

//        for (int i = 0; i < caseProcedures.size(); i++) {
//
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//
//            if (0 != i) {
//                ImageView imageView = new ImageView(mActivity);
//                imageView.setLayoutParams(layoutParams);
//                imageView.setBackgroundResource(R.color.default_no_bg);
//                imageView.setImageResource(R.drawable.back_btn);
//
//                parentLayout.addView(imageView);
//            }
//
//            TextView textView = new TextView(mActivity);
//            textView.setLayoutParams(layoutParams);
//            textView.setTextAppearance(mActivity, R.style.default_text_medium_1);
//            textView.setText(caseProcedures.get(i).toString());
//            textView.setBackgroundResource(R.color.progressbar_blue);
//            textView.setTextColor(Color.WHITE);
//
//            parentLayout.addView(textView);
//        }
    }
}

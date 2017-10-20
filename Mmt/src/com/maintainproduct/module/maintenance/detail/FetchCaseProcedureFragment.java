package com.maintainproduct.module.maintenance.detail;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.maintainproduct.entity.CaseProcedure;
import com.maintainproduct.entity.CaseProcedureResult;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.R;

import java.util.List;

public class FetchCaseProcedureFragment extends Fragment {
    private LinearLayout parentLayout;

    private final MaintainSimpleInfo itemEntity;

    public FetchCaseProcedureFragment(MaintainSimpleInfo itemEntity) {
        this.itemEntity = itemEntity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        parentLayout = new LinearLayout(getActivity());
        parentLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        parentLayout.setMinimumHeight(500);
        parentLayout.setOrientation(LinearLayout.VERTICAL);
        parentLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        scrollView.addView(parentLayout);

        new FetchCaseProcedureTask().executeOnExecutor(MyApplication.executorService, itemEntity.CaseNo);

        return scrollView;
    }

    public void setContent(List<CaseProcedure> caseProcedures) {

        if (getActivity() == null) {
            return;
        }

        for (int i = 0; i < caseProcedures.size(); i++) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);

            if (0 != i && (i - 1 >= 0 && !caseProcedures.get(i).StepName.equals(caseProcedures.get(i - 1).StepName))) {
                ImageView imageView = new ImageView(getActivity());
                imageView.setLayoutParams(layoutParams);
                imageView.setBackgroundResource(R.color.default_no_bg);
                imageView.setImageResource(R.drawable.back_btn);

                parentLayout.addView(imageView);
            }
            String text = caseProcedures.get(i).toString();
            int index = -1;
//            if (i+1 <caseProcedures.size() && caseProcedures.get(i + 1).HandOverDirection < 0) {
//                index = text.indexOf("{承办}");
//                text=text.replace("{承办}", "回退");
//            } else {
//                text=text.replace("{承办}", "承办");
//            }
            if (caseProcedures.get(i).UndertakeOpinion!=null&&caseProcedures.get(i).UndertakeOpinion.contains("退单")) {
                index = text.indexOf("{承办}");
                text = text.replace("{承办}", "回退");
            } else {
                text = text.replace("{承办}", "承办");
            }
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            if (index >= 0) {
                builder.setSpan(new ForegroundColorSpan(Color.RED), index, index+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            TextView textView = new TextView(getActivity());
            textView.setLayoutParams(layoutParams);
            textView.setTextAppearance(getActivity(), R.style.default_text_medium_1);
            textView.setText(builder);
            textView.setBackgroundResource(R.color.progressbar_blue);
            textView.setTextColor(Color.WHITE);

            parentLayout.addView(textView);
        }

    }

    class FetchCaseProcedureTask extends AsyncTask<String, Integer, CaseProcedureResult> {

        @Override
        protected CaseProcedureResult doInBackground(String... params) {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/RepairStandardRest.svc/FetchCaseProcedure";
            String jsonStr = NetUtil.executeHttpGet(url, "caseNo", params[0]);

            jsonStr = jsonStr.replace("[\"[", "[[").replace("]\"]", "]]");
            jsonStr = jsonStr.replace("\\", "");

            CaseProcedureResult resultData = new Gson().fromJson(jsonStr, CaseProcedureResult.class);

            return resultData;
        }

        @Override
        protected void onPostExecute(CaseProcedureResult result) {
            if (result.ResultCode > 0) {
                setContent(result.DataList.get(0));
            }
        }
    }
}

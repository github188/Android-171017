package com.repair.quanzhou.module;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;

/**
 * Created by liuyunfan on 2016/1/20.
 */
public class GDQueryActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleAndClear("工单查询");
        addFragment(new GDQueryFragment());
    }
    @Override
    public void onCustomBack() {
        super.backByReorder();
    }
    public static class GDQueryFragment extends Fragment {
        private EditText gdcasenoEditText;
        private Button confirmBtn;
        private BaseActivity context;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.qz_gd_query, container, false);
            gdcasenoEditText = (EditText) view.findViewById(R.id.casenoEditText);
            confirmBtn = (Button) view.findViewById(R.id.confirmBtn);
            context = (BaseActivity) getActivity();
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            confirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String caseno = gdcasenoEditText.getText().toString();
                    if (caseno.trim().length() == 0) {
                        Toast.makeText(context, "工单编号不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(context, GDDetailActivity.class);
                    intent.putExtra("caseno", caseno);
                    context.startActivity(intent);

                }
            });
        }
    }
}

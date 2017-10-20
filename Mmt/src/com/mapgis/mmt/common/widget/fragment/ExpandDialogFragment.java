package com.mapgis.mmt.common.widget.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mapgis.mmt.R;

/**
 * Created by cmios on 2017/3/16.
 */

public class ExpandDialogFragment extends DialogFragment {
    private   String title;
    private   String detailScription;
    private View myView;
    public ExpandDialogFragment(String title,View myView){
        this.title = title;
        this.myView = myView;
    }
    public ExpandDialogFragment(String title,String detailScription){
        this.title = title;
        this.detailScription =detailScription;

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view1 = inflater.inflate(R.layout.expand_btn_fragment, container,true);
        final TextView textView = (TextView) view1.findViewById(R.id.tv_tips);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        textView.setText(title);
        Button button = (Button) view1.findViewById(R.id.expand_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TextView textView = (TextView) view1.findViewById(R.id.tv_tips);
                textView.setText(detailScription);
            }
        });
        return view1;

    }
}

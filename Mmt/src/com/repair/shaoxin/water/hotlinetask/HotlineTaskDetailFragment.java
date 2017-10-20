package com.repair.shaoxin.water.hotlinetask;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapgis.mmt.R;

public class HotlineTaskDetailFragment extends Fragment {

    private HotlineTaskEntity hotlineTask;

    public static Fragment newInstance(HotlineTaskEntity hotlineTask) {
        HotlineTaskDetailFragment fragment = new HotlineTaskDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("HotlineTask", hotlineTask);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.hotlineTask = getArguments().getParcelable("HotlineTask");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.hotline_task_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showData(view);
    }

    private void showData(View view) {

        ((TextView) view.findViewById(R.id.hotlineDetailWorkTaskSeq)).setText(hotlineTask.workTaskSeq);
        ((TextView) view.findViewById(R.id.hotlineDetailSaveDate)).setText(hotlineTask.saveDate);
        ((TextView) view.findViewById(R.id.hotlineDetailComplainTypeName)).setText(hotlineTask.complainTypeName);
        ((TextView) view.findViewById(R.id.hotlineDetailComplainTypeinfoName)).setText(hotlineTask.complainTypeinfoName);
        ((TextView) view.findViewById(R.id.hotlineDetailComplainPerson)).setText(hotlineTask.complainPerson);

        TextView tvTel = (TextView) view.findViewById(R.id.hotlineDetailRelationTel);

        tvTel.setText(Html.fromHtml("<u>" + hotlineTask.relationTel + "</u>"));

        tvTel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String number = ((TextView) v).getText().toString();

                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));

                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        ((TextView) view.findViewById(R.id.hotlineDetailCustomerId)).setText(hotlineTask.customerId);

        ((TextView) view.findViewById(R.id.hotlineDetailAddressTablet)).setText(hotlineTask.addressTablet);
        ((TextView) view.findViewById(R.id.hotlineDetailDealtimeLongdate)).setText(hotlineTask.dealtimeLongdate);
        ((TextView) view.findViewById(R.id.hotlineDetailQuestionMemo)).setText(hotlineTask.questionMemo);
        ((TextView) view.findViewById(R.id.hotlineDetailMemo)).setText(hotlineTask.memo);
        ((TextView) view.findViewById(R.id.hotlineDetailServiceTime)).setText(hotlineTask.serviceTime);
        ((TextView) view.findViewById(R.id.hotlineDetailStateName)).setText(hotlineTask.stateName);

        HotlinePhotoFragment takePhotoFragment = HotlinePhotoFragment.newInstance("", hotlineTask.addFileSave);
        takePhotoFragment.setAddEnable(false);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fl_attachment_container, takePhotoFragment)
                .commitAllowingStateLoss();
    }

}

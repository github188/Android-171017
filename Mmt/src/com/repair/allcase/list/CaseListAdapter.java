package com.repair.allcase.list;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.R;
import com.repair.allcase.AllCaseActivity;
import com.repair.allcase.detail.CaseDetailFragment;
import com.repair.common.CaseItem;

import java.util.List;

public class CaseListAdapter extends BaseAdapter {
    protected final AllCaseActivity activity;
    protected final List<CaseItem> caseItems;
    protected final LayoutInflater inflater;

    public CaseListAdapter(BaseActivity activity, List<CaseItem> caseItems) {
        this.activity = (AllCaseActivity) activity;
        this.caseItems = caseItems;

        this.inflater = LayoutInflater.from(activity);
    }

    @Override
    public int getCount() {
        return caseItems.size();
    }

    @Override
    public Object getItem(int position) {
        return caseItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.wx_case_list_item, null);

        try {
            final CaseItem item = (CaseItem) getItem(position);

            NetworkImageView ivImage = (NetworkImageView) convertView.findViewById(R.id.ivImage);

            ivImage.setDefaultImageResId(R.drawable.no_image);
            ivImage.setErrorImageResId(R.drawable.no_image);

            if (!TextUtils.isEmpty(item.Picture)) {
                String url = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath()
                        + "/OutFiles/UpLoadFiles/" + item.Picture.split(",")[0];

                url = Uri.encode(url, ":/");

                ivImage.setImageUrl(url, MyApplication.getInstance().imageLoader);
            } else
                ivImage.setImageUrl(null, null);

            ((TextView) convertView.findViewById(R.id.tvCaseID)).setText(item.CaseCode);

            String delay = "";

            if (!TextUtils.isEmpty(item.Direction) && Integer.valueOf(item.Direction) < 0)
                delay += "审核打回";

            String state = item.DelayRequestState;

            if (!TextUtils.isEmpty(state) && !state.equals("审核不通过")) {
                switch (state) {
                    case "待审核":
                        delay += delay.length() > 0 ? "/延期待审核" : "延期待审核";
                        break;
                    case "审核通过":
                        delay += delay.length() > 0 ? "/已延期" : "已延期";
                        break;
                }
            }

            ((TextView) convertView.findViewById(R.id.tvDelay)).setText(delay.length() > 0 ? ("(" + delay + ")") : "");

            if (item.State.equals("已完工")) {
                convertView.findViewById(R.id.ivStatus).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.tvState).setVisibility(View.GONE);
            } else {
                convertView.findViewById(R.id.ivStatus).setVisibility(View.GONE);
                convertView.findViewById(R.id.tvState).setVisibility(View.VISIBLE);

                ((TextView) convertView.findViewById(R.id.tvState)).setText(item.State);
            }

            ImageView imageView = (ImageView) convertView.findViewById(R.id.ivIsRead);

            if (item.IsRead.equals("未阅读")) {
                imageView.setImageResource(R.drawable.msg_new);
            } else {
                imageView.setImageResource(R.drawable.msg_read);
            }

            ((TextView) convertView.findViewById(R.id.tvEventClass)).setText(item.EventClass);
            ((TextView) convertView.findViewById(R.id.tvEventType)).setText(item.EventType);

            TextView tvRemainTime = (TextView) convertView.findViewById(R.id.tvRemainTime);

            item.calculateRemainTime();

            if (item.intervalTime >= 0)
                tvRemainTime.setTextColor(activity.getResources().getColor(R.color.red));
            else if (item.intervalTime >= -3 * 24 * 60 * 60 * 1000)
                tvRemainTime.setTextColor(activity.getResources().getColor(R.color.goldenrod));
            else
                tvRemainTime.setTextColor(activity.getResources().getColor(R.color.limegreen));

            tvRemainTime.setText(item.intervalTimeStr);

            String desc = BaseClassUtil.isNullOrEmptyString(item.Description) ? "(无描述信息)" : item.Description;
            ((TextView) convertView.findViewById(R.id.tvDesc)).setText(desc);

            String addr = BaseClassUtil.isNullOrEmptyString(item.Address) ? "(无地址信息)" : item.Address;
            ((TextView) convertView.findViewById(R.id.tvAddress)).setText(addr);

            Button btnLocation = (Button) convertView.findViewById(R.id.btnLocation);
            TextView tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);

            if (TextUtils.isEmpty(item.Position)) {
                tvDistance.setVisibility(View.GONE);
                btnLocation.setVisibility(View.GONE);
            } else {
                tvDistance.setVisibility(View.VISIBLE);
                tvDistance.setText(item.intervalDistanceStr);

                btnLocation.setVisibility(View.VISIBLE);
                btnLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BaseMapCallback callback = new ShowMapPointCallback(activity, item.Position,
                                item.CaseCode, item.Address, position);

                        MyApplication.getInstance().sendToBaseMapHandle(callback);
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return convertView;
    }

    public void onDetailClick(final CaseItem caseItem, String from) {
        activity.selectedItem = caseItem;

        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

//        transaction.addToBackStack(null);
//        transaction.replace(R.id.baseFragment, fragment);

//        transaction.setCustomAnimations(R.anim.slide_in_right,
//                R.anim.slide_out_left);

        Fragment fragment = manager.findFragmentByTag(CaseDetailFragment.class.getName());

        if (fragment == null) {
            fragment = new CaseDetailFragment();

            Bundle bundle = new Bundle();

            bundle.putParcelable("caseItem", caseItem);
            bundle.putString("from", from);

            fragment.setArguments(bundle);

            transaction.add(R.id.base_root_relative_layout, fragment, CaseDetailFragment.class.getName());
        }

        transaction.show(fragment);

        transaction.hide(manager.findFragmentByTag(CaseListFragment.class.getName()));

        transaction.commitAllowingStateLoss();
    }
}

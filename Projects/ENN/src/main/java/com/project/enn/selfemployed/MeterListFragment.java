package com.project.enn.selfemployed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.project.enn.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.CaseItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class  MeterListFragment extends Fragment {

    public static final int DEFAULT_REQUEST_CODE = 0x1001;
    public static final int ADD_INFO_REQUEST_CODE = 0x1002;

    private CaseItem caseItem;
    private final List<CommeicalChangeInfo> dataList = new ArrayList<>();

    private MeterListItemAdapter adapter;

    private CommeicalChangeInfo currentSelectedData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null || !args.containsKey("ListItemEntity")) {
            Toast.makeText(getActivity(), "获取参数失败", Toast.LENGTH_SHORT).show();
            return;
        }
        this.caseItem = args.getParcelable("ListItemEntity");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_meter_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    private void initView(View view) {

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.contents_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        adapter = new MeterListItemAdapter(getActivity());
        recyclerView.setAdapter(adapter);
    }

    private void initData() {

        MmtBaseTask<Void, Void, Results<CommeicalChangeInfo>> mmtBaseTask = new MmtBaseTask<Void, Void, Results<CommeicalChangeInfo>>(getActivity()) {
            @Override
            protected Results<CommeicalChangeInfo> doInBackground(Void... params) {

                Results<CommeicalChangeInfo> result;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetCommeicalChangeDataByEventCode"
                        + "?eventCode=" + caseItem.EventCode
                        + "&_token=" + UUID.randomUUID().toString();

                try {

                    String jsonResult = NetUtil.executeHttpGet(url);

                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取数据失败");
                    }

                    result = new Gson().fromJson(jsonResult, new TypeToken<Results<CommeicalChangeInfo>>() {
                    }.getType());

                } catch (Exception e) {
                    e.printStackTrace();
                    result = new Results<>("1001", e.getMessage());
                }

                return result;
            }

            @Override
            protected void onSuccess(Results<CommeicalChangeInfo> result) {

                ResultData<CommeicalChangeInfo> newResult = result.toResultData();

                if (newResult.ResultCode != 200) {
                    Toast.makeText(getActivity(), newResult.ResultMessage, Toast.LENGTH_SHORT).show();
                } else {
                    dataList.clear();
                    dataList.addAll(newResult.DataList);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private class MeterListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView tvNo; // 顺序号
        public final TextView tvSteelGrade; // 表钢号
        public final TextView tvType; // 类型 - 型号
        public final TextView tvInitialValue; // 表底数

        private CommeicalChangeInfo data; // 数据项的索引位置

        public MeterListItemViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            tvNo = (TextView) itemView.findViewById(R.id.itemIndex);
            tvSteelGrade = (TextView) itemView.findViewById(R.id.desc_top_left);
            tvType = (TextView) itemView.findViewById(R.id.desc_mid_left);
            tvInitialValue = (TextView) itemView.findViewById(R.id.desc_mid_bottom_left);

            itemView.findViewById(R.id.desc_top_right).setVisibility(View.GONE);
            itemView.findViewById(R.id.desc_bottom_left).setVisibility(View.GONE);
            itemView.findViewById(R.id.desc_bottom_right).setVisibility(View.GONE);
        }

        public void bindData(CommeicalChangeInfo info) {
            tvNo.setText(" ");
            tvSteelGrade.setText(info.SteelGrade);
            tvType.setText(String.format("%s - %s", info.Type, info.ModelType));
            tvInitialValue.setText(info.InitialValue);

            this.data = info;
        }

        @Override
        public void onClick(View v) {

            currentSelectedData = data;

            Intent intent = new Intent(getActivity(), MeterDetailActivity.class);
            intent.putExtra("ListItemEntity", caseItem);
            intent.putExtra("CommercialChangeInfo", data);
            startActivityForResult(intent, DEFAULT_REQUEST_CODE);
        }
    }

    private final class MeterListItemAdapter extends RecyclerView.Adapter<MeterListItemViewHolder> {

        private LayoutInflater layoutInflater;

        public MeterListItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public MeterListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = layoutInflater.inflate(R.layout.base_list_item, parent, false);
            return new MeterListItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MeterListItemViewHolder holder, int position) {
            holder.bindData(dataList.get(position));
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case DEFAULT_REQUEST_CODE:
                    removeSelectedItem(); // 删除成功后本地更新数据列表
                    break;
                case ADD_INFO_REQUEST_CODE:
                    updateData(); // 添加数据后重新请求列表数据
                    break;
            }
        }
    }

    private void removeSelectedItem() {
        int index = dataList.indexOf(currentSelectedData);
        dataList.remove(currentSelectedData);
        currentSelectedData = null;
        adapter.notifyItemRemoved(index);
    }

    public void updateData() {
        initData();
    }
}

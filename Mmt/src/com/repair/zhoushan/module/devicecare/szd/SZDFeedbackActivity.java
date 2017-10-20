package com.repair.zhoushan.module.devicecare.szd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultStatus;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.BizFlowNodeMeta;
import com.repair.zhoushan.entity.FeedbackData;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.zbar.lib.CaptureActivity;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SZDFeedbackActivity extends BaseActivity {
    private ScheduleTask mScheduleTask;

    private BizFlowNodeMeta meta;
    // 界面结构数据
    private FlowNodeMeta mFlowNodeMeta;

    private FlowBeanFragment mFlowBeanFragment;

    // 上报信息
    private final FeedbackData mFeedbackData = new FeedbackData();

    private int userId;
    private String bar;
    private boolean isSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");
        this.userId = MyApplication.getInstance().getUserId();

        getBaseTextView().setText("设备反馈");

        startActivityForResult(new Intent(this, CaptureActivity.class), 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            try {
                String code = data.getExtras().getString("code");

                if (code == null || code.length() == 0) {
                    Toast.makeText(this, "扫码结果为空", Toast.LENGTH_LONG).show();

                    return;
                }

                int len = code.split(",").length;

                if (len != 3) {
                    Toast.makeText(this, "扫码结果[" + code + "]不符合标准[设备类型,编码,反馈类型]", Toast.LENGTH_LONG).show();

                    return;
                }

                this.bar = code.replace(",", "-");

                initView();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initView() {
        new MmtBaseTask<String, Void, String>(SZDFeedbackActivity.this, true) {
            @Override
            protected String doInBackground(String... params) {
                // 界面结构
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/FetchSZDTableMeta";

                return NetUtil.executeHttpGet(url, "userID", userID, "bar", bar);
            }

            @Override
            protected void onSuccess(String json) {
                try {
                    ResultData<BizFlowNodeMeta> data = Utils.resultDataJson2ResultDataToast(BizFlowNodeMeta.class,
                            SZDFeedbackActivity.this, json, "获取界面结构失败", false);

                    if (data == null || data.DataList == null || data.DataList.size() == 0
                            || data.getSingleData().MetaList == null || data.getSingleData().MetaList.size() == 0) {
                        Toast.makeText(SZDFeedbackActivity.this, "没有获取到反馈字段", Toast.LENGTH_SHORT).show();

                        return;
                    }

                    meta = data.getSingleData();
                    mFlowNodeMeta = meta.MetaList.get(0);

                    mScheduleTask = new ScheduleTask();

                    mScheduleTask.BizName = meta.BizName;
                    mScheduleTask.TaskCode = meta.TaskCode;
                    mScheduleTask.BizFeedBackTable = meta.BizFeedBackTable;

                    createView();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.mmtExecute();
    }

    private void createView() {
        this.mFlowBeanFragment = new FlowBeanFragment();

        Bundle args = new Bundle();

        args.putParcelable("GDFormBean", mFlowNodeMeta.mapToGDFormBean());
        args.putString("CacheSearchParam", ("userId=" + userId + " and key='" + mScheduleTask.TaskCode + "'"));

        mFlowBeanFragment.setArguments(args);

        mFlowBeanFragment.setFragmentFileRelativePath(mScheduleTask.TaskCode);

        mFlowBeanFragment.setFilterCriteria(null);

        addFragment(mFlowBeanFragment);

        createBottomView();
    }

    private void createBottomView() {
        BottomUnitView materialUnitView = new BottomUnitView(SZDFeedbackActivity.this);

        materialUnitView.setContent("查看详情");

        addBottomUnitView(materialUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SZDFeedbackActivity.this, SZDDetailActivity.class);

                intent.putExtra("ListItemEntity", mScheduleTask);

                startActivity(intent);
            }
        });

        BottomUnitView feedbackUnitView = new BottomUnitView(SZDFeedbackActivity.this);

        feedbackUnitView.setContent("反馈上报");

        addBottomUnitView(feedbackUnitView, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                doFeedback();
            }
        });
    }

    private void doFeedback() {
        if (mFlowBeanFragment == null) {
            return;
        }

        try {
            List<FeedItem> feedItemList = mFlowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

            if (feedItemList == null) {
                return;
            }

            // 把Feedback的值映射到Value上,并去除不需要反馈的Value，效果是使Value的个数与FeedbackItem的数目保持一致
            // 两方面考虑： 1.缓存的是B状态的界面数据，重新打开界面会是A状态，B状态下的多余数据需要清除
            // 2.数据库中NULL与空字符串的区别
            Iterator<FlowNodeMeta.TableValue> valueIterator = mFlowNodeMeta.Values.iterator();
            FlowNodeMeta.TableValue value;
            boolean containItem;

            boolean[] missedIndexes = new boolean[feedItemList.size()];
            Arrays.fill(missedIndexes, true);
            FeedItem feedItem;

            // FeedbackItem -> Form (可能会有Form字段的缺失，Form中多余的字段删掉)
            while (valueIterator.hasNext()) {
                value = valueIterator.next();
                containItem = false;

                for (int i = 0; i < feedItemList.size(); i++) {
                    feedItem = feedItemList.get(i);
                    if (value.FieldName.equals(feedItem.Name)) {
                        value.FieldValue = feedItem.Value;
                        containItem = true;
                        missedIndexes[i] = false;
                        break;
                    }
                }
                if (!containItem) {
                    valueIterator.remove();
                }
            }

            // 将缺失的 Value加进来
            for (int i = 0; i < missedIndexes.length; i++) {
                if (missedIndexes[i]) {
                    feedItem = feedItemList.get(i);
                    mFlowNodeMeta.Values.add(mFlowNodeMeta.new TableValue(feedItem.Name, feedItem.Value));
                }
            }

            mFeedbackData.DataParam.flowNodeMeta = mFlowNodeMeta;
            mFeedbackData.TableName = mScheduleTask.BizFeedBackTable;
            mFeedbackData.DefaultParam = " : ";

            // 创建服务路径
            String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveFeedbackData";

            String data = new Gson().toJson(mFeedbackData, new TypeToken<FeedbackData>() {
            }.getType());

            // 将所有信息封装成后台上传的数据模型
            final ReportInBackEntity entity = new ReportInBackEntity(
                    data,
                    MyApplication.getInstance().getUserId(),
                    ReportInBackEntity.REPORTING,
                    uri,
                    UUID.randomUUID().toString(),
                    "",
                    mFlowBeanFragment.getAbsolutePaths(),
                    mFlowBeanFragment.getRelativePaths());

            // 先上报反馈数据，再更新任务表中的反馈记录Id字段
            new MmtBaseTask<ReportInBackEntity, String, ResultWithoutData>(this, true, new MmtBaseTask.OnWxyhTaskListener<ResultWithoutData>() {

                @Override
                public void doAfter(ResultWithoutData data) {
                    if (data == null) {
                        Toast.makeText(SZDFeedbackActivity.this, "反馈失败", Toast.LENGTH_SHORT).show();
                    } else if (data.ResultCode < 0) {
                        String errorMsg = "反馈失败";

                        if (!BaseClassUtil.isNullOrEmptyString(data.ResultMessage)) {
                            errorMsg = data.ResultMessage;
                        }

                        Toast.makeText(SZDFeedbackActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SZDFeedbackActivity.this, "反馈成功", Toast.LENGTH_SHORT).show();
                        success();
                    }
                }
            }) {
                @Override
                protected void onPreExecute() {
                    setCancellable(false);
                    super.onPreExecute();
                }

                @Override
                protected ResultWithoutData doInBackground(ReportInBackEntity... params) {
                    ResultWithoutData resultWithoutData = null;

                    try {
                        // 反馈信息上报
                        ResultData<Integer> resultDatas = params[0].report(this);

                        if (resultDatas.ResultCode < 0) {
                            return resultDatas;
                        }

                        // ReportInBackEntity的上报方法返回的DataList第一个值是网络返回的状态码
                        if (resultDatas.DataList.size() < 2 || resultDatas.DataList.get(1) < 0) {
                            resultDatas.ResultCode = -100;
                            return resultDatas;
                        }

                        // 更新反馈表
                        int feedbackId = resultDatas.DataList.get(1);

                        // ==0：feedbackID已存在不需要更新；==-1：反馈失败；>0：返回新插入的id
                        if (feedbackId <= 0) {
                            resultDatas.ResultCode = -100;
                            return resultDatas;
                        }

                        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/UpdateYHFeedback";

                        String json = NetUtil.executeHttpGet(url, "userID", userID, "bizName", mScheduleTask.BizName,
                                "taskNo", mScheduleTask.TaskCode, "fbID", String.valueOf(feedbackId));

                        if (BaseClassUtil.isNullOrEmptyString(json)) {
                            resultWithoutData = new ResultWithoutData();

                            resultWithoutData.ResultMessage = "更新反馈信息失败";

                            return resultWithoutData;
                        }

                        resultWithoutData = new Gson().fromJson(json, new TypeToken<ResultWithoutData>() {
                        }.getType());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return resultWithoutData;
                }
            }.mmtExecute(entity);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void success() {
        isSuccess = true;

        // 上报成功后清除当前记录的缓存数据
        mFlowBeanFragment.deleteCacheData(userId, mScheduleTask.TaskCode);

        startActivity(new Intent(this, SZDFeedbackActivity.class));

        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 上报成功后就不需要再缓存
        if (!isSuccess && mFlowBeanFragment != null) {
            mFlowBeanFragment.saveCacheData(userId, mScheduleTask.TaskCode);
        }
    }
}
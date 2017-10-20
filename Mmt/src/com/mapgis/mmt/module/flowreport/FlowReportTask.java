package com.mapgis.mmt.module.flowreport;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.entity.BigFileInfo;
import com.mapgis.mmt.entity.SavedReportInfo;
import com.mapgis.mmt.module.taskcontrol.TaskControlEntity;
import com.mapgis.mmt.net.BaseTask;
import com.mapgis.mmt.net.BaseTaskListener;

import java.util.List;
import java.util.UUID;

public class FlowReportTask extends BaseTask<String> {

    public Boolean reportBigFileFlag;

    public FlowReportTask(FlowReportTaskParameters paramTaskParameters) {
        super(paramTaskParameters);
        reportBigFileFlag = false;
    }

    public FlowReportTask(FlowReportTaskParameters paramTaskParameters, BaseTaskListener<String> paramTaskListener) {
        super(paramTaskParameters, paramTaskListener);
        reportBigFileFlag = false;
    }

    @Override
    public String execute() {
        try {

            FlowReportTaskParameters parameters = (FlowReportTaskParameters) this.actionInput;

            String url = parameters.getUrl();

            url += NetUtil.resolveRequestParams(parameters.generateRequestParams());

            String uuid = UUID.randomUUID().toString().replaceAll("-", "");

            // 如果需要上传大图和录音，刚将大图和录音保存到 BigFileInfo 表中
            if (this.reportBigFileFlag) {
                saveBigFile(parameters, uuid);
            }

            SavedReportInfo info = new SavedReportInfo(uuid, url, parameters.getMediaString(), parameters.getRecordString(),
                    "flow");

            long i = DatabaseHelper.getInstance().insert(info);

            if (i >= 0) {

//                List<SavedReportInfo> infos = DatabaseHelper.getInstance().query(SavedReportInfo.class,
//                        new SQLiteQueryParameters("taskId='" + info.getTaskId() + "'"));
//
//                if (infos != null && infos.size() > 0) {
//                    SavedReportInfo savedReportInfo = infos.get(0);
//                    TaskControlEntity entity = new TaskControlEntity(0, BaseClassUtil.getSystemTime(), "事件上报",
//                            savedReportInfo.getTaskId(), MyApplication.getInstance().getUserId(), SavedReportInfo.class.getName());
//                    entity.insertData();
//                }

                return info.getTaskId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setReportBigFileFlag(Boolean flag) {
        reportBigFileFlag = flag;
    }

    private void saveBigFile(FlowReportTaskParameters parameters, String uuid) {
        if (!BaseClassUtil.isNullOrEmptyString(parameters.getMediaString())) {
            String[] pics = parameters.getMediaString().split(",");
            BigFileInfo bgFile = null;
            for (int i2 = 0; i2 < pics.length; i2++) {
                bgFile = new BigFileInfo(uuid, "", pics[i2].trim(), "图片", "unreported");
                DatabaseHelper.getInstance().insert(bgFile);
                bgFile = null;
            }
        }

        if (!BaseClassUtil.isNullOrEmptyString(parameters.getRecordString())) {
            String[] recs = parameters.recPaths.split(",");
            BigFileInfo bgFile2 = null;
            for (int i2 = 0; i2 < recs.length; i2++) {
                bgFile2 = new BigFileInfo(uuid, "", recs[i2].trim(), "录音", "unreported");
                DatabaseHelper.getInstance().insert(bgFile2);
                bgFile2 = null;
            }
        }
    }
}

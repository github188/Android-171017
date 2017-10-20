package com.repair.shaoxin.water.hotlinetask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.OnNoDoubleClickListener;
import com.zondy.mapgis.android.graphic.Graphic;

import java.util.List;

public class HotlineTaskDetailActivity extends BaseActivity {

    private HotlineTaskEntity hotlineTask;

    private GDFormBean gdFormBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.hotlineTask = getIntent().getParcelableExtra("HotlineTask");

        if (MyApplication.getInstance().getConfigValue("sxHotlineAudio", 0) > 0) {
            this.gdFormBean = GDFormBean.generateSimpleForm(
                    new String[]{"DisplayName", "照片", "Name", "照片", "Type", "拍照"},
                    new String[]{"DisplayName", "录音", "Name", "录音", "Type", "录音"},
                    new String[]{"DisplayName", "描述", "Name", "描述", "Type", "短文本", "DisplayColSpan", "100"});
        } else {
            this.gdFormBean = GDFormBean.generateSimpleForm(
                    new String[]{"DisplayName", "照片", "Name", "照片", "Type", "拍照"},
                    new String[]{"DisplayName", "描述", "Name", "描述", "Type", "短文本", "DisplayColSpan", "100"});
        }

        createView();
        createBottomView();
    }

    private void createView() {

        getBaseTextView().setText("工单详情");

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.baseFragment);

        if (fragment == null) {

            fragment = HotlineTaskDetailFragment.newInstance(hotlineTask);
            addFragment(fragment);
        }

        ImageView ivLocate = getBaseRightImageView();

        ivLocate.setVisibility(View.VISIBLE);
        ivLocate.setImageResource(R.drawable.navigation_locate);

        ivLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MmtBaseTask<String, Integer, String>(HotlineTaskDetailActivity.this) {
                    @Override
                    protected String doInBackground(String... params) {
                        String loc = "";

                        try {
                            List<Graphic> graphics = GisQueryUtil.conditionQuery("用户", "用户号='" + hotlineTask.customerId + "'");

                            if (graphics != null && graphics.size() > 0)
                                loc = graphics.get(0).getCenterPoint().toString();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        return loc;
                    }

                    @Override
                    protected void onSuccess(String loc) {
                        try {
                            if (TextUtils.isEmpty(loc))
                                Toast.makeText(HotlineTaskDetailActivity.this, "用户号无效", Toast.LENGTH_SHORT).show();

                            BaseMapCallback callback = new ShowMapPointCallback(context, loc, hotlineTask.customerId, "", -1);

                            MyApplication.getInstance().sendToBaseMapHandle(callback);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.mmtExecute();
            }
        });
    }

    private void createBottomView() {

        addBottomUnitView("到达", false, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                Intent intent = new Intent(HotlineTaskDetailActivity.this, HotlineTaskReportDialogActivity.class);
                intent.putExtra("Tag", "到达");
                intent.putExtra("Title", "到达上报");
                intent.putExtra("TaskID", hotlineTask.workTaskSeq);
                intent.putExtra("NodeType", 1);
                intent.putExtra("GDFormBean", gdFormBean);

                startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
            }
        });

        addBottomUnitView("处理", false, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                Intent intent = new Intent(HotlineTaskDetailActivity.this, HotlineTaskReportDialogActivity.class);
                intent.putExtra("Title", "处理上报");
                intent.putExtra("Tag", "处理");
                intent.putExtra("TaskID", hotlineTask.workTaskSeq);
                intent.putExtra("NodeType", 2);
                intent.putExtra("GDFormBean", gdFormBean);

                startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
            }
        });

        addBottomUnitView("销单", false, new OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {

                Intent intent = new Intent(HotlineTaskDetailActivity.this, HotlineTaskReportDialogActivity.class);
                intent.putExtra("Title", "销单申请");
                intent.putExtra("Tag", "销单");
                intent.putExtra("TaskID", hotlineTask.workTaskSeq);
                intent.putExtra("NodeType", 3);
                intent.putExtra("GDFormBean", gdFormBean);

                startActivityForResult(intent, Constants.SPECIAL_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK); // 操作成功后返回需要刷新列表

            if (requestCode == Constants.SPECIAL_REQUEST_CODE) { // 销单操作成功后要关闭详情页面
                AppManager.finishActivity(this);
            }
        }
    }
}

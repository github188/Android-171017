package com.mapgis.mmt.common.attach;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.engine.SDCardManager;
import com.mapgis.mmt.entity.docment.Document;

import java.io.File;
import java.util.ArrayList;

/**
 * 手机文件系统的根目录
 */
public class RootFileActivity extends BaseActivity {
    private static final String TAG = RootFileActivity.class.getSimpleName();
    public static final int RESULT_CODE = 20;
    private static final int REQUEST_CODE = 10;

    private LinearLayout mLinearLayout = null;
    private ArrayList<Document> mDocSelects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseTextView().setText("文件列表");
        getLayoutInflater().inflate(R.layout.activity_attach_file
                , (ViewGroup) findViewById(R.id.baseFragment));

        mLinearLayout = (LinearLayout) findViewById(R.id.ll_root_directory);

        mDocSelects = new ArrayList<>();
        ArrayList<Document> docList = getIntent().getParcelableArrayListExtra("selected_doc");
        mDocSelects.addAll(docList);
        getRootDirectory();
    }

    /**
     * 得到手机的根目录
     */
    public void getRootDirectory() {
        String[] pathDesc = getResources().getStringArray(R.array.root_path);
        // 我的文件
        File myFile = new File(Battle360Util.getFixedPath(Battle360Util.GlobalPath.Media));
        addRootItem(pathDesc[0], R.drawable.file_icon_folder, myFile.getParentFile().getAbsolutePath());
        Log.i(TAG, "我的文件：" + myFile.getParentFile().getAbsolutePath());
        // 手机存储
        final File dataDirectory = Environment.getRootDirectory();
        addRootItem(pathDesc[1], R.drawable.file_icon_folder, dataDirectory.getParentFile().getAbsolutePath());
        Log.i(TAG, "手机内存：" + dataDirectory.getParentFile().getAbsolutePath());

        // sd卡路径
        SDCardManager sdCardManager = new SDCardManager(this);
        String[] volumePaths = sdCardManager.getVolumePaths();

        for (int i = 0; i < volumePaths.length && i < 2; i++) {
            if ((new File(volumePaths[i])).list() == null) continue;
            addRootItem(pathDesc[i + 2], R.drawable.file_icon_folder, volumePaths[i]);
            Log.i(TAG, pathDesc[i + 2] + ":" + volumePaths[i]);
        }
    }

    private void addRootItem(final String pathName, int resId, final String absolutePath) {
        View view = View.inflate(this, R.layout.item_file_list, null);
        TextView tvFileName = (TextView) view.findViewById(R.id.tvFileName);
        tvFileName.setText(pathName);

        ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_file_icon);
        ivIcon.setImageResource(resId);
        mLinearLayout.addView(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterListFile(pathName, absolutePath);
            }
        });
    }

    private void enterListFile(String title, String absolutePath) {
        Log.i(TAG, "进入文件列表：" + absolutePath);
        Intent intent = new Intent(this, FileListActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("path", absolutePath);
        intent.putParcelableArrayListExtra("selected_doc", mDocSelects);
        startActivityForResult(intent, REQUEST_CODE);
        MyApplication.getInstance().startActivityAnimation(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_CODE && resultCode == RESULT_CODE) {
                ArrayList<Document> documents = data.getParcelableArrayListExtra("selected_doc");
                mDocSelects.clear();
                mDocSelects.addAll(documents);

                Log.i(TAG, "已经选中的文件：" + mDocSelects.toString());
                if (data.getBooleanExtra("btn_confirm", true)) {
                    // 点击确定按钮直接返回
                    onCustomBack();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCustomBack() {
        // 将选中的文件的路径返回
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("selected_doc", mDocSelects);
        this.setResult(RootFileActivity.RESULT_CODE, intent);

        super.onCustomBack();
    }
}

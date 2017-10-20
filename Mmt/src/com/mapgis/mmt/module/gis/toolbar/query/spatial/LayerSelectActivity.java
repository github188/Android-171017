package com.mapgis.mmt.module.gis.toolbar.query.spatial;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;

import com.mapgis.mmt.common.adapter.TreeAdapter;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.module.gis.toolbar.layercontrol.LayerManage;

/**
 * 选择可分组图层Activity
 * 注册为  Theme.Dialog 模式
 */
public class LayerSelectActivity extends LayerManage {

    @Override
    protected void onCurCreate() {
        isContainCheckBox = false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Node clickNode = (Node) ((TreeAdapter.ViewHolder) view.getTag()).mutilTreeItemSelect.getTag();

        if (clickNode.isLeaf()) {
            Intent intent = new Intent();

            intent.putExtra("layer", clickNode.getText());  // 返回图层名

            setResult(ResultCode.RESULT_LAYER_SELECTED, intent);

            finish();
        } else {
            // 根据点击的项是否 是 叶子节点  来重置 ListView 显示的数据源 ， 写在点击事件处理的后面，避免处理的是更新之后的数据源
            ((TreeAdapter) parent.getAdapter()).ExpandOrCollapse(position);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(Activity.RESULT_CANCELED);

            finish();
        }

        return true;
    }
}
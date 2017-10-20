package com.repair.zhoushan.module.devicecare.consumables;

import android.app.Activity;
import android.content.Intent;

import com.mapgis.mmt.common.widget.pinyinsearch.BaseEntity;
import com.mapgis.mmt.common.widget.pinyinsearch.PinyinSearchActivity;

import java.util.ArrayList;
import java.util.LinkedList;

public class AddPurchaseOrderChooseSupplierActivity extends PinyinSearchActivity {

    private ArrayList<String> costCenterData;

    @Override
    protected LinkedList<BaseEntity> getBaseEntities() {

        this.costCenterData = getIntent().getStringArrayListExtra("CostCenterData");

        LinkedList<BaseEntity> dataList = new LinkedList<BaseEntity>();
        CharacterEntity tempEntity;
        for (String data : costCenterData) {
            tempEntity = new CharacterEntity(data);
            dataList.add(tempEntity);
        }
        return dataList;
    }

    @Override
    protected void onResultSelected(BaseEntity baseEntity) {
        CharacterEntity characterEntity = (CharacterEntity) baseEntity;

        String result = characterEntity.getKey();

        Intent intent = new Intent();
        intent.putExtra("RESULT", result);
        intent.putExtra("INDEX", costCenterData.indexOf(result));
        setResult(Activity.RESULT_OK, intent);

        finish();
    }

}
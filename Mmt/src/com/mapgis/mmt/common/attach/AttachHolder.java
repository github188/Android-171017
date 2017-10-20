package com.mapgis.mmt.common.attach;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.mapgis.mmt.R;

/**
 * Created by Comclay on 2016/12/13.
 *
 */

public class AttachHolder extends RecyclerView.ViewHolder {
    public ImageView ivItem;
    public AttachHolder(View itemView) {
        super(itemView);
        ivItem = (ImageView) itemView.findViewById(R.id.ivItem);
    }
}

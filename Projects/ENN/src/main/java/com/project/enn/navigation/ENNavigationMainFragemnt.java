package com.project.enn.navigation;

import android.view.View;

import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.module.navigation.NavigationMainFragment;
import com.project.enn.util.ExitAppUtil;

/**
 * Created by Comclay on 2017/6/2.
 * 主菜单页
 */

public class ENNavigationMainFragemnt extends NavigationMainFragment {
    @Override
    protected void showLoginOutDialog() {
        try {
            OkCancelDialogFragment fragment = new OkCancelDialogFragment("是否退出");

            fragment.show(getActivity().getSupportFragmentManager(), "1");

            fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                @Override
                public void onRightButtonClick(View view) {
                    ExitAppUtil.exitDHZondy(getActivity());
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

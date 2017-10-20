package com.mapgis.mmt.module.navigation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.config.Product;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.NavigationMenuRegistry;
import com.mapgis.mmt.constant.RequestCode;
import com.mapgis.mmt.entity.MenuItem;
import com.mapgis.mmt.module.login.LoginUserInfo;
import com.mapgis.mmt.module.login.RoundImage;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.module.navigation.circular.CircleLayoutItemView;
import com.mapgis.mmt.module.navigation.circular.SemiCircleLayout;
import com.mapgis.mmt.module.navigation.normal.MyGridAdapter;
import com.mapgis.mmt.module.navigation.normal.MyGridView;
import com.mapgis.mmt.module.systemsetting.SystemSettingNavigationMenu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NavigationMainFragment extends Fragment implements NavigationActivity.FragmentCallback {

    public ArrayList<ArrayList<NavigationItem>> navigationGroup;

    private RoundImage navigationMenuIcon;
    UserBean user = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);
    private MyGridAdapter adapter;
    //    int navigationStyleResourceID = AppStyle.NavigationStyle.TRADITIONAL.getResourceId();
    int navigationStyleResourceID = AppStyle.NavigationStyle.CIRCULAR.getResourceId();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        navigationStyleResourceID = AppStyle.getNavigationStyleResource();

        View view = inflater.inflate(navigationStyleResourceID, container, false);

        try {

            TextView title = (TextView) view.findViewById(R.id.baseActionBarTextView);
            if (title != null) {
                title.setText(Product.getInstance().Title);
            }

            initMenuView(view);

            initDatasource();
            ArrayList<NavigationItem> items = new ArrayList<>();
            for (ArrayList<NavigationItem> list : navigationGroup) {
                items.addAll(list);
            }

            if (navigationStyleResourceID == AppStyle.NavigationStyle.TRADITIONAL.getResourceId()) {

                View mv = view.findViewById(R.id.navigationMainView);
                mv.setVisibility(View.GONE);

                LinearLayout rootLayout = (LinearLayout) mv.getParent();

                NavigationActivity activity = (NavigationActivity) getActivity();

                int w = DimenTool.getWidthPx(activity) - DimenTool.dip2px(activity, 10);
                int h = DimenTool.getHeightPx(activity) - DimenTool.dip2px(activity, 50);

                for (ArrayList<NavigationItem> navigationItems : navigationGroup) {
                    NavigationView v = new NavigationView((NavigationActivity) getActivity(), navigationItems);

                    if (navigationItems.size() < 2)
                        v.setLayoutParams(new ViewGroup.LayoutParams(w, (int) (h * 0.4)));
                    else if (navigationItems.size() < 4)
                        v.setLayoutParams(new ViewGroup.LayoutParams(w, (int) (h * 0.6)));
                    else
                        v.setLayoutParams(new ViewGroup.LayoutParams(w, h));

                    rootLayout.addView(v);
                }

            } else if (navigationStyleResourceID == AppStyle.NavigationStyle.NORMAL.getResourceId()) {

                adapter = new MyGridAdapter(getActivity(), items);
                MyGridView gvMain = (MyGridView) view.findViewById(R.id.gvMain);
                gvMain.setAdapter(adapter);

                gvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        String alias = ((TextView) view.findViewById(R.id.tv_item)).getText().toString();
                        for (ArrayList<NavigationItem> items : navigationGroup) {
                            for (NavigationItem item : items) {
                                if (item.Function.Alias.equals(alias)) {
                                    ((NavigationActivity) getActivity()).onNavigationItemClick(item);

                                    return;
                                }
                            }
                        }
                    }
                });

            } else if (navigationStyleResourceID == AppStyle.NavigationStyle.CIRCULAR.getResourceId()) {

                view.findViewById(R.id.mainActionBar).setVisibility(View.GONE);

                final SemiCircleLayout semiCircleLayout = (SemiCircleLayout) view.findViewById(R.id.semiCircleLayout);

                int maxCount = semiCircleLayout.getMaxShowCount();
                maxCount = 7;
                for (int i = 0, length = items.size(); i < maxCount && i < length; i++) {

                    NavigationItem navigationItem = items.get(i);

                    CircleLayoutItemView circleLayoutItemView = new CircleLayoutItemView(getActivity());
                    circleLayoutItemView.setMenuIconAndText(
                            NavigationMenuRegistry.getInstance().getMenuInstance((NavigationActivity) getActivity(), navigationItem).getIcons()[2],
                            navigationItem.Function.Alias);

                    semiCircleLayout.addView(circleLayoutItemView);

                    circleLayoutItemView.setOnTouchListener(new View.OnTouchListener() {
                        float startX;
                        float startY;
                        int left;
                        int top;
                        float dis = -1.0f;
                        boolean isCanceled;

                        @Override
                        public boolean onTouch(final View v, MotionEvent event) {
                            itemView = null;
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    isCanceled = false;
                                    System.out.println("手指按下！");

                                    startX = event.getRawX();
                                    startY = event.getRawY();

                                    orignalX = (int) v.getX();
                                    orignalY = (int) v.getY();

                                    ((CircleLayoutItemView) v).setTextVisibility(View.INVISIBLE);
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (isCanceled) {
                                        break;
                                    }
                                    int dx = (int) (event.getRawX() - startX);
                                    int dy = (int) (event.getRawY() - startY);

                                    dis = (int) Math.sqrt(Math.pow(v.getX() + v.getHeight() / 2 + dx - semiCircleLayout.getCenterPointOffsetX(), 2)
                                            + Math.pow(v.getY() + v.getHeight() / 2 + dy - semiCircleLayout.getCenterPointOffsetY(), 2));

                                    left = (int) (v.getX() + dx);
                                    top = (int) (v.getY() + dy);

                                    startX = event.getRawX();
                                    startY = event.getRawY();

                                    // 过滤掉点击菜单或者拖动的距离不够的操作
                                    if (dis < semiCircleLayout.getRadius() + v.getHeight()) {
                                        v.layout(left, top, left + v.getWidth(), top + v.getHeight());
                                        System.out.println("手指移动!");
                                    } else {
                                        // 取消
                                        System.out.println("取消这次触摸事件!");
                                        isCanceled = true;
                                        startX = 0;
                                        startY = 0;
                                        ((CircleLayoutItemView) v).reset(orignalX, orignalY);
                                    }
                                    break;
                                case MotionEvent.ACTION_UP:
                                    System.out.println("手指离开!");
                                    if (dis >= 0 && dis < semiCircleLayout.getRadius() / 3 * 2) {
                                        itemView = (CircleLayoutItemView) v;
                                        itemView.startAnimSet(new CircleLayoutItemView.AnimationCallBack() {
                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                // 菜单别名
                                                String alias = itemView.getMenuName();
                                                for (ArrayList<NavigationItem> navigationItems : navigationGroup) {
                                                    for (NavigationItem item : navigationItems) {
                                                        if (item.Function.Alias.equals(alias)) {
                                                            ((NavigationActivity) getActivity()).onNavigationItemClick(item);
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(getActivity(), "拖拽的距离不够", Toast.LENGTH_SHORT).show();
                                        ((CircleLayoutItemView) v).reset(orignalX, orignalY);
                                    }
                                    break;
                            }
                            return true;
                        }
                    });
                }
            }

            NavigationEntity navigationEntity = new NavigationEntity(items);

            Thread thread = NewTipThread.getInstance(getActivity(), navigationEntity);

            if (thread.getState() == Thread.State.NEW) {
                thread.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return view;
    }

    private int orignalX;
    private int orignalY;
    private CircleLayoutItemView itemView;

    @Override
    public void onResume() {
        super.onResume();
        if (navigationStyleResourceID == AppStyle.NavigationStyle.CIRCULAR.getResourceId() && itemView != null) {
            itemView.reset(orignalX, orignalY);
            itemView.clearAnimation();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);

            (view.findViewById(R.id.baseActionBarRightImageView)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLoginOutDialog();
                }
            });

            (view.findViewById(R.id.baseActionBarImageView)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                navigationMainView.toggle();
                    Intent intent = new Intent(getActivity(), LoginUserInfo.class);

                    startActivityForResult(intent, RequestCode.SHOW_USER_INFO);

                    MyApplication.getInstance().startActivityAnimation(getActivity());
                }
            });

            ((NavigationActivity) getActivity()).onFragmentViewCreated(view);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    @Override
//    public void onResume() {
//        try {
//            super.onResume();
//
//            if (user == null||navigationMenuIcon==null) {
//                return;
//            }
//
//            if (!BaseClassUtil.isNullOrEmptyString(user.Icon) && !user.Icon.equals("default_user.png")) {
//                File iconFile = new File(Environment.getExternalStorageDirectory() + "/MapGIS/UserImage/" + user.Icon);
//                if (iconFile.exists()) {
//                    navigationMenuIcon.setImageBitmap(FileZipUtil.getBitmapFromFile(iconFile));
//                } else {
//                    // 从服务器上取
//                    new DownLoadingUserIcoBitmap().execute(user.Icon);
//                }
//            } else {
//                // 用默认头像替换服务器上的默认卡通图像
//                navigationMenuIcon.setImageResource(R.drawable.default_user);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    private void initMenuView(View view) {
        try {
            if (user == null) {
                return;
            }

            // 显示头像
            navigationMenuIcon = (RoundImage) view.findViewById(R.id.navigationMenuIcon);

            if (navigationMenuIcon == null)
                return;

            navigationMenuIcon.setBorderOutsideColor(getResources().getColor(R.color.whitesmoke));
            navigationMenuIcon.setBorderThickness(DimenTool.dip2px(getActivity(), 1));

            navigationMenuIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LoginUserInfo.class);
                    startActivityForResult(intent, RequestCode.SHOW_USER_INFO);
                    MyApplication.getInstance().startActivityAnimation(getActivity());
                }
            });

            ((TextView) view.findViewById(R.id.navigationMenuUser)).setText(user.LoginName);
            ((TextView) view.findViewById(R.id.navigationMenuRole)).setText(user.Role);

            view.findViewById(R.id.navigationMenuOne).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LoginUserInfo.class);
                    startActivityForResult(intent, RequestCode.SHOW_USER_INFO);
                    MyApplication.getInstance().startActivityAnimation(getActivity());
                }
            });

            view.findViewById(R.id.navigationMenuTwo).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavigationActivity activity = (NavigationActivity) getActivity();
                    SystemSettingNavigationMenu menu = new SystemSettingNavigationMenu(activity, null);
                    menu.onItemSelected();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 弹出是否注销界面
     */
    protected void showLoginOutDialog() {
        try {
            OkCancelDialogFragment fragment = new OkCancelDialogFragment("是否退出");

            fragment.show(getActivity().getSupportFragmentManager(), "1");

            fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
                @Override
                public void onRightButtonClick(View view) {
                    NavigationController.exitApp(getActivity());
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 数据初始化 *
     */
    private void initDatasource() {

        if (navigationGroup == null) {
            navigationGroup = new ArrayList<>();
            navigationGroup = MyApplication.getInstance().getConfigValue("navigationGroup", navigationGroup.getClass());

            if (navigationGroup != null) {
                return;
            }
            navigationGroup = new ArrayList<>();

            ArrayList<MenuItem> items = Product.getInstance().MainMenus;

            if (items == null) {
                return;
            }

            int groupCount = items.size() % 8 == 0 ? items.size() / 8 : (items.size() / 8 + 1);

            for (int i = 0; i < groupCount; i++) {
                navigationGroup.add(new ArrayList<NavigationItem>());
            }

            for (int i = 0; i < items.size(); i++) {
                int index = i / 8;

                NavigationItem item = new NavigationItem();
                item.Function = items.get(i);

                navigationGroup.get(index).add(item);
            }
        }
    }

    @Override
    public void onNewTipReceive() {
        try {

            if (navigationStyleResourceID == AppStyle.NavigationStyle.NORMAL.getResourceId()) {

                adapter.notifyDataSetChanged();

            } else if (navigationStyleResourceID == AppStyle.NavigationStyle.TRADITIONAL.getResourceId()) {

                LinearLayout rootLayout = (LinearLayout) getActivity().findViewById(R.id.navigationMainView).getParent();

                for (int i = 0; i < rootLayout.getChildCount(); i++) {
                    View v = rootLayout.getChildAt(i);

                    if (v instanceof NavigationView)
                        v.postInvalidate();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ArrayList<ArrayList<NavigationItem>> getNavigationItemList() {
        return navigationGroup;
    }

    /**
     * 从服务器上获取图片，并保存到本地
     */
    class DownLoadingUserIcoBitmap extends AsyncTask<String, Void, Bitmap> {
        public byte[] getBytes(InputStream is) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            is.close();
            bos.flush();
            return bos.toByteArray();
        }

        public Bitmap getImage(String address) throws Exception {
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            InputStream is = conn.getInputStream();
            byte[] imagebytes = getBytes(is);
            return BitmapFactory.decodeByteArray(imagebytes, 0, imagebytes.length);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                String path = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath() + "/OutFiles/UpLoadFiles/UserImage/"
                        + params[0];
                return getImage(path);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {
                navigationMenuIcon.setImageBitmap(result);
                try {
                    File outputImage = new File(Battle360Util.getFixedPath("UserImage")
                            + MyApplication.getInstance().getUserId() + "user.png");

                    if (outputImage.exists() && !outputImage.delete()) {
                        return;
                    }

                    if (!outputImage.createNewFile())
                        return;

                    FileOutputStream out = new FileOutputStream(outputImage);
                    result.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

}

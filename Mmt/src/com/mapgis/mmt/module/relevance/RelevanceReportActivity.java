package com.mapgis.mmt.module.relevance;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.fragment.PhotoFragment;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.module.login.UserBean;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RelevanceReportActivity extends BaseActivity {

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addFragment(new RelevanceReportFragment());
    }

    class RelevanceReportFragment extends Fragment {
        private ListView listView;
        private FrameLayout frameLayout;

        private PhotoFragment photoFragment;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            RelativeLayout relativeLayout = new RelativeLayout(getActivity());
            relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            LayoutParams viewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            viewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            viewParams.setMargins(DimenTool.dip2px(getActivity(), 5), DimenTool.dip2px(getActivity(), 5),
                    DimenTool.dip2px(getActivity(), 5), DimenTool.dip2px(getActivity(), 5));

            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setId(linearLayout.hashCode());
            linearLayout.setLayoutParams(viewParams);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            ImageView imageView = new ImageView(getActivity());
            imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            imageView.setPadding(DimenTool.dip2px(getActivity(), 10), DimenTool.dip2px(getActivity(), 10),
                    DimenTool.dip2px(getActivity(), 10), DimenTool.dip2px(getActivity(), 10));
            imageView.setImageResource(R.drawable.flex_flow_takephoto);

            linearLayout.addView(imageView);

            frameLayout = new FrameLayout(getActivity());
            frameLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            frameLayout.setId(frameLayout.hashCode());

            linearLayout.addView(frameLayout);

            LayoutParams listViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            listViewParams.addRule(RelativeLayout.ABOVE, linearLayout.getId());

            listView = new ListView(getActivity());
            listView.setLayoutParams(listViewParams);
            listView.setBackgroundResource(R.color.white);
            listView.setSelector(R.drawable.item_focus_bg);

            relativeLayout.addView(listView);
            relativeLayout.addView(linearLayout);

            return relativeLayout;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            HashMap<String, String> graphicMap = (HashMap<String, String>) getIntent().getSerializableExtra("graphicMap");
            final String layerName = getIntent().getStringExtra("layerName");

            getBaseTextView().setText(layerName);

            photoFragment = new PhotoFragment.Builder("DevicePhoto/" + (new SimpleDateFormat("yyMMdd")).format(new Date()) + "/").build();
            photoFragment.setPrefixName("设备实图_");
            photoFragment.createFileByUUID();
            photoFragment.setPhotoBitmapWidth(120);
            photoFragment.setPhotoBitmapHeight(90);
            photoFragment.setCreateByOpenCamera(true);

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(frameLayout.getId(), photoFragment);
            transaction.commitAllowingStateLoss();

            String[] keys = getGISFields(layerName);

            if (keys != null) {

                List<String> cnKeys = filterEn(graphicMap, keys);

                RelevanceActivityAdapter adapter = new RelevanceActivityAdapter(getActivity(), graphicMap, cnKeys);
                listView.setAdapter(adapter);

                getBaseRightImageView().setVisibility(View.VISIBLE);
                getBaseRightImageView().setImageResource(R.drawable.flex_flow_report);
                getBaseRightImageView().setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String absolutePaths = photoFragment.getAbsolutePhoto();

                        if (BaseClassUtil.isNullOrEmptyString(absolutePaths)) {
                            Toast.makeText(getActivity(), "未拍摄照片", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (BaseClassUtil.isNullOrEmptyString(MobileConfig.MapConfigInstance.VectorService)) {
                            Toast.makeText(getActivity(), "请配置<VectorService>节点信息", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 策略:每一张照片对应一条数据，保证WEB端能立马看见照片
                        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/Zondy_MapGISCitySvr_Media/REST/MediaREST.svc/" + MobileConfig.MapConfigInstance.VectorService
                                + "/MediaServer/UpLoadDeviceMediaFile";

                        List<String> relativePaths = BaseClassUtil.StringToList(photoFragment.getRelativePhoto(), ",");

                        String result = null;

                        for (int i = 0; i < relativePaths.size(); i++) {

                            String absolutePath = photoFragment.getAbsolutePhotoList().get(i);

                            if (!new File(absolutePath).exists()) {
                                continue;
                            }

                            DeviceMedia deviceMedia = new DeviceMedia();
                            deviceMedia.cityServerIp = ServerConnectConfig.getInstance().getServerConfigInfo().IpAddress;
                            deviceMedia.elemGuid = getIntent().getStringExtra("deviceKey");
                            deviceMedia.fldName = getIntent().getStringExtra("fieldName");
                            deviceMedia.layerName = layerName;
                            deviceMedia.mapServerName = MobileConfig.MapConfigInstance.VectorService;
                            deviceMedia.path = relativePaths.get(i);
                            deviceMedia.uploader = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

                            String jsonStr = new Gson().toJson(deviceMedia, DeviceMedia.class);

                            ReportInBackEntity entity = new ReportInBackEntity(jsonStr, MyApplication.getInstance().getUserId(),
                                    ReportInBackEntity.REPORTING, url, UUID.randomUUID().toString(), "拍照上报", absolutePath, relativePaths
                                    .get(i));

                            long m = entity.insert();

                            if (m < 0) {
                                result = "插入本地数据库失败";
                            }
                        }

                        if (BaseClassUtil.isNullOrEmptyString(result)) {
                            AppManager.finishActivity();
                            showToast("保存成功,等待上传");
                        } else {
                            showToast(result);
                        }
                    }
                });
            }
        }

        private String[] getGISFields(String layerName) {
            if (BaseClassUtil.isNullOrEmptyString(layerName)) {
                return null;
            }

            SQLiteDatabase database = null;
            Cursor cursor = null;

            try {
                String name = MobileConfig.MapConfigInstance.getMapType(MapConfig.MOBILE_EMS).Name;

                String path = MyApplication.getInstance().getMapFilePath() + name + "/" + name + ".db";

                database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

                cursor = database.rawQuery("SELECT * FROM " + layerName + " WHERE 1=-1", null);

                String[] columnNames = cursor.getColumnNames();

                return columnNames;
            } catch (Exception ex) {
                ex.printStackTrace();

                return null;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }

                if (database != null) {
                    database.close();
                }
            }
        }

        /**
         * 过滤不必要的和英文的属性
         */
        private List<String> filterEn(HashMap<String, String> graphicMap, String[] keys) {
            List<String> cnList = new ArrayList<String>();

            for (String str : keys) {

                if (!graphicMap.containsKey(str)) {
                    continue;
                }

                // 跳过类似这种自定义的字段 $图层名称$
                if (str.startsWith("$") && str.endsWith("$")) {
                    continue;
                }

                // 判断key是否包含中文，如果没有中文不做显示
                boolean isExistChinese = false;

                for (char k : str.toCharArray()) {
                    isExistChinese = String.valueOf(k).matches("[\\u4e00-\\u9fa5]+");

                    if (isExistChinese) {
                        break;
                    }
                }

                if (!isExistChinese) {
                    continue;
                }

                cnList.add(str);
            }

            return cnList;
        }
    }

    public class RelevanceActivityAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final HashMap<String, String> hashMap;
        private final List<String> cnKeys;

        public RelevanceActivityAdapter(Context context, HashMap<String, String> hashMap, List<String> cnKeys) {
            mInflater = LayoutInflater.from(context);
            this.hashMap = hashMap;
            this.cnKeys = cnKeys;
        }

        @Override
        public int getCount() {
            return cnKeys.size();
        }

        @Override
        public Object getItem(int position) {
            return cnKeys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.asset_detail_props_item, null);
                holder = new ViewHolder();
                holder.assetKey = (TextView) convertView.findViewById(R.id.asset_key);
                holder.assetValue = (TextView) convertView.findViewById(R.id.asset_value);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String key = getItem(position).toString();
            String value = hashMap.get(key);

            holder.assetKey.setText(key);
            holder.assetValue.setText(TextUtils.isEmpty(value) ? "-" : value);

            return convertView;
        }

        class ViewHolder {
            public TextView assetKey;
            public TextView assetValue;
        }
    }
}

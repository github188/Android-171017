package com.patrol.module.posandpath;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.R;
import com.patrol.module.posandpath.beans.DeptBean;
import com.patrol.module.posandpath.beans.PathBean;
import com.patrol.module.posandpath.beans.PointInfo;
import com.patrol.module.posandpath.beans.UserBean;
import com.patrol.module.posandpath.path.PathOnMapCallBack;
import com.patrol.module.posandpath.position.PosOnMapCallBack;
import com.zondy.mapgis.geometry.Dot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by meiko on 2016/3/15 0015.
 */
public class PosAndPathFragment extends Fragment {

    private Context context;

    private RadioGroup radioGroup;

    private Spinner mSpinner;
    private ArrayAdapter spinnerAdapter;
    private ArrayList<String> depts;

    private PullToRefreshListView mPullToRefreshListView;
    private PosAndPathAdapter mPosAndPathAdapter;
    private ArrayList<UserBean.BodyInfo> list;

    private ArrayList<UserBean.BodyInfo> temp;

    private DeptBean userDeptBean;

    /**
     * "全部"  全部
     * 1  在线
     * 0  离线
     */
    private String state = "全部";   // 默认的状态为全部

    // 默认部门
    private String dept = "全部";

    // 在线人数
    private int onLineCount = 0;

    // 离线人数
    private int offLineCount = 0;

    // 总人数
    private int allCount = 0;

    private RadioButton rbAll;
    private RadioButton rbOnLine;
    private RadioButton rbOffLine;
    private TextView tvState;
    private TextView tvDept;
    private TextView tvTime;

    public String getState() {
        return state;
    }

    public String getDept() {
        return dept;
    }

    public PosAndPathFragment(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pos_and_path_view, container, false);

        initView(view);
        initData();

        return view;
    }

    private int mYear;
    private int mMonth;
    private int mDay;

    /**
     * 初始化布局
     *
     * @param view
     */
    private void initView(View view) {
        tvState = (TextView) view.findViewById(R.id.tv_state);
        tvDept = (TextView) view.findViewById(R.id.tv_dept);
        tvTime = (TextView) view.findViewById(R.id.tv_time);

        tvState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStateDialog();
            }
        });

        tvDept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeptDialog();
            }
        });

        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog();
            }
        });

//        radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                switch (i) {
//                    case 0:
//                        state = "???";
//                        break;
//                    case 1:
//                        state = "1";  // ????
//                        break;
//                    case 2:
//                        state = "0";  // ????
//                        break;
//                }
//                flushView();
//            }
//        });
//
//        rbAll = (RadioButton) view.findViewById(R.id.rbAll);
//        rbOnLine = (RadioButton) view.findViewById(R.id.rbOnLine);
//        rbOffLine = (RadioButton) view.findViewById(R.id.rbOffLine);

//        mSpinner = (Spinner) view.findViewById(R.id.mSpinner);
//        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                dept = depts.get(i);
//                flushView();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        mPullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.mPullToRefreshListView);
        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showDialog(i - 1);
            }
        });

        mPullToRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        mPullToRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        mPullToRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("释放以刷新");

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE);
                mPullToRefreshListView.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(label);
                getUserData();
            }
        });
    }

    private final static String[] STATES = {"全部", "在线", "离线"};

    /**
     * 选择状态的对话框
     */
    private void showStateDialog() {
        final String[] states = {"全部：" + allCount, "在线：" + onLineCount, "离线：" + offLineCount};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("状态");
        builder.setItems(states, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        state = "全部";
                        break;
                    case 1:
                        state = "1";
                        break;
                    case 2:
                        state = "0";
                        break;
                }
                tvState.setText(states[i]);
                flushView();
            }
        });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    /**
     * 选择部门的对话框
     */
    private void showDeptDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("部门");
        builder.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, depts)
                , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                tvDept.setText(depts.get(i));
                flushView();
            }
        });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    /**
     * 选择日期的对话框
     */
    private void showDateDialog() {
        DatePickerDialog mDatePickerDialog = new DatePickerDialog(
                context
                , new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        mYear = i;
                        mMonth = i1;
                        mDay = i2;
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(i, i1, i2);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String date = sdf.format(calendar.getTime());
                        Toast.makeText(context,"选中："+date,Toast.LENGTH_SHORT).show();
                        tvTime.setText(date);
                    }
                }
                , mYear
                , mMonth
                , mDay);
        mDatePickerDialog.setCanceledOnTouchOutside(true);
        mDatePickerDialog.show();
    }

    private void setDefaultDate(){
        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        tvTime.setText(sdf.format(calendar.getTime()));
    }

    /**
     * 设置不同状态的人数
     */
    private void setCount() {
        // 初始化为0
        allCount = 0;
        onLineCount = 0;
        offLineCount = 0;
        for (UserBean.BodyInfo info :
                list) {
            if ("全部".equals(dept)) {
                // 全部
                allCount = list.size();
                if ("1".equals(info.Perinfo.IsOline)) {
                    // 在线
                    onLineCount++;
                } else {
                    offLineCount++;
                }
            } else if (info.Perinfo.partment.equals(dept)) {
                allCount++;
                if ("1".equals(info.Perinfo.IsOline)) {
                    // 在线
                    onLineCount++;
                } else {
                    offLineCount++;
                }
            }
        }

        switch (state) {
            case "全部":
                tvState.setText("全部：" + allCount);
                break;
            case "1":
                tvState.setText("在线：" + onLineCount);
                break;
            case "0":
                tvState.setText("离线：" + offLineCount);
                break;
        }
//
//        rbAll.setText("???:" + allCount);
//        rbOnLine.setText("????:" + onLineCount);
//        rbOffLine.setText("????:" + offLineCount);
    }

    /**
     * 设置下拉列表要显示的用户集合
     */
    private void setTemp() {
        if (list == null || list.size() == 0) {
            getUserData();
        }
        temp.clear();
        setDept();
//        setState();
        setCount();
        for (UserBean.BodyInfo bodyInfo :
                list) {
            if ("全部".equals(state) && "全部".equals(dept)) {
                temp.add(bodyInfo);
            } else if (bodyInfo.Perinfo.IsOline.equals(state) && "全部".equals(dept)) {
                temp.add(bodyInfo);
            } else if ("全部".equals(state) && bodyInfo.Perinfo.partment.equals(dept)) {
                temp.add(bodyInfo);
            } else {
                if (bodyInfo.Perinfo.IsOline.equals(state) && bodyInfo.Perinfo.partment.equals(dept))
                    temp.add(bodyInfo);
            }
        }
    }

//    private void setState() {
//        int i = radioGroup.getCheckedRadioButtonId();
//        if (i == R.id.rbOnLine) {
//            state = "1";
//        } else if (i == R.id.rbOffLine) {
//            state = "0";
//        } else {
//            state = "???";
//        }
//    }

    private void setDept() {
//        dept = (String) mSpinner.getSelectedItem();
        dept = tvDept.getText().toString().trim();
    }


    private Dialog dialog;

    /**
     * 功能选择对话框
     *
     * @param position
     */
    private void showDialog(int position) {
        final UserBean.BodyInfo userInfo = temp.get(position);

        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:  // 取消
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:  // 定位
                        posit(userInfo);
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:   // 今日轨迹
                        todayPath(userInfo);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("详细信息");

        View view = View.inflate(context, R.layout.dialog_pos_path_view, null);
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText("姓名" + userInfo.Perinfo.name);

        TextView IsOline = (TextView) view.findViewById(R.id.IsOline);
        IsOline.setText("状态" + (userInfo.Perinfo.IsOline.equals("1") ? "在线" : "离线"));

        TextView partment = (TextView) view.findViewById(R.id.partment);
        partment.setText("部门" + userInfo.Perinfo.partment);

        TextView PHONE_NUMBER = (TextView) view.findViewById(R.id.PHONE_NUMBER);
        PHONE_NUMBER.setText("电话" + userInfo.Perinfo.PHONE_NUMBER);

        TextView Position = (TextView) view.findViewById(R.id.Position);
        Position.setText("位置" + userInfo.point.Position);

        builder.setView(view);
        builder.setPositiveButton("取消", onClickListener);
        builder.setNegativeButton("定位", onClickListener);
        builder.setNeutralButton("今日轨迹", onClickListener);

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    /**
     * 今日轨迹
     */
    private void todayPath(UserBean.BodyInfo userInfo) {
        // 获取今日轨迹信息
        getPath(userInfo.Perinfo.USERID);
    }

    private ArrayList<PointInfo> listInfos;

    /**
     * 获取轨迹信息
     */
    private void getPath(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String STTime = tvTime.getText().toString().trim() + " 00:00:00";
                String ymax = "0";
                String ENDTime = tvTime.getText().toString().trim() + " 23:59:59";
                String xmin = "0";
                String OptimizeTrace = "true";
                String xmax = "0";
                String f = "json";
                String OnlyWorkTime = "false";
                String time = (new Date()).toString();
                String ymin = "0";
                String IDList = id;
                String url = ServerConnectConfig.getInstance().getBaseServerPath() + "/services/zondy_mapgiscitysvr_newpatrol/rest/newpatrolrest.svc/GetPerHisPosition";
                String result = NetUtil.executeHttpGet(url
                        , "xmin", xmin
                        , "OnlyWorkTime", OnlyWorkTime
                        , "ymin", ymin
                        , "ENDTime", ENDTime
                        , "xmax", xmax
                        , "STTime", STTime
                        , "IDList", IDList
                        , "f", f
                        , "time", time
                        , "ymax", ymax
                        , "OptimizeTrace", OptimizeTrace
                );

                if (result != null) {
                    PathBean bean = new Gson().fromJson(result, PathBean.class);
                    if ("true".equals(bean.rntinfo.IsSuccess)) {
                        listInfos = new ArrayList<>();
                        PathBean.Path path = bean.Ppoint.get(0);
                        if (path.Ppoint != null && !path.Ppoint.equals("")) {
                            String[] points = path.Ppoint.split("\\|");

                            for (int i = 0; i < points.length; i++) {
                                String[] infos = points[i].split(",");

                                System.out.println(infos.toString());

                                String[] equipmentInfo = infos[7].split("_");

                                System.out.println(equipmentInfo.toString());

                                PointInfo info = new PointInfo(
                                        path.PerName,
                                        infos[8],
                                        infos[0],
                                        infos[1] + "," + infos[2],
                                        infos[6],
                                        equipmentInfo[1],
                                        equipmentInfo[2],
                                        equipmentInfo[3],
                                        equipmentInfo[0],
                                        infos[3],
                                        infos[4],
                                        infos[5]
                                );
                                System.out.println(info.toString());
                                listInfos.add(info);
                            }
                            handler.sendEmptyMessage(5);
                        } else {
                            // 没有数据
                            handler.sendEmptyMessage(4);
                        }
                    }
                } else {
                    handler.sendEmptyMessage(0);
                }
            }
        }).start();
    }

    /**
     * 定位
     */
    private void posit(final UserBean.BodyInfo userInfo) {
        String Position = userInfo.point.Position;

        String p[] = Position.split(",");

        final Dot dot = new Dot(Double.valueOf(p[0]), Double.valueOf(p[1]));

        MyApplication.getInstance().sendToBaseMapHandle(new PosOnMapCallBack(context, userInfo.Perinfo.name, dot));

        Toast.makeText(context, "开启定位功能", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(context, MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private boolean flag = false;

    private void flushView() {
        if (mPosAndPathAdapter != null && flag) {
            setTemp();
            mPosAndPathAdapter.setPpoint(temp);
            mPosAndPathAdapter.notifyDataSetChanged();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: // 失败
                    Toast.makeText(context, "数据获取失败", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    getDeptDataById();
                    break;
                case 2:
                    setTemp();
                    if (mPosAndPathAdapter == null) {
//                        mPosAndPathAdapter = new PosAndPathAdapter(context, temp);
//                        mPullToRefreshListView.setAdapter(mPosAndPathAdapter);
                    } else {
                        mPosAndPathAdapter.notifyDataSetChanged();
                    }
                    flag = true;
                    Toast.makeText(context, "刷新成功", Toast.LENGTH_SHORT).show();
                    mPullToRefreshListView.onRefreshComplete();
                    break;
                case 3:
                    tvDept.setText(userDeptBean.DeptName);
                    getUserData();
                    break;
                case 4:
                    Toast.makeText(context, "没有数据", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(context, "轨迹信息获取成功", Toast.LENGTH_SHORT).show();
                    MyApplication.getInstance().sendToBaseMapHandle(new PathOnMapCallBack(context, listInfos));

                    Intent intent = new Intent(context, MapGISFrame.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(intent);
                    break;
            }
        }
    };

    /**
     * 初始化数据
     */
    private void initData() {
        setDefaultDate();

        depts = new ArrayList<>();
        temp = new ArrayList<>();

//        spinnerAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, depts);
//        mSpinner.setAdapter(spinnerAdapter);
        mPosAndPathAdapter = new PosAndPathAdapter(context, temp);
        mPullToRefreshListView.setAdapter(mPosAndPathAdapter);
        mPullToRefreshListView.setRefreshing();

        depts.add("全部");
        getDeptData();
    }

    /**
     * 获取用户信息
     */
    private void getUserData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Date time = new Date();
                String url = ServerConnectConfig.getInstance().getBaseServerPath() + "/services/zondy_mapgiscitysvr_newpatrol/rest/newpatrolrest.svc/GetPatrolerPosition";
                String result = NetUtil.executeHttpGet(url, "IDList", "", "time", time.toString());
                UserBean bean = new Gson().fromJson(result, UserBean.class);
                list = bean.Ppoint;
                handler.sendEmptyMessage(2);
            }
        }).start();
    }

    /**
     * 获取部门信息
     */
    public void getDeptData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Date time = new Date();
                Boolean IsAll = true;
                String UserID = MyApplication.getInstance().getUserId() + "";
                String url = ServerConnectConfig.getInstance().getBaseServerPath() + "/services/zondy_mapgiscitysvr_newpatrol/rest/newpatrolrest.svc/GetDepartment";
                String result = NetUtil.executeHttpGet(url, "time", time.toString(), "IsAll", IsAll.toString(), "UserID", UserID);

                if (result == null) {
                    handler.sendEmptyMessage(0);
                } else {
                    try {
                        JSONArray array = new JSONArray(result);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = (JSONObject) array.get(i);
                            depts.add(object.getString("DeptName"));
                        }
                        handler.sendEmptyMessage(1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 根据用户的ID好获取对应的部门信息
     */
    public void getDeptDataById() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Date time = new Date();
                String UserID = MyApplication.getInstance().getUserId() + "";
                String url = ServerConnectConfig.getInstance().getBaseServerPath() + "/services/zondy_mapgiscitysvr_newpatrol/rest/newpatrolrest.svc/GetDepartmentByID";
                String result = NetUtil.executeHttpGet(url, "UserID", UserID, "time", time.toString());

                if (result == null) {
                    handler.sendEmptyMessage(0);
                } else {
                    result = result.replace("\"", "");
                    String[] info = result.split(",");
                    userDeptBean = new DeptBean(info[0], info[1]);
                    dept = userDeptBean.DeptName;
                    handler.sendEmptyMessage(3);
                }
            }
        }).start();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View getView() {
        return super.getView();
    }
}

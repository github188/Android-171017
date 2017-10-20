package com.repair.eventreport;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.v2.module.EventTypeItem;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.fragment.AccessoryFragment;
import com.mapgis.mmt.common.widget.fragment.LevelItemFragment;
import com.mapgis.mmt.common.widget.fragment.ListTreeDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.LevelItem;
import com.mapgis.mmt.entity.LevelItemBase;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.common.PositionChoseFragment;
import com.repair.reporthistory.PatrolEventEntityTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by liuyunfan on 2015/8/28.
 */
public class EventReportFragmentView extends Fragment implements View.OnClickListener, LevelItemFragment.OnItemCheckedListener {

    private LinearLayout contentButton;
    private AccessoryFragment accessoryFragment;

    private Button saveCheckBtn;
    private Button reportBtn;
    private ListTreeDialogFragment contentListTreeFragment;
    private BaseActivity activity;

    /**
     * 巡线上报 内容 常用语
     */
    public static ArrayList<EventTypeItem> contentNodeList = new ArrayList<>();
    /**
     * 巡线上报 事件类型
     */
    public static ArrayList<EventTypeItem> eventTypeNodeList = new ArrayList<>();

    LevelItemFragment levelItemFragment = new LevelItemFragment();
    LevelItem item;
    private ArrayList<LevelItem> checkedLevelItemList = new ArrayList<>();

    //常用语
    private EditText contentEdt;
    private TextView tvDescRemain;

    //头部
    private TextView txtChangeType;
    private TextView txtTitle;

    //定位方式
    private PositionChoseFragment positionFragment;
    private LinearLayout lineLytPosition;

    private boolean isNeedShowBigSmallPanel = false;
    private boolean allowMultiple = false;

    public static EventReportFragmentView newInstance(boolean allowMultiple) {
        EventReportFragmentView fragment = new EventReportFragmentView();
        Bundle args = new Bundle();
        args.putBoolean("AllowMultiple", allowMultiple);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allowMultiple = getArguments().getBoolean("AllowMultiple", false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        try {
            this.activity = (BaseActivity) getActivity();

            activity.setCustomView(getTopView());
            view = inflater.inflate(R.layout.event_report, container, false);


            String userName = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA); // 设置日期格式
            String currentDate = df.format(new Date()); // new Date()为获取当前系统时间

            FragmentTransaction ft = getFragmentManager().beginTransaction();

            positionFragment = new PositionChoseFragment();
            ft.add(R.id.positionFragment, positionFragment);
            ft.show(positionFragment);

            accessoryFragment = new AccessoryFragment("巡线上报/" + currentDate + "/" + userName + "/", "巡线上报/" + currentDate + "/" + userName + "/");
            ft.add(R.id.photoFrameLayout, accessoryFragment);
            ft.show(accessoryFragment);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //  levelItemFragment.setBackLastActivity(true);
        levelItemFragment.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent arg2) {
                if (keyCode == KeyEvent.KEYCODE_BACK && arg2.getAction() == KeyEvent.ACTION_UP) {
                    if (levelItemFragment.getView().findViewById(R.id.layoutTwo).getVisibility() == View.VISIBLE) {
                        levelItemFragment.backEvent(arg0, keyCode, arg2);

                        return true;
                    } else {
                        if (txtTitle.getText().equals("未选择事件类型")) {
                            ((BaseActivity) getActivity()).backByReorder();
                        }
                        return false;
                    }
                }

                return false;
            }
        });

        //获取大小类数据,获取到数据后自动打开选择面板
        if (eventTypeNodeList.size() == 0) {
            new FetchEventTypeTask(activity, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<EventTypeItem>>() {
                @Override
                public void doAfter(ResultData<EventTypeItem> result) {
                    try {
                        if (result.DataList == null || result.DataList.size() == 0)
                            return;

                        eventTypeNodeList.clear();
                        eventTypeNodeList.addAll(result.DataList);

                        initListFragment();
                        txtChangeType.performClick();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).mmtExecute("GetEventType");
        } else {
            initListFragment();
            isNeedShowBigSmallPanel = true;
        }

        //获取描述数据
        if (contentNodeList.size() == 0) {
            new FetchEventTypeTask(activity, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<EventTypeItem>>() {
                @Override
                public void doAfter(ResultData<EventTypeItem> result) {
                    try {
                        if (result.DataList == null || result.DataList.size() == 0)
                            return;

                        contentNodeList.clear();
                        contentNodeList.addAll(result.DataList);

                        initContentTreeListFragment();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).mmtExecute("GetContentType");
        } else {
            initContentTreeListFragment();
        }

        //获取单个view
        findView(view);

        //监听点击事件
        initListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isNeedShowBigSmallPanel && !levelItemFragment.isVisible() && eventTypeNodeList.size() > 0) {
            txtChangeType.performClick();
        }
        isNeedShowBigSmallPanel = false;
    }

    private View getTopView() {
        View view = LayoutInflater.from(activity).inflate(R.layout.head_eventrport, null);

        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        txtTitle.setMaxWidth(400);
        txtTitle.setEllipsize(TextUtils.TruncateAt.END);
        txtTitle.getPaint().setTypeface(Typeface.DEFAULT_BOLD);
        txtTitle.setText("未选择事件类型");
        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
                if (checkedLevelItemList.size() == 0) {
                    sb.append("未选择事件类型");
                } else {
                    LevelItem levelItem = checkedLevelItemList.get(0);
                    if (levelItem.parent != null) {
                        sb.append(levelItem.parent.name).append(" ");
                    }
                    sb.append("[ ").append(getCheckedItemContent()).append(" ]");
                }

                Toast.makeText(activity, sb.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        //大小类切换按钮
        txtChangeType = (TextView) view.findViewById(R.id.txtChangeType);
        txtChangeType.setText("切换类型");
        // 返回按钮
        view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });
        return view;
    }

    private void initListener() {

        //  常用语描述字数变化监听
        contentEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int remain = 100 - s.length();
                tvDescRemain.setText("还可输入" + remain + "字");

                tvDescRemain.setTextColor(remain > 0 ? Color.BLACK : Color.RED);
            }
        });


        //大小类切换按钮
        txtChangeType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("item", item);
                    bundle.putParcelableArrayList("checkedItemList", checkedLevelItemList);
                    bundle.putBoolean("allowMultiSelect", allowMultiple);
                    levelItemFragment.setArguments(bundle);
                    levelItemFragment.show(getFragmentManager(), "");

                } else {
                    Toast.makeText(activity, "还未获取到事件分类", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //常用描述语选择
        contentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contentListTreeFragment != null) {
                    contentListTreeFragment.show(activity.getSupportFragmentManager(), getTag());
                } else {
                    Toast.makeText(activity, "还未获取到常用语", Toast.LENGTH_SHORT).show();
                }
            }
        });

        saveCheckBtn.setOnClickListener(this);
        reportBtn.setOnClickListener(this);
    }

    private void findView(View view) {

        tvDescRemain = (TextView) view.findViewById(R.id.tvDescRemain);
        contentEdt = (EditText) view.findViewById(R.id.txtDesc);
        contentButton = (LinearLayout) view.findViewById(R.id.comDes);

        reportBtn = (Button) view.findViewById(R.id.patrolReportBtn);
        saveCheckBtn = (Button) view.findViewById(R.id.selfCheckBtn);

        if (MyApplication.getInstance().getConfigValue("SelfCheck", 0) > 0)
            saveCheckBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemChecked(ArrayList<LevelItem> checkedItemList) {
        checkedLevelItemList.clear();
        checkedLevelItemList.addAll(checkedItemList);
        if (checkedLevelItemList.size() == 0) {
            return;
        }

        txtTitle.setText(getCheckedItemContent());
    }

    private String getCheckedItemContent() {
        StringBuilder sb = new StringBuilder();
        for (LevelItem levelItem : checkedLevelItemList) {
            sb.append(levelItem.name).append(",");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
    }

    /**
     * 大类、小类 弹出选择框
     */
    public void initListFragment() {
        item = new LevelItem("请选择事件类型");

        for (EventTypeItem p : eventTypeNodeList) {
            LevelItem pItem = new LevelItem(Integer.valueOf(p.NODEID), p.NODENNAME);

            for (EventTypeItem c : p.SubItem) {
                LevelItem cItem = new LevelItem(Integer.valueOf(c.NODEID), c.NODENNAME);
                cItem.parent = new LevelItemBase(pItem.id, pItem.name);
                pItem.children.add(cItem);
            }

            item.children.add(pItem);
        }
    }

    /**
     * 内容 常用语 树形选择 菜单
     */
    public void initContentTreeListFragment() {

        Node root = new Node("常用语", "000000");
        root.setCheckBox(false);

        for (int i = 0; i < contentNodeList.size(); i++) {
            Node levelOneNode = new Node(contentNodeList.get(i).NODENNAME, "000" + i);
            levelOneNode.setParent(root);
            root.add(levelOneNode);

            for (int j = 0; j < contentNodeList.get(i).SubItem.size(); j++) {
                Node levelTwoNode = new Node(contentNodeList.get(i).SubItem.get(j).NODENNAME, "000" + i + j);
                levelTwoNode.setParent(levelOneNode);
                levelOneNode.add(levelTwoNode);
            }
        }

        contentListTreeFragment = new ListTreeDialogFragment(activity, "描述常用语", root, 1, true);

        contentListTreeFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {

            @Override
            public void onRightButtonClick(View view) {
                @SuppressWarnings("unchecked")
                List<Node> nodes = (List<Node>) view.getTag();

                String result = "";

                for (Node node : nodes) {
                    result += node.getParent().getText() + node.getText() + ",";
                }

                if (result.length() > 0) {
                    result = result.substring(0, result.length() - 1);
                }

                contentEdt.setText(result);
            }
        });
    }

    @Override
    public void onClick(View v) {
        //常见问题
        if (v.getId() == R.id.patrolReportBtn) {
            doReport("ReportEvent");
        } else if (v.getId() == R.id.selfCheckBtn) {
            UserBean userInfo = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);

            String svcName = "ReportSelfCase?dispatchMan=" + Uri.encode(userInfo.TrueName) + "&dispatchManID=" + userInfo.UserID;

            doReport(svcName);
        }
    }

    private void doReport(String svcName) {
        if (checkedLevelItemList.size() == 0) {
            Toast.makeText(activity, "请选择事件内容", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isOk = true;

        if (activity.getResources().getBoolean(R.bool.select_point_when_event_report))
            isOk = activity.getIntent().hasExtra("addr") || activity.getIntent().hasExtra("address");

        if (isOk)
            isOk = !TextUtils.isEmpty(positionFragment.getXyTxt().getText());

        if (!isOk) {
            Toast.makeText(activity, "请从地图点选事件发生地点", Toast.LENGTH_SHORT).show();

            return;
        }

        PatrolEventEntityTrue entity = new PatrolEventEntityTrue();
        entity.setEventState("未上报");
        entity.setReportTime(BaseClassUtil.getSystemTime());
        LevelItem levelItem = checkedLevelItemList.get(0);
        entity.EventType = levelItem.parent != null ? levelItem.parent.name : "";
        entity.EventClass = getCheckedItemContent();
        entity.Position = "" + positionFragment.getXyTxt().getText();
        entity.Address = "" + positionFragment.getBDaddressEdt().getText();
        entity.Description = contentEdt.getText().toString();

        entity.ImageUrl = accessoryFragment.getRelativePhoto();

        String names = accessoryFragment.getRelativeRec();

        if (names.length() > 0) {
            names = names.replace(".amr", ".wav");
        }

        entity.AudiosUrl = names;

        entity.ReportName = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;
        entity.ReportID = MyApplication.getInstance().getUserId() + "";

        if (activity.getIntent().hasExtra("identityField"))
            entity.setIdentityField(activity.getIntent().getStringArrayExtra("identityField"));

        String data = new Gson().toJson(entity, PatrolEventEntityTrue.class);

        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/services/MapgisCity_WXYH_GL_MOBILE/REST/CaseManageREST.svc/" + svcName;

        String absolutePath = accessoryFragment.getAbsolutePath();
        String relativePath = accessoryFragment.getRelatePath();

        final ReportInBackEntity backEntity = new ReportInBackEntity(data, MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING, url, UUID.randomUUID().toString(), "巡线上报", absolutePath, relativePath);

        new EventReportTask(activity, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode > 0) {
                        Toast.makeText(activity, "上传成功!", Toast.LENGTH_SHORT).show();

                        success();
                    } else if (result.getSingleData() == 200) {// 降低要求，只要网络传输没问题就认为成功
                        Toast.makeText(activity, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    } else {
                        OkCancelDialogFragment fragment = new OkCancelDialogFragment(
                                result.ResultMessage + "，是否保存至后台等待上传？");

                        fragment.setLeftBottonText("放弃");
                        fragment.setRightBottonText("保存");

                        fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                            @Override
                            public void onRightButtonClick(View view) {
                                backEntity.insert();

                                success();
                            }
                        });

                        fragment.show(activity.getSupportFragmentManager(), "");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(backEntity);
    }

    private void success() {
        Toast.makeText(activity, "巡线上报保存成功", Toast.LENGTH_SHORT).show();

//        //成功后自己打开自己，达到重置界面的目的
//        Intent intent = new Intent(activity, activity.getClass());
//
//        intent.putExtras(activity.getIntent());
//
//        intent.removeExtra("loc");
//        intent.removeExtra("addr");
//        intent.removeExtra("names");
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//
//        startActivity(intent);

       // activity.finish();
        activity.onBackPressed();
    }
}

package com.patrol.module.posandpath2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.tree.ChildTreeNode;
import com.mapgis.mmt.common.widget.tree.ParentTreeNode;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.login.UserBean;
import com.patrol.module.posandpath2.beans.DeptBean;
import com.patrol.module.posandpath2.beans.UserInfo;
import com.patrol.module.posandpath2.detailinfo.DetailInfoActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class UserInfoFragment extends Fragment {
    private final static String TAG = UserInfoFragment.class.getSimpleName();

    private static final String ARG_CONTENT_VALUE = "list_bodyinfo";
    private static final String ARG_CONTENT_DEPT = "list_deptinfo";

    private int mUserDeptIndex = -1;
    // 用户信息列表，子条目信息
    private ArrayList<UserInfo> mInfoItems;
    // 部门信息列表，组信息
    private ArrayList<DeptBean> mDeptItems;

    private ArrayList<ParentTreeNode<DeptBean>> mTreeList;

    private ArrayList<UserInfo> mSelectedUsers;

    // 数据层
    private LinearLayout mDataView = null;
    private AutoCompleteTextView mAutoCompleteTextView = null;      // 自动填充搜索内容
    private ImageView mIvSearch = null;                             // 搜索按钮
    private TextView mTvPatrolState = null;                         // 在线人数
    private CheckBox mCbSelect = null;
    private RadioGroup mRgPatrolState = null;                       // RadioGroup控制列表选中在线和离线
    //    private View mFirstDivider = null;                              // ExpandableListView的第一条分隔线
    private TextView mTvDept = null;                                // 悬浮显示的部门信息
    private ExpandableListView mExpandableListView = null;          // 用户列表
    private UserBean mLoginBean;

    // 空数据层
    private FrameLayout mEmptyDataView = null;
    // 数据加载层
    private FrameLayout mLoadingData = null;

    private UserSelectableAdapter mAdapter;

    public UserInfoFragment() {
    }

    public static UserInfoFragment newInstance(ArrayList<String> mDeptItems, ArrayList<Parcelable> mInfoItems) {
        UserInfoFragment fragment = new UserInfoFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_CONTENT_DEPT, mDeptItems);
        args.putParcelableArrayList(ARG_CONTENT_VALUE, mInfoItems);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInfoItems = new ArrayList<>();
        mDeptItems = new ArrayList<>();
        mTreeList = new ArrayList<>();
        mSelectedUsers = new ArrayList<>();
        if (getArguments() != null) {
            ArrayList<UserInfo> infoList = getArguments().getParcelableArrayList(ARG_CONTENT_VALUE);
            mInfoItems.addAll(infoList);
            ArrayList<DeptBean> deptBeanArrayList = getArguments().getParcelableArrayList(ARG_CONTENT_DEPT);
            mDeptItems.addAll(deptBeanArrayList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userinfo_list, container, false);

        initView(view);

        initData();

        initListener();

        return view;
    }

    private void initView(View view) {
        mDataView = (LinearLayout) view.findViewById(R.id.frame_data_view);

        mAutoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);

        mIvSearch = (ImageView) view.findViewById(R.id.iv_searche);
        mTvPatrolState = (TextView) view.findViewById(R.id.tv_patrol_state);
        mCbSelect = (CheckBox) view.findViewById(R.id.cb_select);
        mRgPatrolState = (RadioGroup) view.findViewById(R.id.rg_patrol_state);
        mTvDept = (TextView) view.findViewById(R.id.tv_dept);
        mExpandableListView = (ExpandableListView) view.findViewById(R.id.list);


        mEmptyDataView = (FrameLayout) view.findViewById(R.id.emptyData);
        mLoadingData = (FrameLayout) view.findViewById(R.id.loadingData);
    }

    private void initListener() {
        // 子条目点击进入详情
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                enterDetailActivity((UserInfo) mAdapter.getChild(groupPosition, childPosition).getObject());
                return true;
            }
        });

        mCbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mAdapter.selectAll();
                } else {
                    mAdapter.selectNone();
                }
            }
        });

        setExpandListViewListener();

        setAutoCompleteTextListener();

        setRadioGroupListener();

        initChildStateListener();
    }

    /**
     * RadioGroup的监听事件
     */
    private void setRadioGroupListener() {
        mRgPatrolState.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_patrol_on) {
                    selectOnLine();
                } else if (checkedId == R.id.rb_patrol_off) {
                    selectOffLine();
                } else if (checkedId == R.id.rb_patrol_none) {
                    mAdapter.selectNone();
                } else if (checkedId == R.id.rb_patrol_all) {
                    mAdapter.selectAll();
                }
            }
        });
    }

    /**
     * 根据用户是否在线的状态来选择用户
     *
     * @param state ‘1’表示在线；‘0’表示离线
     */
    public void selectUsersByState(String state) {
        for (ParentTreeNode<DeptBean> parentNode : mTreeList) {
            for (ChildTreeNode childNode : parentNode.getNodeList()) {
                if (state.equals(((UserInfo) childNode.getObject()).Perinfo.IsOline)) {
                    childNode.setSelected(true);
                } else {
                    childNode.setSelected(false);
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 选中在线
     */
    private void selectOffLine() {
        selectUsersByState("0");
    }

    /**
     * 选中离线的
     */
    private void selectOnLine() {
        selectUsersByState("1");
    }

    private int lastIndex = 0;
    private int[] groupIndexs;
    private int mFirstVisibleItem;
    private int mVisibleItemCount;

    // 当前显示的第一个条目的位置
    private int firstGroupPosition;
    private int firstChildPosition;

    /**
     * 设置ExpandListView的监听事件
     */
    private void setExpandListViewListener() {
        // 监听滑动时间，设置悬浮预览
        mExpandableListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                UserInfoFragment.this.mFirstVisibleItem = firstVisibleItem;
                UserInfoFragment.this.mVisibleItemCount = visibleItemCount;
                /*if (groupIndexs == null) {
                    groupIndexs = new int[mTreeList.size()];
                    // 初始化数组
                    initGroupViewIndex();
                    Arrays.sort(groupIndexs); // 排序
                }

                int groupPosition = 0;
                int childPosition = -1;
                int firstItemIndex = 0;
                while (firstItemIndex < totalItemCount) {
                    if (firstVisibleItem == firstItemIndex) {
                        // 当前第一个显示的是部门信息
                        break;
                    }

                    if (mExpandableListView.isGroupExpanded(groupPosition)) {
                        // 展开的话就自动加用户数
                        int childCount = mTreeList.get(groupPosition).getChildNodeSize();

                        if (firstVisibleItem > firstItemIndex && firstVisibleItem <= childCount + firstItemIndex) {
                            // 为用户item
                            childPosition = firstVisibleItem - firstItemIndex - 1;
                            break;
                        }
                        firstItemIndex += childCount;
                    }
                    firstItemIndex++;
                    groupPosition++;
                }

                firstGroupPosition = groupPosition;
                firstChildPosition = childPosition;

                if (childPosition > 0) {
                    showTvDept(groupPosition);
                } else {
                    hideTvDept(groupPosition);
                }*/
            }

            private void hideTvDept(int groupPosition) {
                if (!mTvDept.isShown()) return;
                mTvDept.setVisibility(View.GONE);
            }

            private void showTvDept(int groupIndex) {
                if (!mExpandableListView.isGroupExpanded(groupIndex) || mTvDept.isShown()) {
                    return;
                }

                if (groupIndex >= 0 && groupIndex < groupIndexs.length) {
                    mTvDept.setText(mTreeList.get(groupIndex).getObject().DeptName);
                    mTvDept.setVisibility(View.VISIBLE);
                }
            }

            private void initGroupViewIndex() {
                int count = 0;
                ParentTreeNode<DeptBean> node;
                for (int i = 0; i < mTreeList.size(); i++) {
                    node = mTreeList.get(i);
                    groupIndexs[i] = count;
                    count++;
                    count += node.getChildNodeSize();
                }
            }
        });
    }

    /**
     * AutoCompleteTextView的监听事件
     */
    private void setAutoCompleteTextListener() {
        mAutoCompleteTextView.setDropDownHorizontalOffset(0);
      /*  mAutoCompleteTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 执行动画找到对应用户所在的位置并展开父布局
                searchUserOnView(mAutoCompleteTextView.getText().toString().trim());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchUserOnView(mAutoCompleteTextView.getText().toString().trim());
            }
        });

        mIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 执行动画找到对应用户所在的位置并展开父布局
                searchUserOnView(mAutoCompleteTextView.getText().toString().trim());
            }
        });
    }

    /**
     * 定位到用户在布局中所在的位置
     */
    private void searchUserOnView(String userName) {
        // 隐藏软键盘
        hideInputWindow(getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        int childIndex = getChildIndex(userName);
        if (childIndex < 0) {
            Toast.makeText(getActivity(), "列表中不存在" + userName + "用户", Toast.LENGTH_SHORT).show();
            return;
        }
        mExpandableListView.smoothScrollToPosition(childIndex);
    }

    public void hideInputWindow(Activity context) {
        context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    /**
     * 查找布局在当前的列表的索引
     *
     * @param userName 用户名
     * @return 索引
     */
    private int getChildIndex(String userName) {
        Log.i(TAG, "搜索用户名: " + userName);
        // 1，判断字符串的合法性
        if (BaseClassUtil.isNullOrEmptyString(userName)) return -1;

        // 2，找到用户所在位置的父布局
        int index = 0;

        // 3，找到用户所在的位置
        ParentTreeNode<DeptBean> node;
        List<ChildTreeNode> childNodeList;
        for (int groupPosition = 0; groupPosition < mTreeList.size(); groupPosition++) {
            node = mTreeList.get(groupPosition);
            childNodeList = node.getNodeList();
            for (int childPosition = 0; childPosition < childNodeList.size(); childPosition++) {
                if (userName.equals(((UserInfo) childNodeList.get(childPosition).getObject()).Perinfo.name)) {
                    index += childPosition + 1;   // 所要查找的用户在整个列表中的位置，包括group和child
                    Log.i(TAG, "搜索的用户" + userName + "的索引为：" + index);
                    if (!mExpandableListView.isGroupExpanded(groupPosition)) {
                        mExpandableListView.expandGroup(groupPosition);
                    }
                    return index;
                }
            }

            index++;
            if (mExpandableListView.isGroupExpanded(groupPosition)) {
                index += node.getChildNodeSize();
            }
        }
        return -1;
    }


    /**
     * 监听childView的选中状态的改变
     */
    private void initChildStateListener() {
//        mAdapter.setOnChildSelectChangedListener(new TreeSelectableAdapter.OnChildSelectChangedListener() {
//
//            @Override
//            public void onChildSelectChanged(ChildTreeNode childNode, boolean select) {
//                if (select) {
//                    mSelectedUsers.add((UserInfo) childNode.getObject());
//                } else {
//                    mSelectedUsers.remove(childNode.getObject());
//                }
//            }
//        });
    }

    /**
     * 进入详情界面
     */
    private void enterDetailActivity(UserInfo userInfo) {
        Intent intent = new Intent(getActivity(), DetailInfoActivity.class);
        intent.putExtra(DetailInfoActivity.ARG_MAP_OBJECT, userInfo);
        startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(getActivity());
    }

    private void initData() {
        mLoadingData.setVisibility(View.VISIBLE);
        mDataView.setVisibility(View.INVISIBLE);

        if (mInfoItems == null || mInfoItems.size() == 0) {
            mEmptyDataView.setVisibility(View.VISIBLE);
            mLoadingData.setVisibility(View.GONE);
            return;
        }

        mLoginBean = MyApplication.getInstance().getUserBean();

        mTreeList.addAll(createTreeConstruct());
        mAdapter = new UserSelectableAdapter(getActivity(), mTreeList);
        mExpandableListView.setAdapter(mAdapter);
        setDefaultExpand();

        setTopViewData();
//        setStateView(mInfoItems);
//        initAutoCompleteText();

        mEmptyDataView.setVisibility(View.GONE);
        mLoadingData.setVisibility(View.GONE);
        mDataView.setVisibility(View.VISIBLE);
    }

    /**
     * 展开登陆用户所在的部门并滑动到用户所在的位置
     */
    private void setDefaultExpand() {
        try {
            String dept = mLoginBean.Department;
            if (BaseClassUtil.isNullOrEmptyString(dept)) {
                return;
            }
            String trueName = mLoginBean.TrueName;
            for (int i = 0; i < mTreeList.size(); i++) {
                ParentTreeNode<DeptBean> patrenNode = mTreeList.get(i);

                // 1，找到部门所在的索引
                if (dept.equals(patrenNode.getObject().DeptName)) {
                    mExpandableListView.expandGroup(i);
                    List<ChildTreeNode> nodeList = patrenNode.getNodeList();
                    for (int j = 0; j < nodeList.size(); j++) {

                        // 找到用户在部门中的索引
                        if (trueName.equals(((UserInfo) nodeList.get(j).getObject()).Perinfo.name)) {
                            mExpandableListView.smoothScrollToPosition(i + j + 1);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化顶部布局的数据
     */
    private void setTopViewData() {
        int count = 0;
        String[] arrNames = new String[mInfoItems.size()];
        for (int i = 0; i < mInfoItems.size(); i++) {
            UserInfo userInfo = mInfoItems.get(i);
            arrNames[i] = userInfo.Perinfo.name;
            if ("1".equals(userInfo.Perinfo.IsOline)) {
                count++;
            }
        }
        // 设置在线人数
        mTvPatrolState.setText(String.format(Locale.CHINA, "在线人数：%d / %d", count, mInfoItems.size()));
        // 设置自动填充的TextView的数据适配器
        mAutoCompleteTextView.setAdapter(new ArrayAdapter<>(getActivity()
                , android.R.layout.simple_list_item_1, arrNames));
    }


    /**
     * 初始化自动填充的TextView的数据内容
     */
    private void initAutoCompleteText() {
        String[] arrNames = new String[mInfoItems.size()];
        for (int i = 0; i < mInfoItems.size(); i++) {
            arrNames[i] = mInfoItems.get(i).Perinfo.name;
        }
        mAutoCompleteTextView.setAdapter(new ArrayAdapter<>(getActivity()
                , android.R.layout.simple_list_item_1, arrNames));
    }

    /**
     * 统计集合中在线的人数
     *
     * @param userList 用户集合
     * @return 在线人数
     */
    private int getPatrollerOnlineCount(List<UserInfo> userList) {
        int count = 0;
        for (UserInfo userInfo : userList) {
            if ("1".equals(userInfo.Perinfo.IsOline)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 设置在线人数信息到状态栏中
     *
     * @param userList 用户列表
     */
    private void setStateView(List<UserInfo> userList) {
        mTvPatrolState.setText(String.format(Locale.CHINA,"在线人数：%d / %d", getPatrollerOnlineCount(userList), userList.size()));
    }


    /**
     * 用部门和用户信息构造一个树形结构
     *
     * @return List<ParentTreeNode>
     */
    private List<ParentTreeNode<DeptBean>> createTreeConstruct() {
        List<ParentTreeNode<DeptBean>> parentTreeNodeList = new ArrayList<>();
        try {
            String departments = mLoginBean.Department;
            int count = 0;

            ParentTreeNode<DeptBean> parentTreeNode;
            ChildTreeNode<UserInfo> childTreeNode;
            ArrayList<ChildTreeNode> list;
            int onlineLastDeptIndex = -1;   // 最后一个有用户在线的部门
            for (DeptBean dept : mDeptItems) {
                list = new ArrayList<>();
                parentTreeNode = new ParentTreeNode(dept, list);

                count = 0;
                int onlineCount = 0;

                // 添加子条目
                for (UserInfo info : mInfoItems) {
                    if (!dept.DeptName.equals(info.Perinfo.partment)) {
                        continue;
                    }
                    childTreeNode = new ChildTreeNode(info);

                    if ("1".equals(info.Perinfo.IsOline)) {
                        onlineCount++;

                        if (departments != null
                                && departments.equals(info.Perinfo.partment)) {
                            childTreeNode.setSelected(true);
                            count++;
                        }
                    }
                    parentTreeNode.addChildNode(childTreeNode);
                }
                if (parentTreeNode.getChildNodeSize() == 0) {
                    continue;
                }

                parentTreeNode.setSelectCount(count);

                // 排序
                if (onlineCount > 0) {
                    onlineLastDeptIndex++;
                    parentTreeNodeList.add(onlineLastDeptIndex, parentTreeNode);
                } else {
                    parentTreeNodeList.add(parentTreeNode);
                }
            }

            Comparator<ParentTreeNode<DeptBean>> comparator = new Comparator<ParentTreeNode<DeptBean>>() {
                @Override
                public int compare(ParentTreeNode<DeptBean> lhs, ParentTreeNode<DeptBean> rhs) {
                    return getOnlineCount(rhs) - getOnlineCount(lhs);
                }
            };

            // 根据在线人数排序
            Collections.sort(parentTreeNodeList, comparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parentTreeNodeList;
    }

    private int getOnlineCount(ParentTreeNode<DeptBean> node) {
        int count = 0;
        for (ChildTreeNode childTreeNode : node.getNodeList()) {
            if ("1".equals(((UserInfo) childTreeNode.getObject()).Perinfo.IsOline)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 将选中的数据显示到地图界面上
     */
    public void showSelectedDataOnMap() {
        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            @Override
            public boolean handleMessage(Message msg) {
                BaseMapMenu menu = mapGISFrame.getFragment().menu;
                if (menu instanceof PosAndPathMapMenu2) {
                    ((PosAndPathMapMenu2) menu).initMapLayoutView(PosAndPathMapMenu2.Case.CASE_ALL_PATROLLER_POSITION);
                    mSelectedUsers.clear();
                    for (ParentTreeNode<DeptBean> patrentNode : mTreeList) {
                        for (ChildTreeNode childNode :
                                patrentNode.getNodeList()) {
                            if (childNode.isSelected()) {
                                mSelectedUsers.add((UserInfo) childNode.getObject());
                            }
                        }
                    }
                    ((PosAndPathMapMenu2) menu).showUsersOnMap(mSelectedUsers);
                    ((PosAndPathMapMenu2) menu).showProgressBar(false);
                }
                return true;
            }
        });
    }
}

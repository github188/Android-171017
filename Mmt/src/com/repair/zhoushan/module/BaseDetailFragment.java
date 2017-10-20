package com.repair.zhoushan.module;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.R;
import com.repair.zhoushan.common.Utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BaseDetailFragment<T> extends Fragment {

    private boolean isViewInitialized;
    private boolean isDataInitialized;
    private boolean isVisibleToUser;

    private boolean isContentShown = true; // flag current shown status of the content view
    private boolean isCheckVisible = true; // whether check the visibility

    private View progressContainer;
    private View contentContainer;
    private View emptyView;
    private int contentContainerId;

    private TextView txtEmpty;

    private FrameLayout contentView;

    private String userId;

    public String getUserId() {
        return userId;
    }

    private AsyncTask asyncTask;

    private String postJsonContent; // POST的数据。如果不为空，表示需要用POST方式发送请求

    public void setPostJsonContent(String postJsonContent) {
        this.postJsonContent = postJsonContent;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;

        prepareFetchData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isViewInitialized = true;
        prepareFetchData();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = String.valueOf(MyApplication.getInstance().getUserId());

        Bundle args = getArguments();
        if (args != null && args.containsKey("IsCheckVisible")) {
            isCheckVisible = args.getBoolean("IsCheckVisible");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_content_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {

        progressContainer = view.findViewById(R.id.progressContainer);
        contentContainer = view.findViewById(R.id.contentContainer);
        contentContainerId = BaseClassUtil.generateViewId();
        contentContainer.setId(contentContainerId);

        contentView = (FrameLayout) view.findViewById(R.id.contentView);
        emptyView = view.findViewById(R.id.emptyView);
        txtEmpty = (TextView) view.findViewById(android.R.id.text1);

    }

    private void prepareFetchData() {
        if (isViewInitialized && (!isCheckVisible || isVisibleToUser) && !isDataInitialized) {
            isDataInitialized = true;
            fetchData();
        }
    }

    private void fetchData() {

        asyncTask = new AsyncTask<Void, Void, ResultData<T>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                setContentShown(false);
            }

            @Override
            protected ResultData<T> doInBackground(Void... params) {
                ResultData<T> resultData;

                try {
                    String jsonResult;

                    if (TextUtils.isEmpty(postJsonContent)) {
                        jsonResult = NetUtil.executeHttpGet(getRequestUrl());
                    } else {
                        jsonResult = NetUtil.executeHttpPost(getRequestUrl(), postJsonContent);
                    }

                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取详情失败:网络请求错误");
                    }

                    resultData = parseResultData(jsonResult);

                } catch (Exception e) {
                    e.printStackTrace();
                    resultData = new ResultData<>();
                    resultData.ResultCode = -100;
                    resultData.ResultMessage = e.getMessage();
                }

                return resultData;
            }

            @Override
            protected void onPostExecute(ResultData<T> resultData) {
                super.onPostExecute(resultData);

                if (resultData.ResultCode != 200) {
                    setEmptyTxt(resultData.ResultMessage);
                } else {
                    fillContentView(resultData);
                }
                setContentShown(true);
            }
        }.executeOnExecutor(MyApplication.executorService);
    }

    /**
     * 将网络服务返回的字符串解析成 ResultData数据结构,默认将 json字符串解析成 Results再转换成 ResultData
     * (对于不同的数据结构,子类可以重写该解析方法)
     *
     * @param jsonResult 网络返回的 Json字符串
     * @throws Exception
     */
    protected ResultData<T> parseResultData(@NonNull String jsonResult) throws Exception {

        ResultData<T> resultData;

        Type type = BaseDetailFragment.this.getClass().getGenericSuperclass();

        Class<T> entityClass;
        if (type instanceof ParameterizedType) {
            entityClass = (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[0];
        } else {
            throw new Exception("获取详情失败:类型不匹配");
        }
        Results<T> results = new Gson().fromJson(jsonResult, Utils.getType(Results.class, entityClass));
        resultData = results.toResultData();

        // 特殊处理：当查询到记录为空的时候，状态码修改为失败情况下的状态码，以用现有的界面结构显示提示信息
        if (resultData.DataList == null || resultData.DataList.size() == 0) {
            resultData.ResultCode = -100;
            resultData.ResultMessage = "信息为空";
        }

        return resultData;
    }

    /**
     * 填充内容视图
     */
    protected abstract void fillContentView(ResultData<T> resultData);

    /**
     * 请求界面数据结构的 Url
     */
    protected abstract String getRequestUrl();

    /**
     * 添加 View作为内容视图
     */
    public void addContentView(@NonNull View view) {
        contentView.addView(view);
    }

    /**
     * 添加 Fragment作为内容视图
     */
    public void addContentFragment(@NonNull Fragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(contentContainerId, fragment);
        ft.show(fragment);
        ft.commitAllowingStateLoss();
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(contentContainerId, fragment);
        ft.commitAllowingStateLoss();
    }

    private void setEmptyTxt(String tipMsg) {
        txtEmpty.setText(tipMsg);
        contentView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    private void setContentShown(boolean shown) {

        if (isContentShown == shown) {
            return;
        }
        isContentShown = shown;

        if (shown) {
            progressContainer.startAnimation(AnimationUtils.loadAnimation(
                    getContext(), android.R.anim.fade_out));
            contentContainer.startAnimation(AnimationUtils.loadAnimation(
                    getContext(), android.R.anim.fade_in));

            progressContainer.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
        } else {
            progressContainer.startAnimation(AnimationUtils.loadAnimation(
                    getContext(), android.R.anim.fade_in));
            contentContainer.startAnimation(AnimationUtils.loadAnimation(
                    getContext(), android.R.anim.fade_out));

            progressContainer.setVisibility(View.VISIBLE);
            contentContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
    }
}

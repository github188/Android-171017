package com.mapgis.mmt.global;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultWithoutData;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class MmtBaseTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
        implements Reporter<Progress> {

    protected boolean showLoading;
    protected ProgressDialog loadingDialog;
    private OnWxyhTaskListener<Result> listener;
    protected String repairService;
    protected String userID;
    protected Context context;

    public MmtBaseTask(Context context) {
        this(context, true);
    }

    public MmtBaseTask(Context context, boolean showLoading) {
        this(context, showLoading, (OnWxyhTaskListener<Result>) null);
    }

    public MmtBaseTask(Context context, boolean showLoading, OnWxyhTaskListener<Result> listener) {
        this(context, showLoading, "正在处理,请稍候...", listener);
    }

    public MmtBaseTask(Context context, boolean showLoading, String tip) {
        this(context, showLoading, tip, null);
    }

    public MmtBaseTask(Context context, boolean showLoading, String tip, OnWxyhTaskListener<Result> listener) {
        try {
            this.context = context;
            this.showLoading = showLoading;

            if (context != null)
                this.loadingDialog = MmtProgressDialog.getLoadingProgressDialog(context, tip);

            this.listener = listener;

            this.repairService = ServerConnectConfig.getInstance().getBaseServerPath() + "/Services"
                    + MyApplication.getInstance().getResources().getString(R.string.repair_service);

            this.userID = String.valueOf(MyApplication.getInstance().getUserId());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPreExecute() {
        if (showLoading) {
            loadingDialog.show();
        }
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        try {
            // The default implementation simply show the first result.
            // Subclass can provide their own implementation by overriding this method.
            if (values.length > 0 && showLoading) {
                loadingDialog.setMessage(String.valueOf(values[0]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void report(Progress... progress) {
        publishProgress(progress);
    }

    @Override
    protected void onPostExecute(Result result) {
        try {
            if (context instanceof Activity && ((Activity) context).isFinishing())
                return;

            if (loadingDialog.isShowing())
                loadingDialog.dismiss();

            if (listener != null)
                listener.doAfter(result);

            onSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onSuccess(Result result) {
    }

    public MmtBaseTask setListener(OnWxyhTaskListener<Result> listener) {
        this.listener = listener;

        return this;
    }

    public interface OnWxyhTaskListener<Result> {
        void doAfter(Result result);
    }

    /**
     * Should be called before execute, if needed.
     *
     * @param cancellable 是否可取消
     */
    public void setCancellable(boolean cancellable) {
        if (showLoading) {
            loadingDialog.setCanceledOnTouchOutside(cancellable);
        }
    }

    public void mmtExecute(Params... paramses) {
        this.executeOnExecutor(MyApplication.executorService, paramses);
    }

    protected boolean isResultOK(Result result) {
        if (result == null) {
            Toast.makeText(context, "请求服务出错", Toast.LENGTH_SHORT).show();

            return false;
        }

        if (result instanceof ResultWithoutData) {
            ResultWithoutData data = (ResultWithoutData) result;

            if (data.ResultCode < 0) {
                Toast.makeText(context, data.ResultMessage, Toast.LENGTH_SHORT).show();

                return false;
            }
        }

        return true;
    }

    protected final ParameterizedType getParameterizedType(final Class<?> rawClass, final Type... type) {

        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return type;
            }

            @Override
            public Type getRawType() {
                return rawClass;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }
}

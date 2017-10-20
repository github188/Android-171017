package com.mapgis.mmt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * 后台线程模板类,<br>
 * 
 * @author Administrator
 * 
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class ReaderTast<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	protected Activity activity;
	protected ExceptionType exceptionType;
	private boolean isFinish;

	protected ProgressDialog dialog;

	private final int retryCount = 20;

	public static int count = 0;

	/**
	 * 例外时 不关闭当前活动
	 * 
	 * @param activity
	 *            当前活动
	 * 
	 */
	public ReaderTast(Activity activity) {
		this(activity, false);

	}

	/**
	 * 
	 * @param activity
	 *            当前活动
	 * @param isFinish
	 *            例外时 是否关闭当前活动
	 */
	public ReaderTast(Activity activity, boolean isFinish) {
		super();
		this.activity = activity;
		this.isFinish = isFinish;

	}

	@Override
	public Result doInBackground(Params... params) {
		if (count > retryCount) {
			this.cancel(true);
			resetCount();
		}
		try {
			return doReader(params);
		} catch (SocketTimeoutException se) {
			se.printStackTrace();
			exceptionType = ExceptionType.SocketTimeoutException;
			return null;
		} catch (IOException ie) {
			ie.printStackTrace();
			exceptionType = ExceptionType.IOException;
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			exceptionType = ExceptionType.Exception;
			return null;
		}

	}

    public abstract Result doReader(Params... params) throws Exception;

	/**
	 * 后台线程结束后，在没有异常情况下调用本方法
	 * 
	 * @param result
	 */
	public abstract void doComplet(Result result);

	/**
	 * 普通异常处理
	 */
	public void doException() {
		this.cancel(true);
		if (isFinish) {
			activity.finish();
		}
		if (!UseToast()) {
			return;
		}
		String badInernetString = "网络异常";
		Toast.makeText(activity, badInernetString, Toast.LENGTH_SHORT).show();
	}

	protected boolean UseToast() {
		return true;
	}

	/**
	 * IO异常处理
	 */
	public void doIOException() {
		doException();
	}

	/**
	 * 超时异常处理
	 */
	public void doSocketTimeoutException() {
		doException();
	}

	private void doExceptionRetry() {
		this.cancel(true);
		count++;
		doException();

	}

	private void doIOExceptionRetry() {
		this.cancel(true);
		count++;
		doIOException();

	}

	private void doSocketTimeoutExceptionRetry() {
		this.cancel(true);
		count++;
		doSocketTimeoutException();

	}

	@Override
	protected void onPostExecute(Result result) {
		if (result == null) {
			if (dialog != null) {
				dialog.dismiss();
			}

			if (exceptionType == ExceptionType.Exception) {
				Log.e("EXCEPTION", "ExceptionType.Exception");
				doExceptionRetry();
			} else if (exceptionType == ExceptionType.IOException) {
				Log.e("EXCEPTION", "ExceptionType.IOException");
				doIOExceptionRetry();
			} else if (exceptionType == ExceptionType.SocketTimeoutException) {
				Log.e("EXCEPTION", "ExceptionType.SocketTimeoutException");
				doSocketTimeoutExceptionRetry();
			} else {
				doExceptionRetry();
			}
			return;
		} else {
			doComplet(result);
			resetCount();
		}

	}

    private void resetCount() {
		count = 0;
	}

	/**
	 * 捕获的异常类型
	 * 
	 * @author Administrator
	 * 
	 */
	public enum ExceptionType {

		SocketTimeoutException, IOException, Exception

	}

}

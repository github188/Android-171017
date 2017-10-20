package com.mapgis.mmt.net;

import java.io.InterruptedIOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

//Task基础类
public abstract class BaseTask<T> implements Callable<T> {
	BaseTaskListener<T> taskListener;
	short completeFlg = 1;
	protected BaseTaskParameters actionInput;

	protected BaseTask(BaseTaskParameters paramTaskParameters) {
		this(paramTaskParameters, null);
	}

	protected BaseTask(BaseTaskParameters paramTaskParameters,
			BaseTaskListener<T> paramTaskListener) {
		if (paramTaskListener == null) {
			this.taskListener = new BaseTaskListener<T>() {
				public void onCompletion(short s, T objs) {
				}

				public void onError(Throwable e) {
					e.printStackTrace();
				}
			};
		} else {
			this.taskListener = paramTaskListener;
		}

		this.actionInput = paramTaskParameters;
	}

	public BaseTaskParameters getActionInput() {
		return this.actionInput;
	}

	public void setActionInput(BaseTaskParameters taskInput) {
		this.actionInput = taskInput;
	}

	protected abstract T execute() throws Exception;

	public final T call() {
		T localObject1 = null;
		try {
			localObject1 = execute();
		} catch (ExecutionException localExecutionException) {
			this.completeFlg = 0;
		} catch (InterruptedIOException localInterruptedIOException) {
			this.completeFlg = 0;
		} catch (Throwable localThrowable) {
			if (Thread.currentThread().isInterrupted()) {
				this.completeFlg = 0;
			} else {
				this.completeFlg = -1;
				this.taskListener.onError(localThrowable);
			}
		} finally {
			this.taskListener.onCompletion(this.completeFlg, localObject1);
		}
		return localObject1;
	}
}

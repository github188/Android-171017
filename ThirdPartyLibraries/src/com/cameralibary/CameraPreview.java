package com.cameralibary;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * sufaceView 的预览类，其中SurfaceHolder.CallBack用来监听Surface的变化，
 * 当Surface发生改变的时候自动调用该回调方法 通过调用方SurfaceHolder.addCallBack来绑定该方法
 * 
 * @author zw.yan
 * 
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private String TAG = "CameraPreview";
	/**
	 * Surface的控制器，用来控制预览等操作
	 */
	private SurfaceHolder mHolder;
	/**
	 * 相机实例
	 */
	private Camera mCamera = null;
	/**
	 * 图片处理
	 */
	public static final int MEDIA_TYPE_IMAGE = 1;
	/**
	 * 是否支持自动聚焦，默认不支持
	 */
	private Boolean isSupportAutoFocus = false;
	/**
	 * 获取当前的context
	 */
	private Context mContext;
	/**
	 * 当前传感器的方向，当方向发生改变的时候能够自动从传感器管理类接受通知的辅助类
	 */
	MyOrientationDetector cameraOrientation;

	public PictureCallback mPicture;

	@SuppressWarnings("deprecation")
	public CameraPreview(Context context) {
		super(context);
		this.mContext = context;

		isSupportAutoFocus = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);

		cameraOrientation = new MyOrientationDetector(mContext);

		mHolder = getHolder();
		// 兼容android 3.0以下的API，如果超过3.0则不需要设置该方法
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		mHolder.addCallback(this);// 绑定当前的回调方法
	}

	/**
	 * 创建的时候自动调用该方法
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		if (mCamera == null) {
			mCamera = CameraCheck.getCameraInstance(mContext);
            mCamera.setDisplayOrientation(90);
		}
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException e) {
			if (null != mCamera) {
				mCamera.release();
				mCamera = null;
			}

			e.printStackTrace();
		}

	}

	/**
	 * 当surface的大小发生改变的时候自动调用的
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (mHolder.getSurface() == null) {
			return;
		}
		try {
			setCameraParms();
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
			reAutoFocus();
		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

	/**
	 * 设置最适合当前手机的图片宽度、高度、质量
	 */
	public int expectWidth = 1024, expectHeight = 768, expectQuality = 80;

	private void setCameraParms() {
		Camera.Parameters myParam = mCamera.getParameters();

		List<Camera.Size> mSupportedsizeList = myParam.getSupportedPictureSizes();

		int setFixPictureWidth = 0, setFixPictureHeight = 0;

		if (mSupportedsizeList.contains(mCamera.new Size(expectWidth, expectHeight))) {
			setFixPictureWidth = expectWidth;
			setFixPictureHeight = expectHeight;
		} else {
			Iterator<Camera.Size> itos = mSupportedsizeList.iterator();

			while (itos.hasNext()) {
				Camera.Size curSize = itos.next();

				int curSupporSize = curSize.width * curSize.height;
				int fixPictrueSize = setFixPictureWidth * setFixPictureHeight;

				if (curSupporSize > fixPictrueSize && curSupporSize <= expectWidth * expectHeight) {
					setFixPictureWidth = curSize.width;
					setFixPictureHeight = curSize.height;
				}
			}
		}

		myParam.setPictureSize(setFixPictureWidth, setFixPictureHeight);
		myParam.setJpegQuality(expectQuality);

		mCamera.setParameters(myParam);

		if (myParam.getMaxNumDetectedFaces() > 0) {
			mCamera.startFaceDetection();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;

		cameraOrientation.disable();
		cameraOrientation = null;
	}

	/**
	 * Call the camera to Auto Focus
	 */
	public void reAutoFocus() {
		if (isSupportAutoFocus) {
			mCamera.autoFocus(new AutoFocusCallback() {
				public void onAutoFocus(boolean success, Camera camera) {
				}
			});
		}
	}

	/**
	 * 自动聚焦，然后拍照
	 */
	public void takePicture() {
		if (mCamera != null) {
			mCamera.autoFocus(autoFocusCallback);
		}
	}

	private AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {

		public void onAutoFocus(boolean success, Camera camera) {
			// TODO Auto-generated method stub

			if (success) {
				Log.i(TAG, "autoFocusCallback: success...");
				takePhoto();
			} else {
				Log.i(TAG, "autoFocusCallback: fail...");
				if (isSupportAutoFocus) {
					takePhoto();
				}
			}
		}
	};

	/**
	 * 调整照相的方向，设置拍照相片的方向
	 */
	private void takePhoto() {
		if (mCamera != null) {
			int orientation = cameraOrientation.getOrientation();

			Camera.Parameters cameraParameter = mCamera.getParameters();
			cameraParameter.setRotation(90);
			cameraParameter.set("rotation", 90);
			if ((orientation >= 45) && (orientation < 135)) {
				cameraParameter.setRotation(180);
				cameraParameter.set("rotation", 180);
			}
			if ((orientation >= 135) && (orientation < 225)) {
				cameraParameter.setRotation(270);
				cameraParameter.set("rotation", 270);
			}
			if ((orientation >= 225) && (orientation < 315)) {
				cameraParameter.setRotation(0);
				cameraParameter.set("rotation", 0);
			}
			mCamera.setParameters(cameraParameter);
			mCamera.takePicture(shutterCallback, pictureCallback, mPicture);
		}
	}

	private ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			// TODO Auto-generated method stub
		}
	};

	private PictureCallback pictureCallback = new PictureCallback() {

		public void onPictureTaken(byte[] arg0, Camera arg1) {
			// TODO Auto-generated method stub

		}
	};

	public void startPreview() {
		mCamera.startPreview();// 重新开始预览
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		reAutoFocus();

		return false;
	}
}

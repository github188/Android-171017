package com.sltphoto.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sltphoto.activity.ImageFileActivity;
import com.sltphoto.activity.ShowImagesActivity;
import com.sltphoto.util.BitmapCache;
import com.sltphoto.util.BitmapCache.ImageCallback;
import com.sltphoto.util.ImageItem;
import com.thirdpartylibraries.R;

import java.util.ArrayList;

/**
 * 这个是显示所有包含图片的文件夹的适配器
 */
public class FolderAdapter extends BaseAdapter {

	private Activity mContext;
	private Intent mIntent;
	private DisplayMetrics dm;
	BitmapCache cache;
	final String TAG = getClass().getSimpleName();

	public FolderAdapter(Activity c) {
		cache = new BitmapCache();
		init(c);
	}

	// 初始化
	public void init(Activity c) {
		mContext = c;
		mIntent = mContext.getIntent();
		dm = new DisplayMetrics();
		mContext.getWindowManager().getDefaultDisplay().getMetrics(dm);
	}

	public int getCount() {
		return ImageFileActivity.contentList.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	ImageCallback callback = new ImageCallback() {
		public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params) {
			if (imageView != null && bitmap != null) {
				String url = (String) params[0];
				if (url != null && url.equals(imageView.getTag())) {
					imageView.setImageBitmap(bitmap);
				} else {
					Log.e(TAG, "callback, bmp not match");
				}
			} else {
				Log.e(TAG, "callback, bmp null");
			}
		}
	};

	private class ViewHolder {
		// 封面
		public ImageView imageView;
		public ImageView choose_back;
		// 文件夹名称
		public TextView folderName;
		// 文件夹里面的图片数量
		public TextView fileNum;
	}

	ViewHolder holder = null;

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.plugin_camera_select_folder, parent, false);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.file_image);
			holder.choose_back = (ImageView) convertView.findViewById(R.id.choose_back);
			holder.folderName = (TextView) convertView.findViewById(R.id.name);
			holder.fileNum = (TextView) convertView.findViewById(R.id.filenum);
			holder.imageView.setAdjustViewBounds(true);
			// LinearLayout.LayoutParams lp = new
			// LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dipToPx(65));
			// lp.setMargins(50, 0, 50,0);
			// holder.imageView.setLayoutParams(lp);
			holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();
		String path;
		if (ImageFileActivity.contentList.get(position).imageList != null) {

			// path = photoAbsolutePathList.get(position);
			// 封面图片路径
			path = ImageFileActivity.contentList.get(position).imageList.get(0).imagePath;
			// 给folderName设置值为文件夹名称
			// holder.folderName.setText(fileNameList.get(position));
			holder.folderName.setText(ImageFileActivity.contentList.get(position).bucketName);

			// 给fileNum设置文件夹内图片数量
			// holder.fileNum.setText("" + fileNum.get(position));
			holder.fileNum.setText("" + ImageFileActivity.contentList.get(position).count);

		} else
			path = "android_hybrid_camera_default";
		if (path.contains("android_hybrid_camera_default"))
			holder.imageView.setImageResource(R.drawable.plugin_camera_no_pictures);
		else {
			// holder.imageView.setImageBitmap(
			// AlbumActivity.contentList.get(position).imageList.get(0).getBitmap());
			final ImageItem item = ImageFileActivity.contentList.get(position).imageList.get(0);
			holder.imageView.setTag(item.imagePath);
			cache.displayBmp(holder.imageView, item.thumbnailPath, item.imagePath, callback);
		}
		// 为封面添加监听
		holder.imageView.setOnClickListener(new ImageViewClickListener(position, mIntent, holder.choose_back));

		return convertView;
	}

	// 为每一个文件夹构建的监听器
	private class ImageViewClickListener implements OnClickListener {
		private final int position;
		private final ImageView choose_back;

		public ImageViewClickListener(int position, Intent intent, ImageView choose_back) {
			this.position = position;
			this.choose_back = choose_back;
		}

		public void onClick(View v) {
			ShowImagesActivity.dataList = (ArrayList<ImageItem>) ImageFileActivity.contentList.get(position).imageList;
			Intent intent = new Intent();
			String folderName = ImageFileActivity.contentList.get(position).bucketName;
			intent.putExtra("folderName", folderName);
			intent.setClass(mContext, ShowImagesActivity.class);
			mContext.startActivityForResult(intent, 1201);
			choose_back.setVisibility(View.VISIBLE);
		}
	}

	public int dipToPx(int dip) {
		return (int) (dip * dm.density + 0.5f);
	}

}

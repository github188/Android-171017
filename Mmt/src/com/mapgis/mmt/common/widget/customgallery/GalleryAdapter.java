package com.mapgis.mmt.common.widget.customgallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DeviceUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater infalter;
    private ArrayList<CustomGallery> data = new ArrayList<CustomGallery>();
    ImageLoader imageLoader;

    private int mScreenWidth;

    private boolean isActionMultiplePick;

    private DisplayImageOptions displayImageOptions;
    private ImageSize imageSize;

    private Drawable defaultImageDrawable;

    public GalleryAdapter(Context c, ImageLoader imageLoader) {
        infalter = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = c;
        this.imageLoader = imageLoader;

        init();

        // clearCache();
    }

    private void init() {

        this.mScreenWidth = DeviceUtil.getWindowsWidth((BaseActivity) mContext);

        this.defaultImageDrawable = mContext.getResources().getDrawable(R.drawable.ic_default_photo);

        this.displayImageOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(false)
                .cacheInMemory(false)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        this.imageSize = new ImageSize(mScreenWidth / 3, mScreenWidth / 3);

//        try {
//            String CACHE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.temp_tmp";
//            new File(CACHE_DIR).mkdirs();
//
//            File cacheDir = StorageUtils.getOwnCacheDirectory(getBaseContext(), CACHE_DIR);
//
//            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheOnDisc(true)
//                    .imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).build();
//
//            ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(getBaseContext())
//                    .defaultDisplayImageOptions(defaultOptions).discCache(new UnlimitedDiskCache(cacheDir))
//                    .memoryCache(new WeakMemoryCache());
//
//            ImageLoaderConfiguration config = builder.build();
//            imageLoader = ImageLoader.getInstance();
//            imageLoader.init(config);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public CustomGallery getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setMultiplePick(boolean isMultiplePick) {
        this.isActionMultiplePick = isMultiplePick;
    }

    public void selectAll(boolean selection) {
        for (int i = 0; i < data.size(); i++) {
            data.get(i).isSeleted = selection;

        }
        notifyDataSetChanged();
    }

    public boolean isAllSelected() {
        boolean isAllSelected = true;

        for (int i = 0; i < data.size(); i++) {
            if (!data.get(i).isSeleted) {
                isAllSelected = false;
                break;
            }
        }

        return isAllSelected;
    }

    public boolean isAnySelected() {
        boolean isAnySelected = false;

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSeleted) {
                isAnySelected = true;
                break;
            }
        }

        return isAnySelected;
    }

    public ArrayList<CustomGallery> getSelected() {
        ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSeleted) {
                dataT.add(data.get(i));
            }
        }

        return dataT;
    }

    public void addAll(ArrayList<CustomGallery> files) {

        try {
            this.data.clear();
            this.data.addAll(files);

        } catch (Exception e) {
            e.printStackTrace();
        }

        notifyDataSetChanged();
    }

    public void addAllWithoutNotify(ArrayList<CustomGallery> files) {

        try {
            this.data.clear();
            this.data.addAll(files);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeSelection(View v, int position) {

        boolean isSelected;

        isSelected = !data.get(position).isSeleted;
        data.get(position).isSeleted = isSelected;

        ((ViewHolder) v.getTag()).imgQueueMultiSelected.setSelected(isSelected);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = infalter.inflate(R.layout.gallery_item, null);

            convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mScreenWidth / 3 - 8));

            holder = new ViewHolder();
            holder.imgQueue = (ImageView) convertView.findViewById(R.id.imgQueue);
            holder.imgQueueMultiSelected = (ImageView) convertView.findViewById(R.id.imgQueueMultiSelected);

            if (isActionMultiplePick) {
                holder.imgQueueMultiSelected.setVisibility(View.VISIBLE);
            } else {
                holder.imgQueueMultiSelected.setVisibility(View.GONE);
            }
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.imgQueue.setTag(position);

        try {

            holder.imgQueue.setImageDrawable(defaultImageDrawable);
            imageLoader.displayImage("file://" + data.get(position).sdcardPath, new ImageViewAware(holder.imgQueue),
                    displayImageOptions, imageSize, null, null);

            if (isActionMultiplePick) {

                holder.imgQueueMultiSelected.setSelected(data.get(position).isSeleted);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public class ViewHolder {
        ImageView imgQueue;
        ImageView imgQueueMultiSelected;
    }

    public void clearCache() {
        imageLoader.clearDiscCache();
        imageLoader.clearMemoryCache();
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }
}

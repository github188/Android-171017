package com.mapgis.mmt.common.widget.customgallery;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * 自定义相册，支持多选
 */
public class CustomGalleryActivity extends BaseActivity {

    private static final String TAG = "CustomGalleryActivity";

    public static final String ACTION_SINGLE_PICK = "mmt.ACTION_SINGLE_PICK";
    public static final String ACTION_MULTIPLE_PICK = "mmt.ACTION_MULTIPLE_PICK";
    private final int HANDLER_REFRESH_LIST_EVENT = 1002;

    GridView gridGallery;
    Handler handler;
    GalleryAdapter adapter;

    ImageView imgNoMedia;
    Button btnGalleryOk;

    String action;
    private ImageLoader imageLoader;

    private ArrayList<String> initSelectedNames = new ArrayList<String>();

    @Override
    protected void setDefaultContentView() {
        setSwipeBackEnable(false);

        setContentView(R.layout.gallery);

        Intent outterIntent = getIntent();

        action = outterIntent.getAction();
        if (action == null) {
            action = ACTION_MULTIPLE_PICK;
        }
        String[] selectedPhotos = outterIntent.getStringArrayExtra("selectedPhotos");
        if (selectedPhotos != null && selectedPhotos.length > 0) {
            initSelectedNames.addAll(Arrays.asList(selectedPhotos));
        }

        init();
    }

    private void init() {

        imageLoader = ImageLoader.getInstance();

        getBaseTextView().setText("相册");

        addBackBtnListener(getBaseLeftImageView());

        gridGallery = (GridView) findViewById(R.id.gridGallery);
        gridGallery.setFastScrollEnabled(true);
        adapter = new GalleryAdapter(CustomGalleryActivity.this, imageLoader);
        PauseOnScrollListener listener = new PauseOnScrollListener(imageLoader, true, true);
        gridGallery.setOnScrollListener(listener);

        if (action.equalsIgnoreCase(ACTION_MULTIPLE_PICK)) {

            findViewById(R.id.llBottomContainer).setVisibility(View.VISIBLE);
            gridGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                    adapter.changeSelection(v, position);
                }
            });
            adapter.setMultiplePick(true);

        } else if (action.equalsIgnoreCase(ACTION_SINGLE_PICK)) {

            findViewById(R.id.llBottomContainer).setVisibility(View.GONE);
            gridGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                    CustomGallery item = adapter.getItem(position);
                    Intent data = new Intent().putExtra("single_path", item.sdcardPath);
                    setResult(RESULT_OK, data);
                    finish();
                }
            });
            adapter.setMultiplePick(false);
        }

        gridGallery.setAdapter(adapter);
        gridGallery.setSelector(new ColorDrawable(Color.TRANSPARENT));

        imgNoMedia = (ImageView) findViewById(R.id.imgNoMedia);

        btnGalleryOk = (Button) findViewById(R.id.btnGalleryOk);
        btnGalleryOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<CustomGallery> selected = adapter.getSelected();

                String[] allPath = new String[selected.size()];
                for (int i = 0; i < allPath.length; i++) {
                    allPath[i] = selected.get(i).sdcardPath;
                }

                Intent data = new Intent().putExtra("all_path", allPath);
                setResult(RESULT_OK, data);
                finish();

            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == HANDLER_REFRESH_LIST_EVENT) {
                    adapter.notifyDataSetChanged();
                    if (adapter.isEmpty()) {
                        imgNoMedia.setVisibility(View.VISIBLE);
                    } else {
                        imgNoMedia.setVisibility(View.GONE);
                    }
                }
            }
        };
        new Thread() {
            @Override
            public void run() {
                adapter.addAllWithoutNotify(getGalleryPhotos());
                handler.sendEmptyMessage(HANDLER_REFRESH_LIST_EVENT);
            }
        }.start();

    }

    private ArrayList<CustomGallery> getGalleryPhotos() {

        ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();

        Cursor imagecursor = null;
        try {
            final String[] columns = {MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME};
            final String orderBy = MediaStore.Images.Media._ID;

          //  String externalStorageDir = String.valueOf(Environment.getExternalStorageDirectory());
            String selection = MediaStore.Images.Media.DATA + " LIKE \"%DCIM%\"";

            String[] selectionArgs = {};

            imagecursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    columns, selection, selectionArgs, orderBy);

            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            int idColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
            int nameColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);

            String tempDisplayName;
            if (imagecursor != null && imagecursor.getCount() > 0) {

                while (imagecursor.moveToNext()) {
                    CustomGallery item = new CustomGallery();

                    item._id = imagecursor.getInt(idColumnIndex);
                    item.sdcardPath = imagecursor.getString(dataColumnIndex);
                    tempDisplayName = imagecursor.getString(nameColumnIndex);

                    if (initSelectedNames.contains(tempDisplayName)) {
                        item.isSeleted = true;
                    }

                    galleryList.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (imagecursor != null && !imagecursor.isClosed()) {
                imagecursor.close();
            }
        }

        // show newest photo at beginning of the list
        Collections.reverse(galleryList);
        return galleryList;
    }

}

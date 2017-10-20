package com.mapgis.mmt.common.widget;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.BitmapUtil;
import com.mapgis.mmt.config.ServerConnectConfig;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import java.io.File;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class ImageFragment extends Fragment {

    private ImageViewTouch imageViewTouch;
    private WebView wv;
    private String url;
    private TextView tipTV;

    public static ImageFragment newInstance(String url) {

        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString("URL", url);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            this.url = args.getString("URL");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.image_item, container, false);
        this.imageViewTouch = (ImageViewTouch) view.findViewById(R.id.image);

        this.wv = (WebView) view.findViewById(R.id.wv);
        this.wv.getSettings().setJavaScriptEnabled(true);
        this.wv.getSettings().setSupportZoom(true);
        this.wv.getSettings().setBuiltInZoomControls(true);
        this.wv.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        this.tipTV = (TextView) view.findViewById(R.id.tiptv);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (BaseClassUtil.isImg(url)) {

            this.wv.setVisibility(View.GONE);
            this.imageViewTouch.setVisibility(View.VISIBLE);
            tipTV.setVisibility(View.GONE);
            imageViewTouch.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
            if (imageViewTouch.getTag() == null) {
                //之前没有预览小图，在预览页面大图就下载好了
                if (new File(url).exists()) {
                    new AddImageTask(handler).execute(url);
                    return;
                }

               String neturl = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath()
                        + "/OutFiles/UpLoadFiles"
                        + url.substring(url.indexOf("edia") + 4);
                download(Uri.encode(neturl), handler);
               
            } else
                imageViewTouch.setImageBitmap((Bitmap) imageViewTouch.getTag());
        } else {

            if (BaseClassUtil.isReadedFile(url) && MyApplication.getInstance().getConfigValue("CanPreviewFile", 0) == 1) {
                this.wv.setVisibility(View.VISIBLE);
                this.imageViewTouch.setVisibility(View.GONE);
                tipTV.setVisibility(View.GONE);
                String fileurl = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath()
                        + "/OutFiles/UpLoadFiles"
                        + url.substring(url.indexOf("edia") + 4);
                String pageUrl = ServerConnectConfig.getInstance().getHostPath() + "/CityOMS3/pdf/generic/web/viewer.html?file=" + fileurl;
                wv.loadUrl(pageUrl);
            } else {
                this.wv.setVisibility(View.GONE);
                this.imageViewTouch.setVisibility(View.GONE);
                tipTV.setVisibility(View.VISIBLE);
            }
        }
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (getActivity() != null)
                ((BaseActivity) getActivity()).setBaseProgressBarVisibility(false);

            if (msg.obj != null && msg.obj instanceof Bitmap) {
                Bitmap bitmap = (Bitmap) msg.obj;
                imageViewTouch.setImageBitmap(bitmap);
                imageViewTouch.setTag(bitmap);
            }
        }
    };

    /**
     * 旋转图片
     */
    public void rotate(float degrees) {
        Bitmap bitmap = (Bitmap) imageViewTouch.getTag();

        if (bitmap == null) {
            return;
        }

        if (bitmap.isRecycled()) {
            return;
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

        imageViewTouch.setImageBitmap(bitmap);
        imageViewTouch.setTag(bitmap);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Bitmap bitmap = (Bitmap) imageViewTouch.getTag();

        if (bitmap != null) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    /**
     * 将文件压缩
     */
    private class AddImageTask extends AsyncTask<String, Void, Bitmap> {
        private final Handler handler;

        public AddImageTask(Handler handler) {
            this.handler = handler;
        }

        @Override
        protected void onPreExecute() {
            if (getActivity() != null)
                ((BaseActivity) getActivity()).setBaseProgressBarVisibility(true);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String path = params[0];

            if (BaseClassUtil.isNullOrEmptyString(path)) {
                return null;
            }

            Bitmap bitmap = getBitmap(path);

            if (bitmap == null) {
                bitmap = BitmapFactory.decodeFile(path);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            handler.obtainMessage(0, result).sendToTarget();
        }
    }

    /**
     * 将文件下载到本地
     *
     * @param neturl URL路径
     */
    private void download(String neturl, final Handler handler) {
        FinalHttp fh = new FinalHttp();

        // 调用download方法开始下载
        fh.download(neturl, url, false, new AjaxCallBack<File>() {
            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
            }

            @Override
            public void onLoading(long count, long current) {
            }

            @Override
            public void onStart() {
                if (getActivity() != null)
                    ((BaseActivity) getActivity()).setBaseProgressBarVisibility(true);
            }

            @Override
            public void onSuccess(File t) {
                String path = t.getAbsolutePath();

                Bitmap bitmap = getBitmap(path);

                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeFile(path);
                }

                handler.obtainMessage(0, bitmap).sendToTarget();
            }
        });
    }

    private Bitmap getBitmap(String url) {
        return BitmapUtil.decodeSampledBitmapFromFile(url, 720, 960, true, false);
    }

}
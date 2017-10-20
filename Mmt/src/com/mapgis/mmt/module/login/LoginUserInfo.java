package com.mapgis.mmt.module.login;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cameralibary.CameraActivity;
import com.google.gson.Gson;
import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.BitmapUtil;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.popupwindow.ThreeButtonPopupWindow;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;

import java.io.File;
import java.util.Arrays;

public class LoginUserInfo extends BaseActivity {
    private UserBean userInfo;
    private ImageView ib;
    private Bitmap userIcoBitmap;
    private File outputImage;
    private Uri imageUri;
    private static final int GET_BIG_PHOTO = 1;
    private static final int CROP_PHOTO = 3;
    private static int imgSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            imgSize = (int) (60 * getResources().getDisplayMetrics().density + 0.5f + 1);

            FrameLayout root = (FrameLayout) findViewById(R.id.baseFragment);

            View view = getLayoutInflater().inflate(R.layout.common_userinfo,
                    root, true);

            userInfo = MyApplication.getInstance().getUserBean();

            ib = (ImageView) view.findViewById(R.id.userIco);

            userInfo.setHeadIco(LoginUserInfo.this, ib);

            ib.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupWindow();
                }
            });

            ((TextView) view.findViewById(R.id.userInfoUserID))
                    .setText(userInfo.UserID + "");
            ((TextView) view.findViewById(R.id.userInfoTrueName))
                    .setText(userInfo.TrueName);
            ((TextView) view.findViewById(R.id.userInfoLoginName))
                    .setText(userInfo.LoginName);

            ((TextView) view.findViewById(R.id.userInfoDepartCode))
                    .setText(Arrays.toString(userInfo.DepartCode));
            ((TextView) view.findViewById(R.id.userInfoDepartName))
                    .setText(Arrays.toString(userInfo.DepartName));
            ((TextView) view.findViewById(R.id.userInfoGroupCode))
                    .setText(userInfo.GroupCode + "");

            ((TextView) view.findViewById(R.id.userInfoRole))
                    .setText(userInfo.Role);
            ((TextView) view.findViewById(R.id.userIsOffline))
                    .setText(userInfo.isOffline ? "离线模式" : "联机模式");

            ((TextView) view.findViewById(R.id.userLoginCount))
                    .setText(userInfo.LoginCount + "");
            ((TextView) view.findViewById(R.id.userLoginTime))
                    .setText(userInfo.LoginTime);
            ((TextView) view.findViewById(R.id.userPatrolPlanID))
                    .setText(userInfo.PatrolPlanID + "");

            getBaseTextView().setText("用户信息");
            int customBtnStyleResource = AppStyle.getCustromBtnStyleResource();
            if (customBtnStyleResource > 0) {
                view.findViewById(R.id.btnChangePwd).setBackgroundResource(customBtnStyleResource);
            }

            view.findViewById(R.id.btnChangePwd).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final EditText txtPwd = new EditText(LoginUserInfo.this);

                        txtPwd.setLines(1);
                        txtPwd.setBackgroundResource(R.drawable.edit_text_default);

                        OkCancelDialogFragment fragment = new OkCancelDialogFragment("请输入新密码", txtPwd);

                        fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                            @Override
                            public void onRightButtonClick(View view) {
                                try {
                                    String pwd = txtPwd.getText().toString();

                                    if (TextUtils.isEmpty(pwd)) {
                                        Toast.makeText(LoginUserInfo.this, "密码不能为空", Toast.LENGTH_SHORT).show();

                                        return;
                                    }

                                    new MmtBaseTask<String, Integer, ResultWithoutData>(LoginUserInfo.this) {
                                        @Override
                                        protected ResultWithoutData doInBackground(String... params) {
                                            try {
                                                String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc/ChangePwd";
                                                String userID = String.valueOf(MyApplication.getInstance().getUserId());

                                                String json = NetUtil.executeHttpGet(url, "userID", userID, "pwd", params[0]);

                                                return new Gson().fromJson(json, ResultWithoutData.class);
                                            } catch (Exception ex) {
                                                ex.printStackTrace();

                                                return null;
                                            }
                                        }

                                        @Override
                                        protected void onSuccess(ResultWithoutData data) {
                                            try {
                                                if (data == null)
                                                    Toast.makeText(LoginUserInfo.this, "修改密码失败，可能服务不存在", Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(LoginUserInfo.this, data.ResultMessage, Toast.LENGTH_SHORT).show();
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    }.mmtExecute(pwd);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });

                        fragment.show(LoginUserInfo.this.getSupportFragmentManager(), "");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showPopupWindow() {
        final ThreeButtonPopupWindow popupWindow = new ThreeButtonPopupWindow(
                this);

        popupWindow.setFirstView("拍照", new OnClickListener() {
            @Override
            public void onClick(View v) {
                createImgFile();
                takePhoto();
                popupWindow.dismiss();
            }
        });

        popupWindow.setSecondView("从相册获取", new OnClickListener() {
            @Override
            public void onClick(View v) {
                createImgFile();
                pickPhoto();
                popupWindow.dismiss();
            }
        });

        popupWindow.setThirdView("取消", new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupWindow.show(this);
    }

    public void takePhoto() {
        try {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

            // 0：使用系统自带相机；1：使用程序定制相机
            if (MyApplication.getInstance().getConfigValue("MmtCamera", -1) > 0) {
                intent = new Intent(this, CameraActivity.class);
            }

            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            startActivityForResult(intent, GET_BIG_PHOTO);
        } catch (Exception e) {
            Toast.makeText(LoginUserInfo.this, "没有照相功能", Toast.LENGTH_LONG)
                    .show();
            return;
        }
    }

    public void pickPhoto() {
        try {
            Intent intent = new Intent("android.intent.action.PICK");
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("scale", true);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, CROP_PHOTO);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(LoginUserInfo.this, "未找到相册", Toast.LENGTH_LONG)
                    .show();
            return;
        }
    }

    public void createImgFile() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            // sd card 可用
            File dirIco = new File(Battle360Util.getFixedPath("UserImage", false));
            if (!(dirIco.exists())) {
                dirIco.mkdirs();
            }

            outputImage = new File(Battle360Util.getFixedPath("UserImage") + userInfo.UserID + "user.png");
            try {
                if (!outputImage.exists()) {
                    outputImage.createNewFile();
                }
                imageUri = Uri.fromFile(outputImage);
            } catch (Exception e) {
                // TODO: handle exception
                Toast.makeText(LoginUserInfo.this, "文件操作失败", Toast.LENGTH_LONG)
                        .show();
                return;
            }

        } else {
            Toast.makeText(LoginUserInfo.this, "SD卡不可用", Toast.LENGTH_LONG)
                    .show();
            return;
        }
    }

    public int getImgMinLength(Uri imageFileUri) {
        try {
            BitmapFactory.Options factory = new BitmapFactory.Options();
            factory.inJustDecodeBounds = true; // 当为true时 允许查询图片不为 图片像素分配内存
            BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(imageFileUri), null,
                    factory);
            int h = (int) Math.ceil(factory.outHeight);
            int w = (int) Math.ceil(factory.outWidth);
            return Math.min(h, w);
        } catch (Exception ex) {
            return 0;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GET_BIG_PHOTO: {
                if (resultCode == RESULT_OK) {
                    if (imageUri == null) {
                        Toast.makeText(LoginUserInfo.this, "准备图片出错！",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    // 裁剪图片意图
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image/*");
                    intent.putExtra("crop", "true");
                    intent.putExtra("scale", true);
                    intent.putExtra("aspectX", 1);// 裁剪框比例
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("outputX", imgSize);// 输出图片大小
                    intent.putExtra("outputY", imgSize);
                    intent.putExtra("outputFormat",
                            Bitmap.CompressFormat.PNG.toString());// 图片格式
                    // intent.putExtra("noFaceDetection", true);// 取消人脸识别
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, CROP_PHOTO);
                }

            }
            break;
            case CROP_PHOTO: {
                if (resultCode == RESULT_OK) {
                    try {
                        int minLength = getImgMinLength(imageUri);
                        if (minLength > imgSize) {
                            userIcoBitmap = BitmapUtil.compressBySize(
                                    getContentResolver().openInputStream(imageUri),
                                    imgSize, imgSize);
                        } else {
                            userIcoBitmap = BitmapFactory
                                    .decodeStream(getContentResolver()
                                            .openInputStream(imageUri));
                        }

                        if (userIcoBitmap == null) {
                            Toast.makeText(LoginUserInfo.this, "剪裁图像出错！", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String icoFullPath = userInfo.getHeadIcoLocaFullPath();
                        final File icoFile = new File(icoFullPath);
                        if (!icoFile.exists()) {
                            Toast.makeText(LoginUserInfo.this, "剪裁图像出错！", Toast.LENGTH_LONG).show();
                            return;
                        }
                        new MmtBaseTask<Void, Void, String>(LoginUserInfo.this) {
                            @Override
                            protected String doInBackground(Void... params) {
                                try {
                                    byte[] buffer = FileUtil.file2byte(icoFile);
                                    String url = ServerConnectConfig.getInstance().getMobileBusinessURL()
                                            + "/BaseREST.svc/UploadByteResource?path=" + userInfo.getHeadIcoAbsPath();

                                    return NetUtil.executeHttpPost(url, buffer); // 不抛异常就认为成功
                                } catch (Exception ex) {
                                    return null;
                                }

                            }

                            @Override
                            protected void onSuccess(String s) {
                                super.onSuccess(s);
                                if (TextUtils.isEmpty(s)) {
                                    Toast.makeText(LoginUserInfo.this, "头像上传失败！", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                ResultWithoutData result = new Gson().fromJson(s, ResultWithoutData.class);
                                if (result.ResultCode <= 0) {
                                    Toast.makeText(LoginUserInfo.this, "头像上传失败！", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                Toast.makeText(LoginUserInfo.this, "头像上传成功！", Toast.LENGTH_LONG).show();

                                userInfo.setHeadIco(LoginUserInfo.this, ib);
                            }
                        }.mmtExecute();


                    } catch (Exception e) {
                        // TODO: handle exception
                        Toast.makeText(LoginUserInfo.this, "剪裁图像出错！",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            default:
                break;
        }
    }
}

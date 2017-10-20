package com.mapgis.mmt.module.login;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mapgis.mmt.AppManager;

import java.util.ArrayList;

public class Login extends AppCompatActivity {

    protected LoginFragment fragment;

    private static final int RC_REQUEST_PERMISSION = 1;

    private PermissionDesc[] requiredPermissions;

    {
        this.requiredPermissions = new PermissionDesc[] {
                new PermissionDesc(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储读写权限", "读写存储"),
                new PermissionDesc(Manifest.permission.ACCESS_FINE_LOCATION, "位置权限", "获取位置信息定位"),
                new PermissionDesc(Manifest.permission.CAMERA, "相机权限", "访问相机拍照"),
                new PermissionDesc(Manifest.permission.READ_PHONE_STATE, "电话权限", "获取电话状态"),
                new PermissionDesc(Manifest.permission.RECORD_AUDIO, "录音权限", "使用录音功能"),

//                new PermissionDesc(Manifest.permission.WRITE_SETTINGS, "修改系统设置", "修改系统设置"),
//                new PermissionDesc(Manifest.permission.CHANGE_CONFIGURATION, "更改配置权限", "更改配置权限"),
//                new PermissionDesc(Manifest.permission.READ_LOGS, "读取日志权限", "读取日志权限"),
//                new PermissionDesc(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, "文件系统权限", "文件系统权限")
        };
    }

    @Override
    protected void onCreate(Bundle arg0) {
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            super.onCreate(arg0);

            create();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void create() {
        init();
        checkPermissions(false);
    }

    protected void init() {

        FrameLayout layout = new FrameLayout(this);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        layout.setId(layout.hashCode());
        setContentView(layout);

        fragment = generateLoginFragment();

        if (getIntent().getExtras() != null) {
            Bundle args = new Bundle();
            args.putAll(getIntent().getExtras());
            fragment.setArguments(args);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(layout.hashCode(), fragment, LoginFragment.class.getSimpleName());
        ft.commit();
    }

    protected LoginFragment generateLoginFragment() {
        return new LoginFragment();
    }

    /**
     * Check permissions.
     * @param invokeByLogin Whether invoked by clicking Login button or not.
     */
    public void checkPermissions(boolean invokeByLogin) {

        if (invokeByLogin) {
            permissionCheckCallback = (LoginFragment) getSupportFragmentManager().findFragmentByTag(LoginFragment.class.getSimpleName());
        } else {
            permissionCheckCallback = null;
        }

        // Only SDK_VERSION_CODE < 23 need to be checked
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (permissionCheckCallback != null) {
                permissionCheckCallback.onSuccess();
            }
        } else {
            requestRequiredPermissions();
        }
    }

    @TargetApi(23)
    private void requestRequiredPermissions() {

        final ArrayList<PermissionDesc> missedPerm = new ArrayList<>();
        final ArrayList<PermissionDesc> deniedPerm = new ArrayList<>();
        for (PermissionDesc permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(Login.this, permission.name) != PackageManager.PERMISSION_GRANTED) {
                missedPerm.add(permission);
                if (ActivityCompat.shouldShowRequestPermissionRationale(Login.this, permission.name)) {
                    deniedPerm.add(permission);
                }
            }
        }

        if (missedPerm.size() == 0) {
            // All verify-needed permission are granted
            grantPermissionSuccess();

        } else if (deniedPerm.size() > 0) {

            // Some permissions have denied before, so show user some description first
            StringBuilder tipMsgSB = new StringBuilder("应用需要授予权限：\n");
            PermissionDesc permDesc;
            for (int i = 0, length = deniedPerm.size(); i < length; i++) {
                permDesc = deniedPerm.get(i);
                if (i != 0) {
                    tipMsgSB.append("; \n");
                }
                tipMsgSB.append(permDesc.simpleDesc).append("-").append(permDesc.rationale);
            }

            new AlertDialog.Builder(Login.this)
                    .setMessage(tipMsgSB.toString())
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPerms(missedPerm);
                        }
                    })
                    .setCancelable(false)
                    .show();

        } else {
            requestPerms(missedPerm);
        }
    }

    private void requestPerms(ArrayList<PermissionDesc> missedPerm) {

        String[] perms = new String[missedPerm.size()];
        for (int i = 0, length = missedPerm.size(); i < length; i++) {
            perms[i] = missedPerm.get(i).name;
        }

        ActivityCompat.requestPermissions(Login.this, perms, RC_REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case RC_REQUEST_PERMISSION:
                handleRequestPermissionsResult(permissions, grantResults);
                break;
        }
    }

    private void handleRequestPermissionsResult(final String[] permissions, int[] grantResults) {

        final ArrayList<String> deniedPerms = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPerms.add(permissions[i]);
            }
        }

        if (!deniedPerms.isEmpty()) {

            final ArrayList<String> neverAskPerms = new ArrayList<>();
            for (String perm : deniedPerms) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(Login.this, perm)) {
                    neverAskPerms.add(perm);
                }
            }

            if (neverAskPerms.size() == deniedPerms.size()) {

                new AlertDialog.Builder(Login.this)
                        .setTitle("请求权限")
                        .setMessage("缺少必要的权限将导致应用无法正常运行，请在设置界面授予相应的权限！")
                        .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);

                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (permissionCheckCallback != null) {
                                    permissionCheckCallback.onFailed();
                                }
                            }
                        })
                        .setCancelable(false)
                        .show();
            } else {

                new AlertDialog.Builder(Login.this)
                        .setMessage("您拒绝授予某些权限，这将导致应用无法正常运行，请授予应用相应的权限！")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(Login.this,
                                        deniedPerms.toArray(new String[deniedPerms.size()]), RC_REQUEST_PERMISSION);
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        } else {

            grantPermissionSuccess();
        }
    }

    @TargetApi(23)
    private void grantPermissionSuccess() {

        // Dangerous permissions request Ok, now checking system permission(WRITE_SETTINGS)
        if (Settings.System.canWrite(Login.this)) {

            if (permissionCheckCallback != null) {
                permissionCheckCallback.onSuccess();
            }
        } else {

            final Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(getPackageManager()) != null) {
                new AlertDialog.Builder(Login.this)
                        .setTitle("请求权限")
                        .setMessage("应用需要系统设置权限，请在设置界面授予相应的权限！")
                        .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(intent);
                            }
                        })
                        .setCancelable(false)
                        .show();
            } else {
                Toast.makeText(Login.this, "未能获取系统设置权限，请在设置界面中授予该权限！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按下键盘上返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getIntent().hasExtra("forgetpassword")) {
                fragment.showClearMapDataDialog();
            } else {
                AppManager.finishProgram();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public static class PermissionDesc {
        /**
         * 权限名称
         */
        public final String name;
        /**
         * 权限简单描述
         */
        public final String simpleDesc;
        /**
         * 权限申请理由
         */
        public final String rationale;

        public PermissionDesc(String permissionName, String simpleDesc, String rationale) {
            this.name = permissionName;
            this.simpleDesc = simpleDesc;
            this.rationale = rationale;
        }
    }

    public interface PermissionCheckCallback {
        void onSuccess();
        void onFailed();
    }

    private PermissionCheckCallback permissionCheckCallback;

}

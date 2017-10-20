package com.mapgis.mmt.module.systemsetting.locksetting.numpwd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordHandleException;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordManager;

/**
 * 当用户忘记锁屏密码时使用其账户登录也可以解锁
 */
public class UnlockAccountActivity extends Activity implements View.OnClickListener {

    private TextView mForgetPassword;
    private EditText mEtUserName;
    private EditText mEtPassword;
    private Button mBtnLogin;

    private UserBean mUserBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_account);

        initView();

        initData();
    }

    private void initView() {
        mEtUserName = (EditText) findViewById(R.id.txtUserName);
        mEtPassword = (EditText) findViewById(R.id.txtPassword);
        mBtnLogin = (Button) findViewById(R.id.imgLogin);
        mForgetPassword = (TextView) findViewById(R.id.password_forget);

        mBtnLogin.setOnClickListener(this);
        mForgetPassword.setOnClickListener(this);
        mForgetPassword.setVisibility(View.GONE);
    }

    private void initData() {
        mUserBean = MyApplication.getInstance().getConfigValue("UserBean",
                UserBean.class);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.imgLogin) {
            unlockScreen();
        } else if (i == R.id.password_forget) {
            forgetPassword();
        }
    }

    /**
     * 判断用户输入的账户信息是否正确
     * @return 账户信息正确时返回true,不对时返回false.
     */
    private boolean isAccountCorrect(){
        boolean flag = isInfoCorrect(mEtUserName,mUserBean.LoginName,"用户名不能为空","用户名输入不正确")
                || isInfoCorrect(mEtPassword,mUserBean.password,"密码不能为空","密码输入不正确");
        return flag;
    }

    /**
     * 判断账户或者密码输入是否正确
     * @param view EditText布局
     * @param info 正确的账户或密码信息
     * @param emptyError 输入框中为空时提示的信息
     * @param uncorrectError 输入框中的信息不正确时的提示
     * @return
     */
    private boolean isInfoCorrect(EditText view,String info,String emptyError,String uncorrectError){
        String temp = view.getText().toString().trim();

        if (TextUtils.isEmpty(temp)){
            mEtUserName.setError(emptyError);
            return false;
        }else if (!temp.equals(info)){
            mEtUserName.setError(uncorrectError);
            return false;
        }
        return true;
    }

    /**
     * 忘记密码
     */
    private void forgetPassword() {

    }

    // 最大的登录次数
    private int MAX_LOGIN_COUNT = 3;
    // 用户点击登录的次数
    private int mLoginCount;

    /**
     * 点击登录时所进行的操作
     */
    private void unlockScreen() {
        mLoginCount++;
        // 1，判断用户输入的信息是否正确
        boolean flag = isAccountCorrect();

        if (!flag && mLoginCount >= MAX_LOGIN_COUNT){
            showClearMapDataDialog();
            return;
        }

        if (flag){
            //　账户信息正确，就弹出提示框清除锁屏密码
            showClearScreenPwdDialog();
        }else{
            Toast.makeText(this,String.format("你还剩余%d次登录机会！",MAX_LOGIN_COUNT - mLoginCount),Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 弹出提示框清除地图数据
     */
    private void showClearMapDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("是否清除地图数据？");
        builder.setPositiveButton("清除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 清除地图数据
                MyApplication.getInstance().clearCache();
                appExitFuture();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 不清除地图数据
                dialog.dismiss();
                appExitFuture();
            }
        });
        builder.show();
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
                // 退出应用程序
                AppManager.finishActivity();
                UnlockAccountActivity.this.finish();
            }
        }
    };

    /**
     * 应用程序即将退出
     */
    private void appExitFuture(){
        Toast.makeText(this,"应用程序即将退出！",Toast.LENGTH_LONG).show();
        handler.sendEmptyMessageDelayed(0,1500);
    }

    /**
     * 提示用户是否清除锁屏密码
     */
    private void showClearScreenPwdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("是否清除锁屏密码？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 清除锁屏密码
                try {
                    PasswordManager.clearPassword();
                } catch (PasswordHandleException e) {
                    e.printStackTrace();
                }
                UnlockAccountActivity.this.finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 不清除锁屏密码
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {

    }
}

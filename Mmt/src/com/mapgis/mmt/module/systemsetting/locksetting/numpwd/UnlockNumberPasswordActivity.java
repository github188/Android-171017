package com.mapgis.mmt.module.systemsetting.locksetting.numpwd;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.BitmapUtil;
import com.mapgis.mmt.module.systemsetting.locksetting.BaseUnlockActivity;

import java.io.File;
import java.util.Locale;


/**
 * 解锁界面
 */
public class UnlockNumberPasswordActivity extends BaseUnlockActivity
        implements View.OnClickListener {

    private EditText mEditText = null;
    private TextView mTvUnlockMsg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_unlock);

            mTvUnlockMsg = (TextView) findViewById(R.id.tv_unlock_msg);
            mEditText = (EditText) findViewById(R.id.et_pwd);
            findViewById(R.id.numberpwd_unlock_forget).setOnClickListener(this);
            findViewById(R.id.btn_ok).setOnClickListener(this);

            setNumberUnlockIcon();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_ok) {
            unlockPassword();
        } else if (v.getId() == R.id.numberpwd_unlock_forget) {
            forgetLocalePassword();
        }
    }

    @Override
    public String getEnsurePassword() {
        return mEditText.getText().toString().trim();
    }

    @Override
    public void clearInputPassword() {
        mEditText.setText("");
    }

    @Override
    public void hasChanceEnsure() {
        super.hasChanceEnsure();
        String format = String.format(Locale.CHINA, "密码错误，还剩余<font color=\"#ff0000\">%s</font>次尝试机会！"
                , super.mCountChance);
        mTvUnlockMsg.setText(Html.fromHtml(format));
    }

    /*
     * 设置用户图像
     */
    private void setNumberUnlockIcon() {
        // UserBean还未加载
        /*UserBean userInfo = MyApplication.getInstance().getConfigValue("UserBean",
                UserBean.class);
        if (userInfo != null)
            userInfo.setHeadIco(this, (ImageView) findViewById(R.id.numberpwd_unlock_face));*/
        ImageView imageView = (ImageView) findViewById(R.id.numberpwd_unlock_face);
        String imgPath = Battle360Util.getFixedMapGISPath(true) + "UserImage";
        File file = new File(imgPath);
        File[] files = file.listFiles();
        if (file.exists() && files.length > 0 && BaseClassUtil.isImg(files[0].getName())) {
            Bitmap bitmap = BitmapUtil.getBitmapFromFile(files[0], false);
            BitmapUtil.getBitmapFromFile(imgPath, imageView.getHeight(), imageView.getWidth());
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.default_user));
        }
    }
}

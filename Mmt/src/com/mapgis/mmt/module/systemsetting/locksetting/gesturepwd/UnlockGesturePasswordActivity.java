package com.mapgis.mmt.module.systemsetting.locksetting.gesturepwd;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.BitmapUtil;
import com.mapgis.mmt.module.systemsetting.locksetting.util.LockPatternUtils;
import com.mapgis.mmt.module.systemsetting.locksetting.BaseUnlockActivity;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class UnlockGesturePasswordActivity extends BaseUnlockActivity implements View.OnClickListener {

    private LockPatternUtils mLockPatternUtils;
    private LockPatternView mLockPatternView;
    private int mFailedPatternAttemptsSinceLastTimeout = 0;
    private CountDownTimer mCountdownTimer = null;
    private Handler mHandler = new Handler();
    private TextView mHeadTextView;
    private Animation mShakeAnim;
    private ImageView mGesturepwdUnlockIcon;

    private TextView mGesturePwdUnlockForget;

    private Toast mToast;

    private void showToast(CharSequence message) {
        if (null == mToast) {
            mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            mToast.setText(message);
        }

        mToast.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.gesturepassword_unlock);

            mLockPatternUtils = new LockPatternUtils(this);
            mLockPatternView = (LockPatternView) this
                    .findViewById(R.id.gesturepwd_unlock_lockview);
            mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
            mLockPatternView.setTactileFeedbackEnabled(true);
            mHeadTextView = (TextView) findViewById(R.id.gesturepwd_unlock_text);
            mShakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_x);

            mGesturePwdUnlockForget = (TextView) findViewById(R.id.gesturepwd_unlock_forget);
            mGesturePwdUnlockForget.setOnClickListener(this);

            setGestureUnlockIcon();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.gesturepwd_unlock_forget) {
            forgetLocalePassword();
        }
    }

    private void setGestureUnlockIcon() {
        mGesturepwdUnlockIcon = (ImageView) findViewById(R.id.gesturepwd_unlock_face);
        /*UserBean userInfo = MyApplication.getInstance().getConfigValue("UserBean",
                UserBean.class);
        if (userInfo != null)
            userInfo.setHeadIco(this, mGesturepwdUnlockIcon);*/

        // UserBean还未加载
        /*UserBean userInfo = MyApplication.getInstance().getConfigValue("UserBean",
                UserBean.class);
        if (userInfo != null)
            userInfo.setHeadIco(this, (ImageView) findViewById(R.id.numberpwd_unlock_face));*/
        String imgPath = Battle360Util.getFixedMapGISPath(true) + "UserImage";
        File file = new File(imgPath);
        File[] files = file.listFiles();
        if (file.exists() && files.length > 0 && BaseClassUtil.isImg(files[0].getName())) {
            Bitmap bitmap = BitmapUtil.getBitmapFromFile(files[0], false);
            BitmapUtil.getBitmapFromFile(imgPath, mGesturepwdUnlockIcon.getHeight(), mGesturepwdUnlockIcon.getWidth());
            mGesturepwdUnlockIcon.setImageBitmap(bitmap);
        } else {
            mGesturepwdUnlockIcon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.default_user));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountdownTimer != null)
            mCountdownTimer.cancel();
    }

    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

    private List<LockPatternView.Cell> pattern;

    protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {

        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            patternInProgress();
        }

        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            if (pattern == null || pattern.size() < 4) {
                mLockPatternView.clearPattern();
                mHeadTextView.setText("长度不够，请重试！");
                return;
            }
            UnlockGesturePasswordActivity.this.pattern = pattern;
            unlockPassword();
        }

        public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

        }

        private void patternInProgress() {
        }
    };

    @Override
    public void passwordCorrect() {
        super.passwordCorrect();
        mLockPatternView
                .setDisplayMode(LockPatternView.DisplayMode.Correct);
    }

    @Override
    public void passwordError() {
        super.passwordError();
        mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
    }

    @Override
    public void hasChanceEnsure() {
        String format = String.format(Locale.CHINA, "密码错误，还剩余<font color=\"#ff0000\">%s</font>次尝试机会！"
                , super.mCountChance);
        mHeadTextView.setText(Html.fromHtml(format));
        mHeadTextView.startAnimation(mShakeAnim);
    }

    @Override
    public String getEnsurePassword() {
        return LockPatternUtils.patternToString(this.pattern);
    }

    @Override
    public void clearInputPassword() {

    }
}

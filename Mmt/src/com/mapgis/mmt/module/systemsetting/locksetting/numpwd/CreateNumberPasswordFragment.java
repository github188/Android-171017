package com.mapgis.mmt.module.systemsetting.locksetting.numpwd;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.systemsetting.locksetting.presenter.ICreatePasswordPresenter;
import com.mapgis.mmt.module.systemsetting.locksetting.presenter.impl.BaseCreatePasswordPresenter;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordType;
import com.mapgis.mmt.module.systemsetting.locksetting.view.impl.ICreatePasswordView;

/**
 * Setup number password view
 */
public class CreateNumberPasswordFragment extends Fragment
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, ICreatePasswordView {
    private EditText mEtPwd = null;
    private EditText mEtEnsurePwd = null;

    private ICreatePasswordPresenter mPresenter;

    public CreateNumberPasswordFragment() {
        if (mPresenter == null) {
            mPresenter = new BaseCreatePasswordPresenter(this);
        }
    }

    public static CreateNumberPasswordFragment newInstance() {
        return new CreateNumberPasswordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_number_pwd, container, false);
        initView(view);
        return view;
    }

    /**
     * 初始化界面布局
     */
    private void initView(View view) {
        mEtPwd = (EditText) view.findViewById(R.id.et_pwd);
        mEtEnsurePwd = (EditText) view.findViewById(R.id.et_ensurePwd);
        Button mBtnCancel = (Button) view.findViewById(R.id.btn_cancel);
        Button mBtnConfirm = (Button) view.findViewById(R.id.btn_confirm);
        CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.cb_pwdInvisiable);

        // 点击事件
        mBtnCancel.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(this);
        mCheckBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_cancel) {
            cancelCreate();
        } else if (v.getId() == R.id.btn_confirm) {
            confirmCreate();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setPwdVisible(isChecked);
    }

    @Override
    public String getPwd() {
        return mEtPwd.getText().toString().trim();
    }

    @Override
    public String getEnsurePwd() {
        return mEtEnsurePwd.getText().toString().trim();
    }

    @Override
    public int getPasswordType() {
        return PasswordType.PASSWORD_NUMBER;
    }

    @Override
    public void cancelCreate() {
        // 密码输入框中的密码为空时直接退出界面
        if (TextUtils.isEmpty(getPwd()) && TextUtils.isEmpty(getEnsurePwd())) {
            getActivity().finish();
        } else {
            clearText();
        }
    }

    @Override
    public void confirmCreate() {
        String pwd = getPwd();
        String ensurePwd = getEnsurePwd();

        if (TextUtils.isEmpty(pwd) || pwd.length() < 4) {
            mEtPwd.setError(getString(R.string.text_numpwd_too_short));
            clearText();
            return;
        }

        if (TextUtils.isEmpty(ensurePwd) || !ensurePwd.equals(pwd)) {
            mEtEnsurePwd.setError(getString(R.string.text_pwd_not_agree));
            mEtEnsurePwd.setText("");
            return;
        }

        boolean isSuccess = mPresenter.saveLocalePassword();
        if (isSuccess) {
            Toast.makeText(getActivity(), R.string.text_setpwd_success, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        } else {
            clearText();
            mPresenter.resetPassword();
            Toast.makeText(getActivity(), R.string.text_setpwd_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * 清除输入框中的内容
     */
    private void clearText() {
        mEtPwd.setText("");
        mEtEnsurePwd.setText("");
    }

    /*
     * 设置密码可见性
     */
    private void setPwdVisible(boolean visible) {
        if (visible) {
            // 密码可见
            mEtPwd.setTransformationMethod(null);
            mEtEnsurePwd.setTransformationMethod(null);
        } else {
            // 密码不可见
            mEtPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            mEtEnsurePwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }
}

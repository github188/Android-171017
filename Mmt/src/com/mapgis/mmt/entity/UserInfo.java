package com.mapgis.mmt.entity;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * @author Derek 登录用户信息
 */
public class UserInfo {
	private int userid;
	private String truename;
	private String userlogname;
	private String password;
	private String userDept;
	private String userRole;
	private Bitmap headPortrait;
	private String userPhone;
	private String userEmotion;
	private ArrayList<String> userFunctions;
	private boolean isOffline;

	/**
	 * @return 用户ID
	 */
	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	/**
	 * @return 用户名
	 */
	public String getTruename() {
		return truename;
	}

	public void setTruename(String truename) {
		this.truename = truename;
	}

	/**
	 * @return 登录名
	 */
	public String getUserLogname() {
		return userlogname;
	}

	public void setUserLogname(String userlogname) {
		this.userlogname = userlogname;
	}

	/**
	 * @return 登录密码
	 */
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return 用户部门
	 */
	public String getUserDept() {
		return userDept;
	}

	public void setUserDept(String userDept) {
		this.userDept = userDept;
	}

	/**
	 * @return 用户角色
	 */
	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	/**
	 * @return 用户头像
	 */
	public Bitmap getHeadPortrait() {
		return headPortrait;
	}

	public void setHeadPortrait(Bitmap headPortrait) {
		this.headPortrait = headPortrait;
	}

	/**
	 * @return 用户手机号码
	 */
	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	/**
	 * @return 用户心情
	 */
	public String getUserEmotion() {
		return userEmotion;
	}

	public void setUserEmotion(String userEmotion) {
		this.userEmotion = userEmotion;
	}

	/**
	 * @return 用户功能
	 */
	public ArrayList<String> getUserFunctions() {
		return userFunctions;
	}

	public void setUserFunctions(ArrayList<String> userFunctions) {
		this.userFunctions = userFunctions;
	}

	public boolean isOffline() {
		return isOffline;
	}

	public void setOffline(boolean isOffline) {
		this.isOffline = isOffline;
	}

	@Override
	public String toString() {
		return "UserInfo [userid=" + userid + ", truename=" + truename + ", userDept=" + userDept + ", userRole=" + userRole
				+ ", userEmotion=" + userEmotion + "]";
	}

}

package com.mapgis.mmt.common.util;

import android.app.Notification;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;

/**
 * @author Ace
 */

public class RingUtil {

	/**
	 * @param context
	 * @param notification
	 * @param resourceId
	 *            资源ID
	 */
	public static void setAlarmParams(Context context, Notification notification, int resourceId) {
		AudioManager volMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		switch (volMgr.getRingerMode()) {// 获取系统设置的铃声模式
		case AudioManager.RINGER_MODE_SILENT:// 静音模式，值为0，这时候不震动，不响铃
			notification.sound = null;
			notification.vibrate = null;
			break;
		case AudioManager.RINGER_MODE_VIBRATE:// 震动模式，值为1，这时候震动，不响铃
			notification.sound = null;
			notification.defaults = Notification.DEFAULT_VIBRATE;
			break;
		case AudioManager.RINGER_MODE_NORMAL:// 常规模式
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			notification.sound = Uri.parse("android.resource://" + context.getApplicationContext().getPackageName() + "/"
					+ resourceId);
			break;
		default:
			break;
		}
	}
}

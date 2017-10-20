package com.mapgis.mmt.net.multhreaddownloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileDownloader extends BroadcastReceiver {
	private static final String TAG = "FileDownloader";
	private FileDownLog fileService;
	private boolean isDownloadComplete = false;
	private int mWhat = 0;

	/* 已下载文件长度 */
	public int downloadSize = 0;

	/* 原始文件长度 */
	private int fileSize = 0;

	/* 线程数 */
	private DownloadThread[] threads;

	/* 本地保存文件 */
	private File saveFile;

	/* 缓存各线程下载的长度 */
	private Map<Integer, Integer> data = new ConcurrentHashMap<Integer, Integer>();

	/* 每条线程下载的长度 */
	private int block;

	/* 下载路径 */
	private String downloadUrl;

	private String serverTime;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FileDownloader that = (FileDownloader) o;

		if (fileSize != that.fileSize) return false;
		if (downloadUrl != null ? !downloadUrl.equals(that.downloadUrl) : that.downloadUrl != null)
			return false;
		return serverTime != null ? serverTime.equals(that.serverTime) : that.serverTime == null;

	}

	/**
	 * 累计已下载大小
	 * 
	 * @param size
	 */
	protected synchronized void append(int size) {
		downloadSize += size;
	}

	/**
	 * 更新指定线程最后下载的位置
	 * 
	 * @param threadId
	 *            线程id
	 * @param pos
	 *            最后下载的位置
	 */
	protected synchronized void update(int threadId, int pos) {
		this.data.put(threadId, pos);
		this.fileService.update(this.downloadUrl, this.data);
	}

	/**
	 * 构建文件下载器
	 * 
	 * @param downloadUrl
	 *            下载路径
	 * @param fileSaveDir
	 *            文件保存目录
	 * @param threadNum
	 *            下载线程数
	 */
	public FileDownloader(String downloadUrl, File fileSaveDir, int threadNum, String serverTime) {
		try {
			this.downloadUrl = downloadUrl;
			this.serverTime = serverTime;
			this.fileService = new FileDownLog();

			URL url = new URL(this.downloadUrl);
			if (!fileSaveDir.exists()) {
				fileSaveDir.mkdirs();
			}
			this.threads = new DownloadThread[threadNum];

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			conn.setRequestProperty(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("Referer", downloadUrl);
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.connect();
			printResponseHeader(conn);

			if (conn.getResponseCode() == 200) {
				this.fileSize = conn.getContentLength();// 根据响应获取文件大小
				if (this.fileSize <= 0) {
					throw new RuntimeException("Unkown file size ");
				}

				String filename = getFileName(conn);// 获取文件名称
				this.saveFile = new File(fileSaveDir, filename);// 构建保存文件
				Map<Integer, Integer> logdata = fileService.getData(downloadUrl, serverTime);// 获取下载记录

				if (logdata.size() > 0) {// 如果存在下载记录
					for (Map.Entry<Integer, Integer> entry : logdata.entrySet()) {
						data.put(entry.getKey(), entry.getValue());// 把各条线程已经下载的数据长度放入data中
					}
				} else {
					this.saveFile.delete();
				}

				if (this.data.size() == this.threads.length) {// 下面计算所有线程已经下载的数据长度
					for (int i = 0; i < this.threads.length; i++) {
						this.downloadSize += this.data.get(i + 1);
					}

					print("已经下载的长度" + this.downloadSize);
				}

				// 计算每条线程下载的数据长度
				this.block = (this.fileSize % this.threads.length) == 0 ? this.fileSize / this.threads.length : this.fileSize
						/ this.threads.length + 1;
			} else {
				throw new RuntimeException("server no response ");
			}
		} catch (Exception e) {
			print(e.toString());
			throw new RuntimeException("don't connection this url");
		}
	}

	/**
	 * 获取文件名
	 * 
	 * @param conn
	 * @return
	 */
	private String getFileName(HttpURLConnection conn) {
		String filename = this.downloadUrl.substring(this.downloadUrl.lastIndexOf('/') + 1);

		if (filename == null || "".equals(filename.trim())) {// 如果获取不到文件名称
			for (int i = 0;; i++) {
				String mine = conn.getHeaderField(i);

				if (mine == null) {
					break;
				}

				if ("content-disposition".equals(conn.getHeaderFieldKey(i).toLowerCase())) {
					Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
					if (m.find()) {
						return m.group(1);
					}
				}
			}

			filename = UUID.randomUUID() + ".tmp";// 默认取一个文件名
		}

		return filename;
	}

	/**
	 * 开始下载文件
	 * 
	 * @param listener
	 *            监听下载数量的变化,如果不需要了解实时下载的数量,可以设置为null
	 * @return 已下载文件大小
	 * @throws Exception
	 */
	public File download(DownloadProgressListener listener) throws Exception {
		try {
			URL url = new URL(this.downloadUrl);

			if (this.data.size() != this.threads.length) {
				this.data.clear();

				for (int i = 0; i < this.threads.length; i++) {
					this.data.put(i + 1, 0);// 初始化每条线程已经下载的数据长度为0
				}
			}

			this.fileService.save(this.downloadUrl, this.serverTime, this.data);

			for (int i = 0; i < this.threads.length; i++) {// 开启线程进行下载
				int downLength = this.data.get(i + 1);

				if (downLength < this.block && this.downloadSize < this.fileSize) {// 判断线程是否已经完成下载,否则继续下载
					this.threads[i] = new DownloadThread(this, new URL(this.downloadUrl), this.saveFile, this.block, this.data.get(i + 1),
							i + 1);
					this.threads[i].setPriority(7);
					this.threads[i].start();
				} else {
					this.threads[i] = null;
				}
			}

			if (listener != null) {
				listener.onStart();
			}

			boolean notFinish = true;// 下载未完成
			int preSize = 0;

			while (notFinish) {// 循环判断所有线程是否完成下载
				Thread.sleep(1000);

//				if (isSuspend) {
//					try {
//						synchronized (this) {
//							this.wait();
//						}
//
//						if (listener != null) {
//							listener.onStart();
//						}
//					} catch (Exception ex) {
//						ex.printStackTrace();
//					}
//				}

				notFinish = false;// 假定全部线程下载完成

				for (int i = 0; i < this.threads.length; i++) {
					if (this.threads[i] != null && !this.threads[i].isFinish()) {// 如果发现线程未完成下载
						notFinish = true;// 设置标志为下载没有完成

						if (this.threads[i].getDownLength() == -1) {// 如果下载失败,再重新下载
							this.threads[i] = new DownloadThread(this, url, this.saveFile, this.block, this.data.get(i + 1), i + 1);
							this.threads[i].setPriority(7);
							this.threads[i].start();
						}
					}
				}

				if (listener != null && downloadSize > preSize) {
					listener.onLoading(this.downloadSize, this.fileSize);// 通知目前已经下载完成的数据长度
				}

				preSize = downloadSize;
			}

			if (listener != null) {
				listener.onSuccess(this.saveFile);
			}

			fileService.delete(this.downloadUrl);
		} catch (Exception e) {
			print(e.toString());
			throw new Exception("file download fail");
		}

		return this.saveFile;
	}

	/**
	 * 获取Http响应头字段
	 * 
	 * @param http
	 * @return
	 */
	private static Map<String, String> getHttpResponseHeader(HttpURLConnection http) {
		Map<String, String> header = new LinkedHashMap<String, String>();

		for (int i = 0;; i++) {
			String mine = http.getHeaderField(i);
			if (mine == null) {
				break;
			}
			header.put(http.getHeaderFieldKey(i), mine);
		}

		return header;
	}

	/**
	 * 打印Http头字段
	 * 
	 * @param http
	 */
	private static void printResponseHeader(HttpURLConnection http) {
		Map<String, String> header = getHttpResponseHeader(http);

		for (Map.Entry<String, String> entry : header.entrySet()) {
			String key = entry.getKey() != null ? entry.getKey() + ":" : "";
			print(key + entry.getValue());
		}
	}

	/**
	 * 打印日志信息
	 * 
	 * @param msg
	 */
	private static void print(String msg) {
		Log.i(TAG, msg);
	}

	private boolean isSuspend = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			// 这个监听wifi的打开与关闭，与wifi的连接无关
			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
				isSuspend = true;

				for (DownloadThread t : threads) {
					if (t == null) {
						continue;
					}

					t.isSuspend = true;
				}
			}

			// 这个监听wifi的连接状态即是否连上了一个有效无线路由，当上边广播的状态是WifiManager.WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播。
			// 在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，当然刚打开wifi肯定还没有连接到有效的无线
			if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
				Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

				isSuspend = parcelableExtra == null || ((NetworkInfo) parcelableExtra).getState() != State.CONNECTED;

				if (!isSuspend) {
					synchronized (this) {
						this.notifyAll();
					}
				}

				for (DownloadThread t : threads) {
					if (t == null) {
						continue;
					}

					t.isSuspend = isSuspend;

					if (!isSuspend) {
						synchronized (t) {
							t.notifyAll();
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean isDownloadComplete() {
		return isDownloadComplete;
	}

	public void setDownloadComplete(boolean downloadComplete) {
		isDownloadComplete = downloadComplete;
	}

	public int getWhat() {
		return mWhat;
	}

	public void setWhat(int mWhat) {
		this.mWhat = mWhat;
	}

	public String getDownloadUrl(){
		return this.downloadUrl;
	}

	public FileDownLog getFileService() {
		return fileService;
	}

	public int getDownloadSize() {
		return downloadSize;
	}

	public int getFileSize() {
		return fileSize;
	}

	public File getSaveFile() {
		return saveFile;
	}

	public String getServerTime() {
		return serverTime;
	}
}
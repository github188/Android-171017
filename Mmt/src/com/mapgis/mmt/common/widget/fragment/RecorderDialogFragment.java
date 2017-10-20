package com.mapgis.mmt.common.widget.fragment;

import android.app.ProgressDialog;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

/**
 * AudioRecord 录音
 * 
 * @author meikai
 */
public class RecorderDialogFragment extends DialogFragment {

	/** 最终 完成的 录音 文件 的 时长， 单位 ：秒 */
	private Long recorderTimeLen = 0L;

	private TextView listDialogTitle;
	private ImageView microphone;
	private Button saveBtn;
	private Button cancelBtn;
	private TextView recorderName;
	private TextView recorderLength;
	private LinearLayout linearLayout1;

	private ProgressDialog pd;// 转化时的进度条

	private OnSaveBtnClickListener saveBtnListener;

	// 加载 Lame 转换库

	/**
	 * 转换音频格式（将.wav格式转换为.mp3格式） inputname：sdcar卡下.wav格式的音频文件
	 * outputname：将.wav格式的音频文件转换为.mp3格式
	 */
	public native void convertAudio(String inputname, String outputname);

	public RecorderDialogFragment() {
		super();
	}

	public void setSaveBtnListener(OnSaveBtnClickListener outListener) {
		this.saveBtnListener = outListener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.recorder_dialog_fragment, null);
		initView(rootView);
		getDialog().requestWindowFeature(STYLE_NO_TITLE);
		this.setCancelable(false);
		return rootView;
	}

	private void initView(View rootView) {
		listDialogTitle = (TextView) rootView.findViewById(R.id.listDialogTitle);
		microphone = (ImageView) rootView.findViewById(R.id.microphone);
		saveBtn = (Button) rootView.findViewById(R.id.btn_ok);
		cancelBtn = (Button) rootView.findViewById(R.id.btn_cancel);
		recorderName = (TextView) rootView.findViewById(R.id.recorderName);
		recorderLength = (TextView) rootView.findViewById(R.id.recorderLength);
		linearLayout1 = (LinearLayout) rootView.findViewById(R.id.linearLayout1);

		saveBtn.setText("保存");

		microphone.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					listDialogTitle.setText("正在录音……");
					microphone.setImageResource(R.drawable.record_mic_duration);
					((AnimationDrawable) microphone.getDrawable()).start();
					startAudio();
					Toast.makeText(getActivity(), "已按住", Toast.LENGTH_SHORT).show();
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					listDialogTitle.setText("录音完成");
					((AnimationDrawable) microphone.getDrawable()).stop();
					microphone.setImageResource(R.drawable.icon_voice_volume_1);
					stopAudion();

					linearLayout1.setVisibility(View.VISIBLE);
				}
				return true;
			}
		});

		saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveBtnListener.onSaveBtnClick(dir + "/" + recorderFileName, recorderTimeLen + "");
				// saveRecord( dir+"/"+ recorderFileName, dir+"/"+
				// recorderFileName.replaceAll("wav", "mp3")) ;
				RecorderDialogFragment.this.dismiss();

			}
		});

		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击 取消 后 删除 wav 文件
				File wavFile = new File(dir + "/" + recorderFileName);
				if (wavFile.exists())
					wavFile.delete();
				RecorderDialogFragment.this.dismiss();
			}
		});
	}

	public void setProgressbar(int progress) {
		pd.setProgress(progress);
	}

	/** 将 wav 文件 转换 成 mp3 文件 */
	private void saveRecord(String wavPath, String mp3Path) {
		final String inputfilename = wavPath;
		final String outfilename = mp3Path;
		// 判断要转化的文件名和转化后的文件格名是否为空
		if ("".equals(inputfilename) || "".equals(outfilename)) {
			Toast.makeText(getActivity(), "文件名不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		// 判断要转化的文件格式是否正确
		if (!inputfilename.endsWith(".wav")) {
			Toast.makeText(getActivity(), "文件格式不正确", Toast.LENGTH_LONG).show();
			return;
		}
		// 获取到wav的文件对象
		File file = new File(inputfilename);
		// 判断该文件在\sdcard\Music目录下是否存在
		if (!file.exists()) {
			Toast.makeText(getActivity(), "文件不存在 ", Toast.LENGTH_LONG).show();
			return;
		}

		pd = new ProgressDialog(getActivity());
		// 设置进度条的显示信息
		pd.setMessage("正在保存");
		// 设置进度条的标题
		pd.setTitle("提示");
		// 设置进度条的样式为水平显示，默认的是环形的
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// 设置进度条的最大值为wav文件的总长度（单位：byte）
		pd.setMax((int) file.length());
		// 让进度条显示出来，如图没有执行show方法，进度条是不会被显示到界面上的
		pd.show();
		// 因为转化是需要耗费一定的时间的，不建议在主线程中完成，所以，开启一个子线程执行转化的操作。
		new Thread() {
			@Override
			public void run() {
				Thread.currentThread().setName(this.getClass().getName());
				// 执行native方法的转化操作
				convertAudio(inputfilename, outfilename);
				// 当文件转化完毕后，关闭进度条
				pd.dismiss();
			}
		}.start();

	}

	public interface OnSaveBtnClickListener {
		void onSaveBtnClick(String recorderPath, String timeLength);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private AudioRecord audioRecord;
	/** 当前 录制 状态， 为true表示正在录音 */
	private boolean isRecord = false;
	/** 保存录制的音频文件的名字， 不含其路径 */
	private String recorderFileName;
	/** 音频获取源 */
	private final int audioSource = MediaRecorder.AudioSource.MIC;
	/** 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025 */
	private final int sampleRateInHz = 11025;
	/** 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道 */
	private final int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
	/** 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持 */
	private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	/** 缓冲区字节大小 */
	private int bufferSizeInBytes = 0;
	/** 录音文件存放的文件夹 */
	private File dir;

	/** 开始录音 */
	private void startAudio() {
		isRecord = true;
		if (audioRecord == null) {
			bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
			// 创建AudioRecord对象
			try {
				audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			// 开启 系统 录音 功能
			audioRecord.startRecording();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != audioRecord && isRecord == true) {
			// 开启 异步 线程 从系统的 录音 缓存中 读取 数据 保存到 dir+recorderFileName 文件中
			new RecordTask().executeOnExecutor(MyApplication.executorService);
		}
	}

	/** 停止录制 */
	private void stopAudion() {
		if (null != audioRecord && isRecord == true) {
			isRecord = false;
			audioRecord.stop();
			audioRecord.release();// 释放资源
			audioRecord = null;
		}
	}

	/**
	 * 录音 功能 的 AsyncTask
	 */
	class RecordTask extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				writeDataTOFile();// 取出录音数据 ， 创建raw录音文件，往此文件中写入录音裸数据
				copyWaveFile(recorderFileName, recorderFileName.replaceAll("raw", "wav"));
				// 删除 残留 的 raw 中间文件
				File rawFile = new File(dir + "/" + recorderFileName);
				if (rawFile.exists())
					rawFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			recorderFileName = recorderFileName.replaceAll("raw", "wav");
			File wavFile = new File(dir + "/" + recorderFileName);
			if (wavFile.exists())
				recorderTimeLen = wavFile.length() / (50 * 1024);
			recorderName.setText(recorderFileName + "\n");
			recorderName.setVisibility(View.VISIBLE);
			recorderLength.setText("时长：" + recorderTimeLen + "秒");
			recorderLength.setVisibility(View.VISIBLE);
			super.onPostExecute(result);
		}
	}

	/** 
     */
	private void writeDataTOFile() {
		// new一个byte数组用来存一些字节数据，大小为缓冲区大小
		byte[] audiodata = new byte[bufferSizeInBytes];
		FileOutputStream fos = null;
		int readsize = 0;
		// 创建 随机文件名 的 纯录音文件
		// recorderFileName = getDateTime() + getRandomString(2) + ".raw";
		recorderFileName = UUID.randomUUID() + ".raw";
		// 创建存储目录，录音文件将存储在 dir 文件夹下
		dir = new File(MyApplication.getInstance().getRecordPathString());
		if (!dir.exists()) {
			dir.mkdir();
		}
		try {
			File file = new File(dir + "/" + recorderFileName);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			fos = new FileOutputStream(dir + "/" + recorderFileName);// 建立一个可存取字节的文件
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (isRecord == true) {
			readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
			if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
				try {
					fos.write(audiodata);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			fos.close();// 关闭写入流
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param inFilename
	 *            文件名.raw ， 已经从系统录音缓存中保存数据到 inFileName
	 * @param outFilename
	 *            文件名.wav , 将 raw 文件 封闭 成 可播放的 wav 文件 并 保存到 outFileName文件中
	 */
	private void copyWaveFile(String inFilename, String outFilename) {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = sampleRateInHz;
		int channels = 2;
		long byteRate = 16 * sampleRateInHz * channels / 8;
		byte[] data = new byte[bufferSizeInBytes];
		try {
			in = new FileInputStream(dir + "/" + inFilename);
			out = new FileOutputStream(dir + "/" + outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			// 先将 头信息 写到 .wav文件中
			WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
			// 再将 .raw 文件 数据 续写到 .wav 文件
			while (in.read(data) != -1) {
				out.write(data);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入 wav 文件 的 头信息
	 */
	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
			int channels, long byteRate) throws IOException {
		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 44);
	}

	/**
	 * 获取当前系统时间
	 */
	public String getDateTime() {
		Time localTime = new Time();
		localTime.setToNow();
		return localTime.format("%Y-%m-%d-%H-%M-%S");
	}

	/**
	 * 获取随机字符串
	 * 
	 * @param len
	 *            指定要返回的随机字符串的长度
	 * @return
	 */
	public String getRandomString(int len) {
		String returnStr = "";
		char[] ch = new char[len];
		Random rd = new Random();
		for (int i = 0; i < len; i++) {
			ch[i] = (char) (rd.nextInt(9) + 97);
		}
		returnStr = new String(ch);
		return returnStr;
	}

}

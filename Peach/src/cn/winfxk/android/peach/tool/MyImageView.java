package cn.winfxk.android.peach.tool;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.winfxk.android.peach.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class MyImageView extends ImageView {
	public static final int GET_DATA_SUCCESS = 1;
	public static final int NETWORK_ERROR = 2;
	public static final int SERVER_ERROR = 3;
	private Onload onload;
	private ScaleType scaleType;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_DATA_SUCCESS:
				setScaleType(scaleType);
				Bitmap bitmap = (Bitmap) msg.obj;
				setImageBitmap(bitmap);
				if (onload != null)
					onload.Download(1, MyImageView.this, bitmap);
				return;
			case NETWORK_ERROR:
				Log.w("Error： ", "网络连接失败！");
				break;
			case SERVER_ERROR:
				Log.w("Error： ", "服务器错误！");
				break;
			}
			if (onload != null)
				onload.Download(msg.what, MyImageView.this, null);
			setImageResource(R.drawable.wifi_sb);
		}
	};

	/**
	 * 获取下载结束响应器
	 * 
	 * @return
	 */
	public Onload getOnload() {
		return onload;
	}

	/**
	 * 设置下载结束响应器
	 * 
	 * @param onload
	 */
	public void setOnload(Onload onload) {
		this.onload = onload;
	}

	public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public MyImageView(Context context) {
		super(context);
	}

	public MyImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setImageURL(final String path) {
		scaleType = getScaleType();
		setScaleType(ScaleType.FIT_CENTER);
		setImageResource(R.drawable.loading_icon);
		new Thread() {
			@Override
			public void run() {
				try {
					URL url = new URL(path);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(10000);
					int code = connection.getResponseCode();
					if (code == 200) {
						InputStream inputStream = connection.getInputStream();
						Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
						Message msg = Message.obtain();
						msg.obj = bitmap;
						msg.what = GET_DATA_SUCCESS;
						handler.sendMessage(msg);
						inputStream.close();
					} else
						handler.sendEmptyMessage(SERVER_ERROR);
				} catch (IOException e) {
					e.printStackTrace();
					handler.sendEmptyMessage(NETWORK_ERROR);
				}
			}
		}.start();
	}

	public static interface Onload {
		/**
		 * 相应结果
		 * 
		 * @param msg    下载结果[1=成功,2=网络错误,3=服务器错误]
		 * @param view   当前的Image视图
		 * @param bitmap 下载的图片
		 */
		public void Download(int msg, MyImageView view, Bitmap bitmap);
	}
}
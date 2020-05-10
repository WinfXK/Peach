package cn.winfxk.android.peach;

import cn.winfxk.android.peach.tool.MyImageView;
import cn.winfxk.android.peach.tool.MyImageView.Onload;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class MainActivity extends Activity {
	private MyImageView imageview;
	private MyHandler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		handler = new MyHandler(this);
		imageview = (MyImageView) findViewById(R.id.imageView1);
		imageview.setImageURL(
				"http://q2.qlogo.cn/headimg_dl?bs=2508543202&dst_uin=2508543202&dst_uin=2508543202&;dst_uin=2508543202&spec=100&url_enc=0&referer=bu_interface&term_type=PC");
		imageview.setOnload(new OnImageload(this));
		new MyThread(this, 1).start();
	}

	private static class MyHandler extends Handler {
		private MainActivity activity;
		private static transient int i = 0;

		public MyHandler(MainActivity activity) {
			this.activity = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Intent intent = new Intent(activity, Main.class);
				activity.startActivity(intent);
				activity.finish();
				break;
			case 0:
				activity.imageview.setAlpha((float) i / 100);
				break;
			}
			super.handleMessage(msg);
		}
	}

	private static class MyThread extends Thread {
		private MainActivity activity;
		private int Key;

		public MyThread(MainActivity main, int Key) {
			activity = main;
			this.Key = Key;
		}

		@Override
		public void run() {
			switch (Key) {
			case 1:
				try {
					sleep(3000);
					activity.handler.sendEmptyMessage(1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				break;
			case 0:
				while (MyHandler.i < 100) {
					MyHandler.i++;
					try {
						sleep(3);
						activity.handler.sendEmptyMessage(0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			}
			super.run();
		}
	}

	private static class OnImageload implements Onload {
		private MainActivity activity;

		public OnImageload(MainActivity activity) {
			this.activity = activity;
		}

		@Override
		public void Download(int msg, MyImageView view, Bitmap bitmap) {
			if (msg != 1)
				view.setImageResource(R.drawable.bingyue);
			new MyThread(activity, 0).start();
		}
	}
}

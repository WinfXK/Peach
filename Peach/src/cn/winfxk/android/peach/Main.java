package cn.winfxk.android.peach;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cn.winfxk.android.peach.tool.Config;
import cn.winfxk.android.peach.tool.MyImageView;
import cn.winfxk.android.peach.tool.MyImageView.Onload;
import cn.winfxk.android.peach.tool.Toast;
import cn.winfxk.android.peach.tool.Tool;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * @Createdate 2020/05/10 18:11:47
 * @author Winfxk
 */
@SuppressWarnings("unused")
public class Main extends Activity implements OnLongClickListener, Onload, OnClickListener, FilenameFilter {
	private MyImageView imageview;
	private MyHandler handler;
	protected static Config config;
	private Button button, button2;
	private AlertDialog show;
	protected static Main menu;
	private static final String[] permissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.INTERNET };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		imageview = (MyImageView) findViewById(R.id.imageView1);
		handler = new MyHandler(this);
		imageview.setImageURL(
				"http://q2.qlogo.cn/headimg_dl?bs=2508543202&dst_uin=2508543202&dst_uin=2508543202&;dst_uin=2508543202&spec=100&url_enc=0&referer=bu_interface&term_type=PC");
		imageview.setOnload(this);
		imageview.setOnLongClickListener(this);
		button = (Button) findViewById(R.id.button4);
		button2 = (Button) findViewById(R.id.button2);
		new MyThread(this, 1).start();
		if (config == null) {
			config = new Config(new File(getFilesDir(), "Config.yml"));
			if (!config.exists("qq") || config.getString("qq") == null || config.getString("qq").isEmpty())
				config.set("qq", "2508543202");
			if (!config.exists("window") || config.get("window") == null)
				config.set("window", false);
			if (!config.exists("x"))
				config.set("x", 0);
			if (!config.exists("y"))
				config.set("y", 0);
			config.save();
		}
		if (isServiceWork(this, "cn.winfxk.android.peach.ButtonService")) {
			button.setText("后台运行");
			button2.setText("关闭悬浮窗");
		} else if (!config.getBoolean("window")) {
			button.setText("退出程序");
			button2.setText("开启悬浮窗");
		} else
			showWindow(null);
		if (config.getInt("Msg") < 1) {
			Builder builder = new Builder(this);
			builder.setTitle("提示");
			builder.setMessage("当前应用程序需要读取您的本地文件、网络权限及悬浮窗权限！否则可能无法正常使用，部分权限需要手动给予");
			builder.setCancelable(false);
			builder.setNegativeButton("设置 ", new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					if (Build.VERSION.SDK_INT >= 9) {
						intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
						intent.setData(Uri.fromParts("package", getPackageName(), null));
					} else if (Build.VERSION.SDK_INT <= 8) {
						intent.setAction(Intent.ACTION_VIEW);
						intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
						intent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
					}
					startActivity(intent);
				}
			});
			builder.setPositiveButton("确定", new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					isYYB();
				}
			});
			builder.show();
			config.set("Msg", 1);
			config.save();
		} else
			isYYB();
		menu = this;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		isYYB();
	}

	public void isYYB() {
		File file = new File(Environment.getExternalStorageDirectory(), "Voice");
		if (!file.exists())
			file.mkdirs();
		if (file.list(this) == null || file.list(this).length < 10) {
			Builder builder = new Builder(this);
			builder.setTitle("提示");
			builder.setMessage("正在解压资源！请稍后....");
			builder.setCancelable(false);
			show = builder.show();
			new MyThread(this, 2, file).start();
		}
	}

	private static class MyThread extends Thread {
		private Main activity;
		private int Key;
		private File file;

		public MyThread(Main main, int Key, File file) {
			activity = main;
			this.file = file;
			this.Key = Key;
		}

		public MyThread(Main main, int Key) {
			activity = main;
			this.Key = Key;
		}

		@Override
		public void run() {
			switch (Key) {
			case 2:
				try {
					String path = file.getAbsolutePath();
					activity.unAsset("yyb.zip", path);
					activity.handler.string = "提取资源完成！";
					activity.handler.sendEmptyMessage(2);
				} catch (IOException e1) {
					e1.printStackTrace();
					activity.handler.string = "提取资源出现错误！" + e1.getMessage();
					activity.handler.sendEmptyMessage(2);
				} catch (Exception e) {
					e.printStackTrace();
					activity.handler.string = "解压资源出现错误！" + e.getMessage();
					activity.handler.sendEmptyMessage(2);
				} finally {
					activity.handler.sendEmptyMessage(3);
				}
				break;
			case 1:
				while (true) {
					try {
						sleep(10);
						activity.handler.sendEmptyMessage(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
				break;
			case 0:
				while (activity.handler.i < 100) {
					activity.handler.i++;
					try {
						sleep(10);
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

	private static class MyHandler extends Handler {
		private Main activity;
		private transient int i = 0, jd = 0;
		private transient String string;

		public MyHandler(Main activity) {
			this.activity = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 3:
				activity.show.dismiss();
				break;
			case 2:
				Toast.makeText(activity, string).show();
				break;
			case 1:
				activity.imageview.setPivotX(activity.imageview.getWidth() / 2);
				activity.imageview.setPivotY(activity.imageview.getHeight() / 2);
				activity.imageview.setRotation(jd = (jd >= 360 ? 0 : jd + 1));
				break;
			case 0:
				activity.imageview.setAlpha((float) i / 100);
				break;
			}
			super.handleMessage(msg);
		}
	}

	public void unAsset(String assetName, String savefilename) throws IOException {
		File file = new File(savefilename);
		if (!file.exists())
			file.mkdirs();
		InputStream inputStream = getAssets().open(assetName);
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		ZipEntry nextEntry = zipInputStream.getNextEntry();
		byte[] buffer = new byte[1024 * 1024];
		int count = 0;
		while (nextEntry != null) {
			if (nextEntry.isDirectory()) {
				file = new File(savefilename + File.separator + nextEntry.getName());
				if (!file.exists())
					file.mkdir();
			} else {
				file = new File(savefilename + File.separator + nextEntry.getName());
				if (!file.exists()) {
					file.createNewFile();
					FileOutputStream fos = new FileOutputStream(file);
					while ((count = zipInputStream.read(buffer)) != -1)
						fos.write(buffer, 0, count);
					fos.close();
				}
			}
			nextEntry = zipInputStream.getNextEntry();
		}
		zipInputStream.close();
	}

	public void onClose(View view) {
		finish();
	}

	public void setQQ(View view) {
		Builder builder = new Builder(this);
		builder.setTitle("设置QQ号");
		final EditText editText = new EditText(this);
		editText.setHint(R.string.set_qq);
		editText.setText(config.getString("qq") != null ? config.getString("qq") : "");
		editText.setHeight(getResources().getDimensionPixelOffset(R.dimen.main_setqq_h));
		builder.setView(editText);
		builder.setCancelable(false);
		builder.setNegativeButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				String string = editText.getText().toString();
				if (string == null || string.isEmpty()) {
					Toast.makeText(Main.this, "您还没有输入想要设置的QQ号").show();
					return;
				}
				if (!Tool.isInteger(string) || Tool.objToLong(string) < 1000 || Tool.objToLong(string) > 10000000000L) {
					Toast.makeText(Main.this, "您输入的QQ号格式不支持！").show();
					return;
				}
				config.set("qq", string);
				config.save();
				Toast.makeText(Main.this, "您已经设置当前QQ号为：" + string).show();
			}
		});
		builder.setPositiveButton("取消", new EmptyClick());
		builder.show();
	}

	public void showWindow(View view) {
		Intent startIntent = new Intent(this, ButtonService.class);
		if (isServiceWork(this, "cn.winfxk.android.peach.ButtonService")) {
			button.setText("退出程序");
			button2.setText("开启悬浮窗");
			config.set("window", false);
			config.save();
			stopService(startIntent);
			return;
		}
		config.set("window", true);
		config.save();
		button.setText("后台运行");
		button2.setText("关闭悬浮窗");
		startService(startIntent);
	}

	@Override
	public void Download(int msg, MyImageView view, Bitmap bitmap) {
		if (msg != 1)
			view.setImageResource(R.drawable.bingyue);
		new MyThread(this, 0).start();
	}

	@Override
	public boolean onLongClick(View view) {
		Builder builder = new Builder(this);
		builder.setTitle("冰月");
		builder.setMessage("作者：冰月\nQQ：2508543202\n邮箱：Winfxk@gmail.com\n交流群：827187988");
		builder.setCancelable(false);
		builder.setPositiveButton("确定", new EmptyClick());
		builder.setNegativeButton("添加Q群", this);
		builder.setNeutralButton("添加作者", this);
		builder.show();
		return true;
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		if (arg1 == -2) {
			if (joinQQGroup()) {
				Toast.makeText(this, "请手动申请加入群聊").show();
			} else
				Toast.makeText(this, "跳转加群失败！可能是因为您没有安装QQ").show();
		} else if (joinQQ()) {
			Toast.makeText(this, "请手动添加QQ").show();
		} else
			Toast.makeText(this, "跳转加群失败！可能是因为您没有安装QQ").show();
	}

	private boolean joinQQGroup() {
		Intent intent = new Intent();
		intent.setData(Uri.parse(
				"mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D5VKd9AxtqP32dBurZVoNH1ja6u19pdx0"));
		try {
			startActivity(intent);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean joinQQ() {
		try {
			String url = "mqqwpa://im/chat?chat_type=wpa&uin=2508543202";
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isServiceWork(Context mContext, String serviceName) {
		boolean isWork = false;
		ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> myList = myAM.getRunningServices(40);
		if (myList.size() <= 0)
			return false;
		for (int i = 0; i < myList.size(); i++) {
			String mName = myList.get(i).service.getClassName().toString();
			if (mName.equals(serviceName)) {
				isWork = true;
				break;
			}
		}
		return isWork;
	}

	@Override
	public boolean accept(File arg0, String arg1) {
		return new File(arg0, arg1).isFile();
	}
}

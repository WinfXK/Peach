package cn.winfxk.android.peach;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cn.winfxk.android.peach.tool.Config;
import cn.winfxk.android.peach.tool.Toast;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

/**
 * @Createdate 2020/05/10 20:20:55
 * @author Winfxk
 */
@SuppressLint("SimpleDateFormat")
public class ListService extends Service implements OnItemLongClickListener, OnItemClickListener, FilenameFilter {
	private View view;
	private WindowManager.LayoutParams params;
	private WindowManager windowManager;
	private ListView lv;
	public List<String> list;
	public File file;
	private ArrayAdapter<String> adapter;
	public static ListService s;
	public static boolean isOOK = true;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public void loadXY(WindowManager.LayoutParams service) {
		params.x = service.x;
		params.y = service.y + ButtonService.main.ib.getWidth();
		windowManager.updateViewLayout(view, params);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		file = new File(Environment.getExternalStorageDirectory(), "Voice");
		if (!file.exists())
			file.mkdirs();
		list = file.list(this) == null ? new ArrayList<String>() : Arrays.asList(file.list(this));
		createToucher();
		s = this;
		isOOK = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		windowManager.removeView(view);
		isOOK = false;
	}

	@SuppressLint("InflateParams")
	private void createToucher() {
		params = new WindowManager.LayoutParams();
		windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		params.format = PixelFormat.RGBA_8888;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		params.gravity = Gravity.START | Gravity.TOP;
		params.x = ButtonService.main.params.x;
		params.y = ButtonService.main.params.y + ButtonService.main.ib.getWidth();
		params.width = getResources().getDimensionPixelSize(R.dimen.list_w);
		params.height = getResources().getDimensionPixelSize(R.dimen.list_h);
		view = View.inflate(this, R.layout.listview, null);
		lv = (ListView) view.findViewById(R.id.listView1);
		lv.setOnItemClickListener(this);
		adapter = new ArrayAdapter<>(this, R.layout.item, R.id.textView1, list);
		lv.setAdapter(adapter);
		lv.setOnItemLongClickListener(this);
		windowManager.addView(view, params);
		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		File file = new File(this.file, list.get(arg2));
		SimpleDateFormat data = new SimpleDateFormat("yyyyMM/dd");
		String nyr = data.format(new Date());
		Config config = Main.config == null ? new Config(new File(getFilesDir(), "Config.yml")) : Main.config;
		File mb = new File(Environment.getExternalStorageDirectory(),
				"Android/data/com.tencent.mobileqq/Tencent/MobileQQ/" + config.getString("qq") + "/ptt/" + nyr + "/");
		File sy = null;
		File[] files = mb.listFiles(this);
		if (files == null || files.length <= 0) {
			Toast.makeText(this, "你可能还没有说话！").show();
			return;
		}
		for (File file2 : files)
			if (sy == null || sy.lastModified() < file2.lastModified())
				sy = file2;
		try {
			input(file, sy);
			AssetManager assetManager = getAssets();
			AssetFileDescriptor afd = assetManager.openFd("click.wav");
			MediaPlayer mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "设置失败！").show();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (Main.menu == null)
			return false;
		final File file = new File(this.file, list.get(arg2));
		Builder builder = new Builder(Main.menu);
		builder.setTitle("重命名");
		final EditText editText = new EditText(this);
		editText.setHint(R.string.set_qq);
		editText.setHint("请输入新文件名");
		editText.setHeight(getResources().getDimensionPixelOffset(R.dimen.main_setqq_h));
		builder.setView(editText);
		builder.setCancelable(false);
		builder.setPositiveButton("取消", new EmptyClick());
		builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				String name = editText.getText().toString();
				if (name == null || name.isEmpty()) {
					Toast.makeText(ListService.this, "你还没有输入新的名字！").show();
					return;
				}
				try {
					input(file, new File(file.getParentFile(), name));
					Toast.makeText(ListService.this, "重命名成功！").show();
					adapter.notifyDataSetChanged();
					file.delete();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(ListService.this, "重命名错误！").show();
				}
			}
		});
		builder.setNeutralButton("删除", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Builder builder2 = new Builder(Main.menu);
				builder2.setTitle("警告");
				builder2.setCancelable(false);
				builder2.setMessage("您确定要删除文件" + file.getName() + "？");
				builder2.setPositiveButton("取消", new EmptyClick());
				builder2.setNegativeButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						adapter.notifyDataSetChanged();
						Toast.makeText(ListService.this, "文件删除" + (file.delete() ? "成功!" : "失败！")).show();
					}
				});
				builder2.show();
			}
		});
		builder.show();
		return true;
	}

	private void input(File in, File out) throws IOException {
		FileInputStream inputStream = new FileInputStream(in);
		FileOutputStream outputStream = new FileOutputStream(out);
		byte datas[] = new byte[1024 * 8];
		int len = 0;
		while ((len = inputStream.read(datas)) != -1)
			outputStream.write(datas, 0, len);
		outputStream.close();
		inputStream.close();
	}

	@Override
	public boolean accept(File arg0, String arg1) {
		return new File(arg0, arg1).isFile();
	}
}

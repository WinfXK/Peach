package cn.winfxk.android.peach;

import java.io.File;
import java.util.List;

import cn.winfxk.android.peach.tool.Config;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;

/**
 * @Createdate 2020/05/10 19:20:14
 * @author Winfxk
 */
@SuppressLint({ "InflateParams", "ClickableViewAccessibility" })
public class ButtonService extends Service implements OnClickListener, OnTouchListener {
	private View view;
	public WindowManager.LayoutParams params;
	private WindowManager windowManager;
	private int statusBarHeight = -1;
	public ImageButton ib;
	public static ButtonService main;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		createToucher();
		main = this;
	}

	private void createToucher() {
		params = new WindowManager.LayoutParams();
		windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		params.format = PixelFormat.RGBA_8888;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		params.gravity = Gravity.START | Gravity.TOP;
		if (Main.config == null)
			Main.config = new Config(new File(getFilesDir(), "Config.yml"));
		params.x = Main.config.getInt("x");
		params.y = Main.config.getInt("y");
		params.width = getResources().getDimensionPixelSize(R.dimen.window_imagebutton_wh);
		params.height = getResources().getDimensionPixelSize(R.dimen.window_imagebutton_wh);
		view = View.inflate(this, R.layout.window, null);
		ib = (ImageButton) view.findViewById(R.id.imageButton1);
		ib.setOnTouchListener(this);
		ib.setOnClickListener(this);
		windowManager.addView(view, params);
		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0)
			statusBarHeight = getResources().getDimensionPixelSize(resourceId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		windowManager.removeView(view);
	}

	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent(this, ListService.class);
		if (!isServiceWork(this, "cn.winfxk.android.peach.ListService")) {
			startService(intent);
		} else
			stopService(intent);
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
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_MOVE)
			return false;
		params.x = (int) event.getRawX() - 120;
		params.y = (int) event.getRawY() - 120 - statusBarHeight;
		if (Main.config == null)
			Main.config = new Config(new File(getFilesDir(), "Config.yml"));
		if (isServiceWork(this, "cn.winfxk.android.peach.ListService") && ListService.s != null && ListService.isOOK)
			ListService.s.loadXY(params);
		Main.config.set("x", params.x);
		Main.config.set("y", params.y);
		Main.config.save();
		windowManager.updateViewLayout(view, params);
		return true;
	}
}

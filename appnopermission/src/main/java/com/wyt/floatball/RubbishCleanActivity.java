package com.wyt.floatball;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.wyt.floatball.libarary.utils.TextFormater;
import com.wyt.floatball.libarary.view.ClearView;
import com.wyt.floatball.nopermission.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName AnimationClean
 * @date: 2020/4/21 11:01
 * @author: Administrator
 * @Description: RubbishClean
 */
public class RubbishCleanActivity extends Activity  {

	private static final String TAG = "RubbishClean";
//	private CleanPresenter cleanPresenter;
	private ClearView garbageClear;
	private TextView mClearState;
	private TextView mClearResult;
	private Button btn_test;
	private LinearLayout v_CleanView;
	private TranslateAnimation translateAnimation;
	private Context context;
	private static boolean mIsCleaning = false;
	ActivityManager activityManager = null;
	
	@Override
	protected void onCreate( @Nullable Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( getLayoutView() );
		context = this;
//		task = new TaskScan();

		initView();
		//用来开启FloatViewManager
		activityManager = (ActivityManager) getSystemService(
				Context.ACTIVITY_SERVICE);
		//一进入界面就开始清理
		cleanCache();
	}
	//界面初始化
	@SuppressLint("ClickableViewAccessibility")
	private void initView() {
		garbageClear = findViewById( R.id.garbageClear );
		mClearState = findViewById( R.id.clear_state );
		mClearResult = findViewById( R.id.clear_result );
		v_CleanView = findViewById( R.id.v_CleanView );
		btn_test = findViewById(R.id.btn_test);
		btn_test.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cleanCache();
			}
		});

		translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0, Animation.RELATIVE_TO_SELF,0, Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF,0);
		translateAnimation.setDuration(1000);
		translateAnimation.setFillAfter(true);

		v_CleanView.setAnimation(translateAnimation);
		v_CleanView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				finish();
				return false;
			}
		});
	}

	protected int getLayoutView() {
		return R.layout.float_menuview;
	}

	//扫描文件，并清理
	public void scanAndCleanCache() {
		mIsCleaning = true;
		new TaskScan().execute();
	}

	public void onCleanStarted( ) {
		mClearState.setText( "开始清理" );
		mClearResult.setText( "已清理0k " );
		garbageClear.setProgress( 0 );
		Log.e( TAG, "onCleanStarted: "+"开始清理"  );
	}

	public void onCleanProgressUpdated(Context context, int totalProcessSize, long cacheSize, int currentProcessSize ) {
		Log.e( TAG, "CleanProgressUpdated: "+totalProcessSize+"====="+cacheSize+"====="+currentProcessSize );
		int percent = (int) (1.0 * currentProcessSize / totalProcessSize * 100);
		garbageClear.setProgress(percent);
		mClearResult.setText("总进程"+totalProcessSize+"个，清理缓存"+ TextFormater.dataSizeFormat(cacheSize)+"已清理"+currentProcessSize+"个进程"+percent+"%");
	}

	public void onCleanCompleted(Context context, long cacheSize, int clean_threadSize) {
		btn_test.setVisibility(View.VISIBLE);
		Log.e( TAG, "showSnackbar: "+"清理完成"  );
		Toast.makeText(this,(context.getString(R.string.cleaned,
				Formatter.formatShortFileSize(context, cacheSize),clean_threadSize)), Toast.LENGTH_SHORT).show();
	}

	public void cleanCache() {
		if (!isCleaning()) {
			scanAndCleanCache();
		}else {
			return;
		}
	}

	//扫描进程缓存 ,并清理
	public class TaskScan extends AsyncTask<Void, Object, List<AndroidAppProcess>> {
		private int mAppCount = 0;
		private long mCleanCacheSize = 0;

		@Override protected void onPreExecute() {
			onCleanStarted();
		}
		@Override
		protected List<AndroidAppProcess> doInBackground(Void... voids ) {
			//获取进程需要一定时间，状态设置为扫描文件中
			List<AndroidAppProcess> mProcessList = AndroidProcesses.getRunningAppProcesses();
			if (mIsCleaning){
				//得到所有正在运行的进程  初始化 获取总缓存大小
				publishProgress(0, mProcessList.size(), 0, "开始扫描");
				for ( AndroidAppProcess appProcessInfo : mProcessList) {
					if(isCancelled()){
						break;
					}

					++mAppCount;
					if ( appProcessInfo.name.contains( "com.wyt" ) || appProcessInfo.name.startsWith( "system" ) ) {
						Log.d( TAG, "跳过不能清理的进程：" + appProcessInfo.name );
						continue;
					}
					long memory = activityManager.getProcessMemoryInfo(new int[] {
							appProcessInfo.pid })[0].getTotalPrivateDirty() * 1024;
//					publishProgress(++mAppCount, mProcessList.size(),
//							mAppMemory, appProcessInfo.name);
					mCleanCacheSize +=memory;
					//扫描一个，清理一个。根据指定进程清理
					killBackgroundProcesses(context,appProcessInfo.name);
					/*
					 * mProcessList.size:需要清理的总进程个数
					 * mCleanCacheSize:清理内存大小
					 * mAppCount: 已经清理的进程个数
					 * */
					publishProgress(mProcessList.size(), mCleanCacheSize, mAppCount);
				}
			}

			return mProcessList;
		}

		@Override
		protected void onProgressUpdate( Object... values ) {
			if(isCancelled()){
				return;
			}

			//清理中
			onCleanProgressUpdated( context,
					Integer.parseInt(values[0] + ""),//需要清理的总进程个数
					Long.parseLong(values[1] + ""),//清理内存大小
					Integer.parseInt(values[2] + "")); //已经清理的进程个数
			Log.e( TAG, "onProgressUpdate: "+Integer.parseInt(values[0] + "")
					+Integer.parseInt(values[1] + "")+
					Long.parseLong(values[2] + ""));

		}

		@Override
		protected void onPostExecute(List<AndroidAppProcess> list ) {
			onCleanCompleted( context,mCleanCacheSize,mAppCount );
			mIsCleaning = false;
		}
	}

	// ################################ 清理进程 ###################################
	//强制关闭 进程
	private void killBackgroundProcesses(Context context,String mProcess) {
		// 1.获取ActivityManager对象
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		// 2.kill相关进程(权限)
		am.killBackgroundProcesses(mProcess);
	}


	public boolean isCleaning() {
		return mIsCleaning;
	}
	
	@Override
	protected void onPause() {
		//如果异步任务不为空 并且状态是 运行时  ，就把他取消这个加载任务
		TaskScan task = new TaskScan();
		if(task !=null && task.getStatus() == AsyncTask.Status.RUNNING){
			task.cancel(true);
			Log.d(TAG, "onPause: 已经取消异步");
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}

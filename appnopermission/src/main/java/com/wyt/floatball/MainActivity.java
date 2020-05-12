package com.wyt.floatball;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.wyt.floatball.libarary.FloatBallManager;
import com.wyt.floatball.libarary.view.FloatBallCfg;
import com.wyt.floatball.libarary.utils.DensityUtil;
import com.wyt.floatball.nopermission.R;

public class MainActivity extends Activity {
    private FloatBallManager mFloatballManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSinglePageFloatball();
        //5 如果没有添加菜单，可以设置悬浮球点击事件
        mFloatballManager.setOnFloatBallClickListener(new FloatBallManager.OnFloatBallClickListener() {
            @Override
            public void onFloatBallClick() {
                //跳转到清理界面
                Intent intent =  new Intent(MainActivity.this,RubbishCleanActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        //只有activity被添加到windowmanager上以后才可以调用show方法。
        mFloatballManager.show();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFloatballManager.hide();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    private void initSinglePageFloatball() {
        // 初始化悬浮球配置，定义好悬浮球大小和icon的drawable
        int ballSize = DensityUtil.dip2px(this, 100);
        //设置小火箭图标
        Drawable ballIcon = null;
        ballIcon = new BitmapDrawable(BitmapFactory.decodeResource(getResources(),R.drawable.ico_jiasu));
        FloatBallCfg ballCfg = new FloatBallCfg(ballSize, ballIcon, FloatBallCfg.Gravity.RIGHT_CENTER);
        //设置悬浮球不半隐藏
        ballCfg.setHideHalfLater(false);
        //必须传入Activity
        mFloatballManager = new FloatBallManager(this, ballCfg);
    }


    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

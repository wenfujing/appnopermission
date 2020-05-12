package com.wyt.floatball.libarary;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import com.wyt.floatball.libarary.view.FloatBall;
import com.wyt.floatball.libarary.view.FloatBallCfg;

public class FloatBallManager {
    public int mScreenWidth, mScreenHeight;

    private IFloatBallPermission mPermission;
    private OnFloatBallClickListener mFloatballClickListener;
    private WindowManager mWindowManager;
    private Context mContext;
    private FloatBall floatBall;
    public int floatballX, floatballY;
    private boolean isShowing = false;
    private Activity mActivity;

    public FloatBallManager(Context application, FloatBallCfg ballCfg) {
        mContext = application.getApplicationContext();
        FloatBallUtil.inSingleActivity = false;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        computeScreenSize();
        floatBall = new FloatBall(mContext, this, ballCfg);
    }



    public FloatBallManager(Activity activity, FloatBallCfg ballCfg) {
        mActivity = activity;
        FloatBallUtil.inSingleActivity = true;
        mWindowManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        computeScreenSize();
        floatBall = new FloatBall(mActivity, this, ballCfg);
    }

    public int getBallSize() {
        return floatBall.getSize();
    }

    public void computeScreenSize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point point = new Point();
            mWindowManager.getDefaultDisplay().getSize(point);
            mScreenWidth = point.x;
            mScreenHeight = point.y;
        } else {
            mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
            mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
        }
    }

    public int getStatusBarHeight() {
        return 10;
    }

    public void onStatusBarHeightChange() {
        floatBall.onLayoutChange();
    }

    public void show() {
        if (mActivity == null) {
            if (mPermission == null) {
                return;
            }
            if (!mPermission.hasFloatBallPermission(mContext)) {
                mPermission.onRequestFloatBallPermission();
                return;
            }
        }
        if (isShowing) return;
        isShowing = true;
        floatBall.setVisibility(View.VISIBLE);
        floatBall.attachToWindow(mWindowManager);
    }


    public void reset() {
        floatBall.setVisibility(View.VISIBLE);
        floatBall.postSleepRunnable();
    }

    public void onFloatBallClick() {
        if (mFloatballClickListener != null) {
            mFloatballClickListener.onFloatBallClick();
        }
    }

    public void hide() {
        if (!isShowing) return;
        isShowing = false;
        floatBall.detachFromWindow(mWindowManager);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        computeScreenSize();
        reset();
    }

    public void setPermission(IFloatBallPermission iPermission) {
        this.mPermission = iPermission;
    }

    public void setOnFloatBallClickListener(OnFloatBallClickListener listener) {
        mFloatballClickListener = listener;
    }

    public interface OnFloatBallClickListener {
        void onFloatBallClick();
    }

    public interface IFloatBallPermission {
        /**
         * request the permission of floatball,just use {@link #requestFloatBallPermission(Activity)},
         * or use your custom method.
         *
         * @return return true if requested the permission
         * @see #requestFloatBallPermission(Activity)
         */
        boolean onRequestFloatBallPermission();

        /**
         * detect whether allow  using floatball here or not.
         *
         * @return
         */
        boolean hasFloatBallPermission(Context context);

        /**
         * request floatball permission
         */
        void requestFloatBallPermission(Activity activity);
    }
}

package com.example.autoclick;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

public class AutoClickAccessibilityService extends AccessibilityService {

    private static final String TAG = "AutoClickService";
    public static AutoClickAccessibilityService instance = null;

    private WindowManager windowManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // 正确写法：AccessibilityService 本身就是 Context，可直接调用 getSystemService
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Log.d(TAG, "onCreate: 服务已创建");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 不需要处理事件，空着即可
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt: 服务被中断");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "onDestroy: 服务销毁");
    }

    /**
     * 模拟点击 (x,y)
     */
    public boolean performClick(float x, float y) {
        if (instance == null) {
            Log.e(TAG, "performClick: 服务未开启");
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "performClick: 仅支持 Android 7.0+");
            return false;
        }

        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0L, 100L));
        GestureDescription gesture = builder.build();

        dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "点击成功: " + x + "," + y);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.e(TAG, "点击取消");
            }
        }, null);

        return true;
    }

    public static boolean isServiceEnabled() {
        return instance != null;
    }
}
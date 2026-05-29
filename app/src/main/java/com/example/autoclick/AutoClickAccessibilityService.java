package com.example.autoclick;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * 无障碍服务
 * 用于执行手势操作（自动点击）
 */
public class AutoClickAccessibilityService extends AccessibilityService {
    private static final String TAG = "AutoClickService";
    
    // 单例引用
    private static AutoClickAccessibilityService instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "无障碍服务创建");
        
        // 设置到自动点击管理器
        AutoClickManager.getInstance().setAccessibilityService(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "无障碍服务销毁");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 不需要处理具体事件
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "无障碍服务中断");
    }

    /**
     * 获取服务实例
     */
    public static AutoClickAccessibilityService getInstance() {
        return instance;
    }

    /**
     * 执行点击
     */
    public boolean performClick(float x, float y) {
        Log.d(TAG, "执行点击: (" + x + ", " + y + ")");
        
        Path path = new Path();
        path.moveTo(x, y);
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));
        
        GestureDescription gesture = builder.build();
        return dispatchGesture(gesture, null, null);
    }

    /**
     * 执行点击（带回调）
     */
    public boolean performClick(float x, float y, GestureResultCallback callback) {
        Log.d(TAG, "执行点击（带回调）: (" + x + ", " + y + ")");
        
        Path path = new Path();
        path.moveTo(x, y);
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));
        
        GestureDescription gesture = builder.build();
        return dispatchGesture(gesture, callback, null);
    }

    /**
     * 检查服务是否启用
     */
    public static boolean isServiceEnabled() {
        return instance != null;
    }
}

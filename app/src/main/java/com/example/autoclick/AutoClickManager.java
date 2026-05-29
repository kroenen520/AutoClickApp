package com.example.autoclick;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Random;

/**
 * 自动点击管理器
 * 负责基于准星位置执行随机偏移的点击操作
 */
public class AutoClickManager {
    private static final String TAG = "AutoClickManager";
    
    // 单例实例
    private static AutoClickManager instance;
    
    // 准星位置
    private float crosshairX = 0;
    private float crosshairY = 0;
    
    // 是否正在运行
    private boolean isRunning = false;
    
    // 随机数生成器
    private final Random random = new Random();
    
    // 主线程 Handler
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    // 偏移范围（像素）
    private static final int MAX_OFFSET = 30;
    private static final int MIN_OFFSET = 5;
    
    // 点击间隔范围（毫秒）
    private static final int MIN_INTERVAL = 1000;
    private static final int MAX_INTERVAL = 2000;
    
    // 点击持续时间
    private static final long CLICK_DURATION = 100;

    private AutoClickManager() {}

    public static synchronized AutoClickManager getInstance() {
        if (instance == null) {
            instance = new AutoClickManager();
        }
        return instance;
    }

    /**
     * 设置准星位置
     */
    public void setCrosshairPosition(float x, float y) {
        this.crosshairX = x;
        this.crosshairY = y;
        Log.d(TAG, "准星位置更新: (" + x + ", " + y + ")");
    }

    /**
     * 获取准星X坐标
     */
    public float getCrosshairX() {
        return crosshairX;
    }

    /**
     * 获取准星Y坐标
     */
    public float getCrosshairY() {
        return crosshairY;
    }

    /**
     * 开始自动点击
     */
    public void start() {
        if (isRunning) {
            Log.d(TAG, "已经在运行中");
            return;
        }
        
        isRunning = true;
        Log.d(TAG, "开始自动点击");
        scheduleNextClick();
    }

    /**
     * 停止自动点击
     */
    public void stop() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "停止自动点击");
    }

    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 调度下一次点击
     */
    private void scheduleNextClick() {
        if (!isRunning) return;
        
        // 随机间隔 1-2 秒
        int interval = MIN_INTERVAL + random.nextInt(MAX_INTERVAL - MIN_INTERVAL);
        
        handler.postDelayed(() -> {
            if (isRunning) {
                performClick();
                scheduleNextClick();
            }
        }, interval);
    }

    /**
     * 执行点击操作
     */
    private void performClick() {
        // 获取无障碍服务实例
        AutoClickAccessibilityService service = AutoClickAccessibilityService.getInstance();
        if (service == null) {
            Log.e(TAG, "无障碍服务未启用");
            return;
        }

        // 计算随机偏移
        int offsetX = (random.nextInt(2) == 0 ? 1 : -1) * (MIN_OFFSET + random.nextInt(MAX_OFFSET - MIN_OFFSET));
        int offsetY = (random.nextInt(2) == 0 ? 1 : -1) * (MIN_OFFSET + random.nextInt(MAX_OFFSET - MIN_OFFSET));

        float clickX = crosshairX + offsetX;
        float clickY = crosshairY + offsetY;

        Log.d(TAG, "执行点击: (" + clickX + ", " + clickY + ") 偏移: (" + offsetX + ", " + offsetY + ")");

        // 使用无障碍服务执行点击
        service.performClick(clickX, clickY, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "点击完成");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG, "点击取消");
            }
        });
    }
}

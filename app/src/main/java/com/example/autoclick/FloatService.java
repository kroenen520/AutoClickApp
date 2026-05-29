package com.example.autoclick;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.core.app.NotificationCompat;

/**
 * 悬浮窗服务
 * 管理准星和控制小球的显示与交互
 */
public class FloatService extends Service {
    private static final String TAG = "FloatService";
    
    // 通知渠道ID
    private static final String CHANNEL_ID = "auto_click_service";
    private static final int NOTIFICATION_ID = 1;
    
    // 窗口管理器
    private WindowManager windowManager;
    
    // 悬浮窗参数
    private WindowManager.LayoutParams crosshairParams;
    private WindowManager.LayoutParams ballParams;
    
    // 视图
    private CrosshairView crosshairView;
    private FloatingBallView floatingBallView;
    
    // 自动点击管理器
    private AutoClickManager autoClickManager;
    
    // 屏幕尺寸
    private int screenWidth;
    private int screenHeight;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "服务创建");
        
        // 初始化窗口管理器
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        screenWidth = windowManager.getDefaultDisplay().getWidth();
        screenHeight = windowManager.getDefaultDisplay().getHeight();
        
        // 获取自动点击管理器
        autoClickManager = AutoClickManager.getInstance();
        
        // 创建通知渠道
        createNotificationChannel();
        
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification());
        
        // 创建悬浮窗
        createFloatingWindows();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "服务启动");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "服务销毁");
        
        // 停止自动点击
        autoClickManager.stop();
        
        // 移除悬浮窗
        if (crosshairView != null) {
            windowManager.removeView(crosshairView);
        }
        if (floatingBallView != null) {
            windowManager.removeView(floatingBallView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 创建通知渠道
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "自动点击服务",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("自动点击服务运行中");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * 创建通知
     */
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("自动点击")
            .setContentText("服务运行中，点击悬浮球开始")
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
    }

    /**
     * 创建悬浮窗
     */
    private void createFloatingWindows() {
        // 创建准星视图
        createCrosshair();
        
        // 创建悬浮球
        createFloatingBall();
    }

    /**
     * 创建准星
     */
    private void createCrosshair() {
        crosshairView = new CrosshairView(this);
        
        // 设置布局参数
        int crosshairSize = 100;
        crosshairParams = new WindowManager.LayoutParams(
            crosshairSize,
            crosshairSize,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        );
        crosshairParams.gravity = Gravity.TOP | Gravity.START;
        
        // 初始位置：屏幕中央
        crosshairParams.x = (screenWidth - crosshairSize) / 2;
        crosshairParams.y = (screenHeight - crosshairSize) / 2;
        
        // 设置位置变化监听
        crosshairView.setOnPositionChangedListener((x, y) -> {
            autoClickManager.setCrosshairPosition(x, y);
        });
        
        // 添加到窗口
        windowManager.addView(crosshairView, crosshairParams);
        
        // 初始化准星位置
        autoClickManager.setCrosshairPosition(
            crosshairParams.x + crosshairSize / 2f,
            crosshairParams.y + crosshairSize / 2f
        );
    }

    /**
     * 创建悬浮球
     */
    private void createFloatingBall() {
        floatingBallView = new FloatingBallView(this);
        floatingBallView.setScreenSize(screenWidth, screenHeight);
        
        // 设置布局参数
        int ballSize = 80;
        ballParams = new WindowManager.LayoutParams(
            ballSize,
            ballSize,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        );
        ballParams.gravity = Gravity.TOP | Gravity.START;
        
        // 初始位置：右侧边缘
        ballParams.x = screenWidth - ballSize;
        ballParams.y = screenHeight / 2 - ballSize / 2;
        
        // 设置状态变化监听
        floatingBallView.setOnStateChangedListener(isRunning -> {
            if (isRunning) {
                // 开始自动点击
                autoClickManager.start();
                updateNotification("自动点击运行中...");
            } else {
                // 停止自动点击
                autoClickManager.stop();
                updateNotification("已暂停，点击悬浮球继续");
            }
        });
        
        // 添加到窗口
        windowManager.addView(floatingBallView, ballParams);
    }

    /**
     * 更新通知
     */
    private void updateNotification(String text) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("自动点击")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
        
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }
}

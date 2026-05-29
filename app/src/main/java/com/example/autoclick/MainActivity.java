package com.example.autoclick;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 主界面
 * 负责权限申请和服务启动
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    // 请求码
    private static final int REQUEST_OVERLAY_PERMISSION = 1001;
    
    // UI 组件
    private TextView tvStatus;
    private Button btnStart;
    private Button btnStop;
    private Button btnOpenAccessibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnOpenAccessibility = findViewById(R.id.btn_open_accessibility);
        
        btnStart.setOnClickListener(v -> startService());
        btnStop.setOnClickListener(v -> stopService());
        btnOpenAccessibility.setOnClickListener(v -> openAccessibilitySettings());
    }

    /**
     * 更新状态显示
     */
    private void updateStatus() {
        boolean hasOverlayPermission = hasOverlayPermission();
        boolean hasAccessibilityPermission = hasAccessibilityPermission();
        boolean isServiceRunning = isServiceRunning();
        
        StringBuilder status = new StringBuilder();
        status.append("悬浮窗权限: ").append(hasOverlayPermission ? "✓ 已授权" : "✗ 未授权").append("\n");
        status.append("无障碍权限: ").append(hasAccessibilityPermission ? "✓ 已授权" : "✗ 未授权").append("\n");
        status.append("服务状态: ").append(isServiceRunning ? "运行中" : "已停止");
        
        tvStatus.setText(status.toString());
        
        // 更新按钮状态
        boolean canStart = hasOverlayPermission && hasAccessibilityPermission;
        btnStart.setEnabled(canStart && !isServiceRunning);
        btnStop.setEnabled(isServiceRunning);
        
        if (!hasOverlayPermission) {
            btnStart.setText("请先授予悬浮窗权限");
        } else if (!hasAccessibilityPermission) {
            btnStart.setText("请先授予无障碍权限");
        } else {
            btnStart.setText(R.string.start_service);
        }
    }

    /**
     * 检查悬浮窗权限
     */
    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    /**
     * 检查无障碍权限
     */
    private boolean hasAccessibilityPermission() {
        return AutoClickAccessibilityService.isServiceEnabled();
    }

    /**
     * 检查服务是否运行
     */
    private boolean isServiceRunning() {
        // 简单判断：如果无障碍服务已启用，认为服务正在运行
        return AutoClickAccessibilityService.isServiceEnabled();
    }

    /**
     * 启动服务
     */
    private void startService() {
        if (!hasOverlayPermission()) {
            requestOverlayPermission();
            return;
        }
        
        if (!hasAccessibilityPermission()) {
            Toast.makeText(this, "请先在设置中开启无障碍服务", Toast.LENGTH_LONG).show();
            openAccessibilitySettings();
            return;
        }
        
        // 启动悬浮窗服务
        Intent intent = new Intent(this, FloatService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        
        Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    /**
     * 停止服务
     */
    private void stopService() {
        Intent intent = new Intent(this, FloatService.class);
        stopService(intent);
        
        // 同时停止自动点击
        AutoClickManager.getInstance().stop();
        
        Toast.makeText(this, "服务已停止", Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    /**
     * 请求悬浮窗权限
     */
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
        }
    }

    /**
     * 打开无障碍设置
     */
    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "悬浮窗权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "悬浮窗权限被拒绝", Toast.LENGTH_SHORT).show();
            }
            updateStatus();
        }
    }
}

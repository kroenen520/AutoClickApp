package com.example.autoclick;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;

/**
 * 侧边悬浮球视图
 * 可拖拽并自动吸附到屏幕侧边
 * 点击切换启动/暂停状态
 */
public class FloatingBallView extends View {
    private static final String TAG = "FloatingBallView";
    
    // 画笔
    private Paint paint;
    private Paint glowPaint;
    
    // 颜色
    private static final int COLOR_IDLE = Color.parseColor("#3498db");      // 待机状态：蓝色
    private static final int COLOR_RUNNING = Color.parseColor("#e74c3c");   // 运行状态：红色
    private static final int COLOR_GLOW = Color.parseColor("#FFFFFF");      // 发光效果
    
    // 球的半径
    private int radius = 35;
    
    // 当前状态
    private boolean isRunning = false;
    
    // 触摸相关
    private float touchStartX;
    private float touchStartY;
    private float viewStartX;
    private float viewStartY;
    private boolean isDragging = false;
    private long touchStartTime;
    
    // 屏幕尺寸
    private int screenWidth;
    private int screenHeight;
    
    // 吸附边缘阈值
    private static final int EDGE_THRESHOLD = 20;
    
    // 状态变化回调
    private OnStateChangedListener stateListener;
    
    // 动画 Handler
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    // 脉冲动画
    private float pulseScale = 1.0f;
    private ValueAnimator pulseAnimator;

    public FloatingBallView(Context context) {
        super(context);
        init();
    }

    public FloatingBallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatingBallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        
        // 启动脉冲动画
        startPulseAnimation();
    }

    /**
     * 设置屏幕尺寸
     */
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int currentRadius = (int) (radius * pulseScale);
        
        // 绘制发光效果
        if (isRunning) {
            RadialGradient glowGradient = new RadialGradient(
                centerX, centerY, currentRadius + 10,
                new int[]{Color.parseColor("#33FF0000"), Color.TRANSPARENT},
                null, Shader.TileMode.CLAMP
            );
            glowPaint.setShader(glowGradient);
            canvas.drawCircle(centerX, centerY, currentRadius + 10, glowPaint);
        }
        
        // 绘制主球体
        int mainColor = isRunning ? COLOR_RUNNING : COLOR_IDLE;
        RadialGradient gradient = new RadialGradient(
            centerX - currentRadius / 3, centerY - currentRadius / 3, currentRadius * 2,
            new int[]{lightenColor(mainColor, 30), mainColor, darkenColor(mainColor, 30)},
            new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP
        );
        paint.setShader(gradient);
        canvas.drawCircle(centerX, centerY, currentRadius, paint);
        
        // 绘制高光
        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setColor(Color.parseColor("#33FFFFFF"));
        canvas.drawCircle(centerX - currentRadius / 3, centerY - currentRadius / 3, currentRadius / 3, highlightPaint);
    }

    /**
     * 启动脉冲动画
     */
    private void startPulseAnimation() {
        pulseAnimator = ValueAnimator.ofFloat(1.0f, 1.1f, 1.0f);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.addUpdateListener(animation -> {
            pulseScale = (float) animation.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getRawX();
                touchStartY = event.getRawY();
                viewStartX = getX();
                viewStartY = getY();
                touchStartTime = System.currentTimeMillis();
                isDragging = false;
                return true;
                
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - touchStartX;
                float dy = event.getRawY() - touchStartY;
                
                // 判断是否为拖拽
                if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                    isDragging = true;
                }
                
                if (isDragging) {
                    setX(viewStartX + dx);
                    setY(viewStartY + dy);
                }
                return true;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                long touchDuration = System.currentTimeMillis() - touchStartTime;
                
                if (!isDragging && touchDuration < 200) {
                    // 短点击 - 切换状态
                    toggleState();
                } else {
                    // 拖拽结束 - 吸附到边缘
                    snapToEdge();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 切换运行状态
     */
    private void toggleState() {
        isRunning = !isRunning;
        Log.d(TAG, "状态切换: " + (isRunning ? "运行中" : "已暂停"));
        
        // 更新脉冲动画速度
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = ValueAnimator.ofFloat(1.0f, isRunning ? 1.15f : 1.05f, 1.0f);
            pulseAnimator.setDuration(isRunning ? 500 : 1000);
            pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
            pulseAnimator.addUpdateListener(animation -> {
                pulseScale = (float) animation.getAnimatedValue();
                invalidate();
            });
            pulseAnimator.start();
        }
        
        // 通知状态变化
        if (stateListener != null) {
            stateListener.onStateChanged(isRunning);
        }
        
        invalidate();
    }

    /**
     * 吸附到屏幕边缘
     */
    private void snapToEdge() {
        if (screenWidth <= 0) return;
        
        float currentX = getX();
        float currentY = getY();
        
        // 计算目标位置（左边缘或右边缘）
        float targetX;
        if (currentX + getWidth() / 2 < screenWidth / 2) {
            // 靠近左边
            targetX = -getWidth() / 2 + EDGE_THRESHOLD;
        } else {
            // 靠近右边
            targetX = screenWidth - getWidth() / 2 - EDGE_THRESHOLD;
        }
        
        // 限制Y范围
        float targetY = Math.max(0, Math.min(currentY, screenHeight - getHeight()));
        
        // 执行吸附动画
        animate()
            .x(targetX)
            .y(targetY)
            .setDuration(200)
            .start();
    }

    /**
     * 设置运行状态（外部调用）
     */
    public void setRunning(boolean running) {
        if (this.isRunning != running) {
            this.isRunning = running;
            invalidate();
            
            if (stateListener != null) {
                stateListener.onStateChanged(isRunning);
            }
        }
    }

    /**
     * 获取运行状态
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 设置状态变化监听器
     */
    public void setOnStateChangedListener(OnStateChangedListener listener) {
        this.stateListener = listener;
    }

    /**
     * 状态变化监听器接口
     */
    public interface OnStateChangedListener {
        void onStateChanged(boolean isRunning);
    }

    /**
     * 颜色变亮
     */
    private int lightenColor(int color, int percent) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        r = Math.min(255, r + (255 - r) * percent / 100);
        g = Math.min(255, g + (255 - g) * percent / 100);
        b = Math.min(255, b + (255 - b) * percent / 100);
        return Color.rgb(r, g, b);
    }

    /**
     * 颜色变暗
     */
    private int darkenColor(int color, int percent) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        r = r * (100 - percent) / 100;
        g = g * (100 - percent) / 100;
        b = b * (100 - percent) / 100;
        return Color.rgb(r, g, b);
    }
}

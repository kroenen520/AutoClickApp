package com.example.autoclick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 准星视图
 * 可拖拽的十字准星标记
 */
public class CrosshairView extends View {
    private static final String TAG = "CrosshairView";
    
    // 画笔
    private Paint paint;
    
    // 准星颜色
    private static final int COLOR_CROSSHAIR = Color.parseColor("#FF4444");
    private static final int COLOR_CENTER = Color.parseColor("#FFFFFF");
    
    // 准星尺寸
    private static final int LINE_LENGTH = 40;  // 十字线长度
    private static final int LINE_WIDTH = 3;    // 线宽
    private static final int CENTER_RADIUS = 8; // 中心圆半径
    
    // 触摸相关
    private float touchStartX;
    private float touchStartY;
    private float viewStartX;
    private float viewStartY;
    
    // 位置回调
    private OnPositionChangedListener positionListener;

    public CrosshairView(Context context) {
        super(context);
        init();
    }

    public CrosshairView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CrosshairView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setColor(COLOR_CROSSHAIR);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        // 绘制十字线
        paint.setColor(COLOR_CROSSHAIR);
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setStyle(Paint.Style.STROKE);
        
        // 水平线
        canvas.drawLine(centerX - LINE_LENGTH, centerY, centerX + LINE_LENGTH, centerY, paint);
        // 垂直线
        canvas.drawLine(centerX, centerY - LINE_LENGTH, centerX, centerY + LINE_LENGTH, paint);
        
        // 绘制中心圆
        paint.setColor(COLOR_CENTER);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, CENTER_RADIUS, paint);
        
        // 绘制中心圆边框
        paint.setColor(COLOR_CROSSHAIR);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(centerX, centerY, CENTER_RADIUS, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getRawX();
                touchStartY = event.getRawY();
                viewStartX = getX();
                viewStartY = getY();
                return true;
                
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - touchStartX;
                float dy = event.getRawY() - touchStartY;
                
                // 更新位置
                setX(viewStartX + dx);
                setY(viewStartY + dy);
                
                // 通知位置变化
                notifyPositionChanged();
                return true;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 最终位置通知
                notifyPositionChanged();
                return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 通知位置变化
     */
    private void notifyPositionChanged() {
        if (positionListener != null) {
            // 计算准星中心在屏幕上的位置
            float screenX = getX() + getWidth() / 2f;
            float screenY = getY() + getHeight() / 2f;
            positionListener.onPositionChanged(screenX, screenY);
        }
    }

    /**
     * 设置位置变化监听器
     */
    public void setOnPositionChangedListener(OnPositionChangedListener listener) {
        this.positionListener = listener;
    }

    /**
     * 位置变化监听器接口
     */
    public interface OnPositionChangedListener {
        void onPositionChanged(float x, float y);
    }

    /**
     * 获取准星中心X坐标
     */
    public float getCenterX() {
        return getX() + getWidth() / 2f;
    }

    /**
     * 获取准星中心Y坐标
     */
    public float getCenterY() {
        return getY() + getHeight() / 2f;
    }
}

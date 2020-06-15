package com.javason.mycanvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.MonthDisplayHelper;
import android.view.Display;
import android.view.View;

import androidx.annotation.Nullable;

public class CanvasDrawView extends View {
    private float mDensity;
    private Paint mPaint;
    private RectF re1arc;

    public CanvasDrawView(Context context) {
        super(context);
        init(context);
    }

    public CanvasDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context){
        DisplayMetrics displayMetrics=new DisplayMetrics();
        displayMetrics=context.getResources().getDisplayMetrics();
        mDensity=displayMetrics.density;
        mPaint=new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //把整张画布绘制成白色
        canvas.drawColor(Color.WHITE);

        //去锯齿
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(1*mDensity);

        //绘制圆形
        canvas.drawCircle(40*mDensity,40*mDensity,30*mDensity,mPaint);
        re1arc = new RectF(10*mDensity,430*mDensity,70*mDensity,530*mDensity);
        canvas.drawRect(re1arc,mPaint);
        canvas.drawArc(re1arc,30,225,true,mPaint);
        
    }
}

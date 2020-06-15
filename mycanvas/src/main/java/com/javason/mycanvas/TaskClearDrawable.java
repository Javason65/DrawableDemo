package com.javason.mycanvas;

import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import java.security.KeyStore;
import java.security.PrivateKey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TaskClearDrawable extends Drawable {
    private static final String TAG = "Zero";
    //animator state
    private final int STATE_ORIGIN = 0;//初始状态
    private final int STATE_ROTATE = STATE_ORIGIN + 1;//外圈旋转
    private final int STATE_UP = STATE_ROTATE + 1;//上移
    private final int STATE_DOWN = STATE_UP + 1;//下移
    private final int STATE_FINISH = STATE_DOWN + 1;//结束

    String getState(final int state) {
        String result = "STATE_ORIGIN";
        switch (state) {
            case STATE_ORIGIN:
                result = "STATE_ORIGIN";
                break;
            case STATE_ROTATE:
                result = "STATE_ROTATE";
                break;
            case STATE_UP:
                result = "STATE_UP";
                break;
            case STATE_DOWN:
                result = "STATE_DOWN";
                break;
            case STATE_FINISH:
                result = "STATE_FINISH";
                break;
        }
        return result;
    }
    //animator duration time
    private final long DURATION_ROTATE=1250L;//外圈旋转时长
    private final long DURATION_CLEANING=250;//x缩小至点的时长
    private  final long DURATION_POINT_UP=250;//点往上移动的时长
    private final long DURATION_POINT_DOWN=350;//点往下移动的时长
    private final long DURATION_FINISH=200;//短边缩放的时长
    private final long DURATION_CLEANING_DELAY=1000;//cleaning的时长
    private final long DURATIN_ORIGIN_DELAY=3000;//返回初始状态的时长
    private final float PI_DEGREE=(float)(180.0f/Math.PI);//弧度制
    private final float DRAWABLE_WIDTH=180.0f;//drawable_width的宽度
    private final float ROTATE_DEGREE_TOTAL=-1080.0f;//总共旋转的角度，即选装三圈6PI

    private final float PAINT_WIDTH=4.0f;//画x1的笔的宽度
    private final float PAINT_WIDTH_OTHER=1.0f;//画其他笔的宽度
    private final float CROSS_LENGTH=62.0f;//x的长度
    private final float CROSS_DEGREE=45.0f/PI_DEGREE;//pi/4三角函数计算用sin(pi/4)=cos(pi/4)=0.707105
    private final float UP_DISTANCE=24.0f;//往上移动的距离
    private final float DOWN_DISTANCE=20.0f;//往下移动的距离
    private final float FORK_LEFT_LEN=33.0f;//左短边的长度
    private final float FORK_LEFT_DEGREE=40.0f/PI_DEGREE;//左短边的弧度
    private final float FORK_RIGHT_LEN=50.0f;//右长边的长度
    private final float FORK_RIGHT_DEGREE=50.0f/PI_DEGREE;//右边长度的弧度
    private final float CIRCLE_RADIUS=3.0f;//圆点的半径

    private int mWidth, mHeight;

    private int mAnimState = STATE_ORIGIN;//状态
    private float mCleanningScale, mRotateDegreeScale;    //cleanning 缩放，旋转缩放
    private float mScale = 0.0f;
    private float mPaintWidth;//画笔宽度
    private float mPaintWidthOther;
    private float mViewScale;
    private float mCenterX, mCenterY;
    private float mCrossLen,oldCrossLen;
    private float mPointRadius;
    private float mForkLeftLen, mForkRightLen;
    private float mPointUpLen, mPointDownLen;

    private Paint mPaint;
    private Paint mLinePaint;
    private Bitmap mBgBitmap;
    private Bitmap mCircleBitmap;
    private TimeInterpolator fast_out_slow_in;
    private TimeInterpolator fast_out_linear_in;
    private AnimatorSet mAnimatorSet;
    private Matrix mRotateMatrix = new Matrix();


    public TaskClearDrawable(Context context, int width, int height) {
        super();
        init(context, width, height);
    }

    public void init(Context context, int width, int height) {
        mWidth = width;
        mHeight = height;
        mPaint = new Paint();
        mLinePaint = new Paint();

        Bitmap tempCircleBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.circle);

        Bitmap tempBgBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg);
        mCircleBitmap =
                Bitmap.createScaledBitmap(tempCircleBitmap, mWidth, mHeight, true);
        mBgBitmap =
                Bitmap.createScaledBitmap(tempBgBitmap, mWidth, mHeight, true);
        mViewScale = mWidth / DRAWABLE_WIDTH;
        Log.i(TAG, "init: mViewScale="+mViewScale);
        if (mCircleBitmap!=tempCircleBitmap){
            tempCircleBitmap.recycle();
        }
        if (mBgBitmap!=tempBgBitmap){
            tempBgBitmap.recycle();
        }

        mCenterX = mWidth / 2.0f;
        mCenterY = mHeight / 2.0f;
        mPaintWidth = PAINT_WIDTH * mViewScale;
        mPaintWidthOther = PAINT_WIDTH_OTHER * mViewScale;
        mCrossLen = CROSS_LENGTH * mViewScale;
        mPointRadius = CIRCLE_RADIUS * mViewScale;
        mForkLeftLen = FORK_LEFT_LEN * mViewScale;
        mForkRightLen = FORK_RIGHT_LEN * mViewScale;
        mPointUpLen = UP_DISTANCE * mViewScale;
        mPointDownLen = DOWN_DISTANCE * mViewScale;

        mCleanningScale = 1.0f;
        mRotateDegreeScale = 0.0f;

        fast_out_slow_in = AnimationUtils.loadInterpolator(
                context, android.R.interpolator.fast_out_slow_in);
        fast_out_linear_in = AnimationUtils.loadInterpolator(
                context, android.R.interpolator.fast_out_linear_in);

    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        float x1,y1,x2,y2,x3,y3,x4,y4;
        float length;//x的长度
        float sin_45=(float)Math.sin(CROSS_DEGREE);
        float cos_40=(float)Math.cos(FORK_LEFT_DEGREE);//x=r*cos_40
        float sin_40=(float)Math.sin(FORK_LEFT_DEGREE);//y=r*sin_40

        float cos_50=(float)Math.cos(FORK_RIGHT_DEGREE);//x=r*cos_50
        float sin_50=(float)Math.sin(FORK_RIGHT_DEGREE);//y=r*sin_50

        mPaint.setAntiAlias(true);
        mPaint.setColor(0xffffffff);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mPaintWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        //绘制背景
        canvas.drawBitmap(mBgBitmap,0,0,mPaint);
        //画辅助线
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(0xffffffff);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(mPaintWidth);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        //画虚线
        mLinePaint.setPathEffect(new DashPathEffect(new float[]{20,10},0));
        canvas.drawLine(0,mCenterY,mWidth,mCenterX,mLinePaint);
        canvas.drawLine(mCenterX,0,mCenterX,mHeight,mLinePaint);

        //根据五种不同的状态来绘制
        switch(mAnimState){
            case STATE_ORIGIN://绘制mCircleBitmap绘制x
                length=mCrossLen*sin_45/2.0f;
                x1=mCenterX-length;
                y1=mCenterY-length;

                x2=mCenterX+length;
                y2=mCenterY+length;

                x3=mCenterX+length;
                y3=mCenterY-length;

                x4=mCenterX-length;
                y4=mCenterY+length;
                drawPath(canvas,mPaint,x1,y1,x2,y2,x3,y3,x4,y4);
                canvas.drawBitmap(mCircleBitmap,0,0,null);//画圆圈
                break;
            case STATE_ROTATE://旋转，matrix mCircleBitmap绘制x drawPath两个点成线
                float degree=ROTATE_DEGREE_TOTAL*mRotateDegreeScale;
                 mRotateMatrix.setRotate(degree,mCenterX,mCenterY);
                 canvas.drawBitmap(mCircleBitmap,mRotateMatrix,null);
                length=mCleanningScale*mCrossLen*sin_45/2.0f;
                x1=mCenterX-length;
                y1=mCenterY-length;

                x2=mCenterX+length;
                y2=mCenterY+length;

                x3=mCenterX+length;
                y3=mCenterY-length;

                x4=mCenterX-length;
                y4=mCenterY+length;
                drawPath(canvas,mPaint,x1,y1,x2,y2,x3,y3,x4,y4);//画x
                break;
            case STATE_UP://根据m'CenterX,mCenterY -mPointUpLen*m'Scale绘制原点
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setStrokeWidth(mPaintWidthOther);
                float upLen=mPointUpLen*mScale;
                canvas.drawCircle(mCenterX,mCenterY-upLen,mPointRadius,mPaint);
                canvas.drawBitmap(mCircleBitmap,0,0,null);
                break;
            case STATE_DOWN://根据m'CenterX,mCenterY +mPointDownLen*m'Scale绘制原点
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setStrokeWidth(mPaintWidthOther);
               float downPosition=(mPointDownLen+mPointUpLen)*mScale;
                canvas.drawCircle(mCenterX,mCenterY-mPointUpLen+downPosition,mPointRadius,mPaint);
                canvas.drawBitmap(mCircleBitmap,0,0,null);
                break;
            case STATE_FINISH://画勾勾 mCircleBitmap
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mPaintWidth);

                x1 = mCenterX - Math.abs(mScale * mForkLeftLen * cos_40);
                y1 = mCenterY + mPointDownLen - Math.abs(mScale * mForkLeftLen * sin_40);
                x2 = mCenterX;
                y2 = mCenterY + mPointDownLen;
                x3 = mCenterX;
                y3 = mCenterY + mPointDownLen;
                //               x4 = cx - r * cos50
//                y4 =  cy  + mPointDownLen - r * sin50
                x4 = mCenterX + Math.abs(mScale * mForkRightLen * cos_50);
                y4 = mCenterY + mPointDownLen - Math.abs(mScale * mForkRightLen * sin_50);
                drawPath(canvas,mPaint,x1,y1,x2,y2,x3,y3,x4,y4);
                canvas.drawBitmap(mCircleBitmap,0,0, null);//画圆圈
                break;
        }

    }
    private void drawPath(Canvas canvas,Paint paint,float x1,float y1,
                          float x2,float y2,float x3,float y3,float x4,float y4){
        Path path=new Path();
        path.moveTo(x1,y1);
        path.lineTo(x2,y2);
        path.moveTo(x3,y3);
        path.lineTo(x4,y4);
        canvas.drawPath(path,paint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}

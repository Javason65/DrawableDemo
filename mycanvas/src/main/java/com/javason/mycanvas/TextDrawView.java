package com.javason.mycanvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

public class TextDrawView extends View {
    private final int mRadius = Utils.dp2px(80);
    private final int mCy = Utils.dp2px(500);
    private float mDensity;
    private Paint mPaint;

    float[] curWidth = new float[1];
    private int mScreenHeight;
    private float[] pos = new float[] { 80, 100, 80, 200, 80, 300, 80, 400,
            25 * mDensity, 30 * mDensity,
            25 * mDensity, 60 * mDensity,
            25 * mDensity, 90 * mDensity,
            25 * mDensity, 120 * mDensity,};
    private String text = "澳大利亚曾质疑过日本科研捕鲸的真实性。2010年，澳大利亚政府曾向海牙国际法院提起诉讼，控告日本在南冰洋的“科研”捕鲸活动实则是商业捕鲸。2014年，国际法院对此作出终审裁决，认定日本“出于科研目的”的捕鲸理由不成立，其捕鲸行为违背了《国际捕鲸管制公约》。日本表示尊重国际法院的裁决，并有所收敛了一段时间，但捕鲸活动仍未终止。2018年9月，在IWC的巴西峰会上，日本重提恢复商业捕鲸的诉求，但又一次遭到委员会的否决。这被视为日本最终退出该组织的直接原因被“科研”捕杀的鲸鱼，是如何被送上餐桌的？以科研名义被捕杀的鲸鱼，最后被输送到日本国内，满足人们的口腹之欲。负责执行这一系列动作的是一个名为日本鲸类研究所的机构，其上属机构是日本水产厅。日本鲸类研究所对鲸鱼肉有一个有趣的称呼：科研调查的副产物。他们称，根据《国际捕鲸规则公约》第8条的规定，调查后的鲸鱼体应被尽可能充分地利用。因而在鲸鱼被捕捞到渔船上并完成了对其体型、皮脂、胃内容物等款项的检测后，鲸体即会被拆解，用于鲸肉消费品的生产。当渔船抵达日本后，一块块的鲸肉会被分送给日本各级消费市场，或是以远低于市场价的价格出售给各地政府、供应于日本小学生的午餐中。";

    public TextDrawView(Context context) {
        super(context);
        init(context);
    }

    public TextDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        DisplayMetrics displayMetrics;
        displayMetrics=context.getResources().getDisplayMetrics();
        mDensity=displayMetrics.density;
        mPaint=new Paint();
        mScreenHeight=Utils.getScreenHeight(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode==MeasureSpec.UNSPECIFIED){
            setMeasuredDimension(widthSize,mScreenHeight*2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(5);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(80);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPosText("画图实例",pos,mPaint);

        Path lineTextPath=new Path();
        lineTextPath.moveTo(65*mDensity,5*mDensity);
        lineTextPath.lineTo(65*mDensity,250*mDensity);
        canvas.drawPath(lineTextPath,mPaint);
        canvas.drawTextOnPath("画图示例lineTextPath",lineTextPath,0,11,mPaint);

        canvas.save();
        canvas.translate(100 * mDensity, 5 * mDensity);
        canvas.rotate(90);
        canvas.drawText("画图示例string2", 0, 11, 0, 0, mPaint);
        canvas.restore();


        canvas.save();
//        mPaint.setShadowLayer(10, 15, 15, Color.GREEN);// 设置阴影
        canvas.drawText("画图示例string3", 0, 11, 140 * mDensity, 35 *mDensity, mPaint);// 对文字有效
        canvas.drawCircle(200 * mDensity, 150 * mDensity, 40 * mDensity, mPaint);// 阴影对图形无效
        canvas.restore();

        for (int i = 0; i < 6; i++) {
            mPaint.setTextScaleX(0.4f + 0.3f * i);
            canvas.drawText("画", 0, 1,
                    5* mDensity + 50 * mDensity * i, 250 * mDensity, mPaint);
        }

        //沿着任意路径
        Path bSplinePath = new Path();
        bSplinePath.moveTo(5 * mDensity, 320 * mDensity);
        bSplinePath.cubicTo(80 * mDensity, 260 * mDensity,
                200 * mDensity, 480 * mDensity,
                350 * mDensity,350 * mDensity);
        mPaint.setStyle(Paint.Style.STROKE);
        // 先画出这两个路径
        canvas.drawPath(bSplinePath, mPaint);
        // 依据路径写出文字
        String text = "风萧萧兮易水寒，壮士一去兮不复返";
        mPaint.setColor(Color.GRAY);
        mPaint.setTextScaleX(1.0f);
        mPaint.setTextSize(20 * mDensity);
        canvas.drawTextOnPath(text, bSplinePath, 0, 15, mPaint);

        //文字测量
        //绘制两个圆
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(Utils.dp2px(15));
        canvas.drawCircle(Utils.dp2px(90),mCy,mRadius,mPaint);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(Utils.dp2px(15));
        canvas.drawCircle(Utils.dp2px(270),mCy,mRadius,mPaint);

        //画圆弧
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        RectF rectArc=new RectF(Utils.dp2px(10),mCy-mRadius,Utils.dp2px(170),mCy+mRadius);
        canvas.drawArc(rectArc,-90,225,false,mPaint);

        Paint paintLine = new Paint();//这是一个反面教材，容易GC
        paintLine.setStyle(Paint.Style.STROKE);
        canvas.drawLine(0,mCy,getWidth(),mCy,paintLine);
        canvas.drawLine(Utils.dp2px(90),mCy - mRadius,Utils.dp2px(90),mCy + mRadius,paintLine);

        //开始绘制文章
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(Utils.dp2px(50));

        //1.用getTextBounds（）方法
        Rect rect=new Rect();
        mPaint.getTextBounds("fgab",0,4,rect);
        float offsetY=(float)(rect.top+rect.bottom)/2;
        canvas.drawText("fgab",Utils.dp2px(90),mCy-offsetY,mPaint);

        Rect rect1=new Rect();
        mPaint.getTextBounds("fgab",0,4,rect1);
        //2.
        Paint.FontMetrics fontMetrics=new Paint.FontMetrics();
        mPaint.getFontMetrics(fontMetrics);
        float offsetY2=(fontMetrics.ascent+fontMetrics.descent)/2;

        float offsetY1=(float)(rect1.top+rect1.bottom)/2;
        canvas.drawText("aaaa",Utils.dp2px(270),mCy-offsetY2,mPaint);

    }
}

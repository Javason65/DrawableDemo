package com.javason.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

import androidx.core.view.ViewConfigurationCompat;

public class FlowLayout extends ViewGroup {
    private static final String TAG = "javason";
    private List<View> lineViews;//每一行的子view
    private List<List<View>> views;//所有的行，一行一行的存储
    private List<Integer> heights;//每一行的高度
    private int mTouchSlop;//用来判断是不是一次滑动

    private float mLastInterceptX = 0;
    private float mLastInterceptY = 0;
    private float mLastY = 0;
    private boolean scrollable = false;
    private int measureHeight;//代表本身的测量高度
    private int realHeight;//表示内容高度

    private Scroller mScroller;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);//获取最小滑动距离
        mScroller =new Scroller(context);

    }



    private void init() {
        views = new ArrayList<>();
        lineViews = new ArrayList<>();
        heights = new ArrayList<>();
    }

    private String getModeString(int mode) {
        String result = "Unkown";
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
                result = "UNSPECIFIED";
                break;
            case MeasureSpec.EXACTLY:
                result = "EXACTLY";
                break;
            case MeasureSpec.AT_MOST:
                result = "AT_MOST";
                break;
            default:
        }
        return result;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!scrollable) {
            return super.onTouchEvent(event);
        }
        float currY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mScroller.isFinished()){
                    mScroller.abortAnimation();
                }
                mLastY = currY;
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = mLastY - currY;
                int oldScrollY = getScrollY();//已经偏移了的距离
//                int scrollY = (int) (oldScrollY + dy);
//                if (scrollY < 0) {
//                   }
//                if (scrollY > realHeight - measureHeight) {
//                    scrollY = realHeight - measureHeight;
//                }
//                scrollTo(0, scrollY);
                mScroller.startScroll(0,mScroller.getFinalY(),0,(int)dy);
                invalidate();
                mLastY = currY;
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        float xInterceptX = ev.getX();
        float yInterceptY = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastInterceptX = xInterceptX;
                mLastInterceptY = yInterceptY;
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = xInterceptX - mLastInterceptX;
                float dy = yInterceptY - mLastInterceptY;
                if (Math.abs(dx) < Math.abs(dy) && Math.abs(dy) > mTouchSlop) {
                    intercepted = true;//表示本身需要拦截处理
                } else {
                    intercepted = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
        }
        mLastInterceptX = xInterceptX;
        mLastInterceptY = yInterceptY;
        return intercepted;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()){
          scrollTo(0,mScroller.getCurrY());
         postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        measureHeight = heightSize;
        //当前行的宽度和高度
        int lineWidth = 0;//宽度是所有当前行子view的宽度之和
        int lineHeight = 0;//高度是所有子view高度最高的值
        //整个流式布局的
        int flowLayoutWidth = 0;//所有行宽度的最大值
        int flowLayoutHeight = 0;//所有行的高度的累加
        //初始化参数列表
        init();
        int childCount = this.getChildCount();
        //遍历所有的子view，对子view进行测量，分配到具体的行
        for (int i = 0; i < childCount; i++) {
            View child = this.getChildAt(i);
            //测量子view，获取到当前子view 的测量的宽度/高度
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            //看下当前行是否放得下 下一个子view，如果放得下，换行
            //如果放不下，换行，保存当前行所有子view高度，当前行的高度、宽度重新置零
            if (lineWidth + childWidth > widthSize) {//换行
                if (lineViews.size() == 1 && lineViews.get(0).getLayoutParams().height == LayoutParams.MATCH_PARENT) {
                    lineHeight = Utils.dp2px(150);
                }
                views.add(lineViews);
                lineViews = new ArrayList<>();//创建新的一行
                flowLayoutWidth = Math.max(flowLayoutWidth, lineWidth);
                flowLayoutHeight += lineHeight;
                heights.add(lineHeight);
                lineHeight = 0;
                lineWidth = 0;
            }
            lineViews.add(child);
            lineWidth += childWidth;
            if (lp.height != LayoutParams.MATCH_PARENT) {
                lineHeight = Math.max(lineHeight, childHeight);
            }


            if (i == childCount - 1) {
                flowLayoutHeight += lineHeight;
                flowLayoutWidth = Math.max(flowLayoutWidth, lineWidth);
                heights.add(lineHeight);
                views.add(lineViews);
            }

        }
        //重新测量一次layout_height=match_parent
        remeasureChild(widthMeasureSpec, heightMeasureSpec);
        realHeight = flowLayoutHeight;
        scrollable = flowLayoutHeight > measureHeight;

        //FlowLayout最终宽高
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : flowLayoutWidth,
                realHeight);


    }

    public void remeasureChild(int widthMeasureSpec, int heightMeasureSpec) {
        int lineSize = views.size();
        for (int i = 0; i < lineSize; i++) {
            int lineHeight = heights.get(i);//每行的行高
            List<View> lineViews = views.get(i);
            int size = lineViews.size();
            for (int j = 0; j < size; j++) {
                View child = lineViews.get(j);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    int childWidthSpec = getChildMeasureSpec(widthMeasureSpec, 0, lp.width);
                    int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, 0, lineHeight);
                    child.measure(childWidthSpec, childHeightSpec);
                }
            }
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p) && p instanceof LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {
        private int gravity = -1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.FlowLayout_Layout);
            try {
                gravity = a.getInt(R.styleable.FlowLayout_Layout_android_layout_gravity, -1);
            } finally {
                a.recycle();
            }
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int lineCount = views.size();
        int currX = 0;
        int currY = 0;
        for (int i = 0; i < lineCount; i++) {//大循环考虑所有的子view
            List<View> lineViews = views.get(i);
            int lineHeight = heights.get(i);
            //遍历当前行的子view
            int size = lineViews.size();
            for (int j = 0; j < size; j++) {//当前行的每一个view
                View child = lineViews.get(j);
                int left = currX;
                int top = currY;
                int right = left + child.getMeasuredWidth();
                int bottom = top + child.getMeasuredHeight();
                child.layout(left, top, right, bottom);
                currX += child.getMeasuredWidth();
            }
            currY += lineHeight;
            currX = 0;
        }
    }
}

package com.javason.flowlayout;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.WindowManager;

public class Utils {
    public static int dp2px(int dp){
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp, Resources.getSystem().getDisplayMetrics());
    }
}

package com.malmstein.androidtvexplorer.presenters;

import android.content.Context;

public class Utils {

    public static int dpToPx(int dp, Context ctx) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

}

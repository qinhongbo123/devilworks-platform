package com.surfing.util;

import com.surfing.R;

import android.content.Context;
import android.view.View;

public class ThemeUpdateUitl
{
    public static void updateTitlebarBg(View view,Context currentContext,int resid){
        if(currentContext != null){
            view.setBackgroundDrawable(currentContext.getResources().getDrawable(resid));
        }
    }
}

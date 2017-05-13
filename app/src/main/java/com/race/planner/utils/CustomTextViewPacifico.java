package com.race.planner.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by dev on 5/10/2017.
 */

public class CustomTextViewPacifico extends TextView
{

    public CustomTextViewPacifico(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/pacifico.ttf"));
    }
}

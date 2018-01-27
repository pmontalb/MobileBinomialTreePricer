package com.a7raiden.qdev.abp.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by 7Raiden on 27/01/2018.
 */

public class FontManager {

    public static final String FONTAWESOME = "fontawesome-webfont.ttf";

    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

    public static void setFontToContainer(View v, Typeface typeface) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++)
                setFontToContainer(vg.getChildAt(i), typeface);
        }
        else if (v instanceof TextView)
            ((TextView)v).setTypeface(typeface);
    }
}

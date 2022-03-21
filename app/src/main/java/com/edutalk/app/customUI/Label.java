package com.edutalk.app.customUI;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Label extends LinearLayout {
    private TextView titleText;
    private String title;
    private float bigFontSize, smallFontSize;
    private int color;

    public Label(Context context, String title, float smallFontSize, float bigFontSize, int color) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);

        this.title = title;
        this.bigFontSize = bigFontSize;
        this.smallFontSize = smallFontSize;
        this.color = color;

        buildComponents();
        combineComponents();
    }

    private void combineComponents() {
        LayoutParams normalParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        normalParams.setMargins(5, 2, 5, 2);
        LayoutParams topMarginParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        topMarginParams.setMargins(5, 5, 5, 2);

        titleText.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        this.addView(titleText, normalParams);
    }

    private void buildComponents() {
        titleText = new TextView(getContext());
        titleText.setText(title);
        titleText.setTextSize(bigFontSize);
        titleText.setTextColor(color);
        titleText.setTypeface(titleText.getTypeface(), Typeface.BOLD_ITALIC);
    }
}

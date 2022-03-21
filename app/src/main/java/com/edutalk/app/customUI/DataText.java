package com.edutalk.app.customUI;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DataText extends LinearLayout {
    private TextView valueText;
    private float bigFontSize, smallFontSize;
    private int color;

    public DataText(Context context, float smallFontSize, float bigFontSize, int color) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);

        this.bigFontSize = bigFontSize;
        this.smallFontSize = smallFontSize;
        this.color = color;

        buildComponents();
        combineComponents();
    }

    private void buildComponents(){
        valueText=new TextView(getContext());
        valueText.setText("");
        valueText.setTextColor(color);
        valueText.setTextSize(smallFontSize);
    }

    private void combineComponents(){
        LayoutParams matchParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        matchParams.setMargins(5, 5, 5, 5);
        LayoutParams splitParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        splitParams.weight = 1.0f;
        splitParams.setMargins(10, 5, 10, 5);

        LinearLayout horizontalLayout = new LinearLayout(getContext());
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        valueText.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        horizontalLayout.addView(valueText, splitParams);

        this.addView(horizontalLayout, matchParams);
    }

    public void setValues(String value){
        valueText.setText(value);
    }
}

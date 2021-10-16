package com.edutalk.app.customUI;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MutliDimesionDataText extends LinearLayout {
    private TextView titleText;
    private TextView[] dimensionText;
    private TextView[] valueText;
    private String title;
    private int dimension;
    private String[] dimensionNames;
    private float bigFontSize, smallFontSize;
    private int color;

    public MutliDimesionDataText(Context context, String title, String[] dimensionNames, float smallFontSize, float bigFontSize, int color) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);

        this.title = title;
        this.dimension = dimensionNames.length;
        this.dimensionNames = dimensionNames;
        this.bigFontSize = bigFontSize;
        this.smallFontSize = smallFontSize;
        this.color = color;

        buildComponents();
        combineComponents();
    }

    private void buildComponents(){
        titleText = new TextView(getContext());
        titleText.setText(title);
        titleText.setTextSize(bigFontSize);
        titleText.setTextColor(color);
        titleText.setTypeface(titleText.getTypeface(), Typeface.BOLD_ITALIC);

        dimensionText = new TextView[dimension];
        for (int i=0;i<dimension;i++){
            dimensionText[i]=new TextView(getContext());
            dimensionText[i].setText(dimensionNames[i]+" :  ");
            dimensionText[i].setTextSize((bigFontSize+smallFontSize)/2);
            dimensionText[i].setTextColor(color);
            dimensionText[i].setTypeface(Typeface.DEFAULT_BOLD);
        }

        valueText = new TextView[dimension];
        for (int i=0;i<dimension;i++){
            valueText[i]=new TextView(getContext());
            valueText[i].setText("");
            valueText[i].setTextColor(color);
            valueText[i].setTextSize(smallFontSize);
        }
    }

    private void combineComponents(){
        LayoutParams matchParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        matchParams.setMargins(5, 5, 5, 5);
        LayoutParams splitParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        splitParams.weight = 1.0f;
        splitParams.setMargins(10, 5, 10, 5);

        titleText.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        this.addView(titleText, matchParams);

        for (int i=0;i<dimension;i++){
            LinearLayout horizontalLayout = new LinearLayout(getContext());
            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

            dimensionText[i].setTextAlignment(TEXT_ALIGNMENT_VIEW_END);
            horizontalLayout.addView(dimensionText[i], splitParams);

            valueText[i].setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
            horizontalLayout.addView(valueText[i], splitParams);

            this.addView(horizontalLayout, matchParams);
        }
    }

    public void setValues(String[] values){
        for (int i=0;i<dimension;i++){
            valueText[i].setText(values[i]);
        }
    }
}

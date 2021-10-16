package com.edutalk.app.customUI;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.edutalk.app.R;

import androidx.core.content.res.ResourcesCompat;

public class SeekBarWithLabel extends LinearLayout {
    private TextView titleText;
    private TextView valueText;
    private SeekBar seekBar;
    private int lengthOfSeekBar=100, defaultProgress=50;
    private String title;
    private float bigFontSize, smallFontSize;
    private int color;

    public SeekBarWithLabel(Context context, String title, float smallFontSize, float bigFontSize, int color) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);

        this.title = title;
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

        valueText = new TextView(getContext());
        valueText.setTextSize(smallFontSize);
        valueText.setTextColor(color);
        valueText.setTypeface(valueText.getTypeface(), Typeface.BOLD_ITALIC);

        seekBar = new SeekBar(getContext());
        seekBar.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.seekbar_drawable_progress, null));
        seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.seekbar_drawable_thumb, null));
        seekBar.setPadding(0, 15, 0, 10);
        seekBar.setMin(0);
        seekBar.setMax(lengthOfSeekBar);
        seekBar.setProgress(defaultProgress);
    }

    private void combineComponents(){
        LayoutParams normalParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        normalParams.setMargins(5, 2, 5, 2);
        LayoutParams topMarginParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        topMarginParams.setMargins(5, 5, 5, 2);

        titleText.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        this.addView(titleText, normalParams);

        valueText.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        this.addView(valueText, topMarginParams);

        this.addView(seekBar, normalParams);
    }

    public void setSeekBarRange(int lengthOfSeekBar, int defaultProgress){
        this.lengthOfSeekBar = lengthOfSeekBar;
        this.defaultProgress = defaultProgress;

        seekBar.setMin(0);
        seekBar.setMax(lengthOfSeekBar);
        seekBar.setProgress(defaultProgress);
    }

    public void setValue(String value){
        valueText.setText(value);
    }

    public void setSeekBarListener(SeekBar.OnSeekBarChangeListener onSeekBarChangeListener){
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }
}

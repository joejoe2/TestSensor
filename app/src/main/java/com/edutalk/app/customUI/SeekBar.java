package com.edutalk.app.customUI;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edutalk.app.R;

import androidx.core.content.res.ResourcesCompat;

public class SeekBar extends LinearLayout{
    private TextView valueText;
    private android.widget.SeekBar seekBar;
    private int lengthOfSeekBar=100, defaultProgress=50;
    private float bigFontSize, smallFontSize;
    private int color;

    public SeekBar(Context context){
        super(context);
    }

    public SeekBar(Context context, float smallFontSize, float bigFontSize, int color) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);

        this.bigFontSize = bigFontSize;
        this.smallFontSize = smallFontSize;
        this.color = color;

        buildComponents();
        combineComponents();
    }

    private void buildComponents(){
        valueText = new TextView(getContext());
        valueText.setTextSize(smallFontSize);
        valueText.setTextColor(color);
        valueText.setTypeface(valueText.getTypeface(), Typeface.BOLD_ITALIC);

        seekBar = new android.widget.SeekBar(getContext());
        seekBar.setProgressDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.seekbar_drawable_progress, null));
        seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.seekbar_drawable_thumb, null));
        seekBar.setPadding(0, 15, 0, 10);
        seekBar.setMin(0);
        seekBar.setMax(lengthOfSeekBar);
        seekBar.setProgress(defaultProgress);
    }

    private void combineComponents(){
        LinearLayout.LayoutParams normalParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        normalParams.setMargins(5, 2, 5, 2);

        valueText.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        this.addView(seekBar, normalParams);
        this.addView(valueText, normalParams);
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

    public void setSeekBarListener(android.widget.SeekBar.OnSeekBarChangeListener onSeekBarChangeListener){
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }
}

package com.joejoe2.testsensor.customUI;

import android.content.Context;
import android.graphics.Color;

public class CustomUIFactory {
    public static MutliDimesionDataText buildMutlDimesionDataText(Context context, String title, String[] dimensionNames){
        MutliDimesionDataText dataText= new MutliDimesionDataText(context, title, dimensionNames, 18, 22, Color.WHITE);
        return dataText;
    }

    public static SeekBarWithLabel buildSeekBarWithLabel(Context context, String title){
        SeekBarWithLabel seekBar = new SeekBarWithLabel(context, title, 18, 22, Color.WHITE);
        return seekBar;
    }
}

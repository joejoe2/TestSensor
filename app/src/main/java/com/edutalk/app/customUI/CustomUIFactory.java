package com.edutalk.app.customUI;

import android.content.Context;
import android.graphics.Color;

public class CustomUIFactory {
    public static MutliDimesionDataText buildMutlDimesionDataText(Context context, String title, String[] dimensionNames){
        MutliDimesionDataText dataText= new MutliDimesionDataText(context, title, dimensionNames, 18, 22, Color.WHITE);
        return dataText;
    }

    public static DataText buildDataText(Context context){
        DataText dataText= new DataText(context, 16, 20, Color.WHITE);
        return dataText;
    }

    public static SeekBarWithLabel buildSeekBarWithLabel(Context context, String title){
        SeekBarWithLabel seekBar = new SeekBarWithLabel(context, title, 18, 22, Color.WHITE);
        return seekBar;
    }

    public static SeekBar buildSeekBar(Context context){
        SeekBar seekBar = new SeekBar(context, 18, 22, Color.WHITE);
        return seekBar;
    }

    public static Label buildLabel(Context context, String title){
        Label label = new Label(context, title, 18, 22, Color.WHITE);
        return label;
    }
}

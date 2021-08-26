package com.joejoe2.testsensor.sensor.triggersensor;

import android.widget.SeekBar;

import com.joejoe2.testsensor.customUI.SeekBarWithLabel;

/**
 * this enum.toString() is same as featureName()
 * , and getValue() is same as android view Class name
 * , and featureName() is same as edutalk feature name substring
 */
public enum RangeSensorType {
    RangeSlider(SeekBarWithLabel.class.getCanonicalName()),
    ;

    private final String id;
    RangeSensorType(String viewClass) {
        this.id=viewClass;
    }
    public String getValue() { return id; }

    @Override
    public String toString() {
        return this.semanticAlias();
    }

    public String semanticAlias(){
        return this.name();
    }
}

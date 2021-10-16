package com.edutalk.app.sensor;

public interface DFInfo {
    String getDFAlias();

    boolean isNeedTimeStamp();

    void setNeedTimestamp(boolean needTimeStamp);
}

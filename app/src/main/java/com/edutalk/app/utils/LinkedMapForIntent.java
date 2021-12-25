package com.edutalk.app.utils;

import java.io.Serializable;
import java.util.LinkedHashMap;

/*
* this class is used to pass linkedHashMap between android intent
* */
public class LinkedMapForIntent implements Serializable {
    private LinkedHashMap linkedHashMap;

    public LinkedMapForIntent() { }

    public LinkedMapForIntent(LinkedHashMap linkedHashMap) {
        this.linkedHashMap = linkedHashMap;
    }

    public void setMap(LinkedHashMap map){
        this.linkedHashMap = map;
    }

    public LinkedHashMap getMap(){
        return linkedHashMap;
    }
}

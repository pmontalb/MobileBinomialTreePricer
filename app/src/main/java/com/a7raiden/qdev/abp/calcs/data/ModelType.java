package com.a7raiden.qdev.abp.calcs.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 7Raiden on 19/01/2018.
 */

public enum ModelType {
    Null,
    BlackScholes,
    CoxRubinsteinRoss,
    JarrowRuddNeutral,
    Tian,
    LeisenReimer,
    Joshi;

    private static ModelType[] allValues = values();
    public static ModelType parseInt(int n) {return allValues[n];}
}

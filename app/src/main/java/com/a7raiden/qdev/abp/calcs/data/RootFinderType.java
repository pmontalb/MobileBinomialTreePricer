package com.a7raiden.qdev.abp.calcs.data;

/**
 * Created by 7Raiden on 23/01/2018.
 */

public enum RootFinderType {
    Null,
    Bisection,
    Brent,
    Toms348;

    private static RootFinderType[] allValues = values();
    public static RootFinderType parseInt(int n) {return allValues[n];}
}

package com.a7raiden.qdev.abp.calcs.data;

/**
 * Created by 7Raiden on 23/01/2018.
 */

public class RootFinderData {
    public double mRoot;
    public int mIterations;
    public boolean mHasConverged;

    public RootFinderData(double root, int iterations, boolean hasConverged) {
        mRoot = root;
        mIterations = iterations;
        mHasConverged = hasConverged;
    }
}

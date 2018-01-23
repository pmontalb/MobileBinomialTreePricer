package com.a7raiden.qdev.abp.calcs.interfaces;

import com.a7raiden.qdev.abp.calcs.data.RootFinderData;

import java.util.function.Function;

/**
 * Created by 7Raiden on 23/01/2018.
 */

public interface IRootFinder {
    RootFinderData solve(double lowerPoint, double upperPoint, int maxIterations, double absTolerance);
}

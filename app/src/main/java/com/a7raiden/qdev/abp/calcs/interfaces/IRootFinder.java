package com.a7raiden.qdev.abp.calcs.interfaces;

import com.a7raiden.qdev.abp.calcs.data.RootFinderInputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderOutputData;

/**
 * Created by 7Raiden on 23/01/2018.
 */

public interface IRootFinder {
    RootFinderOutputData solve(RootFinderInputData rootFinderInputData);
}

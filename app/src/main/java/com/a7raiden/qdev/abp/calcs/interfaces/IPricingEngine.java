package com.a7raiden.qdev.abp.calcs.interfaces;

import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.OutputData;

/**
 * Created by 7Raiden on 19/01/2018.
 */

public interface IPricingEngine {
    OutputData[] compute();
}

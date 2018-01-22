package com.a7raiden.qdev.abp.calcs.models;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.a7raiden.qdev.abp.calcs.data.InputData;

/**
 * Created by 7Raiden on 22/01/2018.
 */

/** LR
 */
public class LeisenReimerBinomialTree extends BinomialDistributionInversionBinomialTree {
    public LeisenReimerBinomialTree(InputData inputData) {
        super(inputData);
    }

    @Override
    protected double binomialDistributionInversion(double z, int n){
        double result = z / (n + 1.0 / 3.0 + 0.1 / (n + 1.0));
        result *= result;
        result = Math.exp(-result * (n + 1.0 / 6.0));
        result = 0.5 + (z > 0 ? 1 : -1) * Math.sqrt(0.25 * (1.0 - result));
        return result;
    }

    @Override
    protected int getNumberOfSteps() {
        return mInputData.mNodes;
    }
}

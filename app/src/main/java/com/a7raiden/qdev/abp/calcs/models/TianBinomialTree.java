package com.a7raiden.qdev.abp.calcs.models;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.a7raiden.qdev.abp.calcs.data.InputData;

/**
 * Created by 7Raiden on 22/01/2018.
 */

/** Tian
 * u = 1/2 * r * v * (v + 1 + (v^2 + 2v + 3)^(1/2))
 * d = 1/2 * r * v * (v + 1 - (v^2 + 2v + 3)^(1/2))
 *
 * r = exp(b * dt)
 * v = exp(sigma^2 * dt)
 *
 * p = (e^(b * dt) - d) / (u - d)
 * q = 1 - p
 */
public class TianBinomialTree extends BinomialTreePricingEngine {
    public TianBinomialTree(InputData inputData) {
        super(inputData);
    }

    @Override
    protected void buildImpl(InputData inputData) {
        double r = mCachedData.mGrowthFactor;
        double v = Math.exp(inputData.mVolatility * inputData.mVolatility * mDt);
        mU = .5 * r * v* (1.0 + v + Math.sqrt(v * v + 2.0 * v - 3.0));
        mD = .5 * r * v* (1.0 + v - Math.sqrt(v * v + 2.0 * v - 3.0));

        mP = (mCachedData.mGrowthFactor - mD) / (mU - mD);
        mQ = 1.0 - mP;

        // discount probabilities
        mP *= mCachedData.mDiscountFactor;
        mQ *= mCachedData.mDiscountFactor;

        for (int i = 0; i <= mInputData.mNodes; ++i) {
            mGrid[i] = inputData.mSpot * Math.pow(mU, i) * Math.pow(mD, mInputData.mNodes - i);
        }
    }
}

package com.a7raiden.qdev.abp.calcs.models;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.a7raiden.qdev.abp.calcs.data.InputData;

/**
 * Created by 7Raiden on 22/01/2018.
 */

/** CRR
 * u = e^(sigma * sqrt(dt))
 * d = e^(-sigma * sqrt(dt))
 *
 * p = (e^(b * dt) - d) / (u - d)
 * q = 1 - p
 */
public class CoxRubinsteinRossBinomialTree extends BinomialTreePricingEngine {
    public CoxRubinsteinRossBinomialTree(InputData inputData) {
        super(inputData);
    }

    @Override
    protected void buildImpl(InputData inputData) {
        mU = Math.exp(mCachedData.mSigmaSqrtDt);
        mD = 1.0 / mU;

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

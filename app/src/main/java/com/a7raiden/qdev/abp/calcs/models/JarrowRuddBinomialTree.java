package com.a7raiden.qdev.abp.calcs.models;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.a7raiden.qdev.abp.calcs.data.InputData;

/**
 * Created by 7Raiden on 22/01/2018.
 */

/** Neutral JR tree
 * u = e^(mu' * dt + sigma * sqrt(dt))
 * d = e^(mu' * dt - sigma * sqrt(dt))
 *
 * p = (e^(b * dt) - d) / (u - d)
 * q = 1 - p
 */
public class JarrowRuddBinomialTree extends BinomialTreePricingEngine {
    public JarrowRuddBinomialTree(InputData inputData) {
        super(inputData);
    }

    @Override
    protected void buildImpl(InputData inputData) {
        double muPrime = inputData.mCarryRate - .5 * inputData.mVolatility * inputData.mVolatility;
        mU = Math.exp(muPrime * mDt + mCachedData.mSigmaSqrtDt);
        mD = Math.exp(muPrime * mDt - mCachedData.mSigmaSqrtDt);

        double X = 2.0 * mCachedData.mGrowthFactor / (mU + mD);
        mU *= X;
        mD *= X;

        mP = .5;
        mQ = .5;

        // discount probabilities
        mP *= mCachedData.mDiscountFactor;
        mQ *= mCachedData.mDiscountFactor;

        for (int i = 0; i <= mInputData.mNodes; ++i) {
            mGrid[i] = inputData.mSpot * Math.pow(mU, i) * Math.pow(mD, mInputData.mNodes - i);
        }
    }
}

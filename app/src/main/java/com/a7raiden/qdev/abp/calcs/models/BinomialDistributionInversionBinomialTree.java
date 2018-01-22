package com.a7raiden.qdev.abp.calcs.models;

import com.a7raiden.qdev.abp.calcs.data.InputData;

/**
 * Created by 7Raiden on 22/01/2018.
 */

public abstract class BinomialDistributionInversionBinomialTree extends BinomialTreePricingEngine {
    public BinomialDistributionInversionBinomialTree(InputData inputData) {
        super(inputData);
    }

    @Override
    protected void buildImpl(InputData inputData) {
        double sigmaSqrtT = inputData.mVolatility * Math.sqrt(inputData.mExpiry);
        double d2 = Math.log(inputData.mSpot / inputData.mStrike) + (inputData.mCarryRate - .5 * inputData.mVolatility * inputData.mVolatility) *  inputData.mExpiry;
        d2 /= sigmaSqrtT;

        mP = binomialDistributionInversion(d2, getNumberOfSteps());
        mQ = 1.0 - mP;

        double d1 = d2 + sigmaSqrtT;
        double pDash = binomialDistributionInversion(d1, getNumberOfSteps());
        mU = pDash / mP * mCachedData.mGrowthFactor;
        mD = (mCachedData.mGrowthFactor - mP * mU) / mQ;

        // discount probabilities
        mP *= mCachedData.mDiscountFactor;
        mQ *= mCachedData.mDiscountFactor;

        for (int i = 0; i <= mInputData.mNodes; ++i) {
            mGrid[i] = inputData.mSpot * Math.pow(mU, i) * Math.pow(mD, mInputData.mNodes - i);
        }
    }

    protected abstract double binomialDistributionInversion(double z, int n);
    protected abstract int getNumberOfSteps();
}

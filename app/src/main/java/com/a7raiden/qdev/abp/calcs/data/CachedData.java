package com.a7raiden.qdev.abp.calcs.data;

/**
 * Created by 7Raiden on 21/01/2018.
 */

public class CachedData {
    /**
     * Years to expiry
     */
    public double mDt;

    /**
     * Sqrt of time to expiry
     */
    public double mSqrtDt;

    /**
     * Volatility times sqrt of times to expiry
     */
    public double mSigmaSqrtDt;

    /**
     * e^(b * dt)
     */
    public double mGrowthFactor;

    /**
     * e^(-r * dt)
     */
    public double mDiscountFactor;

    /**
     * e^(-r * dt)
     */
    public double mGrowthTimesDiscount;

    public CachedData() {

    }

    public CachedData(double sqrtDt,
               double sigmaSqrtDt,
               double growthFactor,
               double discountFactor,
               double growthFactorTimesDiscountFactor) {
        mSqrtDt = sqrtDt;
        mSigmaSqrtDt = sigmaSqrtDt;
        mGrowthFactor = growthFactor;
        mDiscountFactor = discountFactor;
        mGrowthTimesDiscount = growthFactorTimesDiscountFactor;
    }

    public CachedData(CachedData rhs) {
        mSqrtDt = rhs.mSqrtDt;
        mSigmaSqrtDt = rhs.mSigmaSqrtDt;
        mGrowthFactor = rhs.mGrowthFactor;
        mDiscountFactor = rhs.mDiscountFactor;
        mGrowthTimesDiscount = rhs.mGrowthTimesDiscount;
    }
}

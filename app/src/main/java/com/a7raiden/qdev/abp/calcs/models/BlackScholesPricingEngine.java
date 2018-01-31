package com.a7raiden.qdev.abp.calcs.models;

import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.OutputData;
import com.a7raiden.qdev.abp.calcs.data.CachedData;

import static java.lang.Math.sqrt;

/**
 * Created by 7Raiden on 19/01/2018.
 */

public final class BlackScholesPricingEngine extends PricingEngine {
    /**
     * normCdf(d1)
     */
    private double mNd1;

    /**
     * normCdf(d2)
     */
    private double mNd2;

    /**
     * normCdf(-d1) = 1.0 - N(d1)
     */
    private double mNminusD1;

    /**
     * normCdf(-d2) = 1.0 - N(d2)
     */
    private double mNminusD2;

    /**
     * normPdf(d1)
     */
    private double mPd1;

    /**
     * d1Addend = [-log(K) + (b + .5 * sigma^2) * T] / (sigma * sqrt(T)
     */
    private double mD1Addend;

    /**
     * Cached value for the underlying
     */
    private double mCurrentS;

    private CachedData mCachedData;

    private double mOneOverSigmaSqrtTtimesGrowthFactorTimesDiscountFactor;
    private double mSqrtTtimesGrowthFactorTimesDiscountFactor;

    private double mOneOverSigmaSqrtT;

    /**
     * Precomputes quantities that do not depend on the underlying value
     */
    private void make() {
        double halfSigma2 = .5 * mInputData.mVolatility * mInputData.mVolatility;
        mD1Addend = ((mInputData.mCarryRate + halfSigma2) * mCachedData.mDt - Math.log(mInputData.mStrike)) * mOneOverSigmaSqrtT;
    }

    /**
     * update all cached quantities that depend on underlying value
     */
    public void update(double S) {
        mCurrentS = S;

        /*
          d1 = [log(S / K) + (b + .5 * sigma^2) * T] / (sigma * sqrt(T))
         */
        double mD1 = mOneOverSigmaSqrtT * Math.log(mCurrentS) + mD1Addend;
        /*
          d2 = d1 - sigma * sqrt(T)
         */
        double mD2 = mD1 - mCachedData.mSigmaSqrtDt;

        mNd1 = Stats.normCdf(mD1);
        mNminusD1 = 1.0 - mNd1;

        mNd2 = Stats.normCdf(mD2);
        mNminusD2 = 1.0 - mNd2;

        mPd1 = Stats.normPdf(mD1);
    }

    public BlackScholesPricingEngine(InputData inputData) {
        super(inputData);
        mCachedData = new CachedData();

        mCurrentS = mInputData.mSpot;
        mCachedData.mDt = mInputData.mExpiry;

	    double sqrtT = sqrt(mCachedData.mDt);

        mCachedData.mSigmaSqrtDt = mInputData.mVolatility * sqrtT;
        mOneOverSigmaSqrtT = 1.0 / mCachedData.mSigmaSqrtDt;

        mCachedData.mDiscountFactor = Math.exp(-mInputData.mRiskFreeRate * mCachedData.mDt);
        mCachedData.mGrowthFactor   = Math.exp( mInputData.mCarryRate * mCachedData.mDt);

        mCachedData.mGrowthTimesDiscount = mCachedData.mGrowthFactor * mCachedData.mDiscountFactor;
        mOneOverSigmaSqrtTtimesGrowthFactorTimesDiscountFactor = mOneOverSigmaSqrtT * mCachedData.mGrowthTimesDiscount;
        mSqrtTtimesGrowthFactorTimesDiscountFactor = mCachedData.mGrowthTimesDiscount * sqrtT;

        make();
        update(mCurrentS);
    }

    public BlackScholesPricingEngine(InputData inputData, CachedData cachedData) {
        super(inputData);
        mCurrentS = inputData.mSpot;
        mCachedData = new CachedData(cachedData);

        mOneOverSigmaSqrtT = 1.0 / mCachedData.mSigmaSqrtDt;
        mOneOverSigmaSqrtTtimesGrowthFactorTimesDiscountFactor = mOneOverSigmaSqrtT * mCachedData.mGrowthTimesDiscount;
        mSqrtTtimesGrowthFactorTimesDiscountFactor = mCachedData.mGrowthTimesDiscount * mCachedData.mSqrtDt;

        make();
        update(mCurrentS);
    }

    protected InputData getInputData() { return mInputData; }
    protected void setInputData(InputData inputData) { mInputData = inputData; }

    @Override
    public double[] price(InputData inputData) {
        double[] ret = new double[2];

        ret[0] = mCachedData.mDiscountFactor * (mCurrentS * mCachedData.mGrowthFactor * mNd1 - mInputData.mStrike * mNd2);
        ret[1] = mCachedData.mDiscountFactor * (-mCurrentS * mCachedData.mGrowthFactor * mNminusD1 + mInputData.mStrike * mNminusD2);

        return ret;
    }

    @Override
    public void spatialDerivatives(OutputData callOutputData, OutputData putOutputData) {
        callOutputData.mDelta = mCachedData.mGrowthTimesDiscount * mNd1;
        putOutputData.mDelta = -mCachedData.mGrowthTimesDiscount * mNminusD1;

        callOutputData.mGamma = putOutputData.mGamma = 1.0 / mCurrentS * mPd1 * mOneOverSigmaSqrtTtimesGrowthFactorTimesDiscountFactor;
    }

    @Override
    public void vega(OutputData callOutputData, OutputData putOutputData) {
        callOutputData.mVega = putOutputData.mVega = mCurrentS * mSqrtTtimesGrowthFactorTimesDiscountFactor * mPd1;
    }

    private static class Stats {
        private static double normCdf(double x) {
            double sqrt1_2 = 0.70710678118654752440;
            double a1 = 0.254829592;
            double a2 = -0.284496736;
            double a3 = 1.421413741;
            double a4 = -1.453152027;
            double a5 = 1.061405429;
            double p  = 0.3275911;

            // Save the sign of x
            int sign = 1;
            if (x < 0)
            {
                x *= -sqrt1_2;
                sign = -1;
            }
            else
                x *= sqrt1_2;

            double t = 1.0 / (1.0 + p * x);
            double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

            return 0.5 * (1.0 + sign * y);
        }

        private static double normPdf(double x) {
            return 1.0 / sqrt(2.0 * Math.PI) * Math.exp(-.5 * x * x);
        }
    }
}

package com.a7raiden.qdev.abp.calcs.models;

import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.OutputData;

/**
 * Created by 7Raiden on 19/01/2018.
 */

public final class BlackScholesPricingEngine extends PricingEngine {
    public BlackScholesPricingEngine(InputData inputData) {
        super(inputData);
    }

    @Override
    public OutputData[] compute() {
        OutputData[] ret = new OutputData[2];
        ret[0] = new OutputData.Builder().build();
        ret[1] = new OutputData.Builder().build();

        double discountFactor = Math.exp(-mInputData.mRiskFreeRate * mInputData.mExpiry);
        double growthFactor = Math.exp(mInputData.mCarryRate * mInputData.mExpiry);
        double discTimesGrowth = discountFactor * growthFactor;

        double sqrtT = Math.sqrt(mInputData.mExpiry);
        double sigmaSqrtT = mInputData.mVolatility * sqrtT;
        double oneOverSigmaSqrtT = 1.0 / sigmaSqrtT;

        double d1 = (mInputData.mCarryRate + .5 * mInputData.mVolatility * mInputData.mVolatility);
        d1 *= mInputData.mExpiry;
        d1 += Math.log(mInputData.mSpot / mInputData.mStrike);
        d1 *= oneOverSigmaSqrtT;

        double d2 = d1 - sigmaSqrtT;

        double Nd1 = Stats.normCdf(d1);
        double NminusD1 = 1.0 - Nd1;
        double Nd2 = Stats.normCdf(d2);
        double NminusD2 = 1.0 - Nd2;

        double Pd1 = Stats.normPdf(d1);

        ret[0].mPrice = discountFactor * (mInputData.mSpot * Nd1 - mInputData.mStrike * Nd2);
        ret[1].mPrice = discountFactor * (-mInputData.mSpot * NminusD1 + mInputData.mStrike * NminusD2);

        ret[0].mDelta = discTimesGrowth * Nd1;
        ret[1].mDelta = -discTimesGrowth * NminusD1;

        ret[0].mGamma = ret[1].mGamma = 1.0 / mInputData.mSpot * Pd1 * oneOverSigmaSqrtT * discTimesGrowth;

        ret[0].mVega = ret[1].mVega = mInputData.mSpot * sqrtT * discTimesGrowth * Pd1;

        return ret;
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
            return 1.0 / Math.sqrt(2.0 * Math.PI) * Math.exp(-.5 * x * x);
        }
    }
}

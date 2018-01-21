package com.a7raiden.qdev.abp.calcs.models;

import android.drm.DrmStore;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;

import com.a7raiden.qdev.abp.calcs.data.CachedData;
import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.ModelType;
import com.a7raiden.qdev.abp.calcs.data.OutputData;
import com.a7raiden.qdev.abp.calcs.data.TreeInputData;

import java.lang.reflect.Array;
import java.security.InvalidParameterException;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by 7Raiden on 21/01/2018.
 */

public class BinomialTreePricingEngine extends PricingEngine {
    private TreeInputData mInputData;
    private CachedData mCachedData;

    /**
     * Array that stores the tree leaves
     */
    public double[] mGrid;

    /**
     * Up discounted probability
     */
    private double mP;

    /**
     * Down discounted probability
     */
    private double mQ;

    /**
     * Up movement factor
     */
    public double mU;

    /**
     * Down movement factor
     */
    public double mD;

    private double mDt;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BinomialTreePricingEngine(InputData inputData) {
        super(inputData);

        if (inputData.mModelType == ModelType.LeisenReimer ||
                inputData.mModelType == ModelType.Joshi)
            mInputData.mNodes = (mInputData.mNodes % 2 != 0) ? mInputData.mNodes : mInputData.mNodes + 1;

        mGrid = new double[mInputData.mNodes + 1];
        mDt = mInputData.mExpiry / mInputData.mNodes;
        mCachedData = new CachedData(Math.sqrt(mDt),
                mInputData.mVolatility * Math.sqrt(mDt),
                Math.exp(mInputData.mCarryRate * mDt),
                Math.exp(-mInputData.mRiskFreeRate * mDt),
                Math.exp((mInputData.mCarryRate - mInputData.mRiskFreeRate) * mDt));

        switch (inputData.mModelType) {
            case CoxRubinsteinRoss:
                buildCoxRubinsteinRoss();
                break;
            case JarrowRudd:
                buildJarrowRudd();
                break;
            case Tian:
                buildTian();
                break;
            case LeisenReimer:
                buildLeisenReimer();
                break;
            case Joshi:
                buildYoshi();
                break;
            default:
                throw new InvalidParameterException();
        }
    }

    @Override
    protected InputData getInputData() {
        return mInputData;
    }

    @Override
    protected void setInputData(InputData inputData) {
        mInputData = (TreeInputData)inputData;
    }

    @Override
    public double[] price(InputData inputData) {
        double[] ret = new double[2];

        double[] callPayoff = new double[mGrid.length];
        double[] putPayoff = new double[mGrid.length];

        double[] _grid = mGrid.clone();
        int finalStep = mInputData.mNodes;
        if (mInputData.mSmoothing) {
            updateGrid(_grid, finalStep);

            InputData tmp = new InputData(inputData);
            tmp.mExpiry = mDt;

            BlackScholesPricingEngine bs = new BlackScholesPricingEngine(tmp, mCachedData);
            for (int i = 0; i <= finalStep; ++i) {
                bs.update(_grid[i]);
                double[] price = bs.price(tmp);
                callPayoff[i] = Math.max(price[0], _grid[i] - mInputData.mStrike);
                putPayoff[i] = Math.max(price[1], -_grid[i] + mInputData.mStrike);
            }

            --finalStep;
        }
        else {
            for (int i = 0; i < finalStep; ++i) {
                callPayoff[i] = Math.max(_grid[i] - mInputData.mStrike, 0.0);
                putPayoff[i] = Math.max(-_grid[i] + mInputData.mStrike, 0.0);
            }
        }

        for (int n = finalStep; n > 0; --n) {
            updateGrid(_grid, n);
            for (int i = 0; i < n; ++i) {
                callPayoff[i] = Math.max(_grid[i] - mInputData.mStrike, discountedExpectation(callPayoff, i));
                if (callPayoff[i] < -1e-10)
                    throw new InvalidParameterException(Double.toString(callPayoff[i]));
                putPayoff[i] = Math.max(-_grid[i] + mInputData.mStrike, discountedExpectation(putPayoff, i));
            }
        }

        if (mInputData.mAcceleration) {
            //TODO: avoid doing the unused calculations!
            BlackScholesPricingEngine bs = new BlackScholesPricingEngine(inputData);
            double[] price = bs.price(inputData);

            if (mInputData.mCarryRate - mInputData.mRiskFreeRate > 0 && mInputData.mRiskFreeRate > 0) {
                ret[0] = price[0];
                ret[1] = putPayoff[1];
            }
            else {
                ret[0] = price[0];
                ret[1] = price[1];
            }
        }
        else {
            ret[0] = callPayoff[0];
            ret[1] = putPayoff[1];
        }
        return ret;
    }

    /**
     * Calculates the discounted expectation of input vector v
     * @param v
     */
    private double discountedExpectation(double[] v, int i) {
        return mP * v[i + 1] + mQ * v[i];
    }

    private void updateGrid(double[] grid, int n)  {
        for (int i = 0; i <= n; ++i) {
            grid[i] = mInputData.mSpot * Math.pow(mU, i) * Math.pow(mD, n - i - 1);
        }
        for (int i = n + 1; i < grid.length; ++i) {
            grid[i] = -1;
        }
    }

    // Reference paper:
    //      THE CONVERGENCE OF BINOMIAL TREES FOR PRICING THE AMERICAN PUT
    //      http://fbe.unimelb.edu.au/__data/assets/pdf_file/0010/2591884/170.pdf

    /** CRR
     * u = e^(sigma * sqrt(dt))
     * d = e^(-sigma * sqrt(dt))
     *
     * p = (e^(b * dt) - d) / (u - d)
     * q = 1 - p
     */
    private void buildCoxRubinsteinRoss() {
        mU = Math.exp(mCachedData.mSigmaSqrtDt);
        mD = 1.0 / mU;

        mP = (mCachedData.mGrowthFactor - mD) / (mU - mD);
        mQ = 1.0 - mP;

        // discount probabilities
        mP *= mCachedData.mDiscountFactor;
        mQ *= mCachedData.mDiscountFactor;

        for (int i = 0; i <= mInputData.mNodes; ++i) {
            mGrid[i] = mInputData.mSpot * Math.pow(mU, i) * Math.pow(mD, mInputData.mNodes - i);
        }
    }

    /** Neutral JR tree
     * u = e^(mu' * dt + sigma * sqrt(dt))
     * d = e^(mu' * dt - sigma * sqrt(dt))
     *
     * p = (e^(b * dt) - d) / (u - d)
     * q = 1 - p
     */
    private void buildJarrowRudd() {
        double muPrime = mInputData.mCarryRate - .5 * mInputData.mVolatility * mInputData.mVolatility;
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
            mGrid[i] = mInputData.mSpot * Math.pow(mU, i) * Math.pow(mD, mInputData.mNodes - i);
        }
    }

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
    private void buildTian() {
        double r = mCachedData.mGrowthFactor;
        double v = Math.exp(mInputData.mVolatility * mInputData.mVolatility * mDt);
        mU = .5 * r * v* (1.0 + v + Math.sqrt(v * v + 2.0 * v - 3.0));
        mD = .5 * r * v* (1.0 + v + Math.sqrt(v * v + 2.0 * v - 3.0));

        mP = (mCachedData.mGrowthFactor - mD) / (mU - mD);
        mQ = 1.0 - mP;

        // discount probabilities
        mP *= mCachedData.mDiscountFactor;
        mQ *= mCachedData.mDiscountFactor;

        for (int i = 0; i <= mInputData.mNodes; ++i) {
            mGrid[i] = mInputData.mSpot * Math.pow(mU, i) * Math.pow(mD, mInputData.mNodes - i);
        }
    }

    /** LR
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void buildLeisenReimer() {
        BiFunction<Double, Integer, Double> peizerPrattInversion = (z, n) -> {
            double result = z / (n + 1.0 / 3.0 + 0.1/ (n + 1.0));
            result *= result;
            result = Math.exp(-result*(n + 1.0 / 6.0));
            result = 0.5 + (z > 0 ? 1 : -1) * Math.sqrt((0.25 * (1.0 - result)));
            return result;
        };

        double d2 = Math.log(mInputData.mSpot / mInputData.mStrike) + (mInputData.mCarryRate - .5 * mInputData.mVolatility * mInputData.mVolatility) *  mDt;
        d2 /= mCachedData.mSigmaSqrtDt;

        mP = peizerPrattInversion.apply(d2, mInputData.mNodes);
        mQ = 1.0 - mP;

        double pDash = peizerPrattInversion.apply(d2 + mCachedData.mSigmaSqrtDt, mInputData.mNodes);
        mU = pDash / mP * mCachedData.mGrowthFactor;
        mD = (mCachedData.mGrowthFactor - mP * mU) / mQ;

        // discount probabilities
        mP *= mCachedData.mDiscountFactor;
        mQ *= mCachedData.mDiscountFactor;

        for (int i = 0; i <= mInputData.mNodes; ++i) {
            mGrid[i] = mInputData.mSpot * Math.pow(mU, i) * Math.pow(mD, mInputData.mNodes - i);
        }
    }

    /** LR
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void buildYoshi() {
        BiFunction<Double, Integer, Double> computeP = (dj, k) -> {
            double alpha = dj / (Math.sqrt(8.0));
            double alpha2 = alpha * alpha;
            double alpha3 = alpha * alpha2;
            double alpha5 = alpha3 * alpha2;
            double alpha7 = alpha5 * alpha2;
            double beta = -0.375 * alpha - alpha3;
            double gamma = (5.0 / 6.0) * alpha5 + (13.0 / 12.0)*alpha3 + (25.0/128.0) * alpha;
            double delta = -0.1025 * alpha- 0.9285 * alpha3 - 1.43 * alpha5 -0.5 * alpha7;
            double p =0.5;
            double sqrtK = Math.sqrt(k);
            p += alpha / sqrtK;
            p += beta / (k * sqrtK);
            p += gamma / (k * k * sqrtK);
            p += delta / (k *k *k * sqrtK);
            return p;
        };

        double d2 = Math.log(mInputData.mSpot / mInputData.mStrike) + (mInputData.mCarryRate - .5 * mInputData.mVolatility * mInputData.mVolatility) *  mDt;
        d2 /= mCachedData.mSigmaSqrtDt;

        mP = computeP.apply(d2, (mInputData.mNodes - 1) / 2);
        mQ = 1.0 - mP;

        double pDash = computeP.apply(d2 + mCachedData.mSigmaSqrtDt, (mInputData.mNodes - 1) / 2);
        mU = pDash / mP * mCachedData.mGrowthFactor;
        mD = (mCachedData.mGrowthFactor - mP * mU) / mQ;

        // discount probabilities
        mP *= mCachedData.mDiscountFactor;
        mQ *= mCachedData.mDiscountFactor;

        for (int i = 0; i <= mInputData.mNodes; ++i) {
            mGrid[i] = mInputData.mSpot * Math.pow(mU, i) * Math.pow(mD, mInputData.mNodes - i);
        }
    }
}

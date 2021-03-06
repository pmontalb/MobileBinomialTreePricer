package com.a7raiden.qdev.abp.calcs.models;

import com.a7raiden.qdev.abp.calcs.data.CachedData;
import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.ModelType;

import java.security.InvalidParameterException;
import java.util.concurrent.ExecutionException;

/**
 * Created by 7Raiden on 21/01/2018.
 */

public abstract class BinomialTreePricingEngine extends PricingEngine {
    CachedData mCachedData;

    /**
     * Array that stores the tree leaves
     */
    public double[] mGrid;

    /**
     * Up discounted probability
     */
    double mP;

    /**
     * Down discounted probability
     */
    double mQ;

    /**
     * Up movement factor
     */
    public double mU;

    /**
     * Down movement factor
     */
    public double mD;

    double mDt;

    BinomialTreePricingEngine(InputData inputData) {
        super(inputData);
    }

    public void build(InputData inputData) {
        int minNodes = (int)(mInputData.mCarryRate * mInputData.mCarryRate * mInputData.mExpiry / (mInputData.mVolatility * mInputData.mVolatility));
        mInputData.mNodes = Math.max(minNodes, mInputData.mNodes);

        if (inputData.mModelType == ModelType.LeisenReimer ||
                inputData.mModelType == ModelType.Joshi)
            mInputData.mNodes = (mInputData.mNodes % 2 != 0) ? mInputData.mNodes : mInputData.mNodes + 1;

        mGrid = new double[mInputData.mNodes + 1];
        mDt = inputData.mExpiry / mInputData.mNodes;
        mCachedData = new CachedData(Math.sqrt(mDt),
                inputData.mVolatility * Math.sqrt(mDt),
                Math.exp(inputData.mCarryRate * mDt),
                Math.exp(-inputData.mRiskFreeRate * mDt),
                Math.exp((inputData.mCarryRate - inputData.mRiskFreeRate) * mDt));

        buildImpl(inputData);
    }

    // Reference paper:
    //      THE CONVERGENCE OF BINOMIAL TREES FOR PRICING THE AMERICAN PUT
    //      http://fbe.unimelb.edu.au/__data/assets/pdf_file/0010/2591884/170.pdf
    protected abstract void buildImpl(InputData inputData);

    @Override
    public double[] price(InputData inputData) {
        build(inputData);
        double[] ret = new double[2];

        double[] callPayoff = new double[mGrid.length];
        double[] putPayoff = new double[mGrid.length];

        double[] _grid = mGrid.clone();
        int finalStep = mInputData.mNodes;
        if (mInputData.mSmoothing &&
                mInputData.mModelType != ModelType.LeisenReimer &&
                mInputData.mModelType != ModelType.Joshi) {  // these 2 methods already have smooth convergence
            updateGrid(_grid, inputData.mSpot, finalStep);
            --finalStep;

            InputData tmp = new InputData(inputData);
            tmp.mExpiry = mDt;

            BlackScholesPricingEngine bs = new BlackScholesPricingEngine(tmp, mCachedData);
            for (int i = 0; i <= finalStep; ++i) {
                bs.update(_grid[i]);
                double[] price = bs.price(tmp);
                callPayoff[i] = Math.max(price[0], _grid[i] - inputData.mStrike);
                putPayoff[i] = Math.max(price[1], -_grid[i] + inputData.mStrike);
            }
        }
        else {
            for (int i = 0; i <= finalStep; ++i) {
                callPayoff[i] = Math.max(_grid[i] - inputData.mStrike, 0.0);
                putPayoff[i] = Math.max(-_grid[i] + inputData.mStrike, 0.0);
            }
        }

        for (int n = finalStep; n > 0; --n) {
            updateGrid(_grid, inputData.mSpot, n);
            for (int i = 0; i < n; ++i) {
                callPayoff[i] = Math.max(_grid[i] - inputData.mStrike, discountedExpectation(callPayoff, i));
                putPayoff[i] = Math.max(-_grid[i] + inputData.mStrike, discountedExpectation(putPayoff, i));
            }
        }

        if (mInputData.mAcceleration) {
            //TODO: avoid doing the unused calculations!
            BlackScholesPricingEngine bs = new BlackScholesPricingEngine(inputData);
            double[] price = bs.price(inputData);

            if (mInputData.mCarryRate - mInputData.mRiskFreeRate >= 0 && mInputData.mRiskFreeRate > 0) {
                ret[0] = price[0];
                ret[1] = putPayoff[0];
            }
            else {
                ret[0] = callPayoff[0];
                ret[1] = price[1];
            }
        }
        else {
            ret[0] = callPayoff[0];
            ret[1] = putPayoff[0];
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

    private void updateGrid(double[] grid, double S, int n)  {
        for (int i = 0; i <= n; ++i) {
            grid[i] = S * Math.pow(mU, i) * Math.pow(mD, n - i - 1);
        }
    }
}

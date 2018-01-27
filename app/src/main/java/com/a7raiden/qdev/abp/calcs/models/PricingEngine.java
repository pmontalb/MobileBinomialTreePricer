package com.a7raiden.qdev.abp.calcs.models;

import com.a7raiden.qdev.abp.calcs.data.ImpliedVolatilityInputData;
import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.ModelType;
import com.a7raiden.qdev.abp.calcs.data.OptionType;
import com.a7raiden.qdev.abp.calcs.data.OutputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderOutputData;
import com.a7raiden.qdev.abp.calcs.interfaces.IPricingEngine;
import com.a7raiden.qdev.abp.calcs.interfaces.IRootFinder;
import com.a7raiden.qdev.abp.calcs.math.RootFinder;

import java.security.InvalidParameterException;

/**
 * Created by 7Raiden on 19/01/2018.
 */

public abstract class PricingEngine implements IPricingEngine {
    protected InputData mInputData;

    PricingEngine(InputData inputData) {
        mInputData = inputData;
    }

    public static PricingEngine create(InputData inputData) {
        if (inputData.mModelType == ModelType.BlackScholes)
            return new BlackScholesPricingEngine(inputData);

        BinomialTreePricingEngine pe;
        switch (inputData.mModelType) {
            case CoxRubinsteinRoss:
                pe = new CoxRubinsteinRossBinomialTree(inputData);
                break;
            case JarrowRuddNeutral:
                pe = new JarrowRuddBinomialTree(inputData);
                break;
            case Tian:
                pe = new TianBinomialTree(inputData);
                break;
            case LeisenReimer:
                pe = new LeisenReimerBinomialTree(inputData);
                break;
            case Joshi:
                pe = new JoshiBinomialTree(inputData);
                break;
            default:
                throw new InvalidParameterException();
        }

        pe.build(inputData);

        return pe;
    }

    @Override
    public OutputData[] computeAnalytics() {
        OutputData[] ret = new OutputData[2];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = new OutputData.Builder().build();
        }

        double[] price = price(mInputData);
        ret[0].mPrice = price[0];
        ret[1].mPrice = price[1];

        spatialDerivatives(ret[0], ret[1]);

        vega(ret[0], ret[1]);

        return ret;
    }

    static public RootFinderOutputData computeImpliedVolatility(ImpliedVolatilityInputData impliedVolatilityInputData) {
        RootFinderOutputData[] ret = new RootFinderOutputData[2];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = new RootFinderOutputData(0.0, -1, false);
        }

        int outputIdx = impliedVolatilityInputData.mOptionType == OptionType.Call ? 0 : 1;

        IRootFinder rootFinder = RootFinder.create(
                impliedVolatilityInputData.mRootFinderInputData.mRootFinderType,
                sigma -> {
                    InputData inputData = new InputData(impliedVolatilityInputData.mInputData);
                    inputData.mVolatility = sigma;
                    PricingEngine pe = PricingEngine.create(inputData);
                    return pe.price(inputData)[outputIdx] - impliedVolatilityInputData.mTargetPrice;
                });

        return rootFinder.solve(impliedVolatilityInputData.mRootFinderInputData);
    }

    public abstract double[] price(InputData inputData);

    public void spatialDerivatives(OutputData callOutputData, OutputData putOutputData) {
        InputData temp = new InputData(mInputData);
        double spotIncrement = 1e-4;

        temp.mSpot += spotIncrement;
        double[] priceUp = price(temp);

        temp.mSpot -= 2.0 * spotIncrement;
        double[] priceDn = price(temp);

        callOutputData.mDelta = (priceUp[0] - priceDn[0]) / (2.0 * spotIncrement);
        callOutputData.mGamma = (priceUp[0] -2.0 * callOutputData.mPrice + priceDn[0]) / (spotIncrement * spotIncrement);

        putOutputData.mDelta = (priceUp[1] - priceDn[1]) / (2.0 * spotIncrement);
        putOutputData.mGamma = (priceUp[1] -2.0 * putOutputData.mPrice + priceDn[1]) / (spotIncrement * spotIncrement);
    }

    public void vega(OutputData callOutputData, OutputData putOutputData) {
        InputData temp = new InputData(mInputData);
        double vegaIncrement = 1e-4;

        temp.mVolatility += vegaIncrement;
        double[] priceUp = price(temp);

        temp.mVolatility -= 2.0 * vegaIncrement;
        double[] priceDn = price(temp);

        callOutputData.mVega = (priceUp[0] - priceDn[0]) / (2.0 * vegaIncrement);
        putOutputData.mVega = (priceUp[1] - priceDn[1]) / (2.0 * vegaIncrement);

    }
}

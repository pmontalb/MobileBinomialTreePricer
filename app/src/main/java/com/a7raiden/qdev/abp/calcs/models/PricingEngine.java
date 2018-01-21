package com.a7raiden.qdev.abp.calcs.models;

import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.ModelType;
import com.a7raiden.qdev.abp.calcs.data.OutputData;
import com.a7raiden.qdev.abp.calcs.interfaces.IPricingEngine;

/**
 * Created by 7Raiden on 19/01/2018.
 */

public abstract class PricingEngine implements IPricingEngine {

    PricingEngine(InputData inputData) {
        setInputData(inputData);
    }

    protected abstract InputData getInputData();
    protected abstract void setInputData(InputData inputData);

    public static PricingEngine create(ModelType modelType, InputData inputData) {
        switch (modelType) {
            case BlackScholes:
                return new BlackScholesPricingEngine(inputData);
            case CoxRubinsteinRoss:
                return new BinomialTreePricingEngine(inputData);
            case JarrowRudd:
                break;
            case Tian:
                break;
            case LeisenReimer:
                break;
            case Joshi:
                break;
            default:
                throw new IllegalArgumentException();
        }

        return null;
    }

    @Override
    public OutputData[] compute() {
        OutputData[] ret = new OutputData[2];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = new OutputData.Builder().build();
        }

        double[] price = price(getInputData());
        ret[0].mPrice = price[0];
        ret[1].mPrice = price[1];

        spatialDerivatives(ret[0], ret[1]);

        vega(ret[0], ret[1]);

        return ret;
    }

    public abstract double[] price(InputData inputData);

    public void spatialDerivatives(OutputData callOutputData, OutputData putOutputData) {
        InputData temp = new InputData(getInputData());
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
        InputData temp = new InputData(getInputData());
        double vegaIncrement = 1e-4;

        temp.mVolatility += vegaIncrement;
        double[] priceUp = price(temp);

        temp.mVolatility -= 2.0 * vegaIncrement;
        double[] priceDn = price(temp);

        callOutputData.mVega = (priceUp[0] - priceDn[0]) / (2.0 * vegaIncrement);
        putOutputData.mVega = (priceUp[1] - priceDn[1]) / (2.0 * vegaIncrement);

    }
}

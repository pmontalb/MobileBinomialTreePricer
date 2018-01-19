package com.a7raiden.qdev.abp.calcs.models;

import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.ModelType;
import com.a7raiden.qdev.abp.calcs.data.OutputData;
import com.a7raiden.qdev.abp.calcs.interfaces.IPricingEngine;

/**
 * Created by 7Raiden on 19/01/2018.
 */

public abstract class PricingEngine implements IPricingEngine {

    protected final InputData mInputData;

    PricingEngine(InputData inputData) {
        mInputData = inputData;
    }

    public static PricingEngine create(ModelType modelType, InputData inputData) {
        switch (modelType) {
            case BlackScholes:
                return new BlackScholesPricingEngine(inputData);
            case CoxRubinsteinRoss:
                break;
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

    protected double price(InputData inputData) { return 0.0; }

    protected void spatialDerivatives(OutputData outputData) {
        InputData temp = new InputData(mInputData);
        double spotIncrement = 1e-4;

        temp.mSpot += spotIncrement;
        double priceUp = price(temp);

        temp.mSpot -= 2.0 * spotIncrement;
        double priceDn = price(temp);

        outputData.mDelta = (priceUp - priceDn) / (2.0 * spotIncrement);
        outputData.mGamma = (priceUp -2.0 * outputData.mPrice + priceDn) / (spotIncrement * spotIncrement);
    }

    protected void vega(OutputData outputData) {
        InputData temp = new InputData(mInputData);
        double vegaIncrement = 1e-4;

        temp.mVolatility += vegaIncrement;
        double priceUp = price(temp);

        temp.mVolatility -= 2.0 * vegaIncrement;
        double priceDn = price(temp);

        outputData.mVega = (priceUp - priceDn) / (2.0 * vegaIncrement);
    }
}

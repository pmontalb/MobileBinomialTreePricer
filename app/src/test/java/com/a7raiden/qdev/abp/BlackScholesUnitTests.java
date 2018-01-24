package com.a7raiden.qdev.abp;

import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.ModelType;
import com.a7raiden.qdev.abp.calcs.data.OutputData;
import com.a7raiden.qdev.abp.calcs.interfaces.IPricingEngine;
import com.a7raiden.qdev.abp.calcs.models.BlackScholesPricingEngine;
import com.a7raiden.qdev.abp.calcs.models.PricingEngine;

import org.junit.Test;

import static org.junit.Assert.*;

public class BlackScholesUnitTests {
    @Test
    public void consistency() throws Exception {
        InputData inputData = new InputData.Builder()
                .spot(100.0)
                .strike(100.0)
                .riskFreeRate(0.05)
                .carryRate(.02)
                .volatility(.30)
                .expiry(2.0)
                .modelType(ModelType.BlackScholes)
                .build();
        IPricingEngine pe = PricingEngine.create(inputData);
        OutputData[] outputData = pe.computeAnalytics();

        assertEquals(17.425286472661877, outputData[0].mPrice, 1e-12);
        assertEquals(0.5842281508560783, outputData[0].mDelta, 1e-12);
        assertEquals(0.008449449821696086, outputData[0].mGamma, 1e-12);
        assertEquals(50.69669893017651, outputData[0].mVega, 1e-12);

        assertEquals(13.732574917832963, outputData[1].mPrice, 1e-12);
        assertEquals(-0.35753638272817034, outputData[1].mDelta, 1e-12);
        assertEquals(0.008449449821696086, outputData[1].mGamma, 1e-12);
        assertEquals(50.69669893017651, outputData[1].mVega, 1e-12);
    }

    @Test
    public void putCallParity() throws Exception{
        InputData inputData = new InputData.Builder()
                .spot(100.0)
                .strike(100.0)
                .riskFreeRate(0.05)
                .carryRate(.02)
                .volatility(.30)
                .expiry(2.0).build();

        BlackScholesPricingEngine bs = new BlackScholesPricingEngine(inputData);
        for (int i = 0; i < 20; ++i)
        {
            double S = 80 * (1 + .025 * i);
            bs.update(S);

            double[] price = bs.price(inputData);
            double C = price[0];
            double P = price[1];
            double F = (S * Math.exp(inputData.mCarryRate * inputData.mExpiry) - inputData.mStrike) * Math.exp(-inputData.mRiskFreeRate * inputData.mExpiry);

            assertEquals(F, C - P, 1e-12);

            OutputData callOutputData = new OutputData.Builder().build();
            OutputData putOutputData = new OutputData.Builder().build();
            bs.spatialDerivatives(callOutputData, putOutputData);
            double delta = Math.exp(inputData.mCarryRate * inputData.mExpiry) * Math.exp(-inputData.mRiskFreeRate * inputData.mExpiry);
            assertEquals(delta, callOutputData.mDelta - putOutputData.mDelta, 1e-12);
            assertEquals(0.0, callOutputData.mGamma - putOutputData.mGamma, 1e-12);

            bs.vega(callOutputData, putOutputData);
            assertEquals(0.0, callOutputData.mVega - putOutputData.mVega, 1e-12);
        }
    }
}
package com.a7raiden.qdev.abp;

import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.ModelType;
import com.a7raiden.qdev.abp.calcs.data.OutputData;
import com.a7raiden.qdev.abp.calcs.models.BinomialTreePricingEngine;
import com.a7raiden.qdev.abp.calcs.models.BlackScholesPricingEngine;
import com.a7raiden.qdev.abp.calcs.models.PricingEngine;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BinomialTreeUnitTests {
    @Test
    public void gridConsistency() throws Exception {
        for (ModelType modelType : ModelType.values()) {
            if (modelType == ModelType.BlackScholes || modelType == ModelType.Null)
                continue;
            InputData inputData = new InputData.Builder()
                    .spot(100.0)
                    .strike(100.0)
                    .riskFreeRate(0.05)
                    .carryRate(.02)
                    .volatility(.30)
                    .expiry(2.0)
                    .nodes(90)
                    .smoothing(true)
                    .acceleration(true)
                    .modelType(modelType)
                    .build();
            BinomialTreePricingEngine pe = (BinomialTreePricingEngine)PricingEngine.create(inputData);

            assertEquals(pe.mGrid[0], inputData.mSpot * Math.pow(pe.mD, inputData.mNodes), 1e-12);
            assertEquals(pe.mGrid[pe.mGrid.length - 1], inputData.mSpot * Math.pow(pe.mU, inputData.mNodes), 1e-12);
        }
    }

    @Test
    public void accelerationConsistency() throws Exception {
        for (ModelType modelType : ModelType.values()) {
            if (modelType == ModelType.BlackScholes || modelType == ModelType.Null)
                continue;
            InputData inputData = new InputData.Builder()
                    .spot(100.0)
                    .strike(100.0)
                    .riskFreeRate(0.05)
                    .carryRate(.05)
                    .volatility(.30)
                    .expiry(2.0)
                    .nodes(80)
                    .smoothing(true)
                    .acceleration(true)
                    .modelType(modelType)
                    .build();
            PricingEngine pe = PricingEngine.create(inputData);

            BlackScholesPricingEngine bs = new BlackScholesPricingEngine(inputData);
            OutputData[] bsOutputData = bs.computeAnalytics();

            OutputData[] outputData = pe.computeAnalytics();
            assertEquals(bsOutputData[0].mPrice, outputData[0].mPrice, 1e-12);
        }
    }

    @Test
    public void consistency() throws Exception {
        for (ModelType modelType : ModelType.values()) {
            if (modelType == ModelType.BlackScholes || modelType == ModelType.Null)
                continue;
            InputData inputData = new InputData.Builder()
                    .spot(100.0)
                    .strike(100.0)
                    .riskFreeRate(0.05)
                    .carryRate(.05)
                    .volatility(.30)
                    .expiry(2.0)
                    .nodes(800)
                    .smoothing(true)
                    .acceleration(false)
                    .modelType(modelType)
                    .build();
            PricingEngine pe = PricingEngine.create(inputData);

            BlackScholesPricingEngine bs = new BlackScholesPricingEngine(inputData);
            OutputData[] bsOutputData = bs.computeAnalytics();

            OutputData[] outputData = pe.computeAnalytics();
            assertEquals(bsOutputData[0].mPrice, outputData[0].mPrice, 5e-3);
            assertEquals(bsOutputData[0].mDelta, outputData[0].mDelta, 5e-3);
            assertEquals(bsOutputData[0].mGamma, outputData[0].mGamma, 5e-3);
            assertEquals(bsOutputData[0].mVega / outputData[0].mVega - 1.0, 0.0, 5e-3);

            assertTrue(bsOutputData[1].mPrice < outputData[1].mPrice);
            assertTrue(-bsOutputData[1].mDelta < -outputData[1].mDelta);
        }
    }

    @Test
    public void convergence() throws Exception {
        for (ModelType modelType : ModelType.values()) {
            int nodes = 50;

            double[] currentError = new double[3];  // gamma can be unstable and not exhibiting the rate of convergence
            for (int iteration = 0; iteration < 4; ++iteration) {
                nodes *= 2;
                if (modelType == ModelType.BlackScholes || modelType == ModelType.Null)
                    continue;
                InputData inputData = new InputData.Builder()
                        .spot(100.0)
                        .strike(100.0)
                        .riskFreeRate(0.05)
                        .carryRate(.05)
                        .volatility(.30)
                        .expiry(2.0)
                        .nodes(nodes)
                        .smoothing(true)
                        .acceleration(false)
                        .modelType(modelType)
                        .build();
                PricingEngine pe = PricingEngine.create(inputData);

                BlackScholesPricingEngine bs = new BlackScholesPricingEngine(inputData);
                OutputData[] bsOutputData = bs.computeAnalytics();
                OutputData[] outputData = pe.computeAnalytics();

                double[] previousError = currentError.clone();
                currentError[0] = bsOutputData[0].mPrice / outputData[0].mPrice - 1.0;
                currentError[1] = bsOutputData[0].mDelta / outputData[0].mDelta - 1.0;
                currentError[2] = bsOutputData[0].mVega / outputData[0].mVega - 1.0;

                if (iteration == 0)
                    continue;

                for (int j = 0; j < currentError.length; ++j) {
                    assertTrue("j=" + String.valueOf(j) + " | m=" + modelType.toString() + " | " + String.valueOf(currentError[j]) + " | " + String.valueOf(previousError[j]),
                            Math.abs(currentError[j]) < Math.abs(previousError[j]) + 1e-4);
                }
            }
        }
    }
}
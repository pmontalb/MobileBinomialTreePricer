package com.a7raiden.qdev.abp;

import com.a7raiden.qdev.abp.calcs.data.ImpliedVolatilityInputData;
import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.ModelType;
import com.a7raiden.qdev.abp.calcs.data.OptionType;
import com.a7raiden.qdev.abp.calcs.data.RootFinderInputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderOutputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderType;
import com.a7raiden.qdev.abp.calcs.interfaces.IPricingEngine;
import com.a7raiden.qdev.abp.calcs.models.BinomialTreePricingEngine;
import com.a7raiden.qdev.abp.calcs.models.PricingEngine;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImpliedVolatilityUnitTests {
    @Test
    public void blackScholesConsistency() throws Exception {
        for (RootFinderType rootFinderType : RootFinderType.values()) {
            if (rootFinderType == RootFinderType.Null || rootFinderType == RootFinderType.Brent)
                continue;
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

            RootFinderInputData rootFinderInputData = new RootFinderInputData.Builder()
                    .rootFinderType(rootFinderType)
                    .lowerPoint(1e-4)
                    .upperPoint(1.0)
                    .absTolerance(1e-8)
                    .maxIterations(200)
                    .build();
            ImpliedVolatilityInputData impliedVolatilityInputData = new ImpliedVolatilityInputData
                    .Builder()
                    .targetPrice(pe.computeAnalytics()[0].mPrice)
                    .inputData(inputData)
                    .optionType(OptionType.Call)
                    .rootFinderInputData(rootFinderInputData)
                    .build();
            RootFinderOutputData rootFinderOutputData = PricingEngine.computeImpliedVolatility(impliedVolatilityInputData);

            assertEquals(rootFinderOutputData.mRoot, inputData.mVolatility, 1e-8);
            assertTrue(rootFinderOutputData.mIterations <= rootFinderInputData.mMaxIterations);
            assertTrue(rootFinderOutputData.mHasConverged);
        }
    }

    @Test
    public void treeConsistency() throws Exception {
        for (RootFinderType rootFinderType : RootFinderType.values()) {
            if (rootFinderType == RootFinderType.Null || rootFinderType == RootFinderType.Brent)
                continue;

            for (ModelType modelType: ModelType.values()) {
                if (modelType == ModelType.BlackScholes || modelType == ModelType.Null)
                    continue;

                InputData inputData = new InputData.Builder()
                        .spot(100.0)
                        .strike(100.0)
                        .riskFreeRate(0.05)
                        .carryRate(.02)
                        .volatility(.30)
                        .expiry(2.0)
                        .nodes(80)
                        .smoothing(true)
                        .acceleration(true)
                        .modelType(modelType)
                        .build();
                BinomialTreePricingEngine pe = (BinomialTreePricingEngine)PricingEngine.create(inputData);

                RootFinderInputData rootFinderInputData = new RootFinderInputData.Builder()
                        .rootFinderType(rootFinderType)
                        .lowerPoint(.1)
                        .upperPoint(1.0)
                        .absTolerance(1e-8)
                        .maxIterations(200)
                        .build();
                ImpliedVolatilityInputData impliedVolatilityInputData = new ImpliedVolatilityInputData
                        .Builder()
                        .targetPrice(pe.computeAnalytics()[0].mPrice)
                        .inputData(inputData)
                        .optionType(OptionType.Call)
                        .rootFinderInputData(rootFinderInputData)
                        .build();
                RootFinderOutputData rootFinderOutputData = PricingEngine.computeImpliedVolatility(impliedVolatilityInputData);

                assertEquals(rootFinderOutputData.mRoot, inputData.mVolatility, 1e-8);
                assertTrue(rootFinderOutputData.mIterations <= rootFinderInputData.mMaxIterations);
                assertTrue(rootFinderOutputData.mHasConverged);
            }
        }
    }
}
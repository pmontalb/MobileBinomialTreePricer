package com.a7raiden.qdev.abp;

import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.ModelType;
import com.a7raiden.qdev.abp.calcs.data.OutputData;
import com.a7raiden.qdev.abp.calcs.data.TreeInputData;
import com.a7raiden.qdev.abp.calcs.interfaces.IPricingEngine;
import com.a7raiden.qdev.abp.calcs.models.BinomialTreePricingEngine;
import com.a7raiden.qdev.abp.calcs.models.BlackScholesPricingEngine;
import com.a7raiden.qdev.abp.calcs.models.PricingEngine;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BinomialTreeUnitTests {
    @Test
    public void gridConsistency() throws Exception {
        InputData inputData = new TreeInputData.Builder()
                .spot(100.0)
                .strike(100.0)
                .riskFreeRate(0.05)
                .carryRate(.02)
                .volatility(.30)
                .expiry(2.0)
                .modelType(ModelType.CoxRubinsteinRoss)
                .build();
        TreeInputData treeInputData = new TreeInputData(inputData, 90, true, true);
        BinomialTreePricingEngine pe = new BinomialTreePricingEngine(treeInputData);

        assertEquals(pe.mGrid[0], treeInputData.mSpot * Math.pow(pe.mD, treeInputData.mNodes), 1e-12);
        assertEquals(pe.mGrid[pe.mGrid.length - 1], treeInputData.mSpot * Math.pow(pe.mU, treeInputData.mNodes), 1e-12);
    }

    @Test
    public void accelerationConsistency() throws Exception {
        InputData inputData = new TreeInputData.Builder()
                .spot(100.0)
                .strike(100.0)
                .riskFreeRate(0.05)
                .carryRate(.05)
                .volatility(.30)
                .expiry(2.0)
                .modelType(ModelType.CoxRubinsteinRoss)
                .build();
        TreeInputData treeInputData = new TreeInputData(inputData, 80, true, true);
        BinomialTreePricingEngine pe = new BinomialTreePricingEngine(treeInputData);

        BlackScholesPricingEngine bs = new BlackScholesPricingEngine(inputData);
        OutputData[] bsOutputData = bs.compute();

        OutputData[] outputData = pe.compute();
        assertEquals(bsOutputData[0].mPrice, outputData[0].mPrice, 1e-12);
    }

    @Test
    public void consistency() throws Exception {
        InputData inputData = new TreeInputData.Builder()
                .spot(100.0)
                .strike(100.0)
                .riskFreeRate(0.05)
                .carryRate(.05)
                .volatility(.30)
                .expiry(2.0)
                .modelType(ModelType.CoxRubinsteinRoss)
                .build();
        TreeInputData treeInputData = new TreeInputData(inputData, 800, true, false);
        BinomialTreePricingEngine pe = new BinomialTreePricingEngine(treeInputData);

        BlackScholesPricingEngine bs = new BlackScholesPricingEngine(inputData);
        OutputData[] bsOutputData = bs.compute();

        OutputData[] outputData = pe.compute();
        assertEquals(bsOutputData[0].mPrice, outputData[0].mPrice, 5e-3);
        assertTrue(bsOutputData[1].mPrice < outputData[1].mPrice);
    }
}
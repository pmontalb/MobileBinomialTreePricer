package com.a7raiden.qdev.abp;
import com.a7raiden.qdev.abp.calcs.data.RootFinderInputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderOutputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderType;
import com.a7raiden.qdev.abp.calcs.interfaces.IRootFinder;
import com.a7raiden.qdev.abp.calcs.math.RootFinder;

import org.junit.Test;

import java.util.function.Function;

import static org.junit.Assert.*;

public class RootFinderUnitTests {
    @Test
    public void problem1() throws Exception {
        for (RootFinderType rootFinderType: RootFinderType.values()) {
            if (rootFinderType == RootFinderType.Null || rootFinderType == RootFinderType.Brent)
                continue;

            Function<Double, Double> obj = x -> Math.sin(x) - .5 * x;
            IRootFinder rootFinder = RootFinder.create(rootFinderType, obj);

            RootFinderInputData rootFinderInputData = new RootFinderInputData.Builder()
                    .lowerPoint(.5 * Math.PI)
                    .upperPoint(Math.PI)
                    .maxIterations(100)
                    .absTolerance(1e-8)
                    .build();
            RootFinderOutputData rootFinderOutputData = rootFinder.solve(rootFinderInputData);
            assertEquals(true, rootFinderOutputData.mHasConverged);
            assertEquals(1.89549426703398094714, rootFinderOutputData.mRoot, 1e-8);
        }
    }

    @Test
    public void problem2() throws Exception {
        Function<Double, Double> obj = x -> {
            double ret = 0.0;
            for (int i = 1; i < 20; ++i)
                ret += -2.0 * (2 * i - 5) * (2 * i - 5) / ((x - i * i) * (x - i * i) * (x - i * i));

            return ret;
        };
        IRootFinder rootFinder = RootFinder.create(RootFinderType.Toms348, obj);

        RootFinderInputData rootFinderInputData = new RootFinderInputData.Builder()
                .lowerPoint(1 + 1e-9)
                .upperPoint(4 - 1e-9)
                .maxIterations(100)
                .absTolerance(0)
                .build();
        RootFinderOutputData rootFinderOutputData = rootFinder.solve(rootFinderInputData);
        assertEquals(true, rootFinderOutputData.mHasConverged);
        assertEquals(3.0229153472730568, rootFinderOutputData.mRoot, 1e-5);

        rootFinderInputData = new RootFinderInputData.Builder()
                .lowerPoint(100 + 1e-9)
                .upperPoint(121 - 1e-9)
                .maxIterations(100)
                .absTolerance(0)
                .build();
        RootFinderOutputData rootFinderOutputData2 = rootFinder.solve(rootFinderInputData);
        assertEquals(true, rootFinderOutputData2.mHasConverged);
        assertEquals(110.02653274766949, rootFinderOutputData2.mRoot, 1e-3);
    }
}
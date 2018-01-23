package com.a7raiden.qdev.abp;
import com.a7raiden.qdev.abp.calcs.data.RootFinderData;
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

            RootFinderData rootFinderData = rootFinder.solve(.5 * Math.PI, Math.PI, 100, 1e-8);
            assertEquals(true, rootFinderData.mHasConverged);
            assertEquals(1.89549426703398094714, rootFinderData.mRoot, 1e-8);
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
        IRootFinder rootFinder = RootFinder.create(RootFinderType.Tom348, obj);

        RootFinderData rootFinderData = rootFinder.solve(1 + 1e-9, 4 - 1e-9, 100, 1e-8);
        assertEquals(true, rootFinderData.mHasConverged);
        assertEquals(3.0229153472730568, rootFinderData.mRoot, 1e-5);

        RootFinderData rootFinderData2 = rootFinder.solve(100 + 1e-9, 121 - 1e-9, 100, 1e-8);
        assertEquals(true, rootFinderData2.mHasConverged);
        assertEquals(110.02653274766949, rootFinderData2.mRoot, 1e-3);
    }
}
package com.a7raiden.qdev.abp.calcs.math;

import com.a7raiden.qdev.abp.calcs.data.RootFinderInputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderOutputData;

import java.util.function.Function;

/**
 * Created by 7Raiden on 23/01/2018.
 */

public class BisectionRootFinder extends RootFinder {
    public BisectionRootFinder(IObjectiveFunction objectiveFunction) {
        super(objectiveFunction);
    }

    @Override
    public RootFinderOutputData solve(RootFinderInputData rootFinderInputData) {
        if(mObjectiveFunction.compute(rootFinderInputData.mLowerPoint) * mObjectiveFunction.compute(rootFinderInputData.mUpperPoint) >= 0)
            return new RootFinderOutputData(-1, -1, false);

        int iterations = 0;
        while ((rootFinderInputData.mUpperPoint - rootFinderInputData.mLowerPoint) >= rootFinderInputData.mAbsTolerance)
        {
            ++iterations;

            double midPoint = .5 * (rootFinderInputData.mLowerPoint + rootFinderInputData.mUpperPoint);
            if (Math.abs(mObjectiveFunction.compute(midPoint)) <= rootFinderInputData.mAbsTolerance)
                return new RootFinderOutputData(midPoint, iterations, true);

            else if (mObjectiveFunction.compute(midPoint)*mObjectiveFunction.compute(rootFinderInputData.mLowerPoint) < 0)
                rootFinderInputData.mUpperPoint = midPoint;
            else
                rootFinderInputData.mLowerPoint = midPoint;
        }

        return new RootFinderOutputData(.5 * (rootFinderInputData.mUpperPoint + rootFinderInputData.mLowerPoint),
                iterations, iterations <= rootFinderInputData.mMaxIterations);
    }
}

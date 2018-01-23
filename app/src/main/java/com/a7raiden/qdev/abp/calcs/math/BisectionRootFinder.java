package com.a7raiden.qdev.abp.calcs.math;

import com.a7raiden.qdev.abp.calcs.data.RootFinderData;

import java.util.function.Function;

/**
 * Created by 7Raiden on 23/01/2018.
 */

public class BisectionRootFinder extends RootFinder {
    public BisectionRootFinder(Function<Double, Double> objectiveFunction) {
        super(objectiveFunction);
    }

    @Override
    public RootFinderData solve(double lowerPoint, double upperPoint, int maxIterations, double absTolerance) {
        if(mObjectiveFunction.apply(lowerPoint) * mObjectiveFunction.apply(upperPoint) >= 0)
            return new RootFinderData(-1, -1, false);

        int iterations = 0;
        while ((upperPoint - lowerPoint) >= absTolerance)
        {
            ++iterations;

            double midPoint = .5 * (lowerPoint + upperPoint);
            if (Math.abs(mObjectiveFunction.apply(midPoint)) <= absTolerance)
                return new RootFinderData(midPoint, iterations, true);

            else if (mObjectiveFunction.apply(midPoint)*mObjectiveFunction.apply(lowerPoint) < 0)
                upperPoint = midPoint;
            else
                lowerPoint = midPoint;
        }

        return new RootFinderData(-1, maxIterations, false);
    }
}

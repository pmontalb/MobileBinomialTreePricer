package com.a7raiden.qdev.abp.calcs.math;

import com.a7raiden.qdev.abp.calcs.data.RootFinderType;
import com.a7raiden.qdev.abp.calcs.interfaces.IRootFinder;

import java.security.InvalidParameterException;
import java.util.function.Function;

/**
 * Created by 7Raiden on 23/01/2018.
 */

public abstract class RootFinder implements IRootFinder {
    public interface IObjectiveFunction {
        double compute(double x);
    };

    IObjectiveFunction mObjectiveFunction;

    static final double eps = Math.ulp(1.0);

    RootFinder(IObjectiveFunction objectiveFunction) {
        mObjectiveFunction = objectiveFunction;
    }

    public static IRootFinder create(RootFinderType rootFinderType, IObjectiveFunction objectiveFunction) {
        switch (rootFinderType) {
            case Bisection:
                return new BisectionRootFinder(objectiveFunction);
            case Brent:
                return new BrentRootFinder(objectiveFunction);
            case Toms348:
                return new Toms348RootFinder(objectiveFunction);
            default:
                throw new InvalidParameterException();
        }
    }

    /**
     * floating point comparison function
     * @param x
     * @param y
     * @return
     */
    static boolean almostEqual(double x, double y) {
        return Math.abs(x - y) > eps;
    }
}

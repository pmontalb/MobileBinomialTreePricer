package com.a7raiden.qdev.abp.calcs.math;

import com.a7raiden.qdev.abp.calcs.data.RootFinderInputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderOutputData;

import java.util.function.Function;

/**
 * Created by 7Raiden on 23/01/2018.
 */

/**
 * This class implements the <a href="http://mathworld.wolfram.com/BrentsMethod.html">
 * Brent algorithm</a> for finding zeros of real univariate functions.
 * The function should be continuous but not necessarily smooth.
 * The {@code solve} method returns a zero {@code x} of the function {@code f}
 * in the given interval {@code [a, b]} to within a tolerance
 * {@code 2 eps abs(x) + t} where {@code eps} is the relative accuracy and
 * {@code t} is the absolute accuracy.
 * <p>The given interval must bracket the root.</p>
 * <p>
 *  The reference implementation is given in chapter 4 of
 *  <blockquote>
 *   <b>Algorithms for Minimization Without Derivatives</b>,
 *   <em>Richard P. Brent</em>,
 *   Dover, 2002
 *  </blockquote>
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class BrentRootFinder extends RootFinder {

    public BrentRootFinder(IObjectiveFunction objectiveFunction) {
        super(objectiveFunction);
    }

    /**
     * Adapted from
     * https://github.com/apache/commons-math/blob/master/src/main/java/org/apache/commons/math4/analysis/solvers/BrentSolver.java
     * Search for a zero inside the provided interval.
     * This implementation is based on the algorithm described at page 58 of
     * the book
     * <blockquote>
     *  <b>Algorithms for Minimization Without Derivatives</b>,
     *  <it>Richard P. Brent</it>,
     *  Dover 0-486-41998-3
     * </blockquote>
     *
     * @return the value where the function is zero.
     */
    @Override
    public RootFinderOutputData solve(RootFinderInputData rootFinderInputData) {
        IObjectiveFunction f = mObjectiveFunction;
        double a = rootFinderInputData.mLowerPoint;
        double b = rootFinderInputData.mUpperPoint;

        double fa = f.compute(a);
        double fb = f.compute(b);

        if (fa * fb > 0)
            return new RootFinderOutputData(-1, -1, false);
        double c = a;
        double fc = fa;
        double d = b - a;
        double e = d;

        final double t = rootFinderInputData.mAbsTolerance;
        final double eps = 8.0 * rootFinderInputData.mAbsTolerance;

        int n = 2;
        while (n < rootFinderInputData.mMaxIterations) {
            if (Math.abs(fc) < Math.abs(fb)) {
                a = b;
                b = c;
                c = a;
                fa = fb;
                fb = fc;
                fc = fa;
            }

            final double tol = 2 * eps * Math.abs(b) + t;
            final double m = 0.5 * (c - b);

            if (Math.abs(m) <= tol ||
                    almostEqual(fb, 0))  {
                return new RootFinderOutputData(b, n, true);
            }
            if (Math.abs(e) < tol || Math.abs(fa) <= Math.abs(fb)) {
                // Force bisection.
                d = m;
                e = d;
            } else {
                double s = fb / fa;
                double p;
                double q;
                // The equality test (a == c) is intentional,
                // it is part of the original Brent's method and
                // it should NOT be replaced by proximity test.
                if (a == c) {
                    // Linear interpolation.
                    p = 2 * m * s;
                    q = 1 - s;
                } else {
                    // Inverse quadratic interpolation.
                    q = fa / fc;
                    final double r = fb / fc;
                    p = s * (2 * m * q * (q - r) - (b - a) * (r - 1));
                    q = (q - 1) * (r - 1) * (s - 1);
                }
                if (p > 0) {
                    q = -q;
                } else {
                    p = -p;
                }
                s = e;
                e = d;
                if (p >= 1.5 * m * q - Math.abs(tol * q) ||
                        p >= Math.abs(0.5 * s * q)) {
                    // Inverse quadratic interpolation gives a value
                    // in the wrong direction, or progress is slow.
                    // Fall back to bisection.
                    d = m;
                    e = d;
                } else {
                    d = p / q;
                }
            }
            a = b;
            fa = fb;

            if (Math.abs(d) > tol) {
                b += d;
            } else if (m > 0) {
                b += tol;
            } else {
                b -= tol;
            }
            fb = f.compute(b);
            if ((fb > 0 && fc > 0) ||
                    (fb <= 0 && fc <= 0)) {
                c = a;
                fc = fa;
                d = b - a;
                e = d;
            }

            ++n;
        }

        return new RootFinderOutputData(c, n, false);
    }
}

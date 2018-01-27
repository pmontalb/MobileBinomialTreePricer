// Inspired from here:
// https://gist.github.com/jtravs/5327056

package com.a7raiden.qdev.abp.calcs.math;
import com.a7raiden.qdev.abp.calcs.data.RootFinderInputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderOutputData;

import java.io.IOError;
import java.util.function.Function;

/**
 * Created by 7Raiden on 23/01/2018.
 */

public class Toms348RootFinder extends RootFinder {

    public Toms348RootFinder(IObjectiveFunction objectiveFunction) {
        super(objectiveFunction);
    }

    /**
     * the function fzero finds the root of a continuous function within a provided
     * interval [a, b], without requiring derivatives.
     *
     * It is based on algorithm 4.2 described in:
     * G. E. Alefeld, F. A. Potra, and Y. Shi, "Algorithm 748: enclosing zeros of
     * continuous functions," ACM Trans. Math. Softw. 21, 327–344 (1995).
     *
     * Copyright (c) 2013 John C. Travers <jtravs@gmail.com>
     *     Released under the MIT license.
     *
     * NOTE: the code here was derived directly from the algorithms described in the
     * above paper, no ACM copyrighted code was used. Some implementation tips (but
     * no codes) were obtained by reading the Boost C++ sources in math/tools.
     * @return
     */
    @Override
    public RootFinderOutputData solve(RootFinderInputData rootFinderInputData) {
        IObjectiveFunction f = mObjectiveFunction;  // just for readability
        double lambda = .7;
        double mu = .5;

        double a = rootFinderInputData.mLowerPoint;
        double b = rootFinderInputData.mUpperPoint;

        // TODO accept a scalar guess and try to construct our own bracket
        if (a >= b || f.compute(a) * f.compute(b) >= 0)
            return new RootFinderOutputData(-1, -1, false);

        // start with a secant approximation
        double c = secant(a, b);
        double d;
        // re-bracket and check termination
        BracketData bracketData = bracket(a, b, c, rootFinderInputData.mAbsTolerance, lambda);
        if (bracketData.mRootIsFound)
            return new RootFinderOutputData(bracketData.mBracketInfo[0], 1, true) ;
        else {
            a = bracketData.mBracketInfo[0];
            b = bracketData.mBracketInfo[1];
            d = bracketData.mBracketInfo[2];
        }

        double e = 0.0, aStar, bStar, cStar, dStar, ah, bh, ch, dh;
        for (int n = 2; n < rootFinderInputData.mMaxIterations; ++n){
            // use either a cubic (if possible) or quadratic interpolation
            if (n > 2 && areDistinct(a, b, d, e))
                c = inverseCubicInterpolationRoot(a, b, d, e);
            else
                c = newtonQuadratic(a, b, d, 2);

            // re-bracket and check termination
            BracketData newBracketData = bracket(a, b, c, rootFinderInputData.mAbsTolerance, lambda);
            if (newBracketData.mRootIsFound)
                return new RootFinderOutputData(newBracketData.mBracketInfo[0], n, true);
            else {
                aStar = newBracketData.mBracketInfo[0];
                bStar = newBracketData.mBracketInfo[1];
                dStar = newBracketData.mBracketInfo[2];
            }
            double eb = d;
            // use another cubic (if possible) or quadratic interpolation
            if (areDistinct(aStar, bStar, dStar, eb))
                cStar = inverseCubicInterpolationRoot(aStar, bStar, dStar, eb);
            else
                cStar = newtonQuadratic(aStar, bStar, dStar, 3);

            // re-bracket and check termination
            BracketData newerBracketData = bracket(aStar, bStar, cStar, rootFinderInputData.mAbsTolerance, lambda);
            if (newerBracketData.mRootIsFound)
                return new RootFinderOutputData(newerBracketData.mBracketInfo[0], n, true);
            else {
                aStar = newerBracketData.mBracketInfo[0];
                bStar = newerBracketData.mBracketInfo[1];
                dStar = newerBracketData.mBracketInfo[2];
            }

            // double length secant step, if we fail, use bisection
            double u = Math.abs(f.compute(aStar)) < Math.abs(f.compute(bStar)) ? aStar : bStar;

            cStar = u - 2 * f.compute(u) / (f.compute(bStar) - f.compute(aStar)) * (bStar - aStar);
            ch = Math.abs(cStar - u) > (bStar - aStar) / 2 ? aStar + (bStar - aStar) / 2 : cStar;

            // re-bracket and check termination
            BracketData newestBracketData = bracket(aStar, bStar, ch, rootFinderInputData.mAbsTolerance, lambda);
            if (newestBracketData.mRootIsFound)
                return new RootFinderOutputData(newestBracketData.mBracketInfo[0], 1, true);
            else {
                ah = newestBracketData.mBracketInfo[0];
                bh = newestBracketData.mBracketInfo[1];
                dh = newestBracketData.mBracketInfo[2];
            }

            // if not converging fast enough bracket again on a bisection
            if (bh - ah < mu * (b - a)) {
                a = ah;
                b = bh;
                d = dh;
                e = dStar;
            }
            else {
                e = dh;

                BracketData lastBracketData = bracket(ah, bh, ah + (bh - ah) / 2, rootFinderInputData.mAbsTolerance, lambda);
                if (lastBracketData.mRootIsFound)
                    return new RootFinderOutputData(lastBracketData.mBracketInfo[0], 1, true);
                else {
                    a = lastBracketData.mBracketInfo[0];
                    b = lastBracketData.mBracketInfo[1];
                    d = lastBracketData.mBracketInfo[2];
                }
            }
        }

        return new RootFinderOutputData(-1, rootFinderInputData.mMaxIterations, false);
    }

    /**
     * This is a wrapper class used by the bracketing algorithm that tells whether a root has been
     * found or otherwise a new search interval
     */
    private class BracketData {
        boolean mRootIsFound;
        double[] mBracketInfo;

        BracketData(boolean rootIsFound, double[] bracketInfo) {
            mRootIsFound = rootIsFound;
            mBracketInfo = bracketInfo;
        }
    }

    /**
     * bracket the root, based on algorithm on page 341
     * @param a the current bracket with f(a)f(b) < 0
     * @param b the current bracket with f(a)f(b) < 0
     * @param c within (a,b): current best guess of the root
     * @param tolerance stopping criteria
     * @param lambda stopping criteria
     * @return if root is not yet found, return new BracketData(false, [a*, b*, d]) where
     *         d a point not inside [a*, b*]; if d < ab, then f(ab)f(d) > 0, and f(d)f(bb) > 0 otherwise
     *         if root is found, return new BracketData(true, [x0, f(x0)]), where x0 is the root
     */
    BracketData bracket(double a, double b, double c, double tolerance, double lambda) {
        if (!(a <= c && c <= b))
            return new BracketData(false, new double[]{ -1.0 });
        IObjectiveFunction f = mObjectiveFunction;  // just for readability
        double fa = f.compute(a);
        double fb = f.compute(b);
        double delta = lambda * scaledTolerance(a, b, fa, fb, tolerance);

        if ((b - a) <= 4.0 * delta)
            c = (a + b) / 2;
        else if (c <=a + 2.0 * delta)
            c = a + 2.0 * delta;
        else if (c >=b - 2.0 * delta)
            c = b - 2.0 * delta;

        double aStar, bStar, dStar;
        double fc = f.compute(c);
        if (fc == 0)
            return new BracketData(true, new double[]{c, fc});
        else if (Math.signum(fa) * Math.signum(fc) < 0) {
            aStar = a;
            bStar = c;
            dStar = b;
        }
    else {
            aStar = c;
            bStar = b;
            dStar = a;
        }

        double fAstar = f.compute(aStar);
        double fbStar = f.compute(bStar);
        if (bStar - aStar < 2 * scaledTolerance(aStar, bStar, fAstar, fbStar, tolerance)) {
            double x0, fx0;
            if (Math.abs(fAstar) < Math.abs(fbStar)) {
                x0 = aStar;
                fx0 = fAstar;
            }
            else {
                x0 = bStar;
                fx0 = fbStar;
            }
            return new BracketData(true, new double[]{x0, fx0});
        }
        return new BracketData(false, new double[]{aStar, bStar, dStar});
    }

    /**
     * calculate a scaled tolerance based on algorithm on page 340
     * @param a
     * @param b
     * @param fa
     * @param fb
     * @param tolerance
     * @return
     */
    double scaledTolerance(double a, double b, double fa, double fb, double tolerance) {
        double u = Math.abs(fa) < Math.abs(fb) ? Math.abs(a) : Math.abs(b);
        return 2.0 * u * eps + tolerance;
    }

    /**
     * take a secant step, if the resulting guess is very close to a or b, then
     * use bisection instead
     * @param a
     * @param b
     * @return
     */
    double secant(double a, double b) {
        IObjectiveFunction f = mObjectiveFunction;  // just for readability
        double c = a - f.compute(a) / (f.compute(b) - f.compute(a)) * (b - a);

        double tol = eps * 5;
        if (c <= a + Math.abs(a) * tol || c >= b - Math.abs(b) * tol)
            return a + (b - a) / 2;

        return c;
    }

    /**
     * approximate zero of f using quadratic interpolation
     * if the new guess is outside [a, b] we use a secant step instead
     * based on algorithm on page 330
     * @param a
     * @param b
     * @param d
     * @param k
     * @return
     */
    double newtonQuadratic(double a, double b, double d, int k) {
        IObjectiveFunction f = mObjectiveFunction;  // just for readability
        double fa = f.compute(a);
        double fb = f.compute(b);
        double fd = f.compute(d);

        double B = (fb - fa) / (b - a);
        double A = ((fd - fb) / (d - b) - B) / (d - a);
        if (A == 0)
            return secant(a, b);

        double r = A * fa > 0 ? a : b;

        for (int i = 1; i < k; ++i) {
            r -= (fa + (B + A * (r - b)) * (r - a)) / (B + A * (2 * r - a - b));
        }
        if (r <= a || r >= b)
            r = secant(a, b);

        return r;
    }

     /**
     * check that all interpolation values are distinct
     * @param a
     * @param b
     * @param d
     * @param e
     * @return
     */
    boolean areDistinct(double a, double b, double d, double e) {
        IObjectiveFunction f = mObjectiveFunction;  // just for readability
        double f1 = f.compute(a);
        double f2 = f.compute(b);
        double f3 = f.compute(d);
        double f4 = f.compute(e);
        return !(almostEqual(f1, f2) || almostEqual(f1, f3) || almostEqual(f1, f4) ||
                almostEqual(f2, f3) || almostEqual(f2, f4) || almostEqual(f3, f4));
    }

    /**
     * approximate zero of f using inverse cubic interpolation
     * if the new guess is outside [a, b] we use a quadratic step instead
     * based on algorithm on page 333
     * @param a
     * @param b
     * @param c
     * @param d
     * @return
     */
    double inverseCubicInterpolationRoot(double a, double b, double c, double d) {
        IObjectiveFunction f = mObjectiveFunction;  // just for readability
        double fa = f.compute(a);
        double fb = f.compute(b);
        double fc = f.compute(c);
        double fd = f.compute(d);

        double Q11 = (c - d) * fc / (fd - fc);
        double Q21 = (b - c) * fb / (fc - fb);
        double Q31 = (a - b) * fa / (fb - fa);

        double D21 = (b - c) * fc / (fc - fb);
        double D31 = (a - b) * fb / (fb - fa);
        double Q22 = (D21 - Q11) * fb / (fd - fb);

        double Q32 = (D31 - Q21) * fa / (fc - fa);
        double D32 = (D31 - Q21) * fc / (fc - fa);
        double Q33 = (D32 - Q22) * fa / (fd - fa);

        c = a + (Q31 + Q32 + Q33);
        if ((c <= a) || (c >= b))
            c = newtonQuadratic(a, b, d, 3);
        return c;
    }
}

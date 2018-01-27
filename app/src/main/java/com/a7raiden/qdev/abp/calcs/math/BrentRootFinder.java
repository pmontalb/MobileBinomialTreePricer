package com.a7raiden.qdev.abp.calcs.math;

import com.a7raiden.qdev.abp.calcs.data.RootFinderInputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderOutputData;

import java.util.function.Function;

/**
 * Created by 7Raiden on 23/01/2018.
 */

public class BrentRootFinder extends RootFinder {

    public BrentRootFinder(IObjectiveFunction objectiveFunction) {
        super(objectiveFunction);
    }

    /**
     * http://people.sc.fsu.edu/~jburkardt/cpp_src/brent/brent.html
     *
     * @return
     */
    @Override
    public RootFinderOutputData solve(RootFinderInputData rootFinderInputData) {
        double a = rootFinderInputData.mLowerPoint;
        double b = rootFinderInputData.mUpperPoint;
        int maxIterations = rootFinderInputData.mMaxIterations;

        double c;
        double d = 0.0;
        double e;
        double fu;
        double fv;
        double fw;
        double fx;
        double m;
        double p;
        double q;
        double r;
        double sa;
        double sb;
        double t2;
        double tol;
        double u;
        double v;
        double w;
//
//  C is the square of the inverse of the golden ratio.
//
        c = 0.5 * ( 3.0 - Math.sqrt ( 5.0 ) );

        double _eps = Math.sqrt ( eps );

        sa = a;
        sb = b;
        double x = sa + c * ( b - a );
        w = x;
        v = w;
        e = 0.0;
        fx = mObjectiveFunction.compute( x );
        fw = fx;
        fv = fw;

        int n = 1;
        for ( ; ; )
        {
            ++n;

            m = 0.5 * ( sa + sb ) ;
            tol = _eps * Math.abs ( x ) + rootFinderInputData.mAbsTolerance;
            t2 = 2.0 * tol;
//
//  Check the stopping criterion.
//
            if ( Math.abs ( x - m ) <= t2 - 0.5 * ( sb - sa ) )
            {
                break;
            }
//
//  Fit a parabola.
//
            r = 0.0;
            q = r;
            p = q;

            if ( tol < Math.abs ( e ) )
            {
                r = ( x - w ) * ( fx - fv );
                q = ( x - v ) * ( fx - fw );
                p = ( x - v ) * q - ( x - w ) * r;
                q = 2.0 * ( q - r );
                if ( 0.0 < q )
                {
                    p = - p;
                }
                q = Math.abs ( q );
                r = e;
                e = d;
            }

            if ( Math.abs ( p ) < Math.abs ( 0.5 * q * r ) &&
                    q * ( sa - x ) < p &&
                    p < q * ( sb - x ) )
            {
//
//  Take the parabolic interpolation step.
//
                d = p / q;
                u = x + d;
//
//  F must not be evaluated too close to A or B.
//
                if ( ( u - sa ) < t2 || ( sb - u ) < t2 )
                {
                    if ( x < m )
                    {
                        d = tol;
                    }
                    else
                    {
                        d = - tol;
                    }
                }
            }
//
//  A golden-section step.
//
            else
            {
                if ( x < m )
                {
                    e = sb - x;
                }
                else
                {
                    e = sa - x;
                }
                d = c * e;
            }
//
//  F must not be evaluated too close to X.
//
            if ( tol <= Math.abs ( d ) )
            {
                u = x + d;
            }
            else if ( 0.0 < d )
            {
                u = x + tol;
            }
            else
            {
                u = x - tol;
            }

            fu = mObjectiveFunction.compute ( u );
//
//  Update A, B, V, W, and X.
//
            if ( fu <= fx )
            {
                if ( u < x )
                {
                    sb = x;
                }
                else
                {
                    sa = x;
                }
                v = w;
                fv = fw;
                w = x;
                fw = fx;
                x = u;
                fx = fu;
            }
            else
            {
                if ( u < x )
                {
                    sa = u;
                }
                else
                {
                    sb = u;
                }

                if ( fu <= fw || w == x )
                {
                    v = w;
                    fv = fw;
                    w = u;
                    fw = fu;
                }
                else if ( fu <= fv || v == x || v == w )
                {
                    v = u;
                    fv = fu;
                }
            }
        }

        return new RootFinderOutputData(x, n, n == maxIterations);
    }
}

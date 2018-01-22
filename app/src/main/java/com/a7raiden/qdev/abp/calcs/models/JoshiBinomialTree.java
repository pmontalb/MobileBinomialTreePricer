package com.a7raiden.qdev.abp.calcs.models;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.a7raiden.qdev.abp.calcs.data.InputData;

/**
 * Created by 7Raiden on 22/01/2018.
 */

/** M. Joshi
 * https://papers.ssrn.com/sol3/papers.cfm?abstract_id=2277854
 */
public class JoshiBinomialTree extends BinomialDistributionInversionBinomialTree {
    public JoshiBinomialTree(InputData inputData) {
        super(inputData);
    }

    @Override
    protected double binomialDistributionInversion(double d_j, int n){
        double alpha = d_j / (Math.sqrt(8.0));
        double alpha2 = alpha * alpha;
        double alpha3 = alpha * alpha2;
        double alpha5 = alpha3 * alpha2;
        double alpha7 = alpha5 * alpha2;
        double beta = -0.375 * alpha - alpha3;
        double gamma = (5.0 / 6.0) * alpha5 + (13.0 / 12.0) * alpha3 + (25.0 / 128.0) * alpha;
        double delta = -0.1025 * alpha - 0.9285 * alpha3 - 1.43 * alpha5 - 0.5 * alpha7;
        double p = 0.5;

        double sqrtN = Math.sqrt(n);
        double n2 = n * n;
        double n3 = n2 * n;

        p += alpha / sqrtN;
        p += beta / (n * sqrtN);
        p += gamma / (n2 * sqrtN);
        p += delta / (n3 * sqrtN);

        return p;
    }

    @Override
    protected int getNumberOfSteps() {
        return (mInputData.mNodes - 1) / 2;
    }
}

package com.a7raiden.qdev.abp.calcs.data;

/**
 * Created by 7Raiden on 19/01/2018.
 */

public class OutputData {
    public double mPrice;
    public double mDelta;
    public double mGamma;
    public double mVega;

    OutputData(Builder builder) {
        this.mPrice = builder.mPrice;
        this.mDelta = builder.mDelta;
        this.mGamma = builder.mGamma;
        this.mVega = builder.mVega;
    }

    OutputData(OutputData rhs) {
        this.mPrice = rhs.mPrice;
        this.mDelta = rhs.mDelta;
        this.mGamma = rhs.mGamma;
        this.mVega = rhs.mVega;
    }

    public static class Builder {
        double mPrice = 0.0;
        double mDelta = 0.0;
        double mGamma = 0.0;
        double mVega = 0.0;

        public Builder price(double price) {
            this.mPrice = price;
            return this;
        }

        public Builder delta(double delta) {
            this.mDelta = delta;
            return this;
        }

        public Builder gamma(double gamma) {
            this.mGamma = gamma;
            return this;
        }

        public Builder vega(double vega) {
            this.mVega = vega;
            return this;
        }

        public OutputData build() {
            return new OutputData(this);
        }
    }
}

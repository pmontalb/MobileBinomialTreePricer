package com.a7raiden.qdev.abp.calcs.data;

/**
 * Created by 7Raiden on 19/01/2018.
 */

public class InputData {
    /**
     * Spot Price
     */
    public double mSpot;

    /**
     * Strike Price
     */
    public double mStrike;

    /**
     * Risk-free interest rate
     */
    public double mRiskFreeRate;

    /**
     * Carry rate
     */
    public double mCarryRate;

    /**
     * Years to expiry
     */
    public double mExpiry;

    /**
     * Volatility
     */
    public double mVolatility;

    /**
     * Defines the pricing model to instantiate
     */
    public ModelType mModelType;

    // ================================ Tree Data=================================
    /**
     * Number of nodes in the tree
     */
    public int mNodes;

    /**
     * Payoff smoothing: if true, the last Backward Induction step will be smoothed with Black-Scholes
     */
    public boolean mSmoothing;

    /**
     * Acceleration: if true, it tries (depending on interest rates) to smooth the payoff as much as possible
     */
    public boolean mAcceleration;
    // ============================================================================

    public InputData(Builder builder) {
        this.mSpot = builder.mSpot;
        this.mStrike = builder.mStrike;
        this.mRiskFreeRate = builder.mRiskFreeRate;
        this.mCarryRate = builder.mCarryRate;
        this.mExpiry = builder.mExpiry;
        this.mVolatility = builder.mVolatility;
        this.mModelType = builder.mModelType;
        this.mNodes = builder.mNodes;
        this.mSmoothing = builder.mSmoothing;
        this.mAcceleration = builder.mAcceleration;
    }

    public InputData(InputData rhs) {
        this.mSpot = rhs.mSpot;
        this.mStrike = rhs.mStrike;
        this.mRiskFreeRate = rhs.mRiskFreeRate;
        this.mCarryRate = rhs.mCarryRate;
        this.mExpiry = rhs.mExpiry;
        this.mVolatility = rhs.mVolatility;
        this.mModelType = rhs.mModelType;
        this.mNodes = rhs.mNodes;
        this.mSmoothing = rhs.mSmoothing;
        this.mAcceleration = rhs.mAcceleration;
    }

    public static class Builder {
        private double mSpot = 0.0;
        private double mStrike = 0.0;
        private double mRiskFreeRate = 0.0;
        private double mCarryRate = 0.0;
        private double mExpiry = 0.0;
        private double mVolatility = 0.0;
        private ModelType mModelType = ModelType.Null;
        private int mNodes = 0;
        private boolean mSmoothing = false;
        private boolean mAcceleration = false;

        public Builder spot(double spot) {
            this.mSpot = spot;
            return this;
        }
        public Builder strike(double strike) {
            this.mStrike = strike;
            return this;
        }
        public Builder riskFreeRate(double riskFreeRate) {
            this.mRiskFreeRate = riskFreeRate;
            return this;
        }
        public Builder carryRate(double carryRate) {
            this.mCarryRate = carryRate;
            return this;
        }
        public Builder expiry(double expiry) {
            this.mExpiry = expiry;
            return this;
        }
        public Builder volatility(double volatility) {
            this.mVolatility = volatility;
            return this;
        }
        public Builder modelType(ModelType modelType) {
            this.mModelType = modelType;
            return this;
        }
        public Builder nodes(int nodes) {
            this.mNodes = nodes;
            return this;
        }
        public Builder smoothing(boolean smoothing) {
            this.mSmoothing = smoothing;
            return this;
        }
        public Builder acceleration(boolean acceleration) {
            this.mAcceleration = acceleration;
            return this;
        }

        public InputData build() {
            return new InputData(this);
        }
    }
}

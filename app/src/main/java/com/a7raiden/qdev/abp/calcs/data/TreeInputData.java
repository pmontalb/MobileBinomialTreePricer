package com.a7raiden.qdev.abp.calcs.data;

/**
 * Created by 7Raiden on 19/01/2018.
 */

public class TreeInputData extends InputData {
    /**
     * Number of nodes in the tree
     */
    int mNodes;

    /**
     * Payoff smoothing: if true, the last Backward Induction step will be smoothed with Black-Scholes
     */
    boolean mSmoothing;

    /**
     * Acceleration: if true, it tries (depending on interest rates) to smooth the payoff as much as possible
     */
    boolean mAcceleration;

    public TreeInputData(Builder builder) {
        super(builder);
        this.mNodes = builder.mNodes;
        this.mSmoothing = builder.mSmoothing;
        this.mAcceleration = builder.mAcceleration;
    }

    public TreeInputData(TreeInputData rhs) {
        super(rhs);
        this.mNodes = rhs.mNodes;
        this.mSmoothing = rhs.mSmoothing;
        this.mAcceleration = rhs.mAcceleration;
    }

    public static class Builder extends InputData.Builder {
        private int mNodes = 80;
        private boolean mSmoothing = true;
        private boolean mAcceleration = true;

        public Builder nodes(int nodes) {
            this.mNodes= nodes;
            return this;
        }

        public Builder smoothing(boolean smoothing) {
            this.mSmoothing= smoothing;
            return this;
        }

        public Builder acceleration(boolean acceleration) {
            this.mAcceleration= acceleration;
            return this;
        }

        public TreeInputData build() {
            return new TreeInputData(this);
        }
    }
}

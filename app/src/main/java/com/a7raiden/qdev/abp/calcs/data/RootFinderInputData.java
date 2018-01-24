package com.a7raiden.qdev.abp.calcs.data;

/**
 * Created by 7Raiden on 24/01/2018.
 */

public class RootFinderInputData {
    public double mLowerPoint;
    public double mUpperPoint;
    public int mMaxIterations;
    public double mAbsTolerance;
    public RootFinderType mRootFinderType;

    public RootFinderInputData(Builder builder) {
        this.mLowerPoint = builder.mLowerPoint;
        this.mUpperPoint = builder.mUpperPoint;
        this.mMaxIterations = builder.mMaxIterations;
        this.mAbsTolerance = builder.mAbsTolerance;
        this.mRootFinderType = builder.mRootFinderType;
    }

    public RootFinderInputData(RootFinderInputData rhs) {
        this.mLowerPoint = rhs.mLowerPoint;
        this.mUpperPoint = rhs.mUpperPoint;
        this.mMaxIterations = rhs.mMaxIterations;
        this.mAbsTolerance = rhs.mAbsTolerance;
        this.mRootFinderType = rhs.mRootFinderType;
    }

    public static class Builder {
        double mLowerPoint = 0.0;
        double mUpperPoint = 0.0;
        int mMaxIterations = -1;
        double mAbsTolerance = 0.0;
        RootFinderType mRootFinderType = RootFinderType.Null;

        public Builder lowerPoint(double lowerPoint) {
            this.mLowerPoint = lowerPoint;
            return this;
        }
        public Builder upperPoint(double upperPoint) {
            this.mUpperPoint = upperPoint;
            return this;
        }
        public Builder maxIterations(int maxIterations) {
            this.mMaxIterations = maxIterations;
            return this;
        }
        public Builder absTolerance(double absTolerance) {
            this.mAbsTolerance = absTolerance;
            return this;
        }
        public Builder rootFinderType(RootFinderType rootFinderType) {
            this.mRootFinderType = rootFinderType;
            return this;
        }

        public RootFinderInputData build() {
            return new RootFinderInputData(this);
        }
    }
}

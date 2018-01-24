package com.a7raiden.qdev.abp.calcs.data;

/**
 * Created by 7Raiden on 24/01/2018.
 */

public class ImpliedVolatilityInputData {
    public double mTargetPrice;
    public InputData mInputData;
    public RootFinderInputData mRootFinderInputData;
    public OptionType mOptionType;

    ImpliedVolatilityInputData(double targetPrice,
                               InputData inputData,
                               RootFinderInputData rootFinderInputData,
                               OptionType optionType) {
        mTargetPrice = targetPrice;
        mInputData = inputData;
        mRootFinderInputData = rootFinderInputData;
        mOptionType = optionType;
    }

    public ImpliedVolatilityInputData(Builder builder) {
        this.mTargetPrice = builder.mTargetPrice;
        this.mInputData = builder.mInputData;
        this.mRootFinderInputData = builder.mRootFinderInputData;
        this.mOptionType = builder.mOptionType;
    }

    public ImpliedVolatilityInputData(ImpliedVolatilityInputData rhs) {
        this.mTargetPrice = rhs.mTargetPrice;
        this.mInputData = rhs.mInputData;
        this.mRootFinderInputData = rhs.mRootFinderInputData;
        this.mOptionType = rhs.mOptionType;
    }

    public static class Builder {
        double mTargetPrice;
        InputData mInputData;
        RootFinderInputData mRootFinderInputData;
        OptionType mOptionType;

        public Builder targetPrice(double targetPrice) {
            this.mTargetPrice = targetPrice;
            return this;
        }
        public Builder inputData(InputData inputData) {
            this.mInputData = inputData;
            return this;
        }
        public Builder rootFinderInputData(RootFinderInputData rootFinderInputData) {
            this.mRootFinderInputData = rootFinderInputData;
            return this;
        }
        public Builder optionType(OptionType optionType) {
            this.mOptionType = optionType;
            return this;
        }
          public ImpliedVolatilityInputData build() {
            return new ImpliedVolatilityInputData(this);
        }
    }
}

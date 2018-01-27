package com.a7raiden.qdev.abp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.a7raiden.qdev.abp.R;

import com.a7raiden.qdev.abp.adapters.SectionsPagerAdapter;
import com.a7raiden.qdev.abp.calcs.data.ImpliedVolatilityInputData;
import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.ModelType;
import com.a7raiden.qdev.abp.calcs.data.OptionType;
import com.a7raiden.qdev.abp.calcs.data.OutputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderInputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderOutputData;
import com.a7raiden.qdev.abp.calcs.data.RootFinderType;
import com.a7raiden.qdev.abp.calcs.interfaces.IPricingEngine;
import com.a7raiden.qdev.abp.calcs.models.PricingEngine;

import java.text.DecimalFormat;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private boolean isFirstRun = true;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        Button runButton = findViewById(R.id.runButton);
        runButton.setOnClickListener((View view) -> {
            int currentItem = mViewPager.getCurrentItem();
            if (currentItem == 0)
                runBinomialTree();
            else if (currentItem == 1)
                runImpliedVolatility();
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && isFirstRun) {
            isFirstRun = false;
            // fills up the default values
            runBinomialTree();
            runImpliedVolatility();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_help) {
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void runBinomialTree() {
        InputData inputData = getInputData(false);

        setFromPreferences(inputData);

        IPricingEngine pe = PricingEngine.create(inputData);
        OutputData[] americanOutputs = pe.computeAnalytics();

        inputData.mModelType = ModelType.BlackScholes;
        IPricingEngine bs = PricingEngine.create(inputData);
        OutputData[] europeanOutputs = bs.computeAnalytics();
        populateOutputData(americanOutputs[0], americanOutputs[1], europeanOutputs[0], europeanOutputs[1]);
    }

    private void runImpliedVolatility() {
        InputData inputData = getInputData(true);
        setFromPreferences(inputData);

        EditText editText = findViewById(R.id.optionPriceEditText);
        double price = Double.parseDouble(editText.getText().toString());

        RadioButton rb = findViewById(R.id.callRadioButton);
        OptionType optionType = rb.isChecked() ? OptionType.Call : OptionType.Put;

        ImpliedVolatilityInputData impliedVolatilityInputData = new ImpliedVolatilityInputData
                .Builder()
                .targetPrice(price)
                .inputData(inputData)
                .optionType(optionType)
                .rootFinderInputData(new RootFinderInputData.Builder().build())
                .build();

        setFromPreferences(impliedVolatilityInputData);
        RootFinderOutputData americanOutputData = PricingEngine.computeImpliedVolatility(impliedVolatilityInputData);

        inputData.mModelType = ModelType.BlackScholes;
        impliedVolatilityInputData = new ImpliedVolatilityInputData
                .Builder()
                .targetPrice(price)
                .inputData(inputData)
                .optionType(optionType)
                .rootFinderInputData(new RootFinderInputData.Builder().build())
                .build();
        setFromPreferences(impliedVolatilityInputData);
        RootFinderOutputData europeanOutputData = PricingEngine.computeImpliedVolatility(impliedVolatilityInputData);

        populateOutputData(americanOutputData, europeanOutputData);
    }

    private void setFromPreferences(InputData inputData) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Map<String, ?> allEntries = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue().toString();

            if (key.equals(getResources().getString(R.string.nodes_preference_key)))
                inputData.mNodes = Integer.parseInt(val);
            else if (key.equals(getResources().getString(R.string.smoothing_preference_key)))
                inputData.mSmoothing = Boolean.parseBoolean(val);
            else if (key.equals(getResources().getString(R.string.acceleration_preference_key)))
                inputData.mAcceleration = Boolean.parseBoolean(val);
            else if (key.equals(getResources().getString(R.string.models_preference_key)))
                inputData.mModelType = ModelType.parseInt(Integer.parseInt(val.replace("-", "").replace(" ", "")));
        }

        if (allEntries.isEmpty()) {
            inputData.mNodes = 120;
            inputData.mSmoothing = true;
            inputData.mAcceleration = true;
            inputData.mModelType = ModelType.CoxRubinsteinRoss;
        }
    }

    private void setFromPreferences(ImpliedVolatilityInputData inputData) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Map<String, ?> allEntries = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue().toString();

            if (key.equals(getResources().getString(R.string.optimizer_preference_key)))
                inputData.mRootFinderInputData.mRootFinderType =
                        RootFinderType.parseInt(Integer.parseInt(val.replace("-", "").replace(" ", "")));
            if (key.equals(getResources().getString(R.string.iv_tolerance_preference_key)))
                inputData.mRootFinderInputData.mAbsTolerance = Double.parseDouble(val);
            if (key.equals(getResources().getString(R.string.iv_low_preference_key)))
                inputData.mRootFinderInputData.mLowerPoint = 0.01 * Double.parseDouble(val);
            if (key.equals(getResources().getString(R.string.iv_high_preference_key)))
                inputData.mRootFinderInputData.mUpperPoint = 0.01 * Double.parseDouble(val);
            if (key.equals(getResources().getString(R.string.iv_iterations_preference_key)))
                inputData.mRootFinderInputData.mMaxIterations = Integer.parseInt(val);
        }

        if (allEntries.isEmpty()) {
            inputData.mRootFinderInputData.mRootFinderType = RootFinderType.Bisection;
            inputData.mRootFinderInputData.mAbsTolerance = 1e-8;
            inputData.mRootFinderInputData.mLowerPoint = 0.01;
            inputData.mRootFinderInputData.mUpperPoint = 2;
            inputData.mRootFinderInputData.mMaxIterations = 200;
        }
    }

    private InputData getInputData(boolean isImpliedVolView) {
        String[] names = { "spot", "strike", "riskFreeRate", "carryRate", "volatility", "expiry" };
        double[] values = new double[names.length];
        for (int i = 0; i < names.length; ++i) {
            EditText editText = findViewById(getResources().getIdentifier(
                    names[i] + "EditText" + (isImpliedVolView ? "Iv" : ""),
                    "id",
                    this.getApplicationContext().getPackageName()));
            if (editText == null)
                continue; // means that current view is not interested in this item
            values[i] = Double.parseDouble(editText.getText().toString());
        }

        return new InputData.Builder()
                .spot(values[0])
                .strike(values[1])
                .riskFreeRate(values[2] * 0.01)
                .carryRate(values[3] * 0.01)
                .volatility(values[4] * 0.01)
                .expiry(values[5])
                .build();
    }

    private void populateOutputData(
            OutputData americanCallData, OutputData americanPutData,
            OutputData europeanCallData, OutputData europeanPutData) {

        TextView americanCallPrice = findViewById(R.id.outputAmericanCallPriceTextView);
        americanCallPrice.setText(new DecimalFormat("#0.0000").format(americanCallData.mPrice));
        TextView americanPutPrice = findViewById(R.id.outputAmericanPutPriceTextView);
        americanPutPrice.setText(new DecimalFormat("#0.0000").format(americanPutData.mPrice));

        TextView americanCallDelta = findViewById(R.id.outputAmericanCallDeltaTextView);
        americanCallDelta.setText(new DecimalFormat("#0.0000").format(americanCallData.mDelta));
        TextView americanPutDelta = findViewById(R.id.outputAmericanPutDeltaTextView);
        americanPutDelta.setText(new DecimalFormat("#0.0000").format(americanPutData.mDelta));

        TextView americanCallGamma = findViewById(R.id.outputAmericanCallGammaTextView);
        americanCallGamma.setText(new DecimalFormat("#0.0000").format(americanCallData.mGamma));
        TextView americanPutGamma = findViewById(R.id.outputAmericanPutGammaTextView);
        americanPutGamma.setText(new DecimalFormat("#0.0000").format(americanPutData.mGamma));

        TextView americanCallVega = findViewById(R.id.outputAmericanCallVegaTextView);
        americanCallVega.setText(new DecimalFormat("#0.0000").format(americanCallData.mVega));
        TextView americanPutVega = findViewById(R.id.outputAmericanPutVegaTextView);
        americanPutVega.setText(new DecimalFormat("#0.0000").format(americanPutData.mVega));

        TextView europeanCallPrice = findViewById(R.id.outputEuropeanCallPriceTextView);
        europeanCallPrice.setText(new DecimalFormat("#0.0000").format(europeanCallData.mPrice));
        TextView europeanPutPrice = findViewById(R.id.outputEuropeanPutPriceTextView);
        europeanPutPrice.setText(new DecimalFormat("#0.0000").format(europeanPutData.mPrice));

        TextView europeanCallDelta = findViewById(R.id.outputEuropeanCallDeltaTextView);
        europeanCallDelta.setText(new DecimalFormat("#0.0000").format(europeanCallData.mDelta));
        TextView europeanPutDelta = findViewById(R.id.outputEuropeanPutDeltaTextView);
        europeanPutDelta.setText(new DecimalFormat("#0.0000").format(europeanPutData.mDelta));

        TextView europeanCallGamma = findViewById(R.id.outputEuropeanCallGammaTextView);
        europeanCallGamma.setText(new DecimalFormat("#0.0000").format(europeanCallData.mGamma));
        TextView europeanPutGamma = findViewById(R.id.outputEuropeanPutGammaTextView);
        europeanPutGamma.setText(new DecimalFormat("#0.0000").format(europeanPutData.mGamma));

        TextView europeanCallVega = findViewById(R.id.outputEuropeanCallVegaTextView);
        europeanCallVega.setText(new DecimalFormat("#0.0000").format(europeanCallData.mVega));
        TextView europeanPutVega = findViewById(R.id.outputEuropeanPutVegaTextView);
        europeanPutVega.setText(new DecimalFormat("#0.0000").format(europeanPutData.mVega));
    }

    private void populateOutputData(RootFinderOutputData americanData, RootFinderOutputData europeanData) {
        TextView americanIv = findViewById(R.id.outputAmericanImpliedVolatilityTextView);
        americanIv.setText(new DecimalFormat("#0.0000").format(100 * americanData.mRoot));
        TextView europeanIv = findViewById(R.id.outputEuropeanImpliedVolatilityTextView);
        europeanIv.setText(new DecimalFormat("#0.0000").format(100 * europeanData.mRoot));

        TextView americanIter = findViewById(R.id.outputAmericanIterationTextView);
        americanIter.setText(new DecimalFormat("#0").format(americanData.mIterations));
        TextView europeanIter = findViewById(R.id.outputEuropeanIterationTextView);
        europeanIter.setText(new DecimalFormat("#0").format(europeanData.mIterations));
    }
}

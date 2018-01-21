package com.a7raiden.qdev.abp.activities;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.a7raiden.qdev.abp.R;

import com.a7raiden.qdev.abp.adapters.SectionsPagerAdapter;
import com.a7raiden.qdev.abp.calcs.data.InputData;
import com.a7raiden.qdev.abp.calcs.data.ModelType;
import com.a7raiden.qdev.abp.calcs.data.OutputData;
import com.a7raiden.qdev.abp.calcs.interfaces.IPricingEngine;
import com.a7raiden.qdev.abp.calcs.models.PricingEngine;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

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
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IPricingEngine pe = PricingEngine.create(ModelType.BlackScholes, getInputData());
                OutputData[] europeanOutputs = pe.compute();
                populateOutputData(null, null, europeanOutputs[0], europeanOutputs[1]);
            }
        });
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private InputData getInputData() {
        String[] names = { "spot", "strike", "riskFreeRate", "carryRate", "volatility", "expiry" };
        double[] values = new double[names.length];
        for (int i = 0; i < names.length; ++i) {
            EditText editText = findViewById(getResources().getIdentifier(
                    names[i] + "EditText",
                    "id",
                    this.getApplicationContext().getPackageName()));
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
}

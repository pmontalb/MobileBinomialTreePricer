package com.a7raiden.qdev.abp.adapters;

/**
 * Created by 7Raiden on 19/01/2018.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.a7raiden.qdev.abp.fragments.ModelViewFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        return ModelViewFragment.create(position);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}

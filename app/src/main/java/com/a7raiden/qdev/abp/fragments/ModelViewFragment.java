package com.a7raiden.qdev.abp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.a7raiden.qdev.abp.R;

/**
 * Created by 7Raiden on 19/01/2018.
 */

public class ModelViewFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String IDENTIFIER = "";

    public ModelViewFragment() {
    }

    public static ModelViewFragment create(int sectionNumber) {
        ModelViewFragment fragment = new ModelViewFragment();

        Bundle args = new Bundle();
        args.putInt(IDENTIFIER, sectionNumber);

        fragment.setArguments(args);

        return fragment;
    }

    private static int getResourceIdFromIdentifier(int identifier) {
        switch (identifier) {
            case 0:
                return R.layout.pricer_layout;
            case 1:
                return R.layout.implied_volatility_layout;
            default:
                throw new IllegalArgumentException("Identifier " + identifier);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int resourceId = getResourceIdFromIdentifier(getArguments().getInt(IDENTIFIER));
        return inflater.inflate(resourceId, container, false);
    }
}


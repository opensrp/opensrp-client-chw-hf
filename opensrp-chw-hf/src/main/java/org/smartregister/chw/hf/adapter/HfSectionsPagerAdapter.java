package org.smartregister.chw.hf.adapter;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.smartregister.chw.core.fragment.SentMonthlyFragment;
import org.smartregister.chw.hf.fragment.HfDraftMonthlyFragment;

public class HfSectionsPagerAdapter extends FragmentPagerAdapter {
    private Context context;

    public HfSectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return HfDraftMonthlyFragment.newInstance();
            case 1:
                return SentMonthlyFragment.newInstance();
            default:
                break;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(org.smartregister.chw.core.R.string.hia2_draft_monthly);
            case 1:
                return context.getString(org.smartregister.chw.core.R.string.hia2_sent_monthly);
            default:
                break;
        }
        return null;
    }
}

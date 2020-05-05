package org.smartregister.chw.hf.fragment;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import org.smartregister.chw.core.R;
import org.smartregister.chw.core.fragment.DraftMonthlyFragment;
import org.smartregister.chw.hf.activity.HfHIA2ReportsActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class HfDraftMonthlyFragment extends DraftMonthlyFragment {
    public static HfDraftMonthlyFragment newInstance() {
        HfDraftMonthlyFragment fragment = new HfDraftMonthlyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentview = inflater.inflate(R.layout.sent_monthly_fragment, container, false);
        listView = fragmentview.findViewById(R.id.list);
        noDraftsView = fragmentview.findViewById(R.id.empty_view);
        startNewReportEnabled = fragmentview.findViewById(R.id.start_new_report_enabled);
        startNewReportDisabled = fragmentview.findViewById(R.id.start_new_report_disabled);

        return fragmentview;
    }

    @Override
    protected void updateStartNewReportButton(final List<Date> dates) {
        boolean hia2ReportsReady = dates != null && !dates.isEmpty();

        startNewReportEnabled.setVisibility(View.GONE);
        startNewReportDisabled.setVisibility(View.GONE);

        if (hia2ReportsReady) {
            Collections.sort(dates, new Comparator<Date>() {
                @Override
                public int compare(Date lhs, Date rhs) {
                    return rhs.compareTo(lhs);
                }
            });
            startNewReportEnabled.setVisibility(View.GONE);
            startNewReportEnabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateResults(dates, monthClickListener);
                }
            });

        } else {
            startNewReportDisabled.setVisibility(View.GONE);
            startNewReportDisabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    show(Snackbar.make(startNewReportDisabled, getString(R.string.no_monthly_ready), Snackbar.LENGTH_SHORT));
                }
            });
        }
    }


    private void show(final Snackbar snackbar) {
        if (snackbar == null) {
            return;
        }

        float textSize = getActivity().getResources().getDimension(R.dimen.snack_bar_text_size);

        View snackbarView = snackbar.getView();
        snackbarView.setMinimumHeight(Float.valueOf(textSize).intValue());

        TextView textView = snackbarView.findViewById(R.id.snackbar_text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        snackbar.show();

    }

    @Override
    protected void startMonthlyReportForm(Date date) {
        ((HfHIA2ReportsActivity) getActivity()).startMonthlyReportForm("monthly_report", date);
    }

}

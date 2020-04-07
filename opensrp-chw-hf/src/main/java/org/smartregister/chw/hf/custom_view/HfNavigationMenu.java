package org.smartregister.chw.hf.custom_view;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import org.apache.commons.lang3.tuple.Pair;
import org.smartregister.chw.core.activity.HIA2ReportsActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.ProviderStockUsageReportListAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HfNavigationMenu implements NavigationMenu.Flavour {
    private View rootView = null;

    @Override
    public List<Pair<String, Locale>> getSupportedLanguages() {
        return Arrays.asList(Pair.of("English", Locale.ENGLISH), Pair.of("Kiswahili", new Locale("sw")));
    }

    @Override
    public HashMap<String, String> getTableMapValues() {
        HashMap<String, String> tableMap = new HashMap<>();
        tableMap.put(CoreConstants.DrawerMenu.REFERRALS, CoreConstants.TABLE_NAME.TASK);
        return tableMap;
    }

    @Override
    public void registerServiceActivity(Activity activity) {
        View rlIconServiceReport = rootView.findViewById(R.id.rlServiceReport);
        rlIconServiceReport.setVisibility(View.VISIBLE);
        rlIconServiceReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, HIA2ReportsActivity.class);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public void registerStockReport(Activity activity) {
        View rlIconStockReport = rootView.findViewById(R.id.rlIconStockReport);
        rlIconStockReport.setVisibility(View.VISIBLE);
        rlIconStockReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ProviderStockUsageReportListAdapter.class);
                activity.startActivity(intent);
            }
        });
    }
}

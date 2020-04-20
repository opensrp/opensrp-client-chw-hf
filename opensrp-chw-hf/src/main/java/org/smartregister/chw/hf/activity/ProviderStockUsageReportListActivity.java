package org.smartregister.chw.hf.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import org.smartregister.chw.core.utils.StockUsageReportUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.ProviderStockUsageReportListAdapter;
import org.smartregister.chw.hf.dao.HfStockUsageReportDao;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ProviderStockUsageReportListActivity extends SecuredActivity {
    protected AppBarLayout appBarLayout;


    private List<String> getProviderList() {
        StockUsageReportUtils stockUsageReportUtils = new StockUsageReportUtils();
        HfStockUsageReportDao hfStockUsageReportDao = new HfStockUsageReportDao();
        List<String> arrayList = new LinkedList<>();
        List<String> providers = new ArrayList<>();
        arrayList.add(this.getString(R.string.all_chw));

        if (stockUsageReportUtils.getPreviousMonths().size() > 0) {
            for (Map.Entry<String, String> entry : stockUsageReportUtils.getPreviousMonths().entrySet()) {
                providers.addAll(hfStockUsageReportDao.getListOfProviders(stockUsageReportUtils.getMonthNumber(entry.getKey().substring(0, 3)), entry.getValue()));
            }
        }
        arrayList.addAll(new HashSet<>(providers));

        return arrayList;
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_provider_stock_usage_list_report);
        RecyclerView recyclerView = findViewById(R.id.rv_provider_stock_usage_report);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        ProviderStockUsageReportListAdapter providerStockUsageReportListAdapter = new ProviderStockUsageReportListAdapter(getProviderList(), this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(providerStockUsageReportListAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        Toolbar toolbar = findViewById(R.id.back_to_nav_toolbar);
        CustomFontTextView toolBarTextView = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            upArrow.setColorFilter(getResources().getColor(R.color.text_blue), PorterDuff.Mode.SRC_ATOP);
            upArrow.setVisible(true, true);
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setElevation(0);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolBarTextView.setOnClickListener(v -> finish());
        appBarLayout = findViewById(R.id.app_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(null);
        }
    }

    @Override
    protected void onResumption() {
        //override super
    }
}

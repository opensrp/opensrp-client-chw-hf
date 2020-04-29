package org.smartregister.chw.hf.activity;

import android.content.Intent;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.smartregister.chw.core.activity.CoreStockInventoryReportActivity;
import org.smartregister.chw.core.model.MonthStockUsageModel;
import org.smartregister.chw.core.model.StockUsageItemModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.HfStockUsageItemAdapter;
import org.smartregister.chw.hf.dao.HfStockUsageReportDao;
import org.smartregister.chw.hf.utils.HfInnAppUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InAppInventoryReportActivity extends CoreStockInventoryReportActivity implements HfStockUsageItemAdapter.Paginator {
    private String providerName;
    private int currentLoad = 1;
    private int itemsPerPage = 10;
    private List<StockUsageItemModel> items = new ArrayList<>();
    private List<StockUsageItemModel> paginatedItems = new ArrayList<>();
    private HfStockUsageItemAdapter hfStockUsageItemAdapter;

    public static List<String> getItems() {
        return new ArrayList<>(
                Arrays.asList("newpreg_mama_visit", "oldpreg_mama_visit", "total_preg_visit", "pnc_visit", "total_F_visited", "less1m_visit", "1m1yr_visit", "1yr5yr_visit", "total_U5_visit", "hh_visited", "F_referral_hf", "less1m_referral_hf", "1m1yr_referral_hf", "1yr5yr_referral_hf", "total_referral", "no_healthedu_meet", "no_ppl_attend_meet", "F_death_home", "no_maternal_death", "less1m_death_home", "total_less1m_deaths", "1m1yr_death_home", "total_1m1yr_deaths", "1yr5yr_death_home", "total_1yr5yr_deaths", "birth_home", "birth_home_healer", "birth_way_hf", "total_birth_home", "10y14y_new_clients", "10y14y_return_clients", "10y14y_total_clients", "15y19y_new_clients", "15y19y_return_clients", "15y19y_total_clients", "20y24y_new_clients", "20y24y_return_clients", "20y24y_total_clients", "25_new_clients", "25_return_clients", "25_total_clients", "total_new_clients", "total_return_clients", "total_total_clients", "10y14y_pop", "10y14y_coc", "10y14y_emc", "10y14y_total_pills", "15y19y_pop", "15y19y_coc", "15y19y_emc", "15y19y_total_pills", "20y24y_pop", "20y24y_coc", "20y24y_emc", "20y24y_total_pills", "25_pop", "25_coc", "25_emc", "25_total_pills", "total_pop", "total_coc", "total_emc", "total_total_pills", "10y14y_F_mcondom", "10y14y_F_fcondom", "10y14y_total_condoms", "15y19y_F_mcondom", "15y19y_F_fcondom", "15y19y_total_condoms", "20y24y_F_mcondom", "20y24y_F_fcondom", "20y24y_total_condoms", "25_F_mcondom", "25_F_fcondom", "25_total_condoms", "total_F_mcondom", "total_F_fcondom", "total_total_condoms", "10y14y_beads", "15y19y_beads", "20y24y_beads", "25_beads", "total_beads", "10y14y_cousel_ANC", "15y19y_cousel_ANC", "20y24y_cousel_ANC", "25_cousel_ANC", "total_cousel_ANC", "10y14y_cousel_delivery", "15y19y_cousel_delivery", "20y24y_cousel_delivery", "25_cousel_delivery", "total_cousel_delivery", "10y14y_cousel_PNC", "15y19y_cousel_PNC", "20y24y_cousel_PNC", "25_cousel_PNC", "total_cousel_PNC", "10y14y_referral", "15y19y_referral", "20y24y_referral", "25_referral", "total_fp_referral")
        );
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        Intent intent = getIntent();
        providerName = intent.getStringExtra(CoreConstants.HfStockUsageUtil.PROVIDER_NAME);
    }

    @Override
    public List<StockUsageItemModel> getStockUsageItemReportList(String month, String year) {
        List<StockUsageItemModel> stockUsageItemModelsList = new ArrayList<>();
        for (String item : getItems()) {
            String usage = providerName.equalsIgnoreCase(this.getString(R.string.all_chw)) ? HfStockUsageReportDao.getAllProvidersMonthsValue(HfInnAppUtils.getYearMonth(month, year), item) : HfStockUsageReportDao.getProvidersMonthsValue(HfInnAppUtils.getYearMonth(month, year), item, providerName);
            String name = HfInnAppUtils.getStringResource(this, HfStockUsageReportDao.getIndicatorLabels(item));
            stockUsageItemModelsList.add(new StockUsageItemModel(name, "", usage, providerName));
        }
        return stockUsageItemModelsList;
    }


    @Override
    public void reloadRecycler(MonthStockUsageModel selected) {
        String stockMonth = stockUsageReportUtils.getMonthNumber(selected.getMonth().substring(0, 3));
        String stockYear = selected.getYear();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        items = getStockUsageItemReportList(stockMonth, stockYear);
        hfStockUsageItemAdapter = new HfStockUsageItemAdapter(this, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(hfStockUsageItemAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        startPagination();
    }

    private void startPagination() {
        paginatedItems.clear();
        int start = (currentLoad - 1) * itemsPerPage;
        int end = currentLoad * itemsPerPage;

        if (end > items.size())
            end = items.size();

        while (start < end) {
            paginatedItems.add(items.get(start));
            start++;
        }

        hfStockUsageItemAdapter.setStockUsageItemModelList(paginatedItems, this);
        hfStockUsageItemAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean hasPagination() {
        return items.size() > 0;
    }

    @Override
    public int currentPage() {
        return currentLoad;
    }

    @Override
    public int totalPages() {
        return items.size() / itemsPerPage + (items.size() % itemsPerPage > 0 ? 1 : 0);
    }

    @Override
    public boolean hasNext() {
        return currentPage() < totalPages();
    }

    @Override
    public boolean hasPrevious() {
        return currentPage() > 1;
    }

    @Override
    public void onNextNavigation() {
        // fetch next
        if (currentLoad < totalPages()) {
            currentLoad++;
            startPagination();
        }
    }

    @Override
    public void onPreviousNavigation() {
        // fetch previous
        if (currentLoad > 1) {
            currentLoad--;
            startPagination();
        }
    }
}

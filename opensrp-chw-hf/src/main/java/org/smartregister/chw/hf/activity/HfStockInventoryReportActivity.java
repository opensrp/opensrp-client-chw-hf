package org.smartregister.chw.hf.activity;

import android.content.Intent;

import org.smartregister.chw.core.activity.CoreStockInventoryReportActivity;
import org.smartregister.chw.core.dao.StockUsageReportDao;
import org.smartregister.chw.core.model.StockUsageItemModel;

import java.util.ArrayList;
import java.util.List;

public class HfStockInventoryReportActivity extends CoreStockInventoryReportActivity {
    private String providerName;

    @Override
    protected void onCreation() {
        super.onCreation();
        Intent intent = getIntent();
        providerName = intent.getStringExtra("providerName");
    }

    @Override
    public List<StockUsageItemModel> getStockUsageItemReportList(String month, String year) {
        List<StockUsageItemModel> stockUsageItemModelsList = new ArrayList<>();
        StockUsageReportDao stockUsageReportDao = new StockUsageReportDao();
        for (String item : getItems()) {
            String usage = stockUsageReportDao.getStockUsageForMonth(month, item, year, providerName);
            stockUsageItemModelsList.add(new StockUsageItemModel(stockUsageReportUtils.getFormattedItem(item), stockUsageReportUtils.getUnitOfMeasure(item), usage, providerName));
        }
        return stockUsageItemModelsList;
    }
}

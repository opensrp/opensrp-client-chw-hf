package org.smartregister.chw.hf.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.HfStockInventoryReportActivity;

import java.util.List;

public class ProviderStockUsageReportListAdapter extends RecyclerView.Adapter<ProviderStockUsageReportListAdapter.ProviderStockUsageReportListViewHolder> {
    protected LayoutInflater inflater;
    private List<String> providerList;
    private Context context;

    public ProviderStockUsageReportListAdapter(List<String> providerList, Context context) {
        this.providerList = providerList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProviderStockUsageReportListAdapter.ProviderStockUsageReportListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.provider_stock_usage_items, parent, false);
        return new ProviderStockUsageReportListAdapter.ProviderStockUsageReportListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ProviderStockUsageReportListViewHolder holder, int position) {
        String provider = providerList.get(position);
        holder.providerName.setText(context.getString(R.string.return_to, provider));
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String providerName = "providerName";
                Intent intent = new Intent(context, HfStockInventoryReportActivity.class);

                if(position ==  0){
                    intent.putExtra(providerName, "All-CHWs");
                }
                else {
                    intent.putExtra(providerName, provider);
                }
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return providerList.size();
    }

    public static class ProviderStockUsageReportListViewHolder extends RecyclerView.ViewHolder {
        private TextView providerName;
        private View view;

        private ProviderStockUsageReportListViewHolder(View v) {
            super(v);
            view = v;
            providerName = v.findViewById(R.id.provider_name);
        }

    }
}


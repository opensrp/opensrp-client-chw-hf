package org.smartregister.chw.hf.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.HfStockInventoryReportActivity;
import org.smartregister.chw.hf.activity.InAppInventoryReportActivity;

import java.util.List;

public class ProvidersReportListAdapter extends RecyclerView.Adapter<ProvidersReportListAdapter.ProviderStockUsageReportListViewHolder> {
    protected LayoutInflater inflater;
    private List<String> providerList;
    private Context context;
    private String providerType;

    public ProvidersReportListAdapter(List<String> providerList, Context context, String providerType) {
        this.providerList = providerList;
        this.context = context;
        this.providerType = providerType;
    }

    @NonNull
    @Override
    public ProvidersReportListAdapter.ProviderStockUsageReportListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.provider_stock_usage_items, parent, false);
        return new ProvidersReportListAdapter.ProviderStockUsageReportListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ProviderStockUsageReportListViewHolder holder, int position) {
        String provider = providerList.get(position);
        if (providerType.equalsIgnoreCase(CoreConstants.HfInAppUtil.PROVIDER_TYPE)) {
            holder.providerName.setText(context.getString(R.string.provider_service_text, provider));
        }
        else {
            holder.providerName.setText(context.getString(R.string.provider_text, provider));
        }
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String providerName = CoreConstants.HfStockUsageUtil.PROVIDER_NAME;
                Intent intent = getIntent(context,providerType );
                if (position == 0) {
                    intent.putExtra(providerName, context.getString(R.string.all_chw));
                } else {
                    intent.putExtra(providerName, provider);
                }
                context.startActivity(intent);
            }
        });
    }

    protected Intent getIntent(Context activity, String providerType) {
        if(providerType.equalsIgnoreCase(CoreConstants.HfInAppUtil.PROVIDER_TYPE)){
            return new Intent(activity, InAppInventoryReportActivity.class);
        }
        return new Intent(activity, HfStockInventoryReportActivity.class);
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


package org.smartregister.chw.hf.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.chw.core.activity.CoreStockInventoryItemDetailsReportActivity;
import org.smartregister.chw.core.model.StockUsageItemModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;

import java.util.List;

public class ServiceActivityAdapter extends RecyclerView.Adapter<ServiceActivityAdapter.ServiceActivityViewHolder> {
    protected LayoutInflater inflater;
    private List<String> serviceItems;
    private Context context;

    public ServiceActivityAdapter(List<String> serviceItems, Context context) {
        this.serviceItems = serviceItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ServiceActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.service_activity_items, parent, false);
        return new ServiceActivityAdapter.ServiceActivityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceActivityViewHolder holder, int position) {
        String Item = serviceItems.get(position);
        holder.serviceItem.setText(Item);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    String stockName = CoreConstants.HfStockUsageUtil.STOCK_NAME;
                String providerId = CoreConstants.HfStockUsageUtil.PROVIDER_NAME;
                Intent intent = new Intent(context, CoreStockInventoryItemDetailsReportActivity.class);
                intent.putExtra(stockName, usageModelItem.getStockName());
                intent.putExtra(providerId, usageModelItem.getProviderName());
                context.startActivity(intent);*/
            }
        });
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ServiceActivityViewHolder extends RecyclerView.ViewHolder {
        private TextView serviceItem;
        private ImageView goToDetails;
        private View view;

        public ServiceActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            serviceItem = itemView.findViewById(R.id.service_activity_items);
            goToDetails = itemView.findViewById(R.id.go_to_item_details_image_view);
        }
    }
}

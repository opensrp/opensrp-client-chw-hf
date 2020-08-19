package org.smartregister.chw.hf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.smartregister.chw.core.model.StockUsageItemModel;
import org.smartregister.chw.hf.R;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class HfStockUsageItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected LayoutInflater inflater;
    protected List<StockUsageItemModel> stockUsageItemModelList = new ArrayList<>();
    private Context context;
    private int FOOTER = 1;
    private Paginator paginator;

    public HfStockUsageItemAdapter(Context context, Paginator paginator) {
        this.context = context;
        this.paginator = paginator;
    }

    public void setStockUsageItemModelList(List<StockUsageItemModel> stockUsageItemModelList, Paginator paginator) {
        this.stockUsageItemModelList = stockUsageItemModelList;
        this.paginator = paginator;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        inflater = LayoutInflater.from(parent.getContext());
        if (viewType == FOOTER)
            return new HfStockUsageItemAdapter.FooterViewHolder(inflater.inflate(R.layout.smart_register_pagination, parent, false));

        return new HfStockUsageItemAdapter.HfStockUsageReportViewHolder(inflater.inflate(R.layout.stock_usage_report_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HfStockUsageReportViewHolder) {
            StockUsageItemModel usageModelItem = stockUsageItemModelList.get(position);
            onBindViewHolder((HfStockUsageReportViewHolder) holder, usageModelItem);
        } else {
            onBindFooter((FooterViewHolder) holder);
        }
    }

    private void onBindViewHolder(HfStockUsageReportViewHolder holder, StockUsageItemModel model) {
        holder.stockItemName.setText(model.getStockName());
        holder.stockItemCount.setText(String.format("%s", model.getStockValue()));
        holder.stockUnitOfMeasure.setVisibility(View.GONE);
    }

    private void onBindFooter(FooterViewHolder holder) {
        holder.view.setVisibility(paginator.hasPagination() ? View.VISIBLE : View.GONE);

        if (paginator.hasPagination()) {
            holder.textView.setText(
                    MessageFormat.format(context.getString(org.smartregister.R.string.str_page_info), paginator.currentPage(),
                            paginator.totalPages()));

            holder.next.setVisibility(paginator.hasNext() ? View.VISIBLE : View.GONE);
            holder.previous.setVisibility(paginator.hasPrevious() ? View.VISIBLE : View.GONE);

            holder.next.setOnClickListener(v -> paginator.onNextNavigation());
            holder.previous.setOnClickListener(v -> paginator.onPreviousNavigation());
        }
    }

    @Override
    public int getItemCount() {
        return stockUsageItemModelList.size() + 1;
    }

    @Override
    public final int getItemViewType(int position) {
        if (stockUsageItemModelList.size() == position) {
            return FOOTER;
        } else {
            return 0;
        }
    }

    public interface Paginator {
        boolean hasPagination();

        int currentPage();

        int totalPages();

        boolean hasNext();

        boolean hasPrevious();

        void onNextNavigation();

        void onPreviousNavigation();
    }

    public static class HfStockUsageReportViewHolder extends RecyclerView.ViewHolder {
        public View view;
        private TextView stockItemName;
        private TextView stockItemCount;
        private TextView stockUnitOfMeasure;

        private HfStockUsageReportViewHolder(View v) {
            super(v);
            view = v;
            stockItemName = v.findViewById(R.id.stock_name);
            stockItemCount = v.findViewById(R.id.stock_count);
            stockUnitOfMeasure = v.findViewById(R.id.stock_unit_of_measure);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        public View view;
        private View previous;
        private View next;
        private TextView textView;

        public FooterViewHolder(View v) {
            super(v);
            view = v;
            previous = v.findViewById(R.id.btn_previous_page);
            next = v.findViewById(R.id.btn_next_page);
            textView = v.findViewById(R.id.txt_page_info);
        }
    }
}
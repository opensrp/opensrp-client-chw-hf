package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.pmtct.fragment.BaseHvlResultsFragment;
import org.smartregister.chw.pmtct.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.provider.HvlResultsViewProvider;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

import timber.log.Timber;

public class HeiHivResultsViewProvider extends HvlResultsViewProvider {
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    public HeiHivResultsViewProvider(Context context, View.OnClickListener paginationClickListener, View.OnClickListener onClickListener, Set visibleColumns) {
        super(context, paginationClickListener, onClickListener, visibleColumns);
        this.visibleColumns = visibleColumns;
    }

    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, RegisterViewHolder registerViewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) smartRegisterClient;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, registerViewHolder);
        }
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, final RegisterViewHolder viewHolder) {
        try {

            String sampleId = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.CD4_SAMPLE_ID, false);
            String collectionDate = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.CD4_SAMPLE_COLLECTION_DATE, false);
            String hvlResult = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.CD4_RESULT, false);

            if (StringUtils.isBlank(hvlResult)) {
                viewHolder.hvlWrapper.setVisibility(View.GONE);
                viewHolder.dueWrapper.setVisibility(View.VISIBLE);
            } else {
                viewHolder.hvlResult.setText(hvlResult);
                viewHolder.hvlWrapper.setVisibility(View.VISIBLE);
                viewHolder.dueWrapper.setVisibility(View.GONE);
            }

            viewHolder.sampleId.setText(sampleId);
            viewHolder.collectionDate.setText(collectionDate);
            viewHolder.recordHvl.setTag(pc);
            viewHolder.recordHvl.setTag(org.smartregister.pmtct.R.id.VIEW_ID, BaseHvlResultsFragment.CLICK_VIEW_NORMAL);
            viewHolder.recordHvl.setText(R.string.record_cd4);
            viewHolder.resultTitle.setText(R.string.cd4_count);
            viewHolder.recordHvl.setOnClickListener(onClickListener);

        } catch (Exception e) {
            Timber.e(e);
        }
    }
}

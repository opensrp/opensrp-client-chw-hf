package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.fragment.BaseHvlResultsFragment;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.provider.HvlResultsViewProvider;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

import timber.log.Timber;

public class HeiHivResultsViewProvider extends HvlResultsViewProvider {
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;
    private Context context;

    public HeiHivResultsViewProvider(Context context, View.OnClickListener paginationClickListener, View.OnClickListener onClickListener, Set visibleColumns) {
        super(context, paginationClickListener, onClickListener, visibleColumns);
        this.visibleColumns = visibleColumns;
        this.context = context;
    }

    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, RegisterViewHolder registerViewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) smartRegisterClient;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, registerViewHolder);
        }
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, final RegisterViewHolder viewHolder) {
        try {

            String sampleId = Utils.getValue(pc.getColumnmaps(), Constants.DBConstants.HEI_HIV_SAMPLE_ID, false);
            String collectionDate = Utils.getValue(pc.getColumnmaps(), Constants.DBConstants.HEI_HIV_SAMPLE_COLLECTION_DATE, false);
            String hvlResult = Utils.getValue(pc.getColumnmaps(), Constants.DBConstants.HEI_HIV_TEST_RESULT, false);
            String typeOfTest = Utils.getValue(pc.getColumnmaps(), Constants.DBConstants.HEI_HIV_TYPE_OF_TEST, false);
            String testAtAge = HeiDao.getTestAtAgeForFollowupVisit(pc.getCaseId());

            if (StringUtils.isBlank(hvlResult)) {
                viewHolder.hvlWrapper.setVisibility(View.GONE);
                viewHolder.dueWrapper.setVisibility(View.VISIBLE);
            } else {

                if (hvlResult.equalsIgnoreCase("positive")) {
                    viewHolder.hvlResult.setText(context.getString(R.string.hvl_result_positive));
                } else if (hvlResult.equalsIgnoreCase("negative")) {
                    viewHolder.hvlResult.setText(context.getString(R.string.hvl_result_negative));
                } else {
                    viewHolder.hvlResult.setText(hvlResult);
                }

                viewHolder.hvlWrapper.setVisibility(View.VISIBLE);
                viewHolder.dueWrapper.setVisibility(View.GONE);
            }


            TextView tvTestAtAge = viewHolder.itemView.findViewById(R.id.testAtAge);
            if (testAtAge != null && testAtAge.equalsIgnoreCase(Constants.HeiHIVTestAtAge.AT_18_MONTHS)) {
                tvTestAtAge.setText(testAtAge);
                tvTestAtAge.setVisibility(View.VISIBLE);
            }

            viewHolder.sampleId.setText(sampleId);
            viewHolder.collectionDate.setText(collectionDate);
            viewHolder.recordHvl.setTag(pc);
            viewHolder.recordHvl.setTag(org.smartregister.pmtct.R.id.VIEW_ID, BaseHvlResultsFragment.CLICK_VIEW_NORMAL);
            viewHolder.recordHvl.setOnClickListener(onClickListener);

            TextView tvTypeOfTest = viewHolder.itemView.findViewById(R.id.type_of_test);
            tvTypeOfTest.setText(typeOfTest);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater().inflate(R.layout.hei_hiv_results_list_row, parent, false);
        return new RegisterViewHolder(view);
    }
}

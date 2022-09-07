package org.smartregister.chw.hf.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.core.fragment.CoreAncRegisterFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.PregnancyConfirmationViewActivity;
import org.smartregister.chw.hf.model.AncReferralListRegisterRegisterFragmentModel;
import org.smartregister.chw.hf.presenter.HfAncReferralListRegisterFragmentPresenter;
import org.smartregister.chw.hf.provider.HfAncReferralListRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.Task;
import org.smartregister.family.util.DBConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class AncReferralListRegisterFragment extends CoreAncRegisterFragment {

    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);
        ((TextView) view.findViewById(R.id.txt_title_label)).setText(getString(R.string.action_pregnancy_confirmation_referrals));
        view.findViewById(R.id.due_only_layout).setVisibility(android.view.View.GONE);
    }

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        HfAncReferralListRegisterProvider provider = new HfAncReferralListRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, provider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new HfAncReferralListRegisterFragmentPresenter(this, new AncReferralListRegisterRegisterFragmentModel(), null);
    }

    @Override
    protected void openProfile(CommonPersonObjectClient client) {
        Map<String, String> details = fetchCareGiverDetails(Utils.getValue(client.getColumnmaps(), "primary_caregiver", false));
        String taskId = Utils.getValue(client.getColumnmaps(), "_id", false);
        PregnancyConfirmationViewActivity.startPregnancyConfirmationViewActivity(getActivity(), client, getTask(taskId), CoreConstants.REGISTERED_ACTIVITIES.REFERRALS_REGISTER_ACTIVITY, details);
    }

    private Task getTask(String taskId) {
        return HealthFacilityApplication.getInstance().getTaskRepository().getTaskByIdentifier(taskId);
    }

    @Override
    protected void openHomeVisit(CommonPersonObjectClient client) {
        //Not needed on HF
    }

    @Override
    public String getDueCondition() {
        return "";
    }

    private Map<String, String> fetchCareGiverDetails(String careGiverId) {
        Map<String, String> details = new HashMap<>();

        String query = CoreReferralUtils.mainCareGiverSelect(CoreConstants.TABLE_NAME.FAMILY_MEMBER, careGiverId);
        Timber.d("The caregiver query %s", query);
        try (Cursor cursor = getCommonRepository(CoreConstants.TABLE_NAME.FAMILY_MEMBER).rawCustomQueryForAdapter(query)) {
            if (cursor != null && cursor.moveToFirst()) {
                CommonPersonObject personObject = getCommonRepository(CoreConstants.TABLE_NAME.FAMILY_MEMBER).readAllcommonforCursorAdapter(cursor);
                details = personObject.getColumnmaps();
                //pClient.getColumnmaps().putAll(personObject.getColumnmaps());
            }
        } catch (Exception e) {
            Timber.e(e, "AncRegisterFragment --> fetchCareGiverDetails");
        }

        return details;
    }

    private CommonRepository getCommonRepository(String tableName) {
        return Utils.context().commonrepository(tableName);
    }

    private String defaultFilterAndSortQuery() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);

        String query = "";
        String customFilter = getFilterString();
        try {
            sqb.addCondition(customFilter);
            query = sqb.orderbyCondition(Sortqueries);
            query = sqb.Endquery(sqb.addlimitandOffset(query, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset()));
        } catch (Exception e) {
            Timber.e(e);
        }

        return query;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        if (id == LOADER_ID) {
            return new CursorLoader(getActivity()) {
                @Override
                public Cursor loadInBackground() {
                    // Count query
                    final String COUNT = "count_execute";
                    if (args != null && args.getBoolean(COUNT)) {
                        countExecute();
                    }
                    String query = defaultFilterAndSortQuery();
                    return commonRepository().rawCustomQueryForAdapter(query);
                }
            };
        }
        return super.onCreateLoader(id, args);
    }

    @Override
    public void countExecute() {
        Cursor cursor = null;
        try {

            String query = "select count(*) from " + presenter().getMainTable() +
                    " INNER JOIN " + CoreConstants.TABLE_NAME.TASK + " ON  " + presenter().getMainTable() + "." + org.smartregister.family.util.DBConstants.KEY.BASE_ENTITY_ID + " = " + CoreConstants.TABLE_NAME.TASK + "." + CoreConstants.DB_CONSTANTS.FOR + " COLLATE NOCASE " +
                    " INNER JOIN " + CoreConstants.TABLE_NAME.FAMILY + " ON  " + CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + org.smartregister.family.util.DBConstants.KEY.RELATIONAL_ID + " = " + CoreConstants.TABLE_NAME.FAMILY + "." + org.smartregister.family.util.DBConstants.KEY.BASE_ENTITY_ID + " COLLATE NOCASE " +
                    " LEFT JOIN " + CoreConstants.TABLE_NAME.ANC_MEMBER + " ON " + CoreConstants.TABLE_NAME.TASK + "." + CoreConstants.DB_CONSTANTS.FOR + " = " + CoreConstants.TABLE_NAME.ANC_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID + " COLLATE NOCASE " +
                    " where " + presenter().getMainCondition();

            if (StringUtils.isNotBlank(filters))
                query = query + getFilterString();

            cursor = commonRepository().rawCustomQueryForAdapter(query);
            cursor.moveToFirst();
            clientAdapter.setTotalcount(cursor.getInt(0));
            Timber.v("total count here %d", clientAdapter.getTotalcount());

            clientAdapter.setCurrentlimit(20);
            clientAdapter.setCurrentoffset(0);


        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}

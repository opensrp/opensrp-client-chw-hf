package org.smartregister.chw.hf.fragment;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.fragment.CoreHivRegisterFragment;
import org.smartregister.chw.core.provider.CoreHivProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.QueryBuilder;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.HivProfileActivity;
import org.smartregister.chw.hf.activity.HtsRegisterActivity;
import org.smartregister.chw.hf.dao.HfHtsDao;
import org.smartregister.chw.hf.model.HtsRegisterFragmentModel;
import org.smartregister.chw.hf.presenter.HtsRegisterFragmentPresenter;
import org.smartregister.chw.hf.provider.HfHivRegisterProvider;
import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import timber.log.Timber;

public class HtsRegisterFragment extends CoreHivRegisterFragment {
    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);
        ((CustomFontTextView) view.findViewById(R.id.txt_title_label)).setText(getString(R.string.hts_clients));
    }

    @Override
    public void initializeAdapter(@Nullable Set<? extends View> visibleColumns) {
        CoreHivProvider hivRegisterProvider = new HfHivRegisterProvider(getActivity(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, hivRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        String viewConfigurationIdentifier = null;
        try {
            viewConfigurationIdentifier = ((HtsRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        } catch (NullPointerException e) {
            Timber.e(e);
        }
        presenter = new HtsRegisterFragmentPresenter(this, new HtsRegisterFragmentModel(), viewConfigurationIdentifier);
    }


    @NonNull
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


    private String defaultFilterAndSortQuery() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);

        String query = "";
        StringBuilder customFilter = new StringBuilder();
        if (StringUtils.isNotBlank(filters)) {
            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.FIRST_NAME, filters));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.LAST_NAME, filters));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.MIDDLE_NAME, filters));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.UNIQUE_ID, filters));

        }
        if (dueFilterActive) {
            customFilter.append(MessageFormat.format(" and ( {0}) ", presenter().getDueFilterCondition()));
        }
        try {
            if (isValidFilterForFts(commonRepository())) {

                String myquery = QueryBuilder.getQuery(joinTables, mainCondition, tablename, customFilter.toString(), clientAdapter, Sortqueries);
                List<String> ids = commonRepository().findSearchIds(myquery);
                query = sqb.toStringFts(ids, tablename, CommonRepository.ID_COLUMN,
                        Sortqueries);
                query = sqb.Endquery(query);
            } else {
                sqb.addCondition(customFilter.toString());
                query = sqb.orderbyCondition(Sortqueries);
                query = sqb.Endquery(sqb.addlimitandOffset(query, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset()));

            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return query;
    }

    @Override
    protected void openProfile(CommonPersonObjectClient client) {
        if (getActivity() != null)
            HivProfileActivity.startHivProfileActivity(getActivity(), Objects.requireNonNull(HfHtsDao.getMember(client.getCaseId())), R.string.hts_return_to_previous_page);
    }


    @Override
    protected void openFollowUpVisit(@Nullable HivMemberObject hivMemberObject) {

    }
}



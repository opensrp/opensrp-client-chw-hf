package org.smartregister.chw.hf.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.QueryBuilder;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.HeiProfileActivity;
import org.smartregister.chw.hf.custom_view.FacilityMenu;
import org.smartregister.chw.hf.model.HeiRegisterFragmentModel;
import org.smartregister.chw.hf.presenter.HeiRegisterFragmentPresenter;
import org.smartregister.chw.hf.provider.HeiRegisterProvider;
import org.smartregister.chw.pmtct.fragment.BasePmtctRegisterFragment;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import androidx.appcompat.widget.Toolbar;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import timber.log.Timber;

public class HeiRegisterFragment extends BasePmtctRegisterFragment {
    private View view;

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        HeiRegisterProvider heiRegisterProvider = new HeiRegisterProvider(getActivity(), paginationViewHandler, registerActionHandler, visibleColumns);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, heiRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public void setupViews(View view) {
        initializePresenter();
        super.setupViews(view);
        this.view = view;

        Toolbar toolbar = view.findViewById(org.smartregister.R.id.register_toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setContentInsetsRelative(0, 0);
        toolbar.setContentInsetStartWithNavigation(0);

        try {
            FacilityMenu.getInstance(getActivity(), null, toolbar);
        } catch (NullPointerException e) {
            Timber.e(e);
        }
        View navbarContainer = view.findViewById(org.smartregister.chw.core.R.id.register_nav_bar_container);
        navbarContainer.setFocusable(false);

        CustomFontTextView titleView = view.findViewById(org.smartregister.chw.core.R.id.txt_title_label);
        if (titleView != null) {
            titleView.setText(getString(R.string.menu_hei));
            titleView.setPadding(0, titleView.getTop(), titleView.getPaddingRight(), titleView.getPaddingBottom());
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        View searchBarLayout = view.findViewById(org.smartregister.chw.core.R.id.search_bar_layout);
        searchBarLayout.setLayoutParams(params);
        searchBarLayout.setBackgroundResource(org.smartregister.chw.core.R.color.chw_primary);
        searchBarLayout.setPadding(searchBarLayout.getPaddingLeft(), searchBarLayout.getPaddingTop(), searchBarLayout.getPaddingRight(), (int) Utils.convertDpToPixel(10, getActivity()));

        View topLeftLayout = view.findViewById(org.smartregister.chw.core.R.id.top_left_layout);
        topLeftLayout.setVisibility(View.GONE);

        View topRightLayout = view.findViewById(org.smartregister.chw.core.R.id.top_right_layout);
        topRightLayout.setVisibility(View.VISIBLE);

        View sortFilterBarLayout = view.findViewById(org.smartregister.chw.core.R.id.register_sort_filter_bar_layout);
        sortFilterBarLayout.setVisibility(View.GONE);

        View filterSortLayout = view.findViewById(org.smartregister.chw.core.R.id.filter_sort_layout);
        filterSortLayout.setVisibility(View.GONE);

        View dueOnlyLayout = view.findViewById(org.smartregister.chw.core.R.id.due_only_layout);
        dueOnlyLayout.setVisibility(View.GONE);
        dueOnlyLayout.setOnClickListener(registerActionHandler);
        if (getSearchView() != null) {
            getSearchView().setBackgroundResource(org.smartregister.family.R.color.white);
            getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.family.R.drawable.ic_action_search, 0, 0, 0);
            getSearchView().setTextColor(getResources().getColor(org.smartregister.chw.core.R.color.text_black));
        }
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        String viewConfigurationIdentifier = null;
        try {
            viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        } catch (NullPointerException e) {
            Timber.e(e);
        }
        presenter = new HeiRegisterFragmentPresenter(this, new HeiRegisterFragmentModel(), viewConfigurationIdentifier);
    }


    @Override
    public void onResume() {
        super.onResume();

        Toolbar toolbar = view.findViewById(org.smartregister.R.id.register_toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setContentInsetsRelative(0, 0);
        toolbar.setContentInsetStartWithNavigation(0);
        FacilityMenu.getInstance(getActivity(), null, toolbar);
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
            Log.e(getClass().getName(), e.toString(), e);
        }

        return query;
    }


    @Override
    public void countExecute() {
        Cursor c = null;
        try {

            String query = "select count(*) from " + presenter().getMainTable() + " inner join " + CoreConstants.TABLE_NAME.FAMILY_MEMBER +
                    " on " + presenter().getMainTable() + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " +
                    CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID +
                    " where " + presenter().getMainCondition();

            if (StringUtils.isNotBlank(filters)) {
                query = query + " and ( " + filters + " ) ";
            }


            c = commonRepository().rawCustomQueryForAdapter(query);
            c.moveToFirst();
            clientAdapter.setTotalcount(c.getInt(0));
            Timber.v("total count here %s", clientAdapter.getTotalcount());

            clientAdapter.setCurrentlimit(20);
            clientAdapter.setCurrentoffset(0);

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
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
    protected void openProfile(String baseEntityId) {
        HeiProfileActivity.startProfile(requireActivity(), baseEntityId);
    }
}

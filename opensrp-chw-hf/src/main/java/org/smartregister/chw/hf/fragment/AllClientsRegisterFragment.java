package org.smartregister.chw.hf.fragment;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.smartregister.chw.hf.provider.HfAllClientsRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.fragment.BaseOpdRegisterFragment;
import org.smartregister.opd.utils.ConfigurationInstancesHelper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AllClientsRegisterFragment extends BaseOpdRegisterFragment {

    private HfAllClientsRegisterProvider opdRegisterQueryProvider;

    public AllClientsRegisterFragment() {
        super();
        opdRegisterQueryProvider = (HfAllClientsRegisterProvider) ConfigurationInstancesHelper.newInstance(OpdLibrary.getInstance().getOpdConfiguration().getOpdRegisterQueryProvider());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    protected void startRegistration() {

    }

    @Override
    protected void performPatientAction(@NonNull CommonPersonObjectClient commonPersonObjectClient) {

    }

    @Override
    protected void goToClientDetailActivity(@NonNull CommonPersonObjectClient commonPersonObjectClient) {

    }

    @NotNull
    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {

        List<Cursor> allClientCursors = new LinkedList<>();

        if (id == LOADER_ID && getActivity() != null) {// Returns a new CursorLoader
            return new CursorLoader(getActivity()) {
                @Override
                public Cursor loadInBackground() {
                    return getMergedCursor(allClientCursors);
                }
            };
        }// An invalid id was passed in
        return new CursorLoader(getActivity());
    }

    @NotNull
    private Cursor getMergedCursor(List<Cursor> allClientCursors) {
        String childRegisterQuery = opdRegisterQueryProvider.getChildRegisterQuery().replace("%s", getObjectIds());
        Cursor childCursor = commonRepository().rawCustomQueryForAdapter(childRegisterQuery);

        String ancRegisterQuery = opdRegisterQueryProvider.getANCRegisterQuery().replace("%s", getObjectIds());
        Cursor ancCursor = commonRepository().rawCustomQueryForAdapter(ancRegisterQuery);

        String pncRegisterQuery = opdRegisterQueryProvider.getPNCRegisterQuery().replace("%s", getObjectIds());
        Cursor pncCursor = commonRepository().rawCustomQueryForAdapter(pncRegisterQuery);

        allClientCursors.addAll(Arrays.asList(pncCursor));
        return new MergeCursor(allClientCursors.toArray(new Cursor[0]));
    }

    public String getObjectIds() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);
        String joinedIds = null;
        if (isValidFilterForFts(commonRepository())) {
            String sql = opdRegisterQueryProvider.getObjectIdsQuery(filters);
            sql = sqb.addlimitandOffset(sql, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset());

            List<String> ids = commonRepository().findSearchIds(sql);
            joinedIds = "'" + StringUtils.join(ids, "','") + "'";
        }
        return joinedIds;
    }
}

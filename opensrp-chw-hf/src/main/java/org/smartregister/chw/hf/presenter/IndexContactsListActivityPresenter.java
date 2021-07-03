package org.smartregister.chw.hf.presenter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.hf.activity.HivIndexContactProfileActivity;
import org.smartregister.chw.hf.activity.HivProfileActivity;
import org.smartregister.chw.hf.activity.IndexContactsListActivity;
import org.smartregister.chw.hiv.contract.BaseIndexClientsContactListContract;
import org.smartregister.chw.hiv.dao.HivDao;
import org.smartregister.chw.hiv.domain.HivIndexContactObject;
import org.smartregister.chw.hiv.presenter.BaseHivIndexContactsListPresenter;

import java.util.Objects;

public class IndexContactsListActivityPresenter extends BaseHivIndexContactsListPresenter {
    public IndexContactsListActivityPresenter(@NotNull String hivClientBaseEntityId, @NotNull BaseIndexClientsContactListContract.Interactor interactor, @NotNull BaseIndexClientsContactListContract.View view) {
        super(hivClientBaseEntityId, interactor, view);
    }

    @Override
    public void openIndexContactProfile(@Nullable HivIndexContactObject hivIndexContactObject) {
        if (hivIndexContactObject != null) {
            if (HivDao.isRegisteredForHiv(hivIndexContactObject.getBaseEntityId()))
                HivProfileActivity.startHivProfileActivity(((IndexContactsListActivity) getView()), HivDao.getMember(Objects.requireNonNull(hivIndexContactObject.getBaseEntityId())));
            else
                HivIndexContactProfileActivity.startHivIndexContactProfileActivity(((IndexContactsListActivity) getView()), hivIndexContactObject);
        }

    }
}
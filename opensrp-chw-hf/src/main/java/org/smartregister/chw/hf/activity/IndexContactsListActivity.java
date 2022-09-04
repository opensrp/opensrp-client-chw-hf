package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hiv.util.Constants.ActivityPayload.HIV_MEMBER_OBJECT;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.hf.presenter.IndexContactsListActivityPresenter;
import org.smartregister.chw.hiv.activity.BaseIndexContactsListActivity;
import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.chw.hiv.interactor.BaseHivIndexContactsListInteractor;

public class IndexContactsListActivity extends BaseIndexContactsListActivity {

    public static void startHivClientIndexListActivity(Activity activity, HivMemberObject memberObject) {
        Intent intent = new Intent(activity, IndexContactsListActivity.class);
        intent.putExtra(HIV_MEMBER_OBJECT, memberObject);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initializePresenter() {
        setPresenter(new IndexContactsListActivityPresenter(getMemberObject().getBaseEntityId(), new BaseHivIndexContactsListInteractor(new AppExecutors()), this));
    }


}
 
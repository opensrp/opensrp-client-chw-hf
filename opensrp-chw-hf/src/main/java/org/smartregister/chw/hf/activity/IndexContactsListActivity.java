package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;

import org.smartregister.chw.anc.util.AppExecutors;
import org.smartregister.chw.hiv.activity.BaseIndexContactsListActivity;
import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.chw.hiv.interactor.BaseHivIndexContactsListInteractor;
import org.smartregister.chw.hiv.presenter.BaseHivIndexcContactsListPresenter;
import org.smartregister.chw.hiv.util.Constants;

import timber.log.Timber;

public class IndexContactsListActivity extends BaseIndexContactsListActivity {

    public static void startHivClientIndexListActivity(Activity activity, HivMemberObject memberObject) {
        Intent intent = new Intent(activity, IndexContactsListActivity.class);
        intent.putExtra(Constants.HivMemberObject.MEMBER_OBJECT, memberObject);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.e("nimepokea "+new Gson().toJson(getIntent().getSerializableExtra("coze")));
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initializePresenter() {
        setPresenter(new BaseHivIndexcContactsListPresenter("3956c8e4-576c-4d56-9648-082a12b31032", new BaseHivIndexContactsListInteractor(new AppExecutors()), this));
    }


}
 
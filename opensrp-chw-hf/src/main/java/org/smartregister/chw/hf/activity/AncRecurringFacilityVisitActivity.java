package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.presenter.BaseAncHomeVisitPresenter;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.AncRecurringFacilityVisitInteractor;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ilakozejumanne@gmail.com
 * 17-10-2021
 */
public class AncRecurringFacilityVisitActivity extends AncFirstFacilityVisitActivity {

    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode) {
        Intent intent = new Intent(activity, AncRecurringFacilityVisitActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.EDIT_MODE, isEditMode);
        activity.startActivityForResult(intent, org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT);
    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseAncHomeVisitPresenter(memberObject, this, new AncRecurringFacilityVisitInteractor(baseEntityID));
    }

    @Override
    public void redrawHeader(MemberObject memberObject) {
        tvTitle.setText(MessageFormat.format("{0}, {1} \u00B7 {2}", memberObject.getFullName(), memberObject.getAge(), getString(org.smartregister.chw.hf.R.string.anc_followup_visit)));
    }


    @Override
    public void initializeActions(LinkedHashMap<String, BaseAncHomeVisitAction> map) {
        actionList.clear();

        //Necessary evil to rearrange the actions according to a specific arrangement
        if (map.containsKey(getString(R.string.anc_recuring_visit_pregnancy_status))) {
            BaseAncHomeVisitAction pregnancyStatusAncHomeVisitAction = map.get(getString(R.string.anc_recuring_visit_pregnancy_status));
            actionList.put(getString(R.string.anc_recuring_visit_pregnancy_status), pregnancyStatusAncHomeVisitAction);
        }
        //====================End of Necessary evil ====================================

        for (Map.Entry<String, BaseAncHomeVisitAction> entry : map.entrySet()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                actionList.putIfAbsent(entry.getKey(), entry.getValue());
            } else {
                actionList.put(entry.getKey(), entry.getValue());
            }
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        displayProgressBar(false);
    }
}

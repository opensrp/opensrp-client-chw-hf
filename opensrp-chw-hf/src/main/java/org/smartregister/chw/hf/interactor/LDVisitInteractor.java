package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.LDDao;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.interactor.BaseLDVisitInteractor;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import java.util.LinkedHashMap;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-05-06
 */
public class LDVisitInteractor extends BaseLDVisitInteractor {
    protected Context context;
    final LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();

    @Override
    public void calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) {

        context = view.getContext();

        final Runnable runnable = () -> {
            // update the local database incase of manual date adjustment
            try {
                VisitUtils.processVisits(memberObject.getBaseEntityId());
            } catch (Exception e) {
                Timber.e(e);
            }

            try {

                evaluateGenExamination();
                evaluateVaginalExamination();

            } catch (BaseLDVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateVaginalExamination() throws BaseLDVisitAction.ValidationException {

        BaseLDVisitAction action = getBuilder("Vaginal examination")
                .withOptional(false)
                .withFormName(Constants.JsonForm.LDVisit.getLdVaginalExamination())
                .build();

        actionList.put("Vaginal examination", action);

    }

    private void evaluateGenExamination() throws BaseLDVisitAction.ValidationException {

        BaseLDVisitAction action = getBuilder("General examination")
                .withOptional(false)
                .withFormName(Constants.JsonForm.LDVisit.getLdGeneralExamination())
                .build();

        actionList.put("General examination", action);
    }

    @Override
    public MemberObject getMemberClient(String memberID) {

        return LDDao.getLDMember(memberID);
    }

    public BaseLDVisitAction.Builder getBuilder(String title) {
        return new BaseLDVisitAction.Builder(context, title);
    }

}

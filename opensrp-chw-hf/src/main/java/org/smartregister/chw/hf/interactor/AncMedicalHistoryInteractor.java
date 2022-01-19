package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.anc.contract.BaseAncMedicalHistoryContract;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.CoreBaseAncMedicalHistoryInteractor;

import java.util.ArrayList;
import java.util.List;

public class AncMedicalHistoryInteractor extends CoreBaseAncMedicalHistoryInteractor {
    @Override
    public void getMemberHistory(final String memberID, final Context context, final BaseAncMedicalHistoryContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {

            String[] eventTypes = new String[2];
            eventTypes[0] = "ANC First Facility Visit";
            eventTypes[1] = "ANC Recurring Facility Visit";
            List<Visit> visits = VisitUtils.getVisits(memberID, eventTypes);
            final List<Visit> all_visits = new ArrayList<>(visits);

            for (Visit visit : visits) {
                List<Visit> child_visits = VisitUtils.getChildVisits(visit.getVisitId());
                all_visits.addAll(child_visits);
            }
            appExecutors.mainThread().execute(() -> callBack.onDataFetched(all_visits));
        };

        appExecutors.diskIO().execute(runnable);
    }
}

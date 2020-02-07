package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.joda.time.LocalDate;
import org.smartregister.chw.core.interactor.CoreFamilyPlanningProfileInteractor;
import org.smartregister.chw.fp.contract.BaseFpProfileContract;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;

import java.util.Date;

public class HfFamilyPlanningProfileInteractor extends CoreFamilyPlanningProfileInteractor {
    private Context context;

    public HfFamilyPlanningProfileInteractor(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void updateProfileFpStatusInfo(FpMemberObject memberObject, BaseFpProfileContract.InteractorCallback callback) {
        Runnable runnable = new Runnable() {

            Date lastVisitDate = getLastVisitDate(memberObject);
            Alert upcomingService = getAlerts(context, memberObject.getBaseEntityId());

            @Override
            public void run() {
                appExecutors.mainThread().execute(() -> {
                    callback.refreshLastVisit(lastVisitDate);
                    if (upcomingService == null) {
                        callback.refreshUpComingServicesStatus("", AlertStatus.complete, new Date());
                    } else {
                        callback.refreshUpComingServicesStatus(upcomingService.scheduleName(), upcomingService.status(), new LocalDate(upcomingService.startDate()).toDate());
                    }
                });
            }
        };
        appExecutors.diskIO().execute(runnable);
    }
}

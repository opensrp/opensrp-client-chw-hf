package org.smartregister.chw.hf.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.fp.contract.BaseFpRegisterContract;
import org.smartregister.chw.fp.interactor.BaseFpRegisterInteractor;
import org.smartregister.chw.fp.util.AppExecutors;
import org.smartregister.chw.hf.utils.HFFamilyPlanningUtil;

public class HFFamilyPlanningRegisterInteractor extends BaseFpRegisterInteractor {

    private AppExecutors appExecutors;

    @VisibleForTesting
    HFFamilyPlanningRegisterInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public HFFamilyPlanningRegisterInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void saveRegistration(final String jsonString, final BaseFpRegisterContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            try {
                HFFamilyPlanningUtil.saveFormEvent(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }

            appExecutors.mainThread().execute(() -> callBack.onRegistrationSaved());
        };
        appExecutors.diskIO().execute(runnable);
    }
}

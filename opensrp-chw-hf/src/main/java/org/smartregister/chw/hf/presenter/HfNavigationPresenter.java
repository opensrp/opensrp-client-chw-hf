package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.core.contract.CoreApplication;
import org.smartregister.chw.core.contract.NavigationContract;
import org.smartregister.chw.core.model.NavigationModel;
import org.smartregister.chw.core.presenter.NavigationPresenter;
import org.smartregister.chw.hf.interactor.HfNavigationInteractor;

import java.lang.ref.WeakReference;

public class HfNavigationPresenter extends NavigationPresenter {
    public HfNavigationPresenter(CoreApplication application, NavigationContract.View view, NavigationModel.Flavor modelFlavor) {
        super(application, view, modelFlavor);
        mView = new WeakReference<>(view);

        mInteractor = HfNavigationInteractor.getInstance();
        mInteractor.setApplication(application);

        mModel = NavigationModel.getInstance();
        mModel.setNavigationFlavor(modelFlavor);

        initialize();
    }
}

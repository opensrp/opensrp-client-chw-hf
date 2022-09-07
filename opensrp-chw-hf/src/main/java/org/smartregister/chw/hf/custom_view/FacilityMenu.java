package org.smartregister.chw.hf.custom_view;

import android.app.Activity;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.hf.presenter.HfNavigationPresenter;

import timber.log.Timber;

public class FacilityMenu extends NavigationMenu {

    @Override
    protected void init(Activity activity, View myParentView, Toolbar myToolbar) {
        try {
            setParentView(activity, parentView);
            toolbar = myToolbar;
            parentView = myParentView;
            mPresenter = new HfNavigationPresenter(application, this, modelFlavor);
            prepareViews(activity);
            mPresenter.updateTableMap(menuFlavor.getTableMapValues());
        } catch (Exception e) {
            Timber.e(e);
        }
    }

}

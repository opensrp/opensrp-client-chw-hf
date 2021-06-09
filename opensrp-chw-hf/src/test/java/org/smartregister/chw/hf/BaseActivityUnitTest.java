package org.smartregister.chw.hf;

import android.app.Activity;
import android.content.Intent;

import org.junit.Assert;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;

import timber.log.Timber;

public abstract class BaseActivityUnitTest extends BaseUnitTest {

    protected void destroyController() {
        try {
            getActivity().finish();
            getActivityController().pause().stop().destroy(); //destroy controller if we can

        } catch (Exception e) {
            Timber.e(e);
        }
        System.gc();
    }

    protected abstract Activity getActivity();

    protected abstract ActivityController getActivityController();

    protected void assertActivityStarted(Activity currActivity, Activity nextActivity) {
        Intent expectedIntent = new Intent(currActivity, nextActivity.getClass());
        Intent actual = ShadowApplication.getInstance().getNextStartedActivity();
        Assert.assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }
}

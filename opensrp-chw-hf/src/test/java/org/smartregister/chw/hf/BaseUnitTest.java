package org.smartregister.chw.hf;

import android.os.Build;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith (RobolectricTestRunner.class)
@Config(application = HealthFacilityApplication.class, sdk = Build.VERSION_CODES.P)
public abstract class BaseUnitTest {
    protected static final String DUMMY_USERNAME = "myusername";
    protected static final String DUMMY_PASSWORD = "mypassword";
}

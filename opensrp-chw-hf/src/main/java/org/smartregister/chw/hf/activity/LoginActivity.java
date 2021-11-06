package org.smartregister.chw.hf.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.presenter.LoginPresenter;
import org.smartregister.family.util.Constants;
import org.smartregister.task.SaveTeamLocationsTask;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;


public class LoginActivity extends BaseLoginActivity implements BaseLoginContract.View {
    public static final String TAG = BaseLoginActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView imageView = findViewById(R.id.login_logo);
        if (BuildConfig.BUILD_FOR_BORESHA_AFYA_SOUTH) {
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_logo));
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_logo_ba));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoginPresenter.processViewCustomizations();
        if (!mLoginPresenter.isUserLoggedOut()) {
            goToHome(false);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initializePresenter() {
        mLoginPresenter = new LoginPresenter(this);
    }

    @Override
    public void goToHome(boolean remote) {
        if (remote) {
            Utils.startAsyncTask(new SaveTeamLocationsTask(), null);
        }

        getToFamilyList(remote);

        finish();
    }

    private void getToFamilyList(boolean remote) {
        Intent intent;
        if (BuildConfig.BUILD_FOR_BORESHA_AFYA_SOUTH)
            intent = new Intent(this, AllClientsRegisterActivity.class);
        else
            intent = new Intent(this, FamilyRegisterActivity.class);

        intent.putExtra(Constants.INTENT_KEY.IS_REMOTE_LOGIN, remote);
        startActivity(intent);
    }

}
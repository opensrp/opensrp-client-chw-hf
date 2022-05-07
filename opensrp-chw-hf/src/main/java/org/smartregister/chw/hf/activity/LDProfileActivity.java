package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.vijay.jsonwizard.views.CustomTextView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.ld.activity.BaseLDProfileActivity;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.util.Constants;
import org.smartregister.view.customcontrols.CustomFontTextView;

public class LDProfileActivity extends BaseLDProfileActivity {
    public static void startProfileActivity(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, LDProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        CustomFontTextView recordLdEvent = findViewById(R.id.textview_record_ld);
        recordLdEvent.setText(getString(R.string.record_ld_event, "Partograph"));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.textview_record_ld){
            LDPartographActivity.startMe(this, memberObject.getBaseEntityId(), false,
                    getName(memberObject),  String.valueOf(new Period(new DateTime(this.memberObject.getAge()), new DateTime()).getYears()));
        }
        super.onClick(view);

    }

    private String getName(MemberObject memberObject){
        return getName(getName(memberObject.getFirstName(), memberObject.getMiddleName()), memberObject.getLastName());
    }

    private String getName(String nameOne, String nameTwo){
        return nameOne+" "+nameTwo;
    }

}

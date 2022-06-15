package org.smartregister.chw.hf.custom_view;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.smartregister.chw.core.custom_views.CoreHivFloatingMenu;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hiv.domain.HivMemberObject;

public class HivFloatingMenu extends CoreHivFloatingMenu {
    public HivFloatingMenu(Context context, HivMemberObject hivMemberObject) {
        super(context, hivMemberObject);
        RelativeLayout referToFacilityLayout = findViewById(R.id.refer_to_facility_layout);
        TextView referTextView = (TextView) referToFacilityLayout.getChildAt(0);
        referTextView.setText(R.string.lost_to_followup_referral);
    }
}

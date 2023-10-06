package org.smartregister.chw.hf.custom_view;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.smartregister.chw.core.custom_views.CoreVmmcFloatingMenu;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.vmmc.domain.MemberObject;

public class VmmcFloatingMenu extends CoreVmmcFloatingMenu {
    public VmmcFloatingMenu(Context context, MemberObject memberObject) {
        super(context, memberObject);
        RelativeLayout referToFacilityLayout = findViewById(R.id.refer_to_facility_layout);
        TextView referTextView = (TextView) referToFacilityLayout.getChildAt(0);
        referTextView.setText(R.string.refer_to_facility);
    }
}

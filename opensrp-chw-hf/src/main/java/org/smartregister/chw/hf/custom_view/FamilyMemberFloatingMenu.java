package org.smartregister.chw.hf.custom_view;

import static org.smartregister.chw.core.utils.Utils.redrawWithOption;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.smartregister.chw.core.custom_views.CoreFamilyMemberFloatingMenu;
import org.smartregister.chw.hf.R;

public class FamilyMemberFloatingMenu extends CoreFamilyMemberFloatingMenu {
    public FamilyMemberFloatingMenu(Context context) {
        super(context);
    }

    @Override
    public void initUi() {
        super.initUi();
        fab.setOnClickListener(v -> animateFAB());
        fab.setImageResource(org.smartregister.chw.core.R.drawable.ic_edit_white);
        RelativeLayout referToFacilityLayout = findViewById(R.id.refer_to_facility_layout);
        referToFacilityLayout.setVisibility(VISIBLE);
        TextView referToFacilityLabel = (TextView) referToFacilityLayout.getChildAt(0);
        referToFacilityLabel.setText(R.string.lost_to_followup_referral);
    }

    @Override
    public void reDraw(boolean has_phone) {
        redrawWithOption(this, has_phone);
    }

    @Override
    public void animateFAB() {
        if (menuBar.getVisibility() == GONE) {
            menuBar.setVisibility(VISIBLE);
        }

        if (isFabMenuOpen) {
            activityMain.setBackgroundResource(org.smartregister.chw.core.R.color.transparent);

            fab.startAnimation(rotateBack);
            fab.setImageResource(org.smartregister.chw.core.R.drawable.ic_edit_white);

            callLayout.startAnimation(fabClose);
            referLayout.startAnimation(fabClose);

            callLayout.setClickable(false);
            referLayout.setClickable(false);
            isFabMenuOpen = false;

        } else {
            activityMain.setBackgroundResource(org.smartregister.chw.core.R.color.grey_tranparent_50);

            fab.startAnimation(rotateForward);
            fab.setImageResource(org.smartregister.chw.core.R.drawable.ic_input_add);

            callLayout.startAnimation(fabOpen);
            referLayout.startAnimation(fabOpen);

            callLayout.setClickable(true);
            referLayout.setClickable(true);

            isFabMenuOpen = true;
        }
    }
}

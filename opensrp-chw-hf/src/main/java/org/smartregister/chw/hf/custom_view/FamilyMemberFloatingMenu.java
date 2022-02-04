package org.smartregister.chw.hf.custom_view;

import android.content.Context;

import org.smartregister.chw.core.custom_views.CoreFamilyMemberFloatingMenu;
import org.smartregister.chw.hf.R;

import static org.smartregister.chw.core.utils.Utils.redrawWithOption;

public class FamilyMemberFloatingMenu extends CoreFamilyMemberFloatingMenu {
    public FamilyMemberFloatingMenu(Context context) {
        super(context);
    }

    @Override
    public void initUi() {
        super.initUi();
        fab.setOnClickListener(v -> animateFAB());
        findViewById(R.id.refer_to_facility_layout).setVisibility(GONE);
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
            fab.setImageResource(org.smartregister.chw.core.R.drawable.ic_call_black_24dp);

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

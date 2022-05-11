package org.smartregister.chw.hf.custom_view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.smartregister.chw.anc.custom_views.BaseAncFloatingMenu;
import org.smartregister.chw.hf.fragment.PncNoMotherCallDialogFragment;

public class PncNoMotherFloatingMenu extends BaseAncFloatingMenu {
    private String caregiverName;
    private String phoneNumber;

    public PncNoMotherFloatingMenu(Context context, String ancWomanName, String ancWomanPhone) {
        super(context, ancWomanName, ancWomanPhone, null, null, null);
        caregiverName = ancWomanName;
        phoneNumber = ancWomanPhone;
    }

    public PncNoMotherFloatingMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initUi() {
        inflate(getContext(), org.smartregister.chw.opensrp_chw_anc.R.layout.view_anc_call_woma_floating_menu, this);
        FloatingActionButton fab = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.anc_fab);
        fab.setOnClickListener(this);
        fab.setImageResource(org.smartregister.chw.opensrp_chw_anc.R.drawable.floating_call);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == org.smartregister.chw.opensrp_chw_anc.R.id.anc_fab) {
            Activity activity = (Activity) getContext();
            PncNoMotherCallDialogFragment.launchDialog(activity, caregiverName, phoneNumber);
        } else {
            super.onClick(view);
        }
    }
}

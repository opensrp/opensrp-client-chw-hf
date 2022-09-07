package org.smartregister.chw.hf.fragment;

import static android.view.View.GONE;
import static org.smartregister.util.Utils.getName;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.anc.fragment.BaseAncWomanCallDialogFragment;
import org.smartregister.chw.anc.listener.BaseAncWomanCallWidgetDialogListener;
import org.smartregister.chw.hf.R;

public class PncNoMotherCallDialogFragment extends BaseAncWomanCallDialogFragment {

    private static String pncCaregiverNumber;
    private static String pncCaregiverName;
    private View.OnClickListener listener = null;

    public static PncNoMotherCallDialogFragment launchDialog(Activity activity, String caregiverName, String caregiverPhone) {
        PncNoMotherCallDialogFragment dialogFragment = PncNoMotherCallDialogFragment.newInstance();
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        Fragment prev = activity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
        pncCaregiverNumber = caregiverPhone;
        pncCaregiverName = caregiverName;
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        dialogFragment.show(ft, DIALOG_TAG);
        return dialogFragment;

    }

    public static PncNoMotherCallDialogFragment newInstance() {
        return new PncNoMotherCallDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup dialogView = (ViewGroup) inflater.inflate(org.smartregister.chw.opensrp_chw_anc.R.layout.anc_member_call_widget_dialog_fragment, container, false);
        setUpPosition();
        TextView callTittleTextView = dialogView.findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.call_title);
        TextView callTitle = dialogView.findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.call_anc_woman_title);

        callTittleTextView.setText(R.string.call_child_caregiver);
        callTitle.setText(R.string.call_child_caregiver);

        if (listener == null) {
            listener = new BaseAncWomanCallWidgetDialogListener(this);
        }

        initUI(dialogView);
        return dialogView;
    }

    private void initUI(ViewGroup rootView) {

        if (StringUtils.isNotBlank(pncCaregiverNumber)) {
            TextView ancWomanNameTextView = rootView.findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.call_anc_woman_name);
            ancWomanNameTextView.setText(pncCaregiverName);

            TextView ancCallAncWomanPhone = rootView.findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.call_anc_woman_phone);
            ancCallAncWomanPhone.setTag(pncCaregiverNumber);
            ancCallAncWomanPhone.setText(getName(getCurrentContext().getString(org.smartregister.chw.opensrp_chw_anc.R.string.anc_call), pncCaregiverNumber));
            ancCallAncWomanPhone.setOnClickListener(listener);
        } else {

            rootView.findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.layout_anc_woman).setVisibility(GONE);
        }

        rootView.findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.anc_layout_family_head).setVisibility(GONE);
        rootView.findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.anc_call_close).setOnClickListener(listener);
    }


}

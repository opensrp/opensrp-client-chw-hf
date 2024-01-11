package org.smartregister.chw.hf.custom_view;

import static org.smartregister.chw.core.utils.Utils.redrawWithOption;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.smartregister.chw.core.custom_views.CorePmtctFloatingMenu;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.HeiProfileActivity;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.fragment.BasePmtctCallDialogFragment;

public class PmtctFloatingMenu extends CorePmtctFloatingMenu {
    public FloatingActionButton fab;

    private Animation fabOpen;

    private Animation fabClose;

    private Animation rotateForward;

    private Animation rotateBack;

    private View callLayout;

    protected RelativeLayout referLayout;

    private RelativeLayout activityMain;

    private boolean isFabMenuOpen = false;

    private LinearLayout menuBar;

    private OnClickFloatingMenu onClickFloatingMenu;

    private final MemberObject MEMBER_OBJECT;

    public PmtctFloatingMenu(Context context, MemberObject MEMBER_OBJECT) {
        super(context, MEMBER_OBJECT);
        if(context instanceof HeiProfileActivity){
            referLayout.setVisibility(View.GONE);
        }
        this.MEMBER_OBJECT = MEMBER_OBJECT;
    }

    @Override
    protected void initUi() {
        inflate(getContext(), R.layout.view_hiv_call_client_floating_menu, this);
        fabOpen = AnimationUtils.loadAnimation(getContext(), org.smartregister.chw.core.R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getContext(), org.smartregister.chw.core.R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(getContext(), org.smartregister.chw.core.R.anim.rotate_forward);
        rotateBack = AnimationUtils.loadAnimation(getContext(), org.smartregister.chw.core.R.anim.rotate_back);

        activityMain = findViewById(org.smartregister.chw.core.R.id.activity_main);
        menuBar = findViewById(org.smartregister.chw.core.R.id.menu_bar);

        fab = findViewById(org.smartregister.chw.core.R.id.hiv_fab);
        fab.setOnClickListener(this);

        callLayout = findViewById(org.smartregister.chw.core.R.id.call_layout);
        callLayout.setOnClickListener(this);
        callLayout.setClickable(false);

        referLayout = findViewById(org.smartregister.chw.core.R.id.refer_to_facility_layout);
        referLayout.setOnClickListener(this);
        referLayout.setClickable(false);

        TextView  referTextView = (TextView) referLayout.getChildAt(0);
        referTextView.setText(R.string.lost_to_followup_referral);

        menuBar.setVisibility(GONE);
    }

    public void setFloatMenuClickListener(OnClickFloatingMenu onClickFloatingMenu) {
        this.onClickFloatingMenu = onClickFloatingMenu;
    }
    @Override
    public void onClick(View view) {
        onClickFloatingMenu.onClickMenu(view.getId());
    }

    public void animateFAB() {
        if (menuBar.getVisibility() == GONE) {
            menuBar.setVisibility(VISIBLE);
        }

        if (isFabMenuOpen) {
            activityMain.setBackgroundResource(org.smartregister.chw.core.R.color.transparent);
            fab.startAnimation(rotateBack);
            fab.setImageResource(org.smartregister.chw.core.R.drawable.ic_edit_white);

            callLayout.startAnimation(fabClose);
            callLayout.setClickable(false);

            referLayout.startAnimation(fabClose);
            referLayout.setClickable(false);

            isFabMenuOpen = false;
        } else {
            activityMain.setBackgroundResource(org.smartregister.chw.core.R.color.grey_tranparent_50);
            fab.startAnimation(rotateForward);
            fab.setImageResource(org.smartregister.chw.core.R.drawable.ic_input_add);

            callLayout.startAnimation(fabOpen);
            callLayout.setClickable(true);

            referLayout.startAnimation(fabOpen);
            referLayout.setClickable(true);

            isFabMenuOpen = true;
        }
    }


    public void launchCallWidget() {
        BasePmtctCallDialogFragment.launchDialog((Activity) this.getContext(), MEMBER_OBJECT);
    }

    public void redraw(boolean hasPhoneNumber) {
        redrawWithOption(this, hasPhoneNumber);
    }

    public View getCallLayout() {
        return callLayout;
    }
}

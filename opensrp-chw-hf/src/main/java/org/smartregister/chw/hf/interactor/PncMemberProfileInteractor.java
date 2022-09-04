package org.smartregister.chw.hf.interactor;

import static org.smartregister.util.Utils.getName;

import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.interactor.CorePncMemberProfileInteractor;
import org.smartregister.chw.core.repository.ChwTaskRepository;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.contract.PncMemberProfileContract;
import org.smartregister.chw.hf.repository.HfProfileRepository;
import org.smartregister.chw.pnc.PncLibrary;
import org.smartregister.chw.pnc.util.Constants;
import org.smartregister.chw.pnc.util.PncUtil;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.repository.TaskRepository;

import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class PncMemberProfileInteractor extends CorePncMemberProfileInteractor
        implements PncMemberProfileContract.Interactor {

    @Override
    public void getReferralTasks(String planId, String baseEntityId,
                                 PncMemberProfileContract.InteractorCallback callback) {

        TaskRepository taskRepository = CoreChwApplication.getInstance().getTaskRepository();
        Set<Task> taskList = ((ChwTaskRepository) taskRepository).getReferralTasksForClientByStatus(planId, baseEntityId, CoreConstants.BUSINESS_STATUS.REFERRED);

        callback.updateReferralTasks(taskList);
    }

    @Override
    public String getPncMotherNameDetails(MemberObject memberObject, TextView textView, CircleImageView imageView) {
        List<CommonPersonObjectClient> children = new HfProfileRepository().getChildrenLessThan49DaysOld(memberObject.getBaseEntityId());
        String nameDetails = memberObject.getMemberName();
        textView.setText(nameDetails);
        textView.setSingleLine(false);
        imageView.setImageResource(org.smartregister.chw.opensrp_chw_anc.R.mipmap.ic_member);
        for (CommonPersonObjectClient childObject : children) {
            try {
                char gender = childObject.getColumnmaps().get(Constants.KEY.GENDER).charAt(0);
                textView.append(" +\n" + childNameDetails(childObject.getColumnmaps().get(DBConstants.KEY.FIRST_NAME),
                        childObject.getColumnmaps().get(DBConstants.KEY.MIDDLE_NAME),
                        childObject.getColumnmaps().get(DBConstants.KEY.LAST_NAME),
                        String.valueOf(PncUtil.getDaysDifference(childObject.getColumnmaps().get(DBConstants.KEY.DOB))),
                        gender));
                imageView.setImageResource(org.smartregister.chw.pnc.R.drawable.pnc_less_twenty_nine_days);
                if (children.size() == 1) {
                    imageView.setMaxWidth(10);
                    imageView.setMaxHeight(10);
                    imageView.setBorderWidth(14);
                    if (gender == 'M') {
                        imageView.setBorderColor(PncLibrary.getInstance().context().getColorResource(org.smartregister.chw.pnc.R.color.light_blue));
                    } else {
                        imageView.setBorderColor(PncLibrary.getInstance().context().getColorResource(org.smartregister.chw.pnc.R.color.light_pink));
                    }
                }
            } catch (NullPointerException npe) {
                Timber.e(npe);
            }
        }

        return nameDetails;
    }

    private String childNameDetails(String firstName, String middleName, String surName, String age, char gender) {
        String dayCountString = PncLibrary.getInstance().context().getStringResource(org.smartregister.chw.pnc.R.string.pnc_day_count);
        String spacer = ", ";
        middleName = middleName != null ? middleName : "";
        String name = getName(firstName, middleName);
        name = getName(name, surName);

        if (StringUtils.isNotBlank(firstName)) {
            return name + spacer + age + dayCountString + spacer + gender;
        }
        return null;
    }
}

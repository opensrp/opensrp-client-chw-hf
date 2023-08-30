package org.smartregister.chw.hf.dao;

import org.smartregister.dao.AbstractDao;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class LTFUFeedbackDao extends AbstractDao {

    public static final String TABLE_NAME = "ec_ltfu_feedback";

    public static Date getFeedBackDate(String taskId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "feedback_date");

        String sql = "SELECT feedback_date  from " + TABLE_NAME +
                " WHERE referral_task_id = '" + taskId + "' ";
        List<String> res = readData(sql, dataMap);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTimeInMillis(new BigDecimal(res.get(0)).longValue());
            } catch (Exception e) {
                //NEEDED FOR THE ISSUE IN SOME TABLETS FAILING TO CREATE A TIMESTAMP
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    cal.setTime(sdf.parse(res.get(0)));
                } catch (ParseException parseException) {
                    Timber.e(parseException);
                    return null;
                }
            }
            return new Date(cal.getTimeInMillis());
        }
        return null;
    }

    public static String getFollowupStatus(String taskId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "followup_status");

        String sql = "SELECT followup_status  from " + TABLE_NAME +
                " WHERE referral_task_id = '" + taskId + "' ";
        List<String> res = readData(sql, dataMap);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0);
        }
        return null;
    }

    public static String getReasonsForMissedAppointment(String taskId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "reasons_for_missed_appointment");

        String sql = "SELECT reasons_for_missed_appointment  from " + TABLE_NAME +
                " WHERE referral_task_id = '" + taskId + "' ";
        List<String> res = readData(sql, dataMap);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0);
        }
        return null;
    }

    public static Date getReferralAppointmentDate(String taskId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "referral_appointment_date");

        String sql = "SELECT referral_appointment_date  from " + TABLE_NAME +
                " WHERE referral_task_id = '" + taskId + "' ";
        List<String> res = readData(sql, dataMap);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTimeInMillis(new BigDecimal(res.get(0)).longValue());
            } catch (Exception e) {
                //NEEDED FOR THE ISSUE IN SOME TABLETS FAILING TO CREATE A TIMESTAMP
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    cal.setTime(sdf.parse(res.get(0)));
                } catch (ParseException parseException) {
                    Timber.e(parseException);
                    return null;
                }
            }
            return new Date(cal.getTimeInMillis());
        }
        return null;
    }

    public static Date getLastAppointmentDate(String taskId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "last_appointment_date");

        String sql = "SELECT last_appointment_date  from " + TABLE_NAME +
                " WHERE referral_task_id = '" + taskId + "' ";
        List<String> res = readData(sql, dataMap);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTimeInMillis(new BigDecimal(res.get(0)).longValue());
            } catch (Exception e) {
                //NEEDED FOR THE ISSUE IN SOME TABLETS FAILING TO CREATE A TIMESTAMP
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    cal.setTime(sdf.parse(res.get(0)));
                } catch (ParseException parseException) {
                    Timber.e(parseException);
                    return null;
                }
            }
            return new Date(cal.getTimeInMillis());
        }
        return null;
    }

    public static String getReasonClientNotFound(String taskId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "reason_client_not_found");

        String sql = "SELECT reason_client_not_found  from " + TABLE_NAME +
                " WHERE referral_task_id = '" + taskId + "' ";
        List<String> res = readData(sql, dataMap);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0);
        }
        return null;
    }

    public static Date getDateOfDeath(String taskId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "date_of_death");

        String sql = "SELECT date_of_death  from " + TABLE_NAME +
                " WHERE referral_task_id = '" + taskId + "' ";
        List<String> res = readData(sql, dataMap);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTimeInMillis(new BigDecimal(res.get(0)).longValue());
            } catch (Exception e) {
                //NEEDED FOR THE ISSUE IN SOME TABLETS FAILING TO CREATE A TIMESTAMP
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    cal.setTime(sdf.parse(res.get(0)));
                } catch (ParseException parseException) {
                    Timber.e(parseException);
                    return null;
                }
            }
            return new Date(cal.getTimeInMillis());
        }
        return null;
    }
}

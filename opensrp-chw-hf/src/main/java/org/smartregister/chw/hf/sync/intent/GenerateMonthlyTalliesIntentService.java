package org.smartregister.chw.hf.sync.intent;

import static org.smartregister.chw.anc.util.NCUtils.getSyncHelper;
import static org.smartregister.util.Utils.getAllSharedPreferences;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.joda.time.LocalDate;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.domain.MonthlyTally;
import org.smartregister.chw.core.repository.MonthlyTalliesRepository;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.ReportUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.ReportDao;
import org.smartregister.chw.hf.repository.HfMonthlyTalliesRepository;;
import org.smartregister.domain.Response;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Hia2ReportRepository;
import org.smartregister.service.HTTPAgent;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

public class GenerateMonthlyTalliesIntentService extends IntentService {

    private static final String TAG = GenerateMonthlyTalliesIntentService.class.getSimpleName();

    private HfMonthlyTalliesRepository monthlyTalliesRepository;

    public GenerateMonthlyTalliesIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        monthlyTalliesRepository = new HfMonthlyTalliesRepository();
        String lastCalculatedMonthlyTalliesDate = ReportDao.getLastMonthWithTallies();
        Date maxDate = LocalDate.now().minusMonths(1).toDate();
        Date minDate = LocalDate.now().minusYears(1).toDate();
        try {
            if (lastCalculatedMonthlyTalliesDate != null) {
                minDate = MonthlyTalliesRepository.DF_YYYYMM.parse(lastCalculatedMonthlyTalliesDate);
            }
        } catch (ParseException e) {
            Timber.e(e);
        }
        for (Date date : generateStartDates(minDate, maxDate)) {
            generateMonthlyReport(date);
        }
        pushReportsToServer();
        Timber.i("Generate monthly tallies complete");
    }

    private void generateMonthlyReport(Date date) {
        try {
            List<MonthlyTally> monthlyTallies;
            if (isDateInLastMonth(date)) {
                monthlyTallies = monthlyTalliesRepository.generateNewMonthlyTallies(MonthlyTalliesRepository.DF_YYYYMM.format(date));
            } else {
                monthlyTallies = monthlyTalliesRepository.findDrafts(MonthlyTalliesRepository.DF_YYYYMM.format(date));
            }

            if (monthlyTallies.size() > 0) {
                for (MonthlyTally curTally : monthlyTallies) {
                    List<MonthlyTally> previouslySavedMonthlyTally = monthlyTalliesRepository.findMonthlyTally(curTally.getIndicator().getIndicatorCode(), MonthlyTalliesRepository.DF_YYYYMM.format(date));

                    if (isDateInLastMonth(date) && previouslySavedMonthlyTally.size() != 0) {
                        if (!curTally.getValue().equalsIgnoreCase(previouslySavedMonthlyTally.get(0).getValue())) {
                            continue;
                        }

                        curTally.setUpdatedAt(Calendar.getInstance().getTime());
                    }

                    Date now = Calendar.getInstance().getTime();
                    Date dateSent = curTally.getDateSent() != null ? curTally.getDateSent() : now;
                    Date dateCreated = curTally.getCreatedAt() != null ? curTally.getCreatedAt() : now;

                    curTally.setDateSent(dateSent);
                    curTally.setCreatedAt(dateCreated);
                    curTally.setMonth(date);
                    if (curTally.getSubmissionId() == null) {
                        curTally.setSubmissionId(UUID.randomUUID().toString());
                    }

                    ReportUtils.addEvent(ReportUtils.geValues(curTally, null), curTally.getSubmissionId(), CoreConstants.JSON_FORM.IN_APP_REPORT_FORM, CoreConstants.TABLE_NAME.MONTHLY_TALLIES_REPORT);
                }

                long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
                Date lastSyncDate = new Date(lastSyncTimeStamp);
                NCUtils.getClientProcessorForJava().processClient(getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
                getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
            }

        } catch (Exception e) {
            Timber.e(e);
        }
    }


    public static boolean isDateInLastMonth(Date date) {
        Calendar cal = Calendar.getInstance();

        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        cal.setTime(date);
        int inputMonth = cal.get(Calendar.MONTH);
        int inputYear = cal.get(Calendar.YEAR);

        if (inputYear == currentYear) {
            return inputMonth == currentMonth - 1;
        } else if (inputYear == currentYear - 1) {
            return inputMonth == 11 && currentMonth == 0;
        }
        return false;
    }


    protected void pushReportsToServer() {
        final String REPORTS_SYNC_PATH = "/rest/report/add";
        final Context context = CoreChwApplication.getInstance().getContext().applicationContext();
        HTTPAgent httpAgent = CoreChwApplication.getInstance().getContext().getHttpAgent();
        Hia2ReportRepository hia2ReportRepository = CoreChwApplication.getInstance().hia2ReportRepository();
        try {
            boolean keepSyncing = true;
            int limit = 50;
            while (keepSyncing) {
                List<JSONObject> pendingReports = hia2ReportRepository.getUnSyncedReports(limit);

                if (pendingReports.isEmpty()) {
                    return;
                }

                String baseUrl = CoreChwApplication.getInstance().getContext().configuration().dristhiBaseURL();
                if (baseUrl.endsWith(context.getString(R.string.url_separator))) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(context.getString(R.string.url_separator)));
                }
                // create request body
                JSONObject request = new JSONObject();

                request.put("reports", pendingReports);
                String jsonPayload = request.toString();
                Response<String> response = httpAgent.post(
                        MessageFormat.format("{0}{1}",
                                baseUrl,
                                REPORTS_SYNC_PATH),
                        jsonPayload);
                if (response.isFailure()) {
                    Log.e(getClass().getName(), "Reports sync failed.");
                    return;
                }
                hia2ReportRepository.markReportsAsSynced(pendingReports);
                Log.i(getClass().getName(), "Reports synced successfully.");
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage());
        }
    }


    public static List<Date> generateStartDates(Date startDate, Date endDate) {
        List<Date> dateList = new ArrayList<>();
        Calendar cal = new GregorianCalendar();
        cal.setTime(endDate); // set the calendar to the end date

        while (cal.getTime().after(startDate)) {
            cal.set(Calendar.DAY_OF_MONTH, 1); // set day to the first of the month
            cal.set(Calendar.HOUR_OF_DAY, 0); // set hour to the start of the day
            cal.set(Calendar.MINUTE, 0); // set minute to the start of the hour
            cal.set(Calendar.SECOND, 0); // set second to the start of the minute
            cal.set(Calendar.MILLISECOND, 0); // set millisecond to the start of the second

            dateList.add(cal.getTime()); // add the date to the list

            cal.add(Calendar.MONTH, -1); // subtract one month
        }

        Collections.reverse(dateList);
        return dateList;
    }

}

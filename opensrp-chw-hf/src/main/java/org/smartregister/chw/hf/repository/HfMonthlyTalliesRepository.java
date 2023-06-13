package org.smartregister.chw.hf.repository;

import android.database.Cursor;

import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.domain.DailyTally;
import org.smartregister.chw.core.domain.MonthlyTally;
import org.smartregister.chw.core.repository.MonthlyTalliesRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class HfMonthlyTalliesRepository extends MonthlyTalliesRepository {
    /**
     * Returns a list of newly monthly tallies corresponding to the provided month
     *
     * @param month The month to get the draft tallies for
     * @return
     */
    public List<MonthlyTally> generateNewMonthlyTallies(String month) {
        List<MonthlyTally> monthlyTallies = new ArrayList<>();
        try {
            Map<Long, List<DailyTally>> dailyTallies = CoreChwApplication.getInstance().dailyTalliesRepository().findTalliesInMonth(DF_YYYYMM.parse(month));
            for (List<DailyTally> curList : dailyTallies.values()) {
                MonthlyTally curTally = addUpDailyTallies(curList);
                if (curTally != null) {
                    monthlyTallies.add(curTally);
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return monthlyTallies;
    }


    public List<MonthlyTally> findMonthlyTally(String indicatorCode, String month) {
        // Check if there exists any sent tally in the database for the month provided
        Cursor cursor = null;
        List<MonthlyTally> monthlyTallies = new ArrayList<>();
        try {
            cursor = getReadableDatabase().query(TABLE_NAME, TABLE_COLUMNS,
                    COLUMN_MONTH + " = '" + month +
                            "' AND " + COLUMN_INDICATOR_CODE + " = '" + indicatorCode + "'",
                    null, null, null, null, null);
            monthlyTallies = readAllDataElements(cursor);
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return monthlyTallies;
    }


    @Override
    protected MonthlyTally addUpDailyTallies(List<DailyTally> dailyTallies) {
        String userName = CoreChwApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        MonthlyTally monthlyTally = null;
        double value = 0d;
        monthlyTally = new MonthlyTally();
        monthlyTally.setIndicator(dailyTallies.get(dailyTallies.size() - 1).getIndicator());

        try {
            value = value + Double.valueOf(dailyTallies.get(dailyTallies.size() - 1).getValue());
        } catch (Exception e) {
            Timber.e(e);
        }

        if (monthlyTally != null) {
            monthlyTally.setUpdatedAt(Calendar.getInstance().getTime());
            monthlyTally.setValue(String.valueOf(Math.round(value)));
            monthlyTally.setProviderId(userName);
        }

        return monthlyTally;
    }
}

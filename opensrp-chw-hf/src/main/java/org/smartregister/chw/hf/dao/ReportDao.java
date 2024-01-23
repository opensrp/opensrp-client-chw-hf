package org.smartregister.chw.hf.dao;

import android.database.Cursor;

import androidx.annotation.NonNull;

import org.smartregister.dao.AbstractDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportDao extends AbstractDao {
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public static List<Map<String, String>> getMotherChampions(Date reportDate) {
        String sql = "SELECT chw_name, provider_id \n" +
                "from ec_mother_champion_followup \n" +
                "WHERE chw_name IS NOT NULL \n" +
                "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "      date(substr(strftime('%Y-%m-%d', datetime(last_interacted_with / 1000, 'unixepoch', 'localtime')), 1, 4) ||\n" +
                "           '-' ||\n" +
                "           substr(strftime('%Y-%m-%d', datetime(last_interacted_with / 1000, 'unixepoch', 'localtime')), 6, 2) ||\n" +
                "           '-' || '01')\n" +
                "UNION \n" +
                "SELECT chw_name, provider_id \n" +
                "from ec_anc_partner_community_feedback \n" +
                "WHERE chw_name IS NOT NULL \n" +
                "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "      date(substr(strftime('%Y-%m-%d', datetime(last_interacted_with / 1000, 'unixepoch', 'localtime')), 1, 4) ||\n" +
                "           '-' ||\n" +
                "           substr(strftime('%Y-%m-%d', datetime(last_interacted_with / 1000, 'unixepoch', 'localtime')), 6, 2) ||\n" +
                "           '-' || '01') \n" +
                "UNION \n" +
                "SELECT chw_name, provider_id \n" +
                "from ec_sbcc \n" +
                "WHERE chw_name IS NOT NULL \n" +
                "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "      date(substr(sbcc_date, 7, 4) || '-' || substr(sbcc_date, 4, 2) || '-' || '01')\n" +
                "UNION\n" +
                "SELECT chw_name, provider_id\n" +
                "from ec_pmtct_community_feedback\n" +
                "WHERE chw_name IS NOT NULL\n" +
                "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "      date(substr(strftime('%Y-%m-%d', datetime(pmtct_community_followup_visit_date / 1000, 'unixepoch', 'localtime')),\n" +
                "                  1, 4) ||\n" +
                "           '-' ||\n" +
                "           substr(strftime('%Y-%m-%d', datetime(pmtct_community_followup_visit_date / 1000, 'unixepoch', 'localtime')),\n" +
                "                  6, 2) ||\n" +
                "           '-' || '01')\n";

        String queryDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(reportDate);

        sql = sql.contains("%s") ? sql.replaceAll("%s", queryDate) : sql;

        DataMap<Map<String, String>> map = cursor -> {
            Map<String, String> data = new HashMap<>();
            data.put("chw_name", cursor.getString(cursor.getColumnIndex("chw_name")));
            data.put("provider_id", cursor.getString(cursor.getColumnIndex("provider_id")));
            return data;
        };

        List<Map<String, String>> res = readData(sql, map);


        if (res != null && res.size() > 0) {
            return res;
        } else
            return new ArrayList<>();
    }

    public static List<Map<String, String>> getHfIssuingCdpStockLog(Date reportDate) {

        String query1 = " SELECT point_of_service,other_pos ,female_condoms_offset,male_condoms_offset\n" +
                "        FROM ec_cdp_issuing_hf\n" +
                "        WHERE point_of_service='rch_clinic' \n" +
                " OR point_of_service='ctc' OR point_of_service='opd' OR point_of_service='other'\n" +
                " OR point_of_service='tb_clinic' OR point_of_service='outreach'  \n" +
                "        AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "        date(substr(condom_restock_date, 7, 4) || '-' || substr(condom_restock_date, 4, 2) || '-' || '01')";


        String query2 =
                "SELECT  requester,cof.condom_type as condom_type,quantity_response\n" +
                        "                       FROM ec_cdp_order_feedback cof\n" +
                        "      INNER JOIN ec_cdp_orders eco ON eco.form_submission_id = cof.request_reference\n" +
                        "  INNER JOIN task t ON t.for = eco.base_entity_id\n" +
                        "        WHERE (t.status='IN_PROGRESS'OR t.status='COMPLETED') \n" +
                        "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                        "      date(substr(strftime('%Y-%m-%d', datetime(response_at / 1000, 'unixepoch', 'localtime')),\n" +
                        "                  1, 4) ||\n" +
                        "           '-' ||\n" +
                        "           substr(strftime('%Y-%m-%d', datetime(response_at / 1000, 'unixepoch', 'localtime')),\n" +
                        "                  6, 2) ||\n" +
                        "           '-' || '01')\n";


        String queryDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(reportDate);

        query1 = query1.contains("%s") ? query1.replaceAll("%s", queryDate) : query1;
        query2 = query2.contains("%s") ? query2.replaceAll("%s", queryDate) : query2;

        DataMap<Map<String, String>> map1 = cursor -> {
            Map<String, String> data1 = new HashMap<>();
            data1.put("point_of_service", cursor.getString(cursor.getColumnIndex("point_of_service")));
            data1.put("other_point_of_service", cursor.getString(cursor.getColumnIndex("other_pos")));
            data1.put("female_condoms_offset", cursor.getString(cursor.getColumnIndex("female_condoms_offset")));
            data1.put("male_condoms_offset", cursor.getString(cursor.getColumnIndex("male_condoms_offset")));
            return data1;
        };

        DataMap<Map<String, String>> map2= cursor2 -> {
            Map<String, String> data2 = new HashMap<>();
            data2.put("requester", cursor2.getString(cursor2.getColumnIndex("requester")));
            data2.put("quantity_response", cursor2.getString(cursor2.getColumnIndex("quantity_response")));
            data2.put("condom_type", cursor2.getString(cursor2.getColumnIndex("condom_type")));
            return data2;
        };

        List<Map<String, String>> res1 = readData(query1, map1);
        List<Map<String, String>> res2 = readData(query2, map2);
        List<Map<String, String>> res = new ArrayList<>();
        res.addAll(res1);
        res.addAll(res2);

        if (res.size() > 0) {
            return res;
        } else
            return new ArrayList<>();
    }

    public static List<Map<String, String>> getHfCdpStockLog(Date reportDate)
    {
        String sql = " SELECT female_condoms_offset,male_condoms_offset,issuing_organization,female_condom_brand,male_condom_brand   \n" +
                "                   FROM ec_cdp_stock_log   \n" +
                "                   WHERE (issuing_organization='MSD' OR issuing_organization='PSI' OR issuing_organization='T-MARC' OR  issuing_organization='other')   \n" +
                "                   AND (stock_event_type ='increment' )   \n" +
                "                    AND date(substr(strftime('%Y-%m-%d', datetime(date_updated / 1000, 'unixepoch', 'localtime')), 1, 4) || '-' ||   \n" +
                "                    substr(strftime('%Y-%m-%d', datetime(date_updated / 1000, 'unixepoch', 'localtime')), 6, 2) || '-' || '01') =   \n" +
                "                   date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01'))    \n" +
                "                  UNION ALL   \n" +
                "                  SELECT ec_cdp_order_feedback.quantity_response,'0' as  male_condoms_offset,location.name,ec_cdp_order_feedback.condom_brand,'-' as male_condom_brand   \n" +
                "                   FROM task   \n" +
                "                   INNER JOIN location ON location.uuid = task.group_id   \n" +
                "                   INNER JOIN ec_cdp_order_feedback ON  ec_cdp_order_feedback.request_reference = task.reason_reference   \n" +
                "\t\t\t\t   INNER JOIN ec_cdp_orders ON  ec_cdp_orders.form_submission_id = ec_cdp_order_feedback.request_reference\n" +
                "                   WHERE (ec_cdp_order_feedback.condom_type = 'female_condom'  AND task.status = 'COMPLETED' AND request_type='facility_to_facility')   \n" +
                "                    AND date(substr(strftime('%Y-%m-%d', datetime(response_at / 1000, 'unixepoch', 'localtime')), 1, 4) || '-' ||   \n" +
                "                    substr(strftime('%Y-%m-%d', datetime(response_at / 1000, 'unixepoch', 'localtime')), 6, 2) || '-' || '01') =   \n" +
                "                   date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01'))    \n" +
                "                  UNION ALL   \n" +
                "                   SELECT '0' as  female_condoms_offset,quantity_response,location.name,'-' as female_condom_brand,ec_cdp_order_feedback.condom_brand   \n" +
                "                   FROM task   \n" +
                "                   INNER JOIN location ON location.uuid = task.group_id   \n" +
                "                   INNER JOIN ec_cdp_order_feedback ON  ec_cdp_order_feedback.request_reference = task.reason_reference   \n" +
                "\t\t\t\t   INNER JOIN ec_cdp_orders ON  ec_cdp_orders.form_submission_id = ec_cdp_order_feedback.request_reference\n" +
                "                   WHERE (ec_cdp_order_feedback.condom_type = 'male_condom'  AND task.status = 'COMPLETED' AND request_type='facility_to_facility')   \n" +
                "                    AND date(substr(strftime('%Y-%m-%d', datetime(response_at / 1000, 'unixepoch', 'localtime')), 1, 4) || '-' ||   \n" +
                "                    substr(strftime('%Y-%m-%d', datetime(response_at / 1000, 'unixepoch', 'localtime')), 6, 2) || '-' || '01') =   \n" +
                "                   date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) ";

        String queryDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(reportDate);

        sql = sql.contains("%s") ? sql.replaceAll("%s", queryDate) : sql;

        DataMap<Map<String, String>> map = cursor -> {
            Map<String, String> data = new HashMap<>();
            data.put("female_condoms_offset", cursor.getString(cursor.getColumnIndex("female_condoms_offset")));
            data.put("male_condoms_offset", cursor.getString(cursor.getColumnIndex("male_condoms_offset")));
            data.put("issuing_organization", cursor.getString(cursor.getColumnIndex("issuing_organization")));
            data.put("male_condom_brand", cursor.getString(cursor.getColumnIndex("male_condom_brand")));
            data.put("female_condom_brand", cursor.getString(cursor.getColumnIndex("female_condom_brand")));

            return data;
        };

        List<Map<String, String>> res = readData(sql, map);


        if (res != null && res.size() > 0) {
            return res;
        } else
            return new ArrayList<>();
    }

    public static List<Map<String, String>> getVmmcServiceRegister(Date reportDate)
    {
        String sql = "WITH VMMC_CTE AS (\n" +
                "    SELECT\n" +
                "        ec_vmmc_enrollment.enrollment_date,\n" +
                "        ec_family_member.first_name,\n" +
                "        ec_family_member.middle_name,\n" +
                "        ec_family_member.last_name,\n" +
                "        ec_vmmc_enrollment.vmmc_client_id,\n" +
                "        ec_vmmc_enrollment.reffered_from,\n" +
                "        ec_vmmc_services.tested_hiv,\n" +
                "        ec_vmmc_services.hiv_result,\n" +
                "        ec_vmmc_services.client_referred_to,\n" +
                "        ec_vmmc_procedure.mc_procedure_date,\n" +
                "        ec_vmmc_procedure.male_circumcision_method,\n" +
                "        ec_vmmc_procedure.health_care_provider,\n" +
                "        ec_vmmc_procedure.intraoperative_adverse_event_occured,\n" +
                "        ec_vmmc_follow_up_visit.visit_number,\n" +
                "        ec_vmmc_follow_up_visit.followup_visit_date AS visit_date,\n" +
                "        ec_vmmc_follow_up_visit.post_op_adverse_event_occur AS post_op_adverse,\n" +
                "        ec_vmmc_notifiable_ae.did_client_experience_nae AS NAE,\n" +
                "        ec_family_member.dob\n" +
                "    FROM\n" +
                "        ec_vmmc_enrollment\n" +
                "    LEFT JOIN\n" +
                "        ec_family_member ON ec_family_member.base_entity_id = ec_vmmc_enrollment.base_entity_id\n" +
                "    LEFT JOIN\n" +
                "        ec_vmmc_services ON ec_vmmc_services.entity_id = ec_vmmc_enrollment.base_entity_id\n" +
                "    LEFT JOIN\n" +
                "        ec_vmmc_procedure ON ec_vmmc_procedure.entity_id = ec_vmmc_enrollment.base_entity_id\n" +
                "    LEFT JOIN\n" +
                "        ec_vmmc_follow_up_visit ON ec_vmmc_follow_up_visit.entity_id = ec_vmmc_enrollment.base_entity_id\n" +
                "        AND ec_vmmc_follow_up_visit.follow_up_visit_type = 'routine'\n" +
                "    LEFT JOIN\n" +
                "        ec_vmmc_notifiable_ae ON ec_vmmc_notifiable_ae.entity_id = ec_vmmc_enrollment.base_entity_id\n" +
                "\tWHERE \n" +
                "    date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "   date(substr(ec_vmmc_enrollment.enrollment_date, 7, 4) || '-' || substr(ec_vmmc_enrollment.enrollment_date, 4, 2) || '-' || '01')\t\n" +
                ")\n" +
                "\n" +
                "SELECT\n" +
                "    enrollment_date,\n" +
                "    first_name || ' ' || middle_name || ' ' || last_name AS names,\n" +
                "    vmmc_client_id,\n" +
                "    (strftime('%Y', 'now') - strftime('%Y', dob)) - (strftime('%m-%d', 'now') < strftime('%m-%d', dob)) AS age,\n" +
                "    reffered_from,\n" +
                "    MAX(tested_hiv) AS tested_hiv,\n" +
                "    MAX(hiv_result) AS hiv_result,\n" +
                "    MAX(client_referred_to) AS client_referred_to,\n" +
                "    MAX(mc_procedure_date) AS mc_procedure_date,\n" +
                "    MAX(male_circumcision_method) AS male_circumcision_method,\n" +
                "    MAX(intraoperative_adverse_event_occured) AS intraoperative_adverse_event_occured,\n" +
                "    MAX(CASE WHEN visit_number = 1 THEN visit_date END) AS first_visit,\n" +
                "    MAX(CASE WHEN visit_number = 2 THEN visit_date END) AS sec_visit,\n" +
                "    MAX(post_op_adverse) AS post_op_adverse,\n" +
                "    MAX(NAE) AS NAE,\n" +
                "    MAX(health_care_provider) AS health_care_provider\n" +
                "FROM VMMC_CTE\n" +
                "GROUP BY\n" +
                "    enrollment_date,\n" +
                "    vmmc_client_id,\n" +
                "    names,\n" +
                "    dob\n" +
                "ORDER BY enrollment_date ASC;\n";

        String queryDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(reportDate);

        sql = sql.contains("%s") ? sql.replaceAll("%s", queryDate) : sql;

        DataMap<Map<String, String>> map = cursor -> {
            Map<String, String> data = new HashMap<>();
            data.put("enrollment_date", cursor.getString(cursor.getColumnIndex("enrollment_date")));
            data.put("names", cursor.getString(cursor.getColumnIndex("names")));
            data.put("vmmc_client_id", cursor.getString(cursor.getColumnIndex("vmmc_client_id")));
            data.put("age", cursor.getString(cursor.getColumnIndex("age")));
            data.put("reffered_from", cursor.getString(cursor.getColumnIndex("reffered_from")));
            data.put("tested_hiv", cursor.getString(cursor.getColumnIndex("tested_hiv")));
            data.put("hiv_result", cursor.getString(cursor.getColumnIndex("hiv_result")));
            data.put("client_referred_to", cursor.getString(cursor.getColumnIndex("client_referred_to")));
            data.put("mc_procedure_date", cursor.getString(cursor.getColumnIndex("mc_procedure_date")));
            data.put("male_circumcision_method", cursor.getString(cursor.getColumnIndex("male_circumcision_method")));
            data.put("intraoperative_adverse_event_occured", cursor.getString(cursor.getColumnIndex("intraoperative_adverse_event_occured")));
            data.put("first_visit", cursor.getString(cursor.getColumnIndex("first_visit")));
            data.put("sec_visit", cursor.getString(cursor.getColumnIndex("sec_visit")));
            data.put("post_op_adverse", cursor.getString(cursor.getColumnIndex("post_op_adverse")));
            data.put("NAE", cursor.getString(cursor.getColumnIndex("NAE")));
            data.put("health_care_provider", cursor.getString(cursor.getColumnIndex("health_care_provider")));

            return data;
        };

        List<Map<String, String>> res = readData(sql, map);


        if (res != null && res.size() > 0) {
            return res;
        } else
            return new ArrayList<>();
    }

    public static List<Map<String, String>> getVmmcTheatreRegister(Date reportDate)
    {
        String sql = "WITH VMMC_CTE AS (\n" +
                "    SELECT\n" +
                "        ec_vmmc_procedure.mc_procedure_date,\n" +
                "        ec_family_member.first_name,\n" +
                "        ec_family_member.middle_name,\n" +
                "        ec_family_member.last_name,\n" +
                "        ec_vmmc_enrollment.vmmc_client_id,\n" +
                "        ec_vmmc_procedure.surgeon_name,\n" +
                "        ec_vmmc_procedure.assistant_name,\n" +
                "        ec_vmmc_procedure.size_place,\n" +
                "        ec_vmmc_procedure.start_time,\n" +
                "        ec_vmmc_procedure.end_time,\n" +
                "        ec_vmmc_procedure.aneathesia_administered,\n" +
                "        ec_vmmc_procedure.type_of_adverse_event,\n" +
                "\t\tec_vmmc_procedure.male_circumcision_method,\n" +
                "        \n" +
                "        ec_family_member.dob\n" +
                "    FROM\n" +
                "        ec_vmmc_enrollment\n" +
                "    LEFT JOIN\n" +
                "        ec_family_member ON ec_family_member.base_entity_id = ec_vmmc_enrollment.base_entity_id\n" +
                "    LEFT JOIN\n" +
                "        ec_vmmc_services ON ec_vmmc_services.entity_id = ec_vmmc_enrollment.base_entity_id\n" +
                "    LEFT JOIN\n" +
                "        ec_vmmc_procedure ON ec_vmmc_procedure.entity_id = ec_vmmc_enrollment.base_entity_id\n" +
                "    LEFT JOIN\n" +
                "        ec_vmmc_follow_up_visit ON ec_vmmc_follow_up_visit.entity_id = ec_vmmc_enrollment.base_entity_id\n" +
                "        AND ec_vmmc_follow_up_visit.follow_up_visit_type = 'routine'\n" +
                "    LEFT JOIN\n" +
                "        ec_vmmc_notifiable_ae ON ec_vmmc_notifiable_ae.entity_id = ec_vmmc_enrollment.base_entity_id\n" +
                "\tWHERE \n" +
                "    date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "   date(substr(ec_vmmc_procedure.mc_procedure_date, 7, 4) || '-' || substr(ec_vmmc_procedure.mc_procedure_date, 4, 2) || '-' || '01')\t\n" +
                ")\n" +
                "\n" +
                "SELECT\n" +
                "    mc_procedure_date,\n" +
                "    vmmc_client_id,\n" +
                "    surgeon_name,\n" +
                "    assistant_name,\n" +
                "    size_place,\n" +
                "    type_of_adverse_event,\n" +
                "    first_name || ' ' || middle_name || ' ' || last_name AS names,\n" +
                "    (strftime('%Y', 'now') - strftime('%Y', dob)) - (strftime('%m-%d', 'now') < strftime('%m-%d', dob)) AS age,\n" +
                "\tMAX(male_circumcision_method) AS male_circumcision_method,\n" +
                "    MAX(aneathesia_administered) AS aneathesia_administered,\n" +
                "    MAX(start_time) AS start_time,\n" +
                "    MAX(end_time) AS end_time\n" +
                "        \n" +
                "FROM VMMC_CTE\n" +
                "GROUP BY\n" +
                "    mc_procedure_date,\n" +
                "    vmmc_client_id,\n" +
                "    names,\n" +
                "    dob\n" +
                "ORDER BY mc_procedure_date  ASC;\n";

        String queryDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(reportDate);

        sql = sql.contains("%s") ? sql.replaceAll("%s", queryDate) : sql;

        DataMap<Map<String, String>> map = cursor -> {
            Map<String, String> data = new HashMap<>();
            data.put("mc_procedure_date", cursor.getString(cursor.getColumnIndex("mc_procedure_date")));
            data.put("names", cursor.getString(cursor.getColumnIndex("names")));
            data.put("vmmc_client_id", cursor.getString(cursor.getColumnIndex("vmmc_client_id")));
            data.put("age", cursor.getString(cursor.getColumnIndex("age")));
            data.put("male_circumcision_method", cursor.getString(cursor.getColumnIndex("male_circumcision_method")));
            data.put("size_place", cursor.getString(cursor.getColumnIndex("size_place")));
            data.put("aneathesia_administered", cursor.getString(cursor.getColumnIndex("aneathesia_administered")));
            data.put("start_time", cursor.getString(cursor.getColumnIndex("start_time")));
            data.put("end_time", cursor.getString(cursor.getColumnIndex("end_time")));
            data.put("surgeon_name", cursor.getString(cursor.getColumnIndex("surgeon_name")));
            data.put("assistant_name", cursor.getString(cursor.getColumnIndex("assistant_name")));


            String type_of_adverse_event = getTypeOfAdverseEvent(cursor);
            data.put("type_of_adverse_event", type_of_adverse_event);

            return data;
        };

        List<Map<String, String>> res = readData(sql, map);


        if (res != null && res.size() > 0) {
            return res;
        } else
            return new ArrayList<>();
    }

    @NonNull
    private static String getTypeOfAdverseEvent(Cursor cursor) {

        List<String> type_of_adverse_event = new ArrayList<>();

        if(cursor.getString(cursor.getColumnIndex("type_of_adverse_event")) != null) {
            if(cursor.getString(cursor.getColumnIndex("type_of_adverse_event")).contains("excessive_skin_removed")){
                type_of_adverse_event.add("Excessive skin removed");
            }

            if(cursor.getString(cursor.getColumnIndex("type_of_adverse_event")).contains("excessive_bleeding")){
                type_of_adverse_event.add("Excessive bleeding");
            }

            if(cursor.getString(cursor.getColumnIndex("type_of_adverse_event")).contains("damage_to_penis")){
                type_of_adverse_event.add("Injury to the penis");
            }

            if(cursor.getString(cursor.getColumnIndex("type_of_adverse_event")).contains("anesthetic_related_events")){
                type_of_adverse_event.add("Anesthetic related events");
            }

            if(cursor.getString(cursor.getColumnIndex("type_of_adverse_event")).contains("device_displacement")){
                type_of_adverse_event.add("Device displacement");
            }

            if(cursor.getString(cursor.getColumnIndex("type_of_adverse_event")).contains("others")){
                type_of_adverse_event.add(cursor.getString(cursor.getColumnIndex("type_of_adverse_event_others")));
            }

            else {
                type_of_adverse_event.add(" ");
            }

        }


        String result = String.join(" ,", type_of_adverse_event);

        return result;
    }


    public static int getReportPerIndicatorCode(String indicatorCode, Date reportDate) {
        String reportDateString = simpleDateFormat.format(reportDate);
        String sql = "SELECT indicator_value\n" +
                "FROM indicator_daily_tally\n" +
                "WHERE indicator_code = '" + indicatorCode + "'\n" +
                "  AND date((substr('" + reportDateString + "', 7, 4) || '-' || substr('" + reportDateString + "', 4, 2) || '-' || '01')) = date((substr(day, 1, 4) || '-' || substr(day, 6, 2) || '-' || '01'))\n" +
                "ORDER BY day DESC LIMIT 1";

        DataMap<Integer> map = cursor -> getCursorIntValue(cursor, "indicator_value");

        List<Integer> res = readData(sql, map);


        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0);
        } else
            return 0;
    }


    public static String getLastMonthWithTallies(){
        String sql = " SELECT month FROM monthly_tallies ORDER by month DESC LIMIT 1";
        DataMap<String> map = cursor -> getCursorValue(cursor, "month");
        List<String> res = readData(sql, map);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0);
        } else
            return null;
    }

}

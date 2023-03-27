package org.smartregister.chw.hf.model;

import org.smartregister.chw.core.model.NavigationOption;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NavigationModel implements org.smartregister.chw.core.model.NavigationModel.Flavor {
    private List<NavigationOption> navigationOptions = new ArrayList<>();

    @Override
    public List<NavigationOption> getNavigationItems() {
        if (navigationOptions.size() == 0) {

            NavigationOption op1 = new NavigationOption(R.drawable.sidemenu_all_clients, R.drawable.sidemenu_all_clients_active, R.string.menu_all_clients, CoreConstants.DrawerMenu.ALL_CLIENTS, 0);
            NavigationOption op2 = new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_all_families, CoreConstants.DrawerMenu.ALL_FAMILIES, 0);
            NavigationOption op3 = new NavigationOption(R.mipmap.sidemenu_anc, R.mipmap.sidemenu_anc_active, R.string.menu_anc, CoreConstants.DrawerMenu.ANC, 0);
            NavigationOption op4 = new NavigationOption(R.mipmap.sidemenu_pnc, R.mipmap.sidemenu_pnc_active, R.string.menu_pnc, CoreConstants.DrawerMenu.PNC, 0);
            NavigationOption op5 = new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.menu_child_clients, CoreConstants.DrawerMenu.CHILD_CLIENTS, 0);
            NavigationOption op6 = new NavigationOption(R.mipmap.sidemenu_fp, R.mipmap.sidemenu_fp_active, R.string.menu_family_planning, CoreConstants.DrawerMenu.FAMILY_PLANNING, 0);
            NavigationOption op7 = new NavigationOption(R.mipmap.sidemenu_malaria, R.mipmap.sidemenu_malaria_active, R.string.menu_malaria, CoreConstants.DrawerMenu.MALARIA, 0);
            NavigationOption op8 = new NavigationOption(R.mipmap.sidemenu_hiv, R.mipmap.sidemenu_hiv_active, R.string.menu_hiv, CoreConstants.DrawerMenu.HIV_CLIENTS, 0);
            NavigationOption op9 = new NavigationOption(R.mipmap.sidemenu_hiv, R.mipmap.sidemenu_hiv_active, R.string.menu_hts, CoreConstants.DrawerMenu.HTS_CLIENTS, 0);
            NavigationOption op10 = new NavigationOption(R.mipmap.sidemenu_hiv, R.mipmap.sidemenu_hiv_active, R.string.menu_hiv_index_contacts, CoreConstants.DrawerMenu.HIV_INDEX_CLIENTS_HF, 0);
            NavigationOption op11 = new NavigationOption(R.mipmap.sidemenu_tb, R.mipmap.sidemenu_tb_active, R.string.menu_tb, CoreConstants.DrawerMenu.TB_CLIENTS, 0);
            NavigationOption op12 = new NavigationOption(R.mipmap.sidemenu_referrals, R.mipmap.sidemenu_referrals_active, R.string.menu_referrals, CoreConstants.DrawerMenu.REFERRALS, 0);
            NavigationOption op13 = new NavigationOption(R.drawable.sidemenu_pmtct, R.drawable.sidemenu_pmtct_active, R.string.menu_pmtct, CoreConstants.DrawerMenu.PMTCT, 0);
            NavigationOption op14 = new NavigationOption(R.drawable.ic_sidemenu_hei, R.drawable.ic_sidemenu_hei_active, R.string.menu_hei, CoreConstants.DrawerMenu.HEI, 0);
            NavigationOption op15 = new NavigationOption(R.drawable.ic_sidemenu_labour_and_delivery, R.drawable.ic_sidemenu_labour_and_delivery_active, R.string.menu_ld, CoreConstants.DrawerMenu.LD, 0);
            NavigationOption op16 = new NavigationOption(R.mipmap.sidemenu_referrals, R.mipmap.sidemenu_referrals_active, R.string.menu_ltfu, CoreConstants.DrawerMenu.LTFU, 0);
            NavigationOption op17 = new NavigationOption(R.mipmap.sidemenu_hiv, R.mipmap.sidemenu_hiv_active, R.string.menu_hivst, CoreConstants.DrawerMenu.HIV_SELF_TESTING, 0);
            NavigationOption op18 = new NavigationOption(R.mipmap.sidemenu_hiv, R.mipmap.sidemenu_hiv_active, R.string.menu_cdp, CoreConstants.DrawerMenu.CDP_HF, 0);
            NavigationOption op19 = new NavigationOption(R.mipmap.sidemenu_hiv, R.mipmap.sidemenu_hiv_active, R.string.menu_kvp, CoreConstants.DrawerMenu.KVP, 0);
            NavigationOption op20 = new NavigationOption(R.mipmap.sidemenu_hiv, R.mipmap.sidemenu_hiv_active, R.string.menu_prep, CoreConstants.DrawerMenu.PrEP, 0);

            // ANC, PMTCT, LD, PNC, HEI, Child,LTFU, Referrals
            if (BuildConfig.BUILD_FOR_BORESHA_AFYA_SOUTH) {
                if (BuildConfig.BUILD_FOR_PMTCT_CASE_BASED_MANAGEMENT) {
                    navigationOptions.addAll(Arrays.asList(op1, op3, op4, op13, op14, op12));
                } else {
                    navigationOptions.addAll(Arrays.asList(op1, op3, op4, op13, op14, op9, op8, op10, op16, op12));
                }
                if (HealthFacilityApplication.getApplicationFlavor().hasLD()) {
                    navigationOptions.add(2, op15);
                }
                if (HealthFacilityApplication.getApplicationFlavor().hasChildModule()) {
                    navigationOptions.add(6, op5);
                }
                if (HealthFacilityApplication.getApplicationFlavor().hasMalaria()) {
                    navigationOptions.add(10, op7);
                }
                if (HealthFacilityApplication.getApplicationFlavor().hasCdp()) {
                    navigationOptions.add(10, op18);
                }
                if (HealthFacilityApplication.getApplicationFlavor().hasKvpPrEP()) {
                    navigationOptions.add(10, op20);
                    navigationOptions.add(10, op19);
                }
                if (HealthFacilityApplication.getApplicationFlavor().hasHivst()) {
                    navigationOptions.add(10, op17);
                }
            } else {
                navigationOptions.addAll(Arrays.asList(op1, op2, op3, op4, op5, op6, op7, op12));
            }
        }

        return navigationOptions;
    }
}

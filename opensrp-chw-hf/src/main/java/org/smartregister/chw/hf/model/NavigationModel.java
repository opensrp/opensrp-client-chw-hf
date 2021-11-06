package org.smartregister.chw.hf.model;

import org.smartregister.chw.core.model.NavigationOption;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NavigationModel implements org.smartregister.chw.core.model.NavigationModel.Flavor {
    private List<NavigationOption> navigationOptions = new ArrayList<>();

    @Override
    public List<NavigationOption> getNavigationItems() {
        if (navigationOptions.size() == 0) {

            NavigationOption op1 =new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_all_clients, CoreConstants.DrawerMenu.ALL_CLIENTS, 0);
            NavigationOption op2 =new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_all_families, CoreConstants.DrawerMenu.ALL_FAMILIES, 0);
            NavigationOption op3 =new NavigationOption(R.mipmap.sidemenu_anc, R.mipmap.sidemenu_anc_active, R.string.menu_anc, CoreConstants.DrawerMenu.ANC, 0);
            NavigationOption op4 =new NavigationOption(R.mipmap.sidemenu_pnc, R.mipmap.sidemenu_pnc_active, R.string.menu_pnc, CoreConstants.DrawerMenu.PNC, 0);
            NavigationOption op5 =new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.menu_child_clients, CoreConstants.DrawerMenu.CHILD_CLIENTS, 0);
            NavigationOption op6 =new NavigationOption(R.mipmap.sidemenu_fp, R.mipmap.sidemenu_fp_active, R.string.menu_family_planning, CoreConstants.DrawerMenu.FAMILY_PLANNING, 0);
            NavigationOption op7 =new NavigationOption(R.mipmap.sidemenu_malaria, R.mipmap.sidemenu_malaria_active, R.string.menu_malaria, CoreConstants.DrawerMenu.MALARIA, 0);
            NavigationOption op8 =new NavigationOption(R.mipmap.sidemenu_hiv, R.mipmap.sidemenu_hiv_active, R.string.menu_hiv, CoreConstants.DrawerMenu.HIV_CLIENTS, 0);
            NavigationOption op9 =new NavigationOption(R.mipmap.sidemenu_hiv, R.mipmap.sidemenu_hiv_active, R.string.menu_hts, CoreConstants.DrawerMenu.HTS_CLIENTS, 0);
            NavigationOption op10 =new NavigationOption(R.mipmap.sidemenu_hiv, R.mipmap.sidemenu_hiv_active, R.string.menu_hiv_index_contacts, CoreConstants.DrawerMenu.HIV_INDEX_CLIENTS_HF, 0);
            NavigationOption op11 =new NavigationOption(R.mipmap.sidemenu_tb, R.mipmap.sidemenu_tb_active, R.string.menu_tb, CoreConstants.DrawerMenu.TB_CLIENTS, 0);
            NavigationOption op12=new NavigationOption(R.mipmap.sidemenu_referrals, R.mipmap.sidemenu_referrals_active, R.string.menu_referrals, CoreConstants.DrawerMenu.REFERRALS, 0);
            if (BuildConfig.BUILD_FOR_BORESHA_AFYA_SOUTH) {
                navigationOptions.addAll(Arrays.asList(op1,op2,op8,op9,op10,op12,op3,op4,op5,op6,op7,op11));
            }else{
                navigationOptions.addAll(Arrays.asList(op1,op2,op3,op4,op5,op6,op7,op12));
            }
        }

        return navigationOptions;
    }
}

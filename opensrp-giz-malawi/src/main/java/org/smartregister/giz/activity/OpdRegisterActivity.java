package org.smartregister.giz.activity;

import android.os.Bundle;

import org.smartregister.giz.fragment.OpdRegisterFragment;
import org.smartregister.giz.util.GizConstants;
import org.smartregister.giz.view.NavigationMenu;
import org.smartregister.opd.activity.BaseOpdRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-09-17
 */

public class OpdRegisterActivity extends BaseOpdRegisterActivity {

    private NavigationMenu navigationMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //NavigationMenu.getInstance(this, null, null);
    }


    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new OpdRegisterFragment();
    }


    public void createDrawer() {
        navigationMenu = NavigationMenu.getInstance(this, null, null);
        navigationMenu.getNavigationAdapter().setSelectedView(GizConstants.DrawerMenu.ALL_CLIENTS);
        navigationMenu.runRegisterCount();
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        createDrawer();
    }
}

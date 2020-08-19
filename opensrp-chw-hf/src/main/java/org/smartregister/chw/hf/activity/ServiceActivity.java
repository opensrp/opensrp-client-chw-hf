package org.smartregister.chw.hf.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.ServiceActivityAdapter;
import org.smartregister.view.activity.SecuredActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServiceActivity extends SecuredActivity {
    protected AppBarLayout appBarLayout;

    @Override
    protected void onCreation() {
        setContentView(R.layout.service_activity);

        Toolbar toolbar = findViewById(R.id.back_to_nav_toolbar);
        CustomFontTextView toolBarTextView = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            upArrow.setVisible(true, true);
            actionBar.setHomeAsUpIndicator(upArrow);
            actionBar.setElevation(0);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolBarTextView.setOnClickListener(v -> finish());
        toolBarTextView.setText(this.getString(R.string.service_activity_title));

        appBarLayout = findViewById(R.id.app_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(null);
        }

        RecyclerView recyclerView = findViewById(R.id.rv_stock_activity);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        List<String> items = new ArrayList<>(
                Arrays.asList(getString(R.string.service_activity_reporting), getString(R.string.review_chw_services))
        );

        ServiceActivityAdapter serviceActivityAdapter = new ServiceActivityAdapter(items, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(serviceActivityAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        serviceActivityAdapter.notifyDataSetChanged();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onResumption() {
//Implements Super
    }
}

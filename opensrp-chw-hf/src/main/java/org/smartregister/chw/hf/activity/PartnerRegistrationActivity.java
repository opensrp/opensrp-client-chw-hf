package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.smartregister.chw.hf.R;
import org.smartregister.view.activity.SecuredActivity;

import androidx.constraintlayout.widget.ConstraintLayout;

public class PartnerRegistrationActivity extends SecuredActivity implements View.OnClickListener {

    private final int EXISTING_PARTNER_REQUEST_CODE = 12344;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_partner_registration);
        setupView();
    }

    @Override
    protected void onResumption() {
        //overridden
    }

    public void setupView() {
        ImageView closeImageView = findViewById(R.id.close);
        ConstraintLayout newClientRegistrationView = findViewById(R.id.new_client_registration);
        ConstraintLayout existingClientRegistrationView = findViewById(R.id.existing_client_registration);

        newClientRegistrationView.setOnClickListener(this);
        existingClientRegistrationView.setOnClickListener(this);
        closeImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.close) {
            finish();
        } else if (id == R.id.new_client_registration) {
            startPartnerRegistration();
        } else if (id == R.id.existing_client_registration) {
            searchForPartner();
        }
    }

    private void startPartnerRegistration() {
        //implement
    }

    private void searchForPartner() {
        startActivityForResult(new Intent(this,AllMaleClientsActivity.class),EXISTING_PARTNER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EXISTING_PARTNER_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                String partner_id = data.getStringExtra("BASE_ENTITY_ID");
                Toast.makeText(this,partner_id,Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }
}
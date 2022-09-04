package org.smartregister.chw.hf.fragment;

import static androidx.core.content.ContextCompat.getSystemService;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.AllClientsRegisterActivity;
import org.smartregister.chw.hf.adapter.FamilyMemberAdapter;
import org.smartregister.chw.hf.contract.AdvancedSearchContract;
import org.smartregister.chw.hf.domain.Entity;
import org.smartregister.chw.hf.model.AdvancedSearchFragmentModel;
import org.smartregister.chw.hf.presenter.AdvancedSearchFragmentPresenter;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedSearchFragment extends BaseRegisterFragment implements AdvancedSearchContract.View {

    protected AdvancedSearchTextWatcher advancedSearchTextwatcher = new AdvancedSearchTextWatcher();
    protected Map<String, View> advancedFormSearchableFields = new HashMap<>();
    private View listViewLayout;
    private View advancedSearchForm;
    private ImageButton backButton;
    private Button searchButton;
    private TextView searchCriteria;
    private TextView matchingResults;
    private TextInputEditText firstName;
    private TextInputEditText lastName;
    private final boolean isLocal;
    private boolean listMode = false;

    public AdvancedSearchFragment(boolean isLocal) {
        this.isLocal = isLocal;
    }

    public AdvancedSearchFragment() {
        // doesn't do anything special
        isLocal = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_advanced_search, container, false);

        rootView = view;//handle to the root

        setupViews(view);
        onResumption();
        return view;
    }

    @Override
    protected void initializePresenter() {

        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((AllClientsRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new AdvancedSearchFragmentPresenter(this, new AdvancedSearchFragmentModel(), viewConfigurationIdentifier);

    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {

    }

    @Override
    protected String getMainCondition() {
        return ((AdvancedSearchFragmentPresenter) presenter).getMainCondition();
    }

    @Override
    protected String getDefaultSortQuery() {
        return ((AdvancedSearchFragmentPresenter) presenter).getDefaultSortQuery();
    }


    @Override
    public void setupViews(View view) {
        super.setupViews(view);

        if (titleLabelView != null) {
            if (isLocal) {
                titleLabelView.setText(getString(R.string.search));
            } else {
                titleLabelView.setText(getString(R.string.global_search));
            }
        }

        listViewLayout = view.findViewById(R.id.advanced_search_list);
        listViewLayout.setVisibility(View.GONE);

        advancedSearchForm = view.findViewById(R.id.advanced_search_form);
        backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(registerActionHandler);


        searchCriteria = view.findViewById(R.id.search_criteria);
        matchingResults = view.findViewById(R.id.matching_results);
        searchButton = view.findViewById(R.id.advanced_form_search_btn);
        Button retryButton = view.findViewById(R.id.retry_connection_button);

        retryButton.setOnClickListener(v -> updateOnNetworkChange(view));

        updateOnNetworkChange(view);

        setUpSearchButtons();

        populateSearchableFields(view);

        resetForm();

    }


    private void updateOnNetworkChange(View view){
        if(isNetworkConnected()){
            view.findViewById(R.id.advanced_search_form).setVisibility(View.VISIBLE);
            view.findViewById(R.id.advanced_search_offline).setVisibility(View.GONE);
        }else{
            view.findViewById(R.id.advanced_search_form).setVisibility(View.GONE);
            view.findViewById(R.id.advanced_search_offline).setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager
                = getSystemService(requireContext(), ConnectivityManager.class);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void populateSearchableFields(View view) {

        firstName = view.findViewById(R.id.first_name);
        firstName.addTextChangedListener(advancedSearchTextwatcher);

        lastName = view.findViewById(R.id.last_name);
        lastName.addTextChangedListener(advancedSearchTextwatcher);

        advancedFormSearchableFields.put(Constants.DB.FIRST_NAME, firstName);
        advancedFormSearchableFields.put(Constants.DB.LAST_NAME, lastName);
    }

    private void resetForm() {
        clearSearchCriteria();
        clearMatchingResults();
    }

    private void clearSearchCriteria() {
        if (searchCriteria != null) {
            searchCriteria.setVisibility(View.GONE);
            searchCriteria.setText("");
        }
    }

    private void clearMatchingResults() {
        if (matchingResults != null) {
            matchingResults.setVisibility(View.GONE);
            matchingResults.setText("");
        }
    }


    public void updateMatchingResults(int count) {
        if (matchingResults != null) {
            matchingResults.setText(String.format(getString(R.string.matching_results), String.valueOf(count)));
            matchingResults.setVisibility(View.VISIBLE);
        }
    }

    private void setUpSearchButtons() {
        searchButton.setEnabled(false);
        searchButton.setTextColor(getResources().getColor(R.color.contact_complete_grey_border));
        searchButton.setOnClickListener(registerActionHandler);
    }

    private boolean anySearchableFieldHasValue() {

        for (Map.Entry<String, View> entry : advancedFormSearchableFields.entrySet()) {

            if (entry.getValue() instanceof TextView && !TextUtils.isEmpty(((TextView) entry.getValue()).getText())) {
                return true;
            }
        }
        return false;

    }

    private void checkTextFields() {
        if (anySearchableFieldHasValue()) {

            searchButton.setEnabled(true);
            searchButton.setTextColor(getResources().getColor(R.color.white));
        } else {

            searchButton.setEnabled(false);
            searchButton.setTextColor(getResources().getColor(R.color.contact_complete_grey_border));
        }
    }

    private class AdvancedSearchTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //Todo later
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkTextFields();
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkTextFields();
        }
    }

    @Override
    public void setUniqueID(String s) {
        if (getSearchView() != null) {
            getSearchView().setText(s);
        }
    }

    @Override
    protected void startRegistration() {
        ((BaseRegisterActivity) getActivity()).startRegistration();
    }

    @Override
    public void onViewClicked(View view) {
     if (view.getId() == R.id.advanced_form_search_btn) {
            search();
        } else if (view.getId() == R.id.back_button) {
            switchViews(false);
        }
    }

    private void search() {
        showProgressView();

        Map<String, String> editMap = getSearchMap(isLocal);
        ((AdvancedSearchContract.Presenter) presenter).search(editMap, isLocal);
    }

    protected Map<String, String> getSearchMap(boolean isLocal) {

        Map<String, String> searchParams = new HashMap<>();

        String fn = firstName.getText().toString().trim();

        String ln = lastName.getText().toString().trim();

        if (!TextUtils.isEmpty(fn)) {
            searchParams.put(Constants.DB.FIRST_NAME, fn);
        }

        if (!TextUtils.isEmpty(ln)) {
            searchParams.put(Constants.DB.LAST_NAME, ln);
        }

        return searchParams;
    }

    @Override
    public void showNotFoundPopup(String opensrpID) {
        //Todo implement this
    }

    public void showResults(List<Entity> members, boolean isLocal) {
        FamilyMemberAdapter adapter = new FamilyMemberAdapter(getView().getContext(), members, isLocal);
        ListView listView = rootView.findViewById(R.id.family_member_list);
        listView.setAdapter(adapter);
        updateMatchingResults(members.size());
        switchViews(true);
    }

    public void switchViews(boolean showList) {
        if (showList) {
            Utils.hideKeyboard(getActivity());

            advancedSearchForm.setVisibility(View.GONE);
            listViewLayout.setVisibility(View.VISIBLE);
            clientsView.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.GONE);

            if (titleLabelView != null) {
                titleLabelView.setText(getString(R.string.search_results));
            }

            hideProgressView();
            listMode = true;
        } else {
            if (clientsView.getVisibility() == View.INVISIBLE) {
                ((BaseRegisterActivity) getActivity()).switchToFragment(0);
                return;
            }

            clearSearchCriteria();
            advancedSearchForm.setVisibility(View.VISIBLE);
            listViewLayout.setVisibility(View.GONE);
            clientsView.setVisibility(View.INVISIBLE);
            searchButton.setVisibility(View.VISIBLE);

            if (titleLabelView != null) {
                if (isLocal) {
                    titleLabelView.setText(getString(R.string.search));
                } else {
                    titleLabelView.setText(getString(R.string.global_search));
                }
            }
            listMode = false;
        }
    }

    @Override
    public boolean onBackPressed() {
        goBack();
        return true;
    }

    @Override
    protected void goBack() {
        if (listMode) {
            switchViews(false);
        } else {
            ((BaseRegisterActivity) getActivity()).switchToFragment(0);
        }
    }
}

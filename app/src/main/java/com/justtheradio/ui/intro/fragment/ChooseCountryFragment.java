package com.justtheradio.ui.intro.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.justtheradio.R;
import com.justtheradio.adapter.CountryListRecyclerAdapter;
import com.justtheradio.model.Result;
import com.justtheradio.repository.RadioRepository;
import com.justtheradio.ui.main.MainActivity;
import com.justtheradio.ui.viewmodel.radio.RadioViewModel;
import com.justtheradio.ui.viewmodel.radio.RadioViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Intro screen where the user chooses the country whose radio stations will be shown.
 * If the country codes cannot be loaded or the preference cannot be saved, an error dialog
 * is shown and its only button closes the activity.
 */
public class ChooseCountryFragment extends Fragment {
    /** ViewModel shared with the host activity, used to fetch and save the countries. */
    private RadioViewModel radioViewModel;
    /** Country codes shown in the list. */
    private final List<String> countryCodes = new ArrayList<>();
    /** Adapter of the list of countries. */
    private CountryListRecyclerAdapter adapter;
    /**
     * Empty constructor for {@link ChooseCountryFragment}
     */
    public ChooseCountryFragment() {
        // Required empty public constructor
    }
    /**
     * Executed on the creation of the fragment object.
     * Gets the {@link RadioRepository} and creates the {@link RadioViewModel}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ge tthe radio repository and initialize the view model
        RadioRepository radioRepository = ServiceLocator.getInstance()
                .getRadioRepository(requireActivity().getApplication());
        radioViewModel = new ViewModelProvider(
                // Scope it to the activity to avoid allocating more than one view model
                requireActivity(),
                new RadioViewModelFactory(radioRepository))
                .get(RadioViewModel.class);
    }
    /**
     * Executed when creating the fragment view.
     * Inflates the layout of the fragment
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The root view of the inflated layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_country, container, false);
    }
    /**
     * Executed after creating the view.
     * Sets up the list of countries and starts loading the country codes.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the country recycler adapter and behavior when tapping a list item
        adapter = new CountryListRecyclerAdapter(countryCodes, country ->
                // Save the country to local cache and observe the result
                radioViewModel.saveCountryPreference(country)
                .observe(getViewLifecycleOwner(), result -> {
                    if (result.isSuccess()) { // If the operation is successful
                        // Start the main activity and terminate the intro activity
                        startActivity(new Intent(requireActivity(), MainActivity.class));
                        requireActivity().finish();
                    } else {
                        // Show an error dialogue if the operation throws an error
                        showErrorAlertDialog(
                                requireContext(),
                                R.string.error_saving_country_preference_title,
                                R.string.error_saving_country_preference_message,
                                R.string.error_material_dialogue_close_application_button_text
                        );
                    }
                })
        );

        // Setup recycler view to use the country adapter
        RecyclerView recyclerView = view.findViewById(R.id.countries_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(adapter);

        // Fetch the country codes from remote and observe the result
        radioViewModel.fetchCountryCodes().observe(getViewLifecycleOwner(), result-> {
            if (result.isSuccess()) {
                // Populate the list with the country codes obtained from the API
                Result.Success<List<String>> success = (Result.Success<List<String>>) result;
                List<String> codes = success.getData();
                this.countryCodes.addAll(codes);
                // Notify the adapter that new country codes were inserted in the list
                adapter.notifyItemRangeInserted(0, this.countryCodes.size());
                view.findViewById(R.id.country_loading_progress_indicator).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.countries_recycler_view).setVisibility(View.VISIBLE);
            } else {
                // Show an error dialogue if the operation throws an error
                showErrorAlertDialog(
                        requireContext(),
                        R.string.error_loading_country_codes_title,
                        R.string.error_loading_country_codes_message,
                        R.string.error_material_dialogue_close_application_button_text
                );
            }
        });
    }
    /**
     * Shows an error dialog with a single button that closes the activity (and so the app,
     * since this screen is shown before the main one).
     *
     * @param context       The context used to build the dialog
     * @param title         String resource of the title
     * @param message       String resource of the message
     * @param buttonText    String resource of the text of the button
     */
    private void showErrorAlertDialog(Context context, int title, int message, int buttonText) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(buttonText,
                        ((dialogInterface, i) -> requireActivity().finish()))
                .show();
    }
}
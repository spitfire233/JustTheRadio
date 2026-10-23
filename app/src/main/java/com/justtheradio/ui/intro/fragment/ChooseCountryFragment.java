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
import com.justtheradio.ui.viewmodel.RadioViewModel;
import com.justtheradio.ui.viewmodel.RadioViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChooseCountryFragment extends Fragment {

    private RadioViewModel radioViewModel;
    private final List<String> countryCodes = new ArrayList<>();

    private CountryListRecyclerAdapter adapter;

    public ChooseCountryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RadioRepository radioRepository = ServiceLocator.getInstance()
                .getRadioRepository(requireActivity().getApplication());
        radioViewModel = new ViewModelProvider(
                requireActivity(),
                new RadioViewModelFactory(radioRepository))
                .get(RadioViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_country, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new CountryListRecyclerAdapter(countryCodes, country ->
                radioViewModel.saveCountryPreference(country)
                .observe(getViewLifecycleOwner(), result -> {
                    if (result.isSuccess()) {
                        startActivity(new Intent(requireActivity(), MainActivity.class));
                        requireActivity().finish();
                    } else {
                        showErrorAlertDialog(
                                requireContext(),
                                R.string.error_saving_country_preference_title,
                                R.string.error_saving_country_preference_message,
                                R.string.error_material_dialogue_close_application_button_text
                        );
                    }
                }));

        RecyclerView recyclerView = view.findViewById(R.id.countries_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(adapter);

        radioViewModel.fetchCountryCodes().observe(getViewLifecycleOwner(), result-> {
            if (result.isSuccess()) {
                Result.Success<List<String>> success = (Result.Success<List<String>>) result;
                List<String> codes = success.getData();
                this.countryCodes.addAll(codes);
                adapter.notifyItemRangeInserted(0, this.countryCodes.size());
                view.findViewById(R.id.country_loading_progress_indicator).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.countries_recycler_view).setVisibility(View.VISIBLE);
            } else {
                showErrorAlertDialog(
                        requireContext(),
                        R.string.error_loading_country_codes_title,
                        R.string.error_loading_country_codes_message,
                        R.string.error_material_dialogue_close_application_button_text
                );
            }
        });
    }

    private void showErrorAlertDialog(Context context, int title, int message, int buttonText) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(buttonText,
                        ((dialogInterface, i) -> requireActivity().finish()))
                .show();
    }

}
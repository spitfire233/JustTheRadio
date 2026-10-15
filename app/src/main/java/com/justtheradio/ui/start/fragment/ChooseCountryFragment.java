package com.justtheradio.ui.start.fragment;

import static com.justtheradio.utils.constant.Constants.CHOSEN_COUNTRY_KEY;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.justtheradio.R;
import com.justtheradio.adapter.country.ListRecyclerAdapter;
import com.justtheradio.model.Result;
import com.justtheradio.repository.radio.RadioRepository;
import com.justtheradio.ui.viewmodel.radio.RadioViewModel;
import com.justtheradio.ui.viewmodel.radio.RadioViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChooseCountryFragment extends Fragment {

    private RadioViewModel radioViewModel;
    private final List<String> countryCodes = new ArrayList<>();

    private ListRecyclerAdapter adapter;

    public ChooseCountryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RadioRepository radioRepository = ServiceLocator.getInstance().getRadioRepository();
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

        adapter = new ListRecyclerAdapter(countryCodes, true, itemString -> {

        });

        RecyclerView recyclerView = view.findViewById(R.id.countries_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(adapter);

        radioViewModel.fetchCountryCodes().observe(getViewLifecycleOwner(), result-> {
            if (result.isSuccess()) {
                Result.Success<List<String>> success = (Result.Success<List<String>>) result;
                List<String> codes = success.getResponse();
                this.countryCodes.addAll(codes);
                adapter.notifyItemRangeInserted(0, this.countryCodes.size());
                view.findViewById(R.id.country_loading_progress_indicator).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.countries_recycler_view).setVisibility(View.VISIBLE);
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.error_loading_country_codes_title)
                        .setMessage(R.string.error_loading_country_codes_message)
                        .setNegativeButton(R.string.error_loading_country_codes_negative_button_text,
                                (dialog, which) -> {
                                    requireActivity().finish();
                                })
                        .show();
            }
        });
    }
}
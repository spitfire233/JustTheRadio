package com.justtheradio.ui.main.fragment;

import static com.justtheradio.utils.constant.Constants.CHOSEN_COUNTRY_KEY;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.justtheradio.R;
import com.justtheradio.adapter.list.RadioListPagingDataAdapter;
import com.justtheradio.model.Result;
import com.justtheradio.model.RadioStation;
import com.justtheradio.repository.RadioRepository;
import com.justtheradio.repository.UserRepository;
import com.justtheradio.ui.viewmodel.radio.RadioViewModel;
import com.justtheradio.ui.viewmodel.radio.RadioViewModelFactory;
import com.justtheradio.ui.viewmodel.user.UserViewModel;
import com.justtheradio.ui.viewmodel.user.UserViewModelFactory;
import com.justtheradio.utils.source.ServiceLocator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private UserViewModel userViewModel;
    private RadioViewModel radioViewModel;

    private CircularProgressIndicator circularProgressIndicator;
    private RecyclerView radiosRecyclerView;

    private final List<RadioStation> stations = new ArrayList<>();


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            UserRepository userRepository = ServiceLocator.getInstance()
                    .getUserRepository(requireActivity().getApplication());
            userViewModel = new ViewModelProvider(
                    requireActivity(),
                    new UserViewModelFactory(userRepository)
            ).get(UserViewModel.class);

            RadioRepository radioRepository = ServiceLocator.getInstance()
                    .getRadioRepository(requireActivity().getApplication());
            radioViewModel = new ViewModelProvider(
                    requireActivity(),
                    new RadioViewModelFactory(radioRepository)
            ).get(RadioViewModel.class);
        } catch (Exception e) {
            throw new RuntimeException(e); // TODO: Fatal exception!
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        circularProgressIndicator = view.findViewById(R.id.search_fragment_circular_progress_indicator);
        radiosRecyclerView = view.findViewById(R.id.search_fragment_recycler_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RadioListPagingDataAdapter adapter = new RadioListPagingDataAdapter(stations,
                stations -> {
                    // TODO: Open player
                }, (station, isFavourite) -> {
            // TODO: Set as favourite
            }
        );

        radiosRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        radiosRecyclerView.setAdapter(adapter);

        userViewModel.getUserPreference(CHOSEN_COUNTRY_KEY).observe(getViewLifecycleOwner(),
                result -> {
                    if (result.isSuccess()) {
                        Result.Success<String> success = (Result.Success<String>) result;
                        radioViewModel.getRadioStations(success.getData())
                                .observe(getViewLifecycleOwner(), pagingData -> {
                                    adapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData);
                                    circularProgressIndicator.setVisibility(View.GONE);
                                    radiosRecyclerView.setVisibility(View.VISIBLE);
                                }
                        );
                    } else {
                        displayErrorToast((Result.Failure) result);
                    }
        });
    }

    private void displayErrorToast(Result.Failure failure) {
        CharSequence message = failure.getMessage();
        Toast toast = Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

}